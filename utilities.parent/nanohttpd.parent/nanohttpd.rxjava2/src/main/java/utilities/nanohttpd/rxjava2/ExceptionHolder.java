package utilities.nanohttpd.rxjava2;

public class ExceptionHolder extends Exception {
	/** serialVersionUID */
	private static final long serialVersionUID = 2805467016306172520L;
	private final Session session;

	public ExceptionHolder(Session session, Throwable cause) {
		super(cause);
		this.session = session;
	}

	public Session getSession() {
		return session;
	}
}
