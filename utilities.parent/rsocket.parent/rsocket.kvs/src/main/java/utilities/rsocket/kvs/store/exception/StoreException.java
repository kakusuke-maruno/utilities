package utilities.rsocket.kvs.store.exception;

public class StoreException extends Exception {

	/** serialVersionUID */
	private static final long serialVersionUID = 2924399507982999559L;

	public StoreException() {
	}

	public StoreException(String message) {
		super(message);
	}

	public StoreException(Throwable cause) {
		super(cause);
	}

	public StoreException(String message, Throwable cause) {
		super(message, cause);
	}
}
