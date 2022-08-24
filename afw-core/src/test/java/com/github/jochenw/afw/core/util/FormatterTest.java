package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import javax.naming.spi.ResolveResult;

import org.junit.Test;

import com.github.jochenw.afw.core.data.Data;
import com.github.jochenw.afw.core.util.Formatter.Resolver;

public class FormatterTest {
	@Test
	public void testNoNumbersOrNames() {
		final Resolver resolver = newResolver();

		assertEquals("Parameters: \"abc\", [\"d\",\"e\",\"f\"], \"ghi\"",
			     new Formatter(resolver).format("Parameters: {}, {}, {}"));

		final Resolver resolver2 = new Resolver() {
			@Override
			public Object resolve(int pNumber) {
				switch (pNumber) {
				case 1: return new String[]{"d", "e", "f", "g"};
				default: return resolver.resolve(pNumber);
				}
			}

			@Override
			public Object resolve(String pKey) {
				return resolver.resolve(pKey);
			}
			
		};

		assertEquals("Parameters: \"abc\", [\"d\",\"e\",\"f\",...], \"ghi\"",
			     new Formatter(resolver2).format("Parameters: {}, {}, {}"));

	}

	private Resolver newResolver() {
		final Resolver resolver = new Resolver() {
			@Override
			public Object resolve(int pNumber) {
				switch (pNumber) {
				case 0: return "abc";
				case 1: return new String[]{"d", "e", "f"};
				case 2: return "ghi";
				default: return null;
				}
			}

			@Override
			public Object resolve(String pKey) {
				switch (pKey) {
				case "answer": return Integer.valueOf(42);
				case "number": return Double.valueOf(43.0);
				case "map": return Data.asMap("x", "X", "y", "Y", "z", "Z");
				default: return null;
				}
			}
		};
		return resolver;
	}

}
