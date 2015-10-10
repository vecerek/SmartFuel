package sk.codekitchen.smartfuel.exception;

/**
 * @author Attila Večerek
 */
public class PermissionDeniedException extends Exception {

	protected int requestCode;
	protected static final String MSG = "Permission has been denied.";

	public PermissionDeniedException(int rc) {
		super(MSG);
		this.requestCode = rc;
	}

	public int getDeniedPermission() { return this.requestCode; }
}
