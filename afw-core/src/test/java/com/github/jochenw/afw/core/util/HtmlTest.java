package com.github.jochenw.afw.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/** Test suite for {@link Html}.
 */
public class HtmlTest {
	/** Test case for {@link Html#escapeHtml(String)}.
	 */
	@Test
	public void testEscapeHtml() {
		final BiConsumer<String,String> tester = (inp,exp) -> {
			final String actual = Html.escapeHtml(inp);
			if (exp == null) {
				assertSame(inp, actual);
			} else {
				assertEquals(exp, actual);
			}
		};
		tester.accept("abc\"defg", null);
		tester.accept("abc'defg", null);
		tester.accept("abc<def", "abc&lt;def");
		tester.accept("abcd>ef", "abcd&gt;ef");
		tester.accept("&abcdef", "&amp;abcdef");
	}

	/** Test case for {@link Html#escapeHtmlAttr(String)}.
	 */
	@Test
	public void testEscapeHtmlAttr() {
		final BiConsumer<String,String> tester = (inp,exp) -> {
			final String actual = Html.escapeHtmlAttr(inp);
			if (exp == null) {
				assertSame(inp, actual);
			} else {
				assertEquals(exp, actual);
			}
		};
		tester.accept("abcdef", null);
		tester.accept("abc\"defg", "abc&quot;defg");
		tester.accept("abc'defg", "abc&#39;defg");
		tester.accept("abc<def", "abc&lt;def");
		tester.accept("abcd>ef", "abcd&gt;ef");
		tester.accept("&abcdef", "&amp;abcdef");
	}

	/** Test case for {@link Html#escapeOrNbsp(String)}.
	 */
	@Test
	public void testEscapeOrNbsp() {
		assertEquals("&nbsp;", Html.escapeOrNbsp(null));
		assertEquals("&nbsp;", Html.escapeOrNbsp(""));
		assertEquals(" ", Html.escapeOrNbsp(" "));
		assertEquals("abc&lt;def", Html.escapeOrNbsp("abc<def"));
	}

	/** Test case for {@link Html#escapeOrNbspIf(boolean, Supplier)}.
	 */
	@Test
	public void testEscapeOrNbspIf() {
		assertEquals("&nbsp;", Html.escapeOrNbspIf(false, () -> null));
		assertEquals("&nbsp;", Html.escapeOrNbspIf(false, () -> ""));
		assertEquals("&nbsp;", Html.escapeOrNbspIf(false, () -> " "));
		assertEquals("&nbsp;", Html.escapeOrNbspIf(false, () -> " "));
		assertEquals("abc&lt;def", Html.escapeOrNbspIf(true, () -> "abc<def"));
		assertEquals("&nbsp;", Html.escapeOrNbspIf(true, () -> ""));
		assertEquals(" ", Html.escapeOrNbspIf(true, () -> " "));
		assertEquals("abc&lt;def", Html.escapeOrNbsp("abc<def"));
	}
}
