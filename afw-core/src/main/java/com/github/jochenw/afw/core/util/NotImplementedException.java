package com.github.jochenw.afw.core.util;


/** An {@link IllegalStateException}, which is thrown to indicate, that
 * some code is yet to be implemented.
 */
public class NotImplementedException extends IllegalStateException {
	private static final long serialVersionUID = -7402852484207080555L;

	/** Creates a new instance with the given message.
	 * @param pMsg The exceptions message.
	 */
	public NotImplementedException(String pMsg) {
		super(pMsg);
	}
	/** Creates a new instance with the message "Not implemented".
	 */
	public NotImplementedException() {
		this("Not implemented.");
	}
}
