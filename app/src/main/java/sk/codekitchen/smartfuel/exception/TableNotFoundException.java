package sk.codekitchen.smartfuel.exception;

/**
 * Represents a missing table definition exception.
 * The exception is most likely to be thrown due to programming mistakes.
 *
 * @author Attila Večerek
 * @since 1.0
 */
public class TableNotFoundException extends RuntimeException {

	private String table;
	private static final String MSG = "Table does not exist";

	/**
	 * {@code msg} defaults to {@value #MSG}
	 * @see #TableNotFoundException(String, String)
	 */
	public TableNotFoundException(String table) {
		this(table, MSG);
	}

	/**
	 * Constructs the exception without a StackTrace-builder exception.
	 * @see #TableNotFoundException(String, String, Exception)
	 * @since 1.0
	 */
	public TableNotFoundException(String table, String msg) {
		super(msg);
		this.table = table;
	}

	/**
	 * Constructs the exception.
	 *
	 * @param table name of the missing table
	 * @param msg the exception's message
	 * @param e exception used for StackTrace-building
	 * @since 1.0
	 */
	public TableNotFoundException(String table, String msg, Exception e) {
		super(msg, e);
		this.table = table;
	}

	/**
	 * Returns the table, that caused the rais of the exception.
	 * @return the table name
	 * @since 1.0
	 */
	public String which() {
		return this.table;
	}
}
