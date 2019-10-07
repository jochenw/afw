package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.github.jochenw.afw.core.util.Strings;

/**
 * Test case for the {@link ClassLoaderResourceRepository}.
 */
public class ClassLoaderResourceRepositoryTest {
	/**
	 * Test case for {@link ClassLoaderResourceRepository#list(java.util.function.Consumer)}.
	 */
	@Test
	public void testList() {
		final Set<String> uris = new HashSet<>();
		final ClassLoaderResourceRepository clrr = new ClassLoaderResourceRepository();
		clrr.list((r) -> uris.add(r.getUri()));
		assertTrue(uris.contains("javax/servlet/Filter.class"));  // An entry from Maven's "compile" scope.
		assertTrue(uris.contains("org/junit/Test.class"));       // An entry from Maven's "test" scope.
		assertTrue(Strings.toString(uris), uris.contains("com/github/jochenw/afw/core/plugins/plugin-list.xsd")); // An entry from the "target/classes" folder.
		assertTrue(uris.contains("com/github/jochenw/afw/core/io/ClassLoaderResourceRepositoryTest.class")); // An entry from the "target/test-classes" folder.
		assertFalse(uris.contains("com/github/jochenw/afw/core/io/ClassLoaderResourceRepositoryTest.java"));
	}
}
