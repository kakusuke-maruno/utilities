package utilities.nanohttpd.rxjava2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.reactivestreams.Processor;

import com.ericsson.research.trap.nhttpd.HTTPD;

import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.processors.PublishProcessor;

public class NanoHTTPDPublisher {
	private final HTTPD server;
	private Processor<Session, Session> sessionProcessor;
	private final List<Disposable> streams;
	private final List<Predicate<Session>> checkTargets;

	public NanoHTTPDPublisher(String hostname, int port) {
		server = new HTTPD(hostname, port);
		sessionProcessor = PublishProcessor.create();
		streams = new ArrayList<>();
		checkTargets = new ArrayList<>();
	}

	public NanoHTTPDPublisher(int port) {
		this(null, port);
	}

	public void addHandler(Predicate<Session> isTarget, Consumer<Session> onRequest, BiConsumer<Session, Throwable> onError) {
		addHandler(new RequestHandler() {
			@Override
			public void onRequest(Session session) throws Exception {
				onRequest.accept(session);
			}

			@Override
			public void onError(Session session, Throwable exception) {
				try {
					onError.accept(session, exception);
				} catch (Exception e) {
				}
			}

			@Override
			public boolean isTarget(Session session) {
				try {
					return isTarget.test(session);
				} catch (Exception e) {
					return false;
				}
			}
		});
	}

	public void addHandler(RequestHandler handler) {
		checkTargets.add(handler::isTarget);
		addHandler0(handler);
	}

	public void addHandler0(RequestHandler handler) {
		streams.add( //
				Flowable //
						.fromPublisher(sessionProcessor) //
						.filter(handler::isTarget) //
						.subscribe( //
								session -> {
									try {
										handler.onRequest(session);
									} catch (Exception e) {
										throw new ExceptionHolder(session, e);
									}
								}, //
								t -> {
									if (t instanceof ExceptionHolder) {
										ExceptionHolder holder = (ExceptionHolder) t;
										handler.onError(holder.getSession(), holder.getCause());
									}
								} //
						) //
		) //
		;
	}

	public synchronized void start() throws IOException {
		if (server.isAlive()) {
			return;
		}
		addHandler0(new RequestHandler() {
			@Override
			public void onRequest(Session session) throws Exception {
				session.res.setStatus(404).sendAsyncResponse();
			}

			@Override
			public void onError(Session session, Throwable exception) {
				session.res.setData(exception.getLocalizedMessage()).setStatus(500).sendAsyncResponse();
			}

			@Override
			public boolean isTarget(Session session) {
				for (Predicate<Session> check : checkTargets) {
					try {
						if (check.test(session))
							return false;
					} catch (Exception e) {
					}
				}
				return true;
			}
		});
		server.setHandler((req, res) -> {
			res.setAsync(true);
			sessionProcessor.onNext(new Session(req, res));
		});
		server.start();
	}

	public synchronized void stop() {
		if (!server.isAlive()) {
			return;
		}
		sessionProcessor.onComplete();
		for (Disposable stream : streams) {
			stream.dispose();
		}
		Iterator<Disposable> iterator = streams.iterator();
		while (iterator.hasNext()) {
			Disposable stream = iterator.next();
			while (!stream.isDisposed()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					break;
				}
			}
			iterator.remove();
		}
		server.stop();
	}

	public static void main(String[] args) {
		NanoHTTPDPublisher pub = new NanoHTTPDPublisher(9990);
		Semaphore semaphore = new Semaphore(1);
		pub.addHandler( //
				session -> "/shutdown".equals(session.req.getUri()), // 
				session -> {
					System.out.println("shutting down..");
					session.res.setData(session.req.getUri() + "\n").setStatus(200).sendAsyncResponse();
					semaphore.release();
				}, //
				(session, exception) -> {
					session.res.setData(exception.getLocalizedMessage()).setStatus(500).sendAsyncResponse();
				} //
		);
		pub.addHandler( //
				session -> "/debug".equals(session.req.getUri()), // 
				session -> {
					session.res.setData(session.req.getUri() + "\ndebug\n").setStatus(200).sendAsyncResponse();
				}, //
				(session, exception) -> {
					session.res.setData(exception.getLocalizedMessage()).setStatus(500).sendAsyncResponse();
				} //
		);
		try {
			pub.start();
			semaphore.acquireUninterruptibly();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(9);
			return;
		}
		semaphore.acquireUninterruptibly();
		semaphore.release();
		pub.stop();
		System.exit(0);
	}
}
