package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Test;


/** Test for the {@link Holder} class.
 */
public class HolderTest {
	/** Basic test case.
	 */
	@Test
	public void test() {
		final Holder<String> holder = new Holder<String>();
		assertNull(holder.get());
		holder.set("0");
		assertEquals("0", holder.get());
		assertEquals("0", holder.require());
		final Holder<String> otherHolder = Holder.synchronizedHolder(holder);
		assertEquals("0", otherHolder.get());
		otherHolder.set("1");
		assertEquals("1", otherHolder.get());
		assertEquals("1", otherHolder.require());
		assertEquals("1", holder.get());
		holder.set(null);
		try {
			holder.require();
		} catch (NoSuchElementException e) {
			assertEquals("No value has been given.", e.getMessage());
		}
	}
}
