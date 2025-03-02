package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Assume;
import org.junit.Test;

import com.github.jochenw.afw.core.io.IResourceRepository.IResource;
import com.github.jochenw.afw.core.util.Systems;


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
		Consumer<IResource> consumer = (r) -> {
			uris.add(r.getUri());
		};
		clrr.list(consumer);
		assertTrue(uris.contains("javax/servlet/Filter.class"));  // An entry from Maven's "compile" scope.
		assertTrue(uris.contains("org/junit/Test.class"));       // An entry from Maven's "test" scope.
		assertResourceFound(uris, clrr, "com/github/jochenw/afw/core/plugins/plugin-list-100.xsd", "xsd");
		assertResourceFound(uris, clrr, "com/github/jochenw/afw/core/io/ClassLoaderResourceRepositoryTest.class", "ClassLoader"); // An entry from the "target/test-classes" folder.
		assertFalse(uris.contains("com/github/jochenw/afw/core/io/ClassLoaderResourceRepositoryTest.java"));
	}

	/**
	 * Test case for proper handling of URL's with blanks in the path.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testPathWithBlanks() throws Exception {
		Assume.assumeTrue(Systems.isWindows());
		final String uri = "file:/C:/Program%20Files%20(x86)/Jenkins/workspace/afw-core/afw-core/target/test-classes/META-INF/MANIFEST.MF";
		final URL url = new URL(uri);
		assertEquals("file", url.getProtocol());
		assertEquals("/C:/Program Files (x86)/Jenkins/workspace/afw-core/afw-core/target/test-classes/META-INF/MANIFEST.MF", URLDecoder.decode(url.getFile(), Charset.defaultCharset().name()));
	}

	private void assertResourceFound(final Set<String> pUris, final ClassLoaderResourceRepository pRepository, String pUri, String pMatchString) {
		assertTrue("Resource not found: " + pUri, pUris.contains(pUri));
		if (pUris.contains(pUri)) {
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
