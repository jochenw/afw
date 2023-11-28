package com.github.jochenw.afw.core.io;

import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

/**
 * Test case for {@link DirResourceRepository}.
 */
public class DirResourceRepositoryTest {
	/**
	 * Test case for {@link DirResourceRepository#list(java.util.function.Consumer)}.
	 * @throws Exception The test has failed.
	 */
	@Test
	public void testList() throws Exception {
		final Path path = Paths.get(".");
		final Set<String> namespaces = new HashSet<>();
		final Set<String> uris = new HashSet<>();
		new DirResourceRepository(path).list((r) -> {namespaces.add(r.getNamespace()); uris.add(r.getUri());});
		assertTrue(namespaces.contains(""));
		assertTrue(namespaces.contains("docs"));
		assertTrue(namespaces.contains("src/main/java/com/github/jochenw/afw/core/log/simple"));
		assertTrue(namespaces.contains("src/test/java/com/github/jochenw/afw/core/log/simple"));
		assertTrue(uris.contains("pom.xml"));
		assertTrue(uris.contains("docs/dependencies.html"));
		assertTrue(uris.contains("src/main/java/com/github/jochenw/afw/core/log/simple/SimpleLog.java"));
		assertTrue(uris.contains("target/classes/com/github/jochenw/afw/core/log/simple/SimpleLog.class"));
		assertTrue(uris.contains("src/test/java/com/github/jochenw/afw/core/log/simple/SimpleLogFactoryTest.java"));
		assertTrue(uris.contains("target/test-classes/com/github/jochenw/afw/core/log/simple/SimpleLogFactoryTest.class"));
	}
}
