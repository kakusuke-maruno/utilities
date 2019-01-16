package utilities.rsocket.kvs.storage.exception;

public class StorageException extends Exception {

	/** serialVersionUID */
	private static final long serialVersionUID = -3524873827384914806L;

	public StorageException() {
	}

	public StorageException(String message) {
		super(message);
	}

	public StorageException(Throwable cause) {
		super(cause);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
