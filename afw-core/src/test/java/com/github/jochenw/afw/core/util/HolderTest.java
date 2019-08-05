package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class HolderTest {
	@Test
	public void test() {
		final Holder<String> holder = new Holder<String>();
		assertNull(holder.get());
		holder.set("0");
		assertEquals("0", holder.get());
		final Holder<String> otherHolder = Holder.synchronizedHolder(holder);
		assertEquals("0", otherHolder.get());
		otherHolder.set("1");
		assertEquals("1", otherHolder.get());
		assertEquals("1", holder.get());
	}
}
