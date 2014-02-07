package it.cnr.jada.firma;

import it.cnr.jada.comp.ApplicationException;

public class NotSignedEnvelopeException extends ApplicationException {

	public NotSignedEnvelopeException() {
		super();
	}

	public NotSignedEnvelopeException(String s) {
		super(s);
	}

	public NotSignedEnvelopeException(String s, Throwable detail) {
		super(s, detail);
	}

	public NotSignedEnvelopeException(Throwable detail) {
		super(detail);
	}
}
