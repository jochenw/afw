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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.github.jochenw.afw.core.function.Functions;


/** Test for {@link Exceptions}.
 */
public class ExceptionsTest {
	/** Test case for {@link Exceptions#show(Throwable)}.
	 */
    @Test
    public void testSimpleShow() {
        final NullPointerException npe = new NullPointerException();
        try {
            Exceptions.show(Objects.fakeNonNull());
            fail("Expected NPE");
        } catch (NullPointerException e) {
            assertEquals("The Throwable must not be null.", e.getMessage());
            assertFalse(e == npe);
        }

        try {
            Exceptions.show(npe);
            fail("Expected NPE");
        } catch (NullPointerException e) {
            assertSame(npe, e);
        }
        OutOfMemoryError oome = new OutOfMemoryError();
        try {
            Exceptions.show(oome);
            fail("Expected OutOfMemoryError");
        } catch (OutOfMemoryError e) {
            assertSame(oome, e);
        }
        IOException ioe = new IOException("I/O Error");
        try {
            Exceptions.show(ioe);
            fail("Expected UncheckedIOException");
        } catch (UncheckedIOException e) {
            assertSame(ioe, e.getCause());
        }
        Throwable th = new Throwable("Undefined error");
        try {
            Exceptions.show(th);
            fail("Expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException ute) {
            assertSame(th, ute.getCause());
        }
    }

	/** Test case for {@link Exceptions#show(Throwable, Class)}.
	 * @throws IOException The test failed.
	 */
    @Test
    public void testOneExceptionDeclared() throws IOException {
        final NullPointerException npe = new NullPointerException();
        try {
            Exceptions.show(Objects.fakeNonNull(), IOException.class);
            fail("Expected NPE");
        } catch (NullPointerException e) {
            assertEquals("The Throwable must not be null.", e.getMessage());
            assertFalse(e == npe);
        }

        try {
            Exceptions.show(npe, IOException.class);
            fail("Expected NPE");
        } catch (NullPointerException e) {
            assertSame(npe, e);
        }
        OutOfMemoryError oome = new OutOfMemoryError();
        try {
            Exceptions.show(oome, IOException.class);
            fail("Expected OutOfMemoryError");
        } catch (OutOfMemoryError e) {
            assertSame(oome, e);
        }
        SAXException se = new SAXException("I/O Error");
        try {
            Exceptions.show(se, IOException.class);
            fail("Expected UncheckedIOException");
        } catch (UndeclaredThrowableException ute) {
            assertSame(se, ute.getCause());
        }
        IOException ioe = new IOException("I/O Error");
        try {
            Exceptions.show(ioe, IOException.class);
            fail("Expected UncheckedIOException");
        } catch (IOException e) {
            assertSame(ioe, e);
        }
        Throwable th = new Throwable("Undefined error");
        try {
            Exceptions.show(th, IOException.class);
            fail("Expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException ute) {
            assertSame(th, ute.getCause());
        }
    }

	/** Test case for {@link Exceptions#show(Throwable, Class, Class)}.
	 * @throws IOException The test failed.
	 * @throws SAXException The test failed.
	 */
    @Test
    public void testTwoExceptionDeclared() throws IOException, SAXException {
        final NullPointerException npe = new NullPointerException();
        try {
            Exceptions.show(Objects.fakeNonNull(), IOException.class, SAXException.class);
            fail("Expected NPE");
        } catch (NullPointerException e) {
            assertEquals("The Throwable must not be null.", e.getMessage());
            assertFalse(e == npe);
        }

        Functions.assertFail(npe, () -> Exceptions.show(npe, IOException.class, SAXException.class));
        OutOfMemoryError oome = new OutOfMemoryError();
        Functions.assertFail(oome, () -> Exceptions.show(oome, IOException.class, SAXException.class));
        IOException ioe = new IOException("I/O Error");
        Functions.assertFail(ioe, () -> Exceptions.show(ioe, IOException.class, SAXException.class));
        SQLException sqle = new SQLException("SQL Error");
        try {
            Exceptions.show(sqle, IOException.class, SAXException.class);
            fail("Expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException ute) {
            assertSame(sqle, ute.getCause());
        }
        Throwable th = new Throwable("Undefined error");
        try {
            Exceptions.show(th, IOException.class, SAXException.class);
            fail("Expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException ute) {
            assertSame(th, ute.getCause());
        }
    }

    /** Test case for {@link Exceptions#toString(Throwable)}.
     */
    @Test
    public void testThrowableToString() {
    	final String stackTrace = Exceptions.toString(new NullPointerException());
    	final String[] lines = Strings.toLines(stackTrace);
    	assertEquals(NullPointerException.class.getName(), lines[0]);
    	assertStackTraceLine(lines[1], getClass(), "testThrowableToString");
    }

    private void assertStackTraceLine(String pLine, Class<?> pClass, String pMethod) {
    	final String s = "at " + pClass.getName() + "." + pMethod + "(" + pClass.getSimpleName() + ".java:";
    	assertTrue(pLine.indexOf(s) > 0);
    }
}
