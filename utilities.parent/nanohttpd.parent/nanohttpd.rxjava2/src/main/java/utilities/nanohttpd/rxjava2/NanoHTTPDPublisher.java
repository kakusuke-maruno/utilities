package utilities.nanohttpd.rxjava2;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.reactivestreams.Processor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import com.ericsson.research.trap.nhttpd.HTTPD;

import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;

public class NanoHTTPDPublisher implements Publisher<Session> {
	private final HTTPD server;
	private Processor<Session, Session> sessionProcessor;
	private final Semaphore semaphore;

	public NanoHTTPDPublisher(String hostname, int port) {
		server = new HTTPD(hostname, port);
		sessionProcessor = PublishProcessor.create();
		semaphore = new Semaphore(1);
	}

	public NanoHTTPDPublisher(int port) {
		this(null, port);
	}

	public synchronized void start() throws IOException {
		if (server.isAlive()) {
			return;
		}
		server.setHandler((req, res) -> {
			res.setAsync(true);
			sessionProcessor.onNext(new Session(req, res));
		});
		server.start();
		semaphore.acquireUninterruptibly();
	}

	public synchronized void stop() {
		semaphore.release();
		if (!server.isAlive()) {
			return;
		}
		sessionProcessor.onComplete();
		server.stop();
	}

	public void awaitTermination() throws InterruptedException {
		semaphore.acquire();
		semaphore.release();
	}

	@Override
	public void subscribe(Subscriber<? super Session> s) {
		sessionProcessor.subscribe(s);
	}

	public static void main(String[] args) {
		NanoHTTPDPublisher pub = new NanoHTTPDPublisher(9990);
		try {
			pub.start();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(9);
			return;
		}
		Flowable //
				.fromPublisher(pub) //
				.doOnNext(session -> {
					String uri = session.req.getUri();
					session.res.setData(uri + "\n").setStatus(200).sendAsyncResponse();
					if (uri.equals("/shutdown")) {
						System.out.println("shutting down..");
						pub.stop();
					}
				}) //
				.doOnComplete(() -> {
					System.out.println("complete.");
				}) //
				.subscribe();
		try {
			pub.awaitTermination();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(9);
			return;
		}
		System.exit(0);
	}
}
