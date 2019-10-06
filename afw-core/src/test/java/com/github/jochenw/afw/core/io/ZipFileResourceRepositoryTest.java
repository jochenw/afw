package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.github.jochenw.afw.core.io.IResourceRepository.IResource;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.Streams;


/** Test case for the {@link ZipFileResourceRepository}.
 */
public class ZipFileResourceRepositoryTest {
	/**
	 * Test case for {@link ZipFileResourceRepository#list(Consumer)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testList() throws Exception {
		final Path zipFile = Paths.get("target/unit-tests/ZipFileRepositoryTest.zip");
		createZipFile(zipFile);
		final Set<String> namespaces = new HashSet<>();
		final Set<String> uris = new HashSet<>();
		final String prefix = "jar:" + zipFile.toUri().toURL().toExternalForm() + "!";
		final Holder<IResource> pomXmlResourceHolder = new Holder<IResource>();
		final ZipFileResourceRepository zfrr = new ZipFileResourceRepository(zipFile);
		zfrr.list((r) -> {
			namespaces.add(r.getNamespace());
			final String uri = r.getUri();
			if (uri.startsWith(prefix)) {
				final String subUri = uri.substring(prefix.length());
				uris.add(subUri);
				if ("./pom.xml".equals(subUri)) {
					pomXmlResourceHolder.set(r);
				}
			} else {
				throw new IllegalArgumentException("Expected prefix=" + prefix + ", got " + uri);
			}
			uris.add(r.getUri());
		});
		assertTrue(namespaces.contains("."));
		assertTrue(namespaces.contains("./docs"));
		assertTrue(namespaces.contains("./src/main/java/com/github/jochenw/afw/core/log/simple"));
		assertTrue(namespaces.contains("./src/test/java/com/github/jochenw/afw/core/log/simple"));
		assertTrue(uris.contains("./pom.xml"));
		assertTrue(uris.contains("./docs/dependencies.html"));
		assertTrue(uris.contains("./src/main/java/com/github/jochenw/afw/core/log/simple/SimpleLog.java"));
		assertTrue(uris.contains("./target/classes/com/github/jochenw/afw/core/log/simple/SimpleLog.class"));
		assertTrue(uris.contains("./src/test/java/com/github/jochenw/afw/core/log/simple/SimpleLogFactoryTest.java"));
		assertTrue(uris.contains("./target/test-classes/com/github/jochenw/afw/core/log/simple/SimpleLogFactoryTest.class"));
		final IResource res = pomXmlResourceHolder.get();
		assertNotNull(res);
		try (InputStream in1 = zfrr.open(res);
			 BufferedInputStream bin1 = new BufferedInputStream(in1);
			 InputStream in2 = Files.newInputStream(Paths.get("./pom.xml"));
			 BufferedInputStream bin2 = new BufferedInputStream(in2)) {
			for (;;) {
				final int i1 = bin1.read();
				final int i2 = bin2.read();
				assertEquals(i1, i2);
				if (i1 == -1) {
					break;
				}
			}
		}
	}

	private void createZipFile(final Path zipFile) throws IOException {
		final Path zipFileDir = zipFile.getParent();
		Files.createDirectories(zipFileDir);
		Files.deleteIfExists(zipFile);
		final DirResourceRepository drr = new DirResourceRepository(Paths.get("."));
		try (OutputStream os = Files.newOutputStream(zipFile);
			 ZipOutputStream zos = new ZipOutputStream(os)) {
			final Consumer<IResource> consumer = (r) -> {
				final String uri = r.getUri();
				if (!uri.endsWith(".zip")) { // Do not attempt, to copy the created file into itself.
					final ZipEntry ze = new ZipEntry(uri);
					ze.setMethod(ZipEntry.DEFLATED);
					try {
						zos.putNextEntry(ze);
						try (InputStream in = drr.open(r)) {
							Streams.copy(in, zos);
						}
						zos.closeEntry();
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			};
			drr.list(consumer);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
