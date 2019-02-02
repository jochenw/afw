package com.github.jochenw.afw.core.io;

import org.junit.Assert;
import org.junit.Test;

public class DefaultMatcherTest {
	@Test
	public void tesCaseSensitive() {
		final DefaultMatcher dm = new DefaultMatcher("**/*.java");
		Assert.assertFalse(dm.isMatchingAll());
		Assert.assertTrue(dm.matches("com/foo/bar.java"));
		Assert.assertTrue(dm.matches("com/Foo/bar.java"));
		Assert.assertFalse(dm.matches("com/Foo/bar.javax"));
		Assert.assertFalse(dm.matches("com/Foo/bar.Java"));
		Assert.assertFalse(dm.matches("My.java"));
	}

	@Test
	public void testCaseInsensitive() {
		final DefaultMatcher dm = new DefaultMatcher("**/*.java", false);
		Assert.assertFalse(dm.isMatchingAll());
		Assert.assertTrue(dm.matches("com/foo/bar.java"));
		Assert.assertTrue(dm.matches("com/Foo/bar.java"));
		Assert.assertFalse(dm.matches("com/Foo/bar.javax"));
		Assert.assertTrue(dm.matches("com/Foo/bar.Java"));
		Assert.assertFalse(dm.matches("My.java"));
	}

	@Test
	public void testPrefix() {
		final DefaultMatcher dm = new DefaultMatcher("main/**/*");
		Assert.assertTrue(dm.matches("main/java/com/github/jochenw/afw/core/csv/CsvFormatter.java"));
	}
}
