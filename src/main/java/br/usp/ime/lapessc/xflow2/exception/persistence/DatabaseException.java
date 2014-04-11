package br.usp.ime.lapessc.xflow2.exception.persistence;

import br.usp.ime.lapessc.xflow2.exception.XFlowException;

public class DatabaseException extends XFlowException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 811924985464909218L;

	public DatabaseException(String message) {
		super(message, new Throwable("Database error"));
	}
}
