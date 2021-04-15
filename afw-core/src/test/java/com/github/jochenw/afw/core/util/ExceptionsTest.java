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
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;


/** Test for {@link Exceptions}.
 */
public class ExceptionsTest {
	/** Test case for {@link Exceptions#show(Throwable)}.
	 */
    @Test
    public void testSimpleShow() {
        final NullPointerException npe = new NullPointerException();
        try {
            Exceptions.show(null);
            Assert.fail("Expected NPE");
        } catch (NullPointerException e) {
            Assert.assertEquals("The Throwable must not be null.", e.getMessage());
            Assert.assertFalse(e == npe);
        }

        try {
            Exceptions.show(npe);
            Assert.fail("Expected NPE");
        } catch (NullPointerException e) {
            Assert.assertSame(npe, e);
        }
        OutOfMemoryError oome = new OutOfMemoryError();
        try {
            Exceptions.show(oome);
            Assert.fail("Expected OutOfMemoryError");
        } catch (OutOfMemoryError e) {
            Assert.assertSame(oome, e);
        }
        IOException ioe = new IOException("I/O Error");
        try {
            Exceptions.show(ioe);
            Assert.fail("Expected UncheckedIOException");
        } catch (UncheckedIOException e) {
            Assert.assertSame(ioe, e.getCause());
        }
        Throwable th = new Throwable("Undefined error");
        try {
            Exceptions.show(th);
            Assert.fail("Expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException ute) {
            Assert.assertSame(th, ute.getCause());
        }
    }

	/** Test case for {@link Exceptions#show(Throwable, Class)}.
	 * @throws IOException The test failed.
	 */
    @Test
    public void testOneExceptionDeclared() throws IOException {
        final NullPointerException npe = new NullPointerException();
        try {
            Exceptions.show(null, IOException.class);
            Assert.fail("Expected NPE");
        } catch (NullPointerException e) {
            Assert.assertEquals("The Throwable must not be null.", e.getMessage());
            Assert.assertFalse(e == npe);
        }

        try {
            Exceptions.show(npe, IOException.class);
            Assert.fail("Expected NPE");
        } catch (NullPointerException e) {
            Assert.assertSame(npe, e);
        }
        OutOfMemoryError oome = new OutOfMemoryError();
        try {
            Exceptions.show(oome, IOException.class);
            Assert.fail("Expected OutOfMemoryError");
        } catch (OutOfMemoryError e) {
            Assert.assertSame(oome, e);
        }
        SAXException se = new SAXException("I/O Error");
        try {
            Exceptions.show(se, IOException.class);
            Assert.fail("Expected UncheckedIOException");
        } catch (UndeclaredThrowableException ute) {
            Assert.assertSame(se, ute.getCause());
        }
        IOException ioe = new IOException("I/O Error");
        try {
            Exceptions.show(ioe, IOException.class);
            Assert.fail("Expected UncheckedIOException");
        } catch (IOException e) {
            Assert.assertSame(ioe, e);
        }
        Throwable th = new Throwable("Undefined error");
        try {
            Exceptions.show(th, IOException.class);
            Assert.fail("Expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException ute) {
            Assert.assertSame(th, ute.getCause());
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
            Exceptions.show(null, IOException.class, SAXException.class);
            Assert.fail("Expected NPE");
        } catch (NullPointerException e) {
            Assert.assertEquals("The Throwable must not be null.", e.getMessage());
            Assert.assertFalse(e == npe);
        }

        try {
            Exceptions.show(npe, IOException.class, SAXException.class);
            Assert.fail("Expected NPE");
        } catch (NullPointerException e) {
            Assert.assertSame(npe, e);
        }
        OutOfMemoryError oome = new OutOfMemoryError();
        try {
            Exceptions.show(oome, IOException.class, SAXException.class);
            Assert.fail("Expected OutOfMemoryError");
        } catch (OutOfMemoryError e) {
            Assert.assertSame(oome, e);
        }
        SAXException se = new SAXException("I/O Error");
        try {
            Exceptions.show(se, IOException.class, SAXException.class);
            Assert.fail("Expected UncheckedIOException");
        } catch (SAXException e) {
            Assert.assertSame(se, e);
        }
        IOException ioe = new IOException("I/O Error");
        try {
            Exceptions.show(ioe, IOException.class, SAXException.class);
            Assert.fail("Expected UncheckedIOException");
        } catch (IOException e) {
            Assert.assertSame(ioe, e);
        }
        SQLException sqle = new SQLException("SQL Error");
        try {
            Exceptions.show(sqle, IOException.class, SAXException.class);
            Assert.fail("Expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException ute) {
            Assert.assertSame(sqle, ute.getCause());
        }
        Throwable th = new Throwable("Undefined error");
        try {
            Exceptions.show(th, IOException.class, SAXException.class);
            Assert.fail("Expected UndeclaredThrowableException");
        } catch (UndeclaredThrowableException ute) {
            Assert.assertSame(th, ute.getCause());
        }
    }

    /** Test case for {@link Exceptions#toString(Throwable)}.
     */
    @Test
    public void testThrowableToString() {
    	final String stackTrace = Exceptions.toString(new NullPointerException());
    	final String[] lines = Strings.toLines(stackTrace);
    	Assert.assertEquals(NullPointerException.class.getName(), lines[0]);
    	assertStackTraceLine(lines[1], getClass(), "testThrowableToString");
    }

    private void assertStackTraceLine(String pLine, Class<?> pClass, String pMethod) {
    	final String s = "at " + pClass.getName() + "." + pMethod + "(" + pClass.getSimpleName() + ".java:";
    	Assert.assertTrue(pLine.indexOf(s) > 0);
    }
}
