package sk.codekitchen.smartfuel.exception;

/**
 * It is raised when it's unclear if the data have to be inserted or updated.
 *
 * @author Attila Večerek
 * @since 1.0
 */
public class UnknownDataOriginException extends RuntimeException {

	private String origin;
	public static final String MSG = "The data has unknown origin due to bad programming";

	/**
	 * {@code msg} defaults to {@value #MSG}
	 * @see #UnknownDataOriginException(String, String)
	 * @since 1.0
	 */
	public UnknownDataOriginException(String origin) {
		this(origin, MSG);
	}

	/**
	 * Creates the exception.
	 * @param origin data's origin
	 * @param msg exception message
	 * @since 1.0
	 */
	public UnknownDataOriginException(String origin, String msg) {
		super(msg);
		this.origin = origin;
	}

	/**
	 * @return origin that caused the exception
	 * @since 1.0
	 */
	public String getOrigin() { return this.origin; }
}
