package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import org.junit.Test;

public class CircularByteBufferTest {
	@Test
	public void test() throws Exception {
		final CircularByteBuffer cbb = new CircularByteBuffer(20);
		final byte[] fiveBytes = "01234".getBytes("US-ASCII");
		assertEquals(20, cbb.getSpace());
		assertEquals(0, cbb.getCurrentNumberOfBytes());
		assertFalse(cbb.hasBytes());
		cbb.add(fiveBytes, 0, 5);
		assertTrue(cbb.peek("01234".getBytes("US-ASCII"), 0, 5));
		assertEquals("01234", cbb.toString("US-ASCII"));
		assertEquals(5, cbb.getCurrentNumberOfBytes());
		assertEquals(15, cbb.getSpace());
		assertTrue(cbb.hasBytes());
		cbb.add(fiveBytes, 0, 5);
		assertEquals("0123401234", cbb.toString("US-ASCII"));
		assertTrue(cbb.peek("0123401234".getBytes("US-ASCII"), 0, 10));
		assertTrue(cbb.peek("012340123".getBytes("US-ASCII"), 0, 9));
		assertEquals(10, cbb.getCurrentNumberOfBytes());
		assertEquals(10, cbb.getSpace());
		assertTrue(cbb.hasBytes());
		cbb.add(fiveBytes, 0, 5);
		assertEquals("012340123401234", cbb.toString("US-ASCII"));
		assertTrue(cbb.peek("01234012340123401234".getBytes("US-ASCII"), 0, 15));
		assertTrue(cbb.peek("012340123".getBytes("US-ASCII"), 0, 9));
		assertEquals(15, cbb.getCurrentNumberOfBytes());
		assertEquals(5, cbb.getSpace());
		assertTrue(cbb.hasBytes());
		cbb.add(fiveBytes, 0, 5);
		assertEquals("01234012340123401234", cbb.toString("US-ASCII"));
		assertTrue(cbb.peek("0123401234012340123401234".getBytes("US-ASCII"), 0, 20));
		assertTrue(cbb.peek("01234012340123401234012345".getBytes("US-ASCII"), 0, 20));
		assertTrue(cbb.peek("012340123".getBytes("US-ASCII"), 0, 9));
		assertEquals(20, cbb.getCurrentNumberOfBytes());
		assertEquals(0, cbb.getSpace());
		assertTrue(cbb.hasBytes());
		try {
			cbb.add((byte) 0);
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("No space available", e.getMessage());
		}
		assertEquals(20, cbb.getCurrentNumberOfBytes());
		assertEquals(0, cbb.getSpace());
		assertTrue(cbb.hasBytes());
		assertTrue(cbb.peek(new byte[0], 0, 0));
		cbb.clear();
		assertEquals(0, cbb.getCurrentNumberOfBytes());
		assertEquals(20, cbb.getSpace());
		assertFalse(cbb.hasBytes());
	}

}
