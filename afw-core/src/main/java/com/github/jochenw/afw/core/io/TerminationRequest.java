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

}
