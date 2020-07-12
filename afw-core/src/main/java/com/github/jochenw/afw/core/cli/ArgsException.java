/**
 * 
 */
package com.github.jochenw.afw.core.cli;

/** This Exception is thrown by the {@link Args CLI arguments
 * parser} in the default error handler.
 */
public class ArgsException extends RuntimeException {
	private static final long serialVersionUID = 8007300717914272977L;

	/** Creates a new instance with the given error message, and cause.
	 * @param pMessage The error message.
	 * @param pCause The errror cause.
	 */
	public ArgsException(String pMessage, Throwable pCause) {
		super(pMessage, pCause);
	}

	/** Creates a new instance with the given error message, and no cause.
	 * @param pMessage The error message.
	 */
	public ArgsException(String pMessage) {
		super(pMessage);
	}
}
