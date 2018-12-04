package utilities.nanohttpd.rxjava2;

public interface RequestHandler {
	boolean isTarget(Session session);

	void onRequest(Session session) throws Exception;

	void onError(Session session, Throwable exception);
}
