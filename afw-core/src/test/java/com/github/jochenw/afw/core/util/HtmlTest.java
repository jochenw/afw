package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.function.BiConsumer;

import org.junit.Assert;
import org.junit.Test;

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
				Assert.assertSame(inp, actual);
			} else {
				Assert.assertEquals(exp, actual);
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
				Assert.assertSame(inp, actual);
			} else {
				Assert.assertEquals(exp, actual);
			}
		};
		tester.accept("abcdef", null);
		tester.accept("abc\"defg", "abc&quot;defg");
		tester.accept("abc'defg", "abc&#39;defg");
		tester.accept("abc<def", "abc&lt;def");
		tester.accept("abcd>ef", "abcd&gt;ef");
		tester.accept("&abcdef", "&amp;abcdef");
	}

}
