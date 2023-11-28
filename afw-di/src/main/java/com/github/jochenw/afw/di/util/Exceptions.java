package com.github.jochenw.afw.di.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;


/** Utility class for working with exceptions.
 */
public class Exceptions {
	/** Throws a {@link RuntimeException} with the given throwable as the cause.
	 * @param pTh The {@link Throwable}, that caused the exception, which is
	 *   being thrown.
	 * @return A {@link RuntimeException}, which can be safely thrown, without
	 *   polluting the callers signature.
	 */
	public static RuntimeException show(Throwable pTh) {
		final Throwable th = Objects.requireNonNull(pTh, "Throwable");
		if (th instanceof RuntimeException) {
			throw (RuntimeException) th;
		} else if (th instanceof Error) {
			throw (Error) th;
		} else if (th instanceof IOException) {
			throw new UncheckedIOException((IOException) th);
		} else {
			throw new UndeclaredThrowableException(th);
		}
	}

}
