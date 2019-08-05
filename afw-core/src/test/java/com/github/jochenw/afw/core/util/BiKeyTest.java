package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class BiKeyTest {
	@Test
	public void testHashCode() {
		final BiKey<Boolean,String> expect = new BiKey<>(Boolean.TRUE,"foo");
		final BiKey<Boolean,String> got = new BiKey<>(Boolean.TRUE,"foo");
		assertEquals(expect.hashCode(), got.hashCode());
		assertNotEquals(expect.hashCode(), new BiKey<>(Boolean.FALSE,"foo").hashCode());
		assertNotEquals(expect.hashCode(), new BiKey<>(Boolean.TRUE,"bar").hashCode());
	}

	@Test
	public void testEquals() {
		final BiKey<Boolean,String> expect = new BiKey<>(Boolean.TRUE,"foo");
		final BiKey<Boolean,String> got = new BiKey<>(Boolean.TRUE,"foo");
		assertEquals(expect, got);
		assertNotEquals(expect, new BiKey<>(Boolean.FALSE,"foo"));
		assertNotEquals(expect, new BiKey<>(Boolean.TRUE,"bar"));
	}
}
