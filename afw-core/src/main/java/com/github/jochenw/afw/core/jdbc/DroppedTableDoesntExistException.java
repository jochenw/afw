package com.github.jochenw.afw.core.jdbc;


/**
 * This exception is being thrown, if a "DROP TABLE" statement
 * has been executed, and the respective table did not exist.
 * The purpose is to support defensive programming, if the
 * database doesn't support "DROP TABLE IF EXISTS".
 */
public class DroppedTableDoesntExistException extends RuntimeException {
	private static final long serialVersionUID = 5854101502882241170L;

	/** Creates a new instance.
	 * @param pCause The exception, that was thrown by the database
	 * driver.
	 */
	public DroppedTableDoesntExistException(Throwable pCause) {
		super(pCause);
	}

}
