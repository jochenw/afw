package com.github.jochenw.afw.core.io;

import java.io.IOException;


/** This exception is thrown, if an attempt is made to read from a
 * stream, that has already been closed.
 */
public class StreamAlreadyClosedException extends IOException {
	private static final long serialVersionUID = 4995937849536256420L;

	/**
	 * Creates a new instance with the message "Stream closed",
	 *   and no cause.
	 */
	public StreamAlreadyClosedException() {
		super("Stream closed");
	}

	/**
	 * Creates a new instance, with the given message, and cause.
	 * @param pMessage The exceptions message.
	 * @param pCause The exceptions cause.
	 */
	public StreamAlreadyClosedException(String pMessage, Throwable pCause) {
		super(pMessage, pCause);
	}

	/**
	 * Creates a new instance, with the given message, and no cause.
	 * @param pMessage The exceptions message.
	 */
	public StreamAlreadyClosedException(String pMessage) {
		super(pMessage);
	}

	/**
	 * Creates a new instance, with the message "Stream closed",
	 * and the given cause.
	 * @param pCause The exceptions cause.
	 */
	public StreamAlreadyClosedException(Throwable pCause) {
		super("Stream closed", pCause);
	}

}
