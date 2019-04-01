package utilities.trap.nanohttpd.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import org.reactivestreams.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.research.trap.nhttpd.HTTPD;
import com.ericsson.research.trap.nhttpd.Request;
import com.ericsson.research.trap.nhttpd.RequestHandler;
import com.ericsson.research.trap.nhttpd.Response;
import com.ericsson.research.trap.nhttpd.StatusCodes;

import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;

public class WebApiServer {
	private static final Logger LOG = LoggerFactory.getLogger(WebApiServer.class);

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private String hostname = null;
		private int port = -1;
		private int bufferSize = -1;
		private List<Entry<String, WebApiWrapper>> urls = new ArrayList<>();

		public Builder hostname(String hostname) {
			this.hostname = hostname;
			return this;
		}

		public Builder port(int port) {
			this.port = port;
			return this;
		}

		public Builder bufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		public Builder bind(String urlPattern, WebApi service) {
			urls.add(toEntry(urlPattern, new WebApiWrapper(service)));
			return this;
		}

		public WebApiServer build() {
			if (port < 1024) {
				// reserved.
				LOG.warn("port is not valid.:[{}]", port);
				return null;
			}

			WebApiServer instance = new WebApiServer();
			if (hostname == null) {
				instance.httpd = new HTTPD(port);
			} else {
				instance.httpd = new HTTPD(hostname, port);
			}
			if (bufferSize <= 0) {
				instance.processor = EmitterProcessor.create();
			} else {
				instance.processor = EmitterProcessor.create(bufferSize);
			}

			ConnectableFlux<MessageContext> stream = Flux //
				.from(instance.processor) //
				.doOnNext(messageContext -> {
					LOG.info("url:[{}]", messageContext.getRequest().getUri());
				}) //
				.doOnComplete(instance::shutdown) //
				.publish() //
			;

			urls.add(toEntry("^/shutdown/$", new WebApiWrapper(new WebApi() {
				@Override
				public void execute(Request request, Response response) throws WebApiException {
					response.setStatus(StatusCodes.OK).setData("shutting down...");
					new Thread(() -> {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
						instance.processor.onComplete();
					}).start();
				}

				@Override
				public void onError(Throwable throwable) {
				}

				@Override
				public void onComplete() {
				}
			})));
			urls.add(toEntry(".*", new WebApiWrapper(null) {
				@Override
				void execute0(MessageContext messageContext) {
					if (messageContext.invoked.get() == 0) {
						Response response = messageContext.getResponse();
						response.setStatus(StatusCodes.NOT_FOUND).setData("not found.");
					}
				}
			}));
			urls //
				.forEach(entry -> {
					Pattern pattern = Pattern.compile(entry.getKey());
					LOG.debug("urlPattern:[{}]", entry.getKey());
					WebApiWrapper service = entry.getValue();
					stream //
						.filter(message -> pattern.matcher(message.getRequest().getUri()).find()) //
						.subscribe(service::execute0, service::onError, service::onComplete) //
					;
				}) //
			;
			stream.connect();
			try {
				instance.httpd.setHandler(new RequestHandler() {
					@Override
					public void handleRequest(Request request, Response response) {
						instance.processor.onNext(new MessageContext(request, response));
					}
				});
				instance.httpd.start();
			} catch (IOException e) {
				LOG.error("initialize error", e);
				instance.shutdown();
				return null;
			}
			return instance;
		}
	}

	private static Entry<String, WebApiWrapper> toEntry(String urlPattern, WebApiWrapper service) {
		return new Entry<String, WebApiWrapper>() {

			@Override
			public String getKey() {
				return urlPattern;
			}

			@Override
			public WebApiWrapper getValue() {
				return service;
			}

			@Override
			public WebApiWrapper setValue(WebApiWrapper value) {
				throw new UnsupportedOperationException();
			}
		};
	}

	private HTTPD httpd;
	private Processor<MessageContext, MessageContext> processor;
	private CountDownLatch latch = new CountDownLatch(1);

	private WebApiServer() {
	}

	public void stop() {
		processor.onComplete();
	}

	private void shutdown() {
		httpd.stop();
		latch.countDown();
	}

	public void awaitShutdown() throws InterruptedException {
		latch.await();
	}
}
