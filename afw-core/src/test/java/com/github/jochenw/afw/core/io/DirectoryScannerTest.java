package com.github.jochenw.afw.core.io;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class DirectoryScannerTest {
	private static final DirectoryScanner ds = new DirectoryScanner();

	@Test
	public void testAllFiles() {
		final Set<String> files = new HashSet<>();
		ds.scan(Paths.get("src"), null, null, (c) -> {
			Assert.assertTrue(files.add(c.getUri()));
			System.out.println(c.getUri());
		});
		Assert.assertTrue(files.contains("main/java/com/github/jochenw/afw/core/util/Sax.java"));
		Assert.assertTrue(files.contains("main/resources/com/github/jochenw/afw/core/plugins/plugin-list.xsd"));
		Assert.assertTrue(files.contains("test/java/com/github/jochenw/afw/core/util/StringsTest.java"));
		Assert.assertTrue(files.contains("test/resources/com/github/jochenw/afw/test/mod/test.properties"));
	}

	@Test
	public void testSrcMain() {
		final Set<String> files = new HashSet<>();
		final IMatcher[] includes = new IMatcher[] {
			new DefaultMatcher("main/**/*")
		};
		ds.scan(Paths.get("src"), includes, null, (c) -> {
			Assert.assertTrue(files.add(c.getUri()));
			System.out.println(c.getUri());
		});
		Assert.assertTrue(files.contains("main/java/com/github/jochenw/afw/core/util/Sax.java"));
		Assert.assertTrue(files.contains("main/resources/com/github/jochenw/afw/core/plugins/plugin-list.xsd"));
		Assert.assertFalse(files.contains("test/java/com/github/jochenw/afw/core/util/StringsTest.java"));
		Assert.assertFalse(files.contains("test/resources/com/github/jochenw/afw/test/mod/test.properties"));
	}
}
