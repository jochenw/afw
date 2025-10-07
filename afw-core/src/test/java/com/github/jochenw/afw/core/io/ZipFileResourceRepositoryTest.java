package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Test;

import com.github.jochenw.afw.core.io.IResourceRepository.IResource;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.Streams;


/** Test case for the {@link ZipFileResourceRepository}.
 */
public class ZipFileResourceRepositoryTest {
	private final String[] URIS = {
		"pom.xml",
		"docs/dependencies.html",
		"src/main/java/com/github/jochenw/afw/core/log/simple/SimpleLog.java",
		"target/classes/com/github/jochenw/afw/core/log/simple/SimpleLog.class",
		"src/test/java/com/github/jochenw/afw/core/log/simple/SimpleLogFactoryTest.java",
		"target/test-classes/com/github/jochenw/afw/core/log/simple/SimpleLogFactoryTest.class"
	};

	/**
	 * Test case for {@link ZipFileResourceRepository#list(Consumer)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testList() throws Exception {
		final Path zipFile = Paths.get("target/unit-tests/ZipFileRepositoryTest.zip");
		createZipFile2(zipFile);
		final Set<String> namespaces = new HashSet<>();
		final Set<String> uris = new HashSet<>();
		final Holder<IResource> pomXmlResourceHolder = new Holder<IResource>();
		final ZipFileResourceRepository zfrr = new ZipFileResourceRepository(zipFile);
		zfrr.list((r) -> {
			namespaces.add(r.getNamespace());
			final String uri = r.getUri();
			uris.add(uri);
			if ("pom.xml".equals(uri)) {
				pomXmlResourceHolder.set(r);
			}
			uris.add(uri);
		});
		assertTrue(namespaces.contains(""));
		assertTrue(namespaces.contains("docs"));
		assertTrue(namespaces.contains("src/main/java/com/github/jochenw/afw/core/log/simple"));
		assertTrue(namespaces.contains("src/test/java/com/github/jochenw/afw/core/log/simple"));
		for (String uri : URIS) {
			assertTrue("Uri not found: " + uri, uris.contains(uri));
		}
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

	private void createZipFile2(final Path pZipFile) throws IOException {
		final Path dir = Paths.get(".").toAbsolutePath();
		final List<Path> files = new ArrayList<>();
		final FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				if (attrs.isRegularFile()) {
					final Path relativePath = dir.relativize(file);
					final String uri = relativePath.toString().replace('\\', '/');
					for (int i = 0;  i < URIS.length;  i++) {
						if (URIS[i].equals(uri)) {
							files.add(relativePath);
							break;
						}
					}
				}
				return FileVisitResult.CONTINUE;
			}
		};
		Files.walkFileTree(dir, fv);
		try (OutputStream os = Files.newOutputStream(pZipFile);
				 ZipOutputStream zos = new ZipOutputStream(os)) {
			for (Path file : files) {
				final String uri = file.toString();
				final ZipEntry ze = new ZipEntry(uri);
				ze.setMethod(ZipEntry.DEFLATED);
				try {
					zos.putNextEntry(ze);
					try (InputStream in = Files.newInputStream(file);
						 BufferedInputStream bin = new BufferedInputStream(in)) {
						Streams.copy(in, zos);
					}
					zos.closeEntry();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		}
	}
}
