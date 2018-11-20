package utilities.nanohttpd.rxjava2;

import com.ericsson.research.trap.nhttpd.Request;
import com.ericsson.research.trap.nhttpd.Response;

public class Session {
	public final Request req;
	public final Response res;

	public Session(Request req, Response res) {
		this.req = req;
		this.res = res;
	}
}
