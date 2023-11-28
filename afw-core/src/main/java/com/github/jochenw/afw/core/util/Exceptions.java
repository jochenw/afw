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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


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
    public static RuntimeException show(Throwable pTh) {
        if (pTh == null) {
            throw new NullPointerException("The Throwable must not be null.");
        } else if (pTh instanceof RuntimeException) {
            throw (RuntimeException) pTh;
        } else if (pTh instanceof Error) {
            throw (Error) pTh;
        } else if (pTh instanceof IOException) {
            throw newUncheckedIOException((IOException) pTh);
        } else {
            throw newUncheckedException(pTh);
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
    public static <T extends Throwable> RuntimeException show(Throwable pTh, Class<T> pClass) throws T {
        if (pTh == null) {
            throw new NullPointerException("The Throwable must not be null.");
        } else if (pClass.isAssignableFrom(pTh.getClass())) {
            throw pClass.cast(pTh);
        } else if (pTh instanceof RuntimeException) {
            throw (RuntimeException) pTh;
        } else if (pTh instanceof Error) {
            throw (Error) pTh;
        } else if (pTh instanceof IOException) {
            throw newUncheckedIOException((IOException) pTh);
        } else {
            throw newUncheckedException(pTh);
        }
    }

    /**
     * Rethrows the given {@link Throwable}, either directly, or by wrapping it in a
     * suitable subclass, which can be thrown safely, without affecting the calling
     * methods signature.
     * @param pPrefix A prefix for the thrown exceptions message.
     * @param pTh The throwable being rethrown.
     * @return Nothing, this method will <em>always</em> throw an exception.
     */
	public static RuntimeException show(String pPrefix, Throwable pTh) {
		throw new UndeclaredThrowableException(pTh, pPrefix + pTh.getMessage());
	}

    /** Converts the given {@link IOException} into a {@link RuntimeException}.
     * @param pExc The {@link IOException} being rethrown.
     * @return Nothing, this method will <em>always</em> throw an exception.
     */
    public static RuntimeException newUncheckedIOException(IOException pExc) {
        return new UncheckedIOException(pExc);
    }

    /** Converts the given {@link Exception} into a {@link RuntimeException}.
     * @param pTh The {@link IOException} being rethrown.
     * @return Nothing, this method will <em>always</em> throw an exception.
     */
    public static RuntimeException newUncheckedException(Throwable pTh) {
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
    public static <T1 extends Throwable, T2 extends Throwable> RuntimeException show(Throwable pTh, Class<T1> pClass1, Class<T2> pClass2) throws T1, T2 {
        if (pTh == null) {
            throw new NullPointerException("The Throwable must not be null.");
        } else if (pClass1.isAssignableFrom(pTh.getClass())) {
            throw pClass1.cast(pTh);
        } else if (pClass2.isAssignableFrom(pTh.getClass())) {
            throw pClass2.cast(pTh);
        } else if (pTh instanceof RuntimeException) {
            throw (RuntimeException) pTh;
        } else if (pTh instanceof Error) {
            throw (Error) pTh;
        } else if (pTh instanceof IOException) {
            throw newUncheckedIOException((IOException) pTh);
        } else {
            throw newUncheckedException(pTh);
        }
    }

    /** Returns the given throwables stacktrace as a string.
     * @param pTh The {@link Throwable} to convert into a stack trace.
     * @return The given throwables stack trace.
     */
    public static String toString(Throwable pTh) {
    	final StringWriter sw = new StringWriter();
    	final PrintWriter pw = new PrintWriter(sw);
    	pTh.printStackTrace(pw);
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
	public static @Nullable <E extends Throwable> E getCause(@Nonnull Throwable pTh, @Nonnull Class<E> pType) {
		final Throwable cause = pTh.getCause();
		if (cause != null  &&  cause != pTh  &&  pType.isAssignableFrom(cause.getClass())) {
			return pType.cast(cause);
		} else {
			return null;
		}
	}
}
