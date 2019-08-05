package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class AbstractBuilderTest {
	private static class IntegerBuilder extends AbstractBuilder<Integer,IntegerBuilder> {
		private int num;

		public IntegerBuilder add(int pNum) {
			assertMutable();
			num += pNum;
			return self();
		}

		@Override
		protected Integer newInstance() {
			return Integer.valueOf(num);
		}
	}

	@Test
	public void testIntegerBuilder() {
		final IntegerBuilder ib = new IntegerBuilder();
		assertTrue(ib.isMutable());
		assertSame(ib, ib.add(2));
		final Integer got = ib.add(3).add(4).build();
		assertSame(got, ib.build());
		assertFalse(ib.isMutable());
		assertEquals(9, got.intValue());
		try {
			ib.add(5);
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("This object is no longer mutable.", e.getMessage());
		}
	}
}
