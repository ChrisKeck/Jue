package nl.q42.jue.exceptions;

/**
 * Exception thrown when the API returns an error
 */
@SuppressWarnings("serial")
public class ApiException extends Exception {
	public ApiException() {}
	
	public ApiException(String message) {
		super(message);
	}
}