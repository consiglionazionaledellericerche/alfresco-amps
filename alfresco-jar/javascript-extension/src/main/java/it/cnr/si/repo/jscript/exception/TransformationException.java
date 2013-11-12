package it.cnr.si.repo.jscript.exception;

public class TransformationException extends Exception {

	private static final long serialVersionUID = -6651225733681434161L;

	public TransformationException() {
	}

	public TransformationException(String message) {
		super(message);
	}

	public TransformationException(Throwable cause) {
		super(cause);
	}

	public TransformationException(String message, Throwable cause) {
		super(message, cause);
	}

}
