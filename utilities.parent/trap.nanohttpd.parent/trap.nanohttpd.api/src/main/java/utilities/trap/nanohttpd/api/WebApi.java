package utilities.trap.nanohttpd.api;

import com.ericsson.research.trap.nhttpd.Request;
import com.ericsson.research.trap.nhttpd.Response;

public interface WebApi {
	void execute(Request request, Response response) throws WebApiException;

	void onError(Throwable throwable);

	void onComplete();
}
