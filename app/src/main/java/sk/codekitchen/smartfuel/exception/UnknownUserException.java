package sk.codekitchen.smartfuel.exception;

/**
 * @author Attila Večerek
 * @since 1.0
 */
public class UnknownUserException extends Exception {

	public UnknownUserException(String msg, Exception e) {
		super(msg, e);
	}

	public UnknownUserException(String msg) {
		super(msg);
	}
}
