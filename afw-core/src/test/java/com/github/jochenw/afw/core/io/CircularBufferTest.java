package com.github.jochenw.afw.core.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/** Test for the {@link CircularByteBuffer}, and the {@link CircularCharBuffer}.
 */
public class CircularBufferTest {
	/** Test case for the {@link CircularByteBuffer}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testCircularByteBuffer() throws Exception {
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

	/** Test case for the {@link CircularCharBuffer}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testCircularCharBuffer() {
		final CircularCharBuffer ccb = new CircularCharBuffer(20);
		assertEquals(20, ccb.getSpace());
		assertEquals(0, ccb.getCurrentNumberOfChars());
		assertFalse(ccb.hasChars());
		ccb.add("01234", 0, 5);
		assertTrue(ccb.peek("01234", 0, 5));
		assertEquals("01234", ccb.toString());
		assertEquals(5, ccb.getCurrentNumberOfChars());
		assertEquals(15, ccb.getSpace());
		assertTrue(ccb.hasChars());
		ccb.add("01234", 0, 5);
		assertEquals("0123401234", ccb.toString());
		assertTrue(ccb.peek("0123401234", 0, 10));
		assertTrue(ccb.peek("012340123", 0, 9));
		assertEquals(10, ccb.getCurrentNumberOfChars());
		assertEquals(10, ccb.getSpace());
		assertTrue(ccb.hasChars());
		ccb.add("01234", 0, 5);
		assertEquals("012340123401234", ccb.toString());
		assertTrue(ccb.peek("01234012340123401234", 0, 15));
		assertTrue(ccb.peek("012340123", 0, 9));
		assertEquals(15, ccb.getCurrentNumberOfChars());
		assertEquals(5, ccb.getSpace());
		assertTrue(ccb.hasChars());
		ccb.add("01234", 0, 5);
		assertEquals("01234012340123401234", ccb.toString());
		assertTrue(ccb.peek("0123401234012340123401234", 0, 20));
		assertTrue(ccb.peek("01234012340123401234012345", 0, 20));
		assertTrue(ccb.peek("012340123", 0, 9));
		assertEquals(20, ccb.getCurrentNumberOfChars());
		assertEquals(0, ccb.getSpace());
		assertTrue(ccb.hasChars());
		try {
			ccb.add((char) 0);
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("No space available", e.getMessage());
		}
		assertEquals(20, ccb.getCurrentNumberOfChars());
		assertEquals(0, ccb.getSpace());
		assertTrue(ccb.hasChars());
		assertTrue(ccb.peek(new char[0], 0, 0));
		ccb.clear();
		assertEquals(0, ccb.getCurrentNumberOfChars());
		assertEquals(20, ccb.getSpace());
		assertFalse(ccb.hasChars());
	}
}
