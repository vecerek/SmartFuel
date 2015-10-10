package sk.codekitchen.smartfuel.exception;

/**
 * It is raised when the database tries to create two savepoints with equal names.
 *
 * @author Attila Večerek
 * @since 1.0
 */
public class DuplicateSavepointException extends RuntimeException {

	private String savepoint;
	public static final String MSG = "Savepoint already exists";

	/**
	 * {@code msg} defaults to {@value #MSG}
	 * @see #DuplicateSavepointException(String, String)
	 * @since 1.0
	 */
	public DuplicateSavepointException(String savepoint) { this(savepoint, MSG); }

	/**
	 * Creates exception based on the savepoint's name and the message.
	 * @param savepoint name of the savepoint that caused the exception to be raised
	 * @param msg exception's message
	 * @since 1.0
	 */
	public DuplicateSavepointException(String savepoint, String msg) {
		super(msg);
		this.savepoint = savepoint;
	}

	/**
	 * Returns the savepoint, that caused the raise of the exception.
	 * @return the savepoint's name
	 * @since 1.0
	 */
	public String which() {
		return this.savepoint;
	}
}

