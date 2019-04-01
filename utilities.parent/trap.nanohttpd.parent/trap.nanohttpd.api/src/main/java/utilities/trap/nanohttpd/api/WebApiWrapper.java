package utilities.trap.nanohttpd.api;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.research.trap.nhttpd.StatusCodes;

class WebApiWrapper {
	private static final Logger LOG = LoggerFactory.getLogger(WebApiWrapper.class);
	private final WebApi service;

	public WebApiWrapper(WebApi service) {
		this.service = service;
	}

	void execute0(MessageContext messageContext) {
		if (messageContext.getRequest() != null) {
			try {
				messageContext.invoked.getAndIncrement();
				service.execute(messageContext.getRequest(), messageContext.getResponse());
			} catch (WebApiException e) {
				service.onError(e);
				messageContext //
					.getResponse() //
					.setStatus(e.statusCode) //
					.setData(e.getLocalizedMessage()) //
				;
			} catch (Throwable e) {
				service.onError(e);
				LOG.error("unknown exception:", e);
				PrintWriter writer = new PrintWriter(new StringWriter());
				e.printStackTrace(writer);
				messageContext //
					.getResponse() //
					.setStatus(StatusCodes.INTERNAL_SERVER_ERROR) //
					.setData(writer.toString()) //
				;
				writer.close();
			}
		}
	}

	protected void onError(Throwable throwable) {
		LOG.error("stream error", throwable);
	}

	protected void onComplete() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("{} was completed.", this.getClass().getName());
		}
		if (service != null)
			service.onComplete();
	}
}
