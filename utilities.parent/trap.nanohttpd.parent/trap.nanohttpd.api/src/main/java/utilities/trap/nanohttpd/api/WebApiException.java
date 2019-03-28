package utilities.trap.nanohttpd.api;

import com.ericsson.research.trap.nhttpd.StatusCodes;

public class WebApiException extends Exception {

	/** serialVersionUID */
	private static final long serialVersionUID = -3635741922531934253L;
	public final int statusCode;

	public WebApiException() {
		super();
		statusCode = StatusCodes.INTERNAL_SERVER_ERROR;
	}

	public WebApiException(int statusCode) {
		super();
		this.statusCode = statusCode;
	}

	public WebApiException(String message, Throwable cause) {
		super(message, cause);
		statusCode = StatusCodes.INTERNAL_SERVER_ERROR;
	}

	public WebApiException(int statusCode, String message, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

	public WebApiException(String message) {
		super(message);
		statusCode = StatusCodes.INTERNAL_SERVER_ERROR;
	}

	public WebApiException(int statusCode, String message) {
		super(message);
		this.statusCode = statusCode;
	}

	public WebApiException(Throwable cause) {
		super(cause);
		statusCode = StatusCodes.INTERNAL_SERVER_ERROR;
	}

	public WebApiException(int statusCode, Throwable cause) {
		super(cause);
		this.statusCode = statusCode;
	}

}
