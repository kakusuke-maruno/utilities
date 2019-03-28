package utilities.trap.nanohttpd.api;

import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.research.trap.nhttpd.Request;
import com.ericsson.research.trap.nhttpd.Response;

class MessageContext {
	private final Request request;
	private final Response response;
	final AtomicInteger invoked = new AtomicInteger(0);

	public MessageContext(Request request, Response response) {
		this.request = request;
		this.response = response;
	}

	public Request getRequest() {
		return request;
	}

	public Response getResponse() {
		return response;
	}
}
