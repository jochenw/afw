/**
 * 
 */
package com.github.jochenw.afw.core.io;

/** This exception is thrown, if a callee wishes to inform the caller, that it
 * should terminate the current operation.
 * @see AbstractFileVisitor#visitDirectory(String, java.nio.file.Path, java.nio.file.attribute.BasicFileAttributes)
 */
public class TerminationRequest extends RuntimeException {
	private static final long serialVersionUID = -3342240140383270621L;
	/** The result object, that is being returned when catching this
	 * termination request.
	 */
	private final Object value;

	/** Creates a new instance with the given result value.
	 * @param pValue The result value.
	 */
	public TerminationRequest(Object pValue) {
		value = pValue;
	}

	/** Creates a new instance with no result value.
	 */
	public TerminationRequest() {
		this(null);
	}

	/** Returns the result value (if available), or null.
	 * @return The result value (if available), or null.
	 */
	public Object getValue() {
		return value;
	}
}
