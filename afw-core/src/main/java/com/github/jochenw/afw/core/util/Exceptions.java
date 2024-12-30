/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Utility class for working with Exceptions. Provides static utility methods.
 */
public class Exceptions {
	/**
     * Private constructor, to prevent accidental instantiation.
     */
    private Exceptions() {
        // Does nothing.
    }

    /**
     * Rethrows the given {@link Throwable}, either directly, or by wrapping it in a suitable subclass,
     * which can be thrown safely, without affecting the calling methods signature.
     * @param pTh The throwable being rethrown.
     * @return Nothing, this method will <em>always</em> throw an exception.
     */
    public static RuntimeException show(@NonNull Throwable pTh) {
    	final @NonNull Throwable th = Objects.requireNonNull(pTh, "The Throwable must not be null.");
        if (th instanceof RuntimeException) {
            throw (RuntimeException) th;
        } else if (th instanceof Error) {
            throw (Error) th;
        } else if (th instanceof IOException) {
            throw newUncheckedIOException((IOException) th);
        } else {
            throw newUncheckedException(th);
        }
    }


    /**
     * Rethrows the given {@link Throwable}, either directly, or by wrapping it in a suitable subclass,
     * which can be thrown safely, without affecting the calling methods signature.
     * @param pTh The throwable being rethrown.
     * @param pClass An exception class, which can be thrown without affecting the calling
     * methods signature.
     * @return Nothing, this method will <em>always</em> throw an exception.
     * @throws T Type of an exception, which may be thrown without affecting the calling methods
     *   signature.
     * @param <T> Type of an exception, which may be thrown without affecting the calling methods
     *   signature.
     */
    public static <T extends Throwable> RuntimeException show(@NonNull Throwable pTh, @NonNull Class<T> pClass)
    		throws T {
    	final @NonNull Throwable th = Objects.requireNonNull(pTh, "The Throwable must not be null.");
        if (pClass.isAssignableFrom(th.getClass())) {
            final T thr = pClass.cast(th);
			throw Objects.requireNonNull(thr);
        } else if (th instanceof RuntimeException) {
            throw (RuntimeException) th;
        } else if (th instanceof Error) {
            throw (Error) th;
        } else if (th instanceof IOException) {
            throw newUncheckedIOException((IOException) th);
        } else {
            throw newUncheckedException(th);
        }
    }

    /**
     * Rethrows the given {@link Throwable}, either directly, or by wrapping it in a
     * suitable subclass, which can be thrown safely, without affecting the calling
     * methods signature.
     * @param pPrefix A prefix for the thrown exceptions message.
     * @param pTh The throwable, which is being rethrown.
     * @return Nothing, this method will <em>always</em> throw an exception.
     */
	public static RuntimeException show(String pPrefix, @NonNull Throwable pTh) {
    	final @NonNull Throwable th = Objects.requireNonNull(pTh, "Throwable");
    	final String msg;
    	if (pPrefix == null) {
    		msg = th.getMessage();
    	} else {
    		msg = pPrefix + th.getMessage();
    	}
		throw new UndeclaredThrowableException(th, msg);
	}

    /** Converts the given {@link IOException} into a {@link RuntimeException}.
     * @param pExc The {@link IOException} being rethrown.
     * @return The {@link UncheckedIOException}, which is wrapping the given
     *   {@link IOException}. Never null.
     */
    public static RuntimeException newUncheckedIOException(@NonNull IOException pExc) {
        return new UncheckedIOException(pExc);
    }

    /** Converts the given {@link Exception} into a {@link RuntimeException}.
     * @param pTh The {@link IOException} being rethrown.
     * @return The {@link RuntimeException}, which is wrapping the given
     *   {@link Throwable}. Never null.
     */
    public static RuntimeException newUncheckedException(@NonNull Throwable pTh) {
        return new UndeclaredThrowableException(pTh);
    }

    /**
     * Rethrows the given {@link Throwable}, either directly, or by wrapping it in a suitable subclass,
     * which can be thrown safely, without affecting the calling methods signature.
     * @param pTh The throwable being rethrown.
     * @param pClass1 An exception class, which can be thrown without affecting the calling
     * methods signature.
     * @param pClass2 Another exception class, which can be thrown without affecting the calling
     * methods signature.
     * @return Nothing, this method will <em>always</em> throw an exception.
     * @throws T1 Type of an exception, which may be thrown without affecting the calling methods
     *   signature.
     * @throws T2 Type of another exception, which may be thrown without affecting the calling methods
     *   signature.
     * @param <T1> Type of an exception, which may be thrown without affecting the calling methods
     *   signature.
     * @param <T2> Type of another exception, which may be thrown without affecting the calling methods
     *   signature.
     */
    public static <T1 extends Throwable, T2 extends Throwable> RuntimeException show(@NonNull Throwable pTh, @NonNull Class<T1> pClass1, @NonNull Class<T2> pClass2) throws T1, T2 {
    	final Throwable th = Objects.requireNonNull(pTh, "The Throwable must not be null.");
    	final Class<T1> class1 = Objects.requireNonNull(pClass1, "Class1");
    	final Class<T2> class2 = Objects.requireNonNull(pClass2, "Class2");
        if (class1.isAssignableFrom(th.getClass())) {
            @SuppressWarnings("null")
			final @NonNull T1 th1 = class1.cast(th);
			throw th1;
        } else if (class2.isAssignableFrom(th.getClass())) {
            @SuppressWarnings("null")
			final @NonNull T2 th2 = pClass2.cast(th);
			throw th2;
        } else if (th instanceof RuntimeException) {
            throw (RuntimeException) th;
        } else if (th instanceof Error) {
            throw (Error) th;
        } else if (th instanceof IOException) {
            throw newUncheckedIOException((IOException) th);
        } else {
            throw newUncheckedException(pTh);
        }
    }

    /** Returns the given throwables stacktrace as a string.
     * @param pTh The {@link Throwable} to convert into a stack trace.
     * @return The given throwables stack trace.
     */
    public static String toString(@NonNull Throwable pTh) {
    	final @NonNull Throwable th = Objects.requireNonNull(pTh, "Throwable");
    	final StringWriter sw = new StringWriter();
    	final PrintWriter pw = new PrintWriter(sw);
    	th.printStackTrace(pw);
    	pw.close();
    	return sw.toString();
    }

    /** Checks, whether the given throwable is caused by an instance of the given class.
     * If so, returns the cause. Otherwise, returns null.
     * @param pTh The throwable, that is being checked.
     * @param pType The type of the cause.
     * @return The cause, if it meets the requested criteria, otherwise null.
     * @param <E> The type of the cause.
     */
	public static @Nullable <E extends Throwable> E getCause(@NonNull Throwable pTh, @NonNull Class<E> pType) {
		final Throwable cause = pTh.getCause();
		if (cause != null  &&  cause != pTh  &&  pType.isAssignableFrom(cause.getClass())) {
			return pType.cast(cause);
		} else {
			return null;
		}
	}

	/** Returns true, if the given exception has a non-trivial cause.
	 * (Neither null, nor itself).
	 * @param pTh The exception, which is being checked.
	 * @return True, if the exception has a non-trivial cause.
	 */
	public static boolean hasCause(@NonNull Throwable pTh) {
		final Throwable cause = pTh.getCause();
		return cause != null  &&  cause != pTh;
	}
}
