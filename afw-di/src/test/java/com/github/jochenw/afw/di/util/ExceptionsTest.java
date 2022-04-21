package com.github.jochenw.afw.di.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.common.util.concurrent.UncheckedExecutionException;

public class ExceptionsTest {
	@Test
	public void test() {
		final RuntimeException rte = new RuntimeException();
		try {
			throw Exceptions.show(rte);
		} catch (RuntimeException e) {
			assertSame(rte, e);
		}
		final Error err = new Error();
		try {
			throw Exceptions.show(err);
		} catch (Error e) {
			assertSame(err, e);
		}
		final IOException ioe = new IOException("I/O Error");
		try {
			throw Exceptions.show(ioe);
		} catch (UncheckedIOException e) {
			assertSame(ioe, e.getCause());
		}
		final SAXException se = new SAXException("SAX Error");
		try {
			throw Exceptions.show(se);
		} catch (UndeclaredThrowableException e) {
			assertSame(se, e.getCause());
		}
		try {
			throw Exceptions.show(null);
		} catch (NullPointerException e) {
			assertEquals("Throwable", e.getMessage());
		}
	}

}
