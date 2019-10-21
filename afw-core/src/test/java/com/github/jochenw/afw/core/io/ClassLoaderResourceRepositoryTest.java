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
		final ClassLoaderResourceRepository clrr = new ClassLoaderResourceRepository(getClass().getClassLoader());
		clrr.list((r) -> uris.add(r.getUri()));
		assertTrue(uris.contains("javax/servlet/Filter.class"));  // An entry from Maven's "compile" scope.
		assertTrue(uris.contains("org/junit/Test.class"));       // An entry from Maven's "test" scope.
		assertResourceFound(uris, clrr, "com/github/jochenw/afw/core/plugins/plugin-list.xsd", "xsd");
		assertResourceFound(uris, clrr, "com/github/jochenw/afw/core/io/ClassLoaderResourceRepositoryTest.class", "ClassLoader"); // An entry from the "target/test-classes" folder.
		assertFalse(uris.contains("com/github/jochenw/afw/core/io/ClassLoaderResourceRepositoryTest.java"));
	}

	private void assertResourceFound(final Set<String> pUris, final ClassLoaderResourceRepository pRepository, String pUri, String pMatchString) {
		if (pUris.contains(pUri)) {
			assertTrue(pUris.contains(pUri));
		} else {
			pRepository.list((r) -> {
				final String uri = r.getUri();
				if (uri.contains(pMatchString)) {
					System.out.println(uri);
				}
			});
		}
	}
}
