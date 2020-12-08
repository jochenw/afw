package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.github.jochenw.afw.core.io.IReadable.NoLongerReadableException;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.Streams;

public class IReadableTest {
	@Test
	public void testPath() throws Exception {
		final Path p = Paths.get("pom.xml");
		assertTrue(Files.isRegularFile(p));
		final IReadable r1 = IReadable.of(p);
		validateNonRepeatable(r1, "pom.xml", Files.size(p));
	}

	private void validateNonRepeatable(final IReadable pReadable, String pName, long pNumBytes) throws IOException {
		assertNotNull(pReadable);
		assertEquals(pName, pReadable.getName());
		assertTrue(pReadable.isReadable());
		assertFalse(pReadable.isRepeatable());
		final Holder<String> contentHolder = new Holder<String>();
		pReadable.read((in) -> contentHolder.set(Streams.read(in, StandardCharsets.UTF_8)));
		final String contents = contentHolder.get();
		assertEquals(pNumBytes, contents.getBytes(StandardCharsets.UTF_8).length);
		assertFalse(pReadable.isReadable());
		assertFalse(pReadable.isRepeatable());
		try {
			pReadable.read((FailableConsumer<InputStream,?>) null);
			fail("Expected Exception");
		} catch (NoLongerReadableException e) {
			assertEquals("This IReadable has already been read: " + pName, e.getMessage());
		}
	}

	@Test
	public void testPathRepeatable() throws Exception {
		final Path p = Paths.get("pom.xml");
		assertTrue(Files.isRegularFile(p));
		final IReadable r1 = IReadable.of(p).repeatable();
		validateRepeatable(r1, "pom.xml", Files.size(p));
	}

	private void validateRepeatable(final IReadable r1, String pName, long pNumBytes) throws IOException {
		assertNotNull(r1);
		assertEquals(pName, r1.getName());
		assertTrue(r1.isReadable());
		assertTrue(r1.isRepeatable());
		r1.read((in) -> Streams.read(in));
		assertTrue(r1.isReadable());
		assertTrue(r1.isRepeatable());
		final Holder<String> contentHolder = new Holder<String>();
		r1.read((in) -> contentHolder.set(Streams.read(in, StandardCharsets.UTF_8)));
		final String contents = contentHolder.get();
		assertEquals(pNumBytes, contents.getBytes(StandardCharsets.UTF_8).length);
		final String contents2 = r1.apply((in) -> Streams.read(in, StandardCharsets.UTF_8));
		assertEquals(contents2, contents);
		assertTrue(r1.isReadable());
		assertTrue(r1.isRepeatable());
	}

	@Test
	public void testFile() throws Exception {
		final File f = new File("pom.xml");
		assertTrue(f.isFile());
		final IReadable r1 = IReadable.of(f);
		assertNotNull(r1);
		assertEquals("pom.xml", r1.getName());
		assertTrue(r1.isReadable());
		assertFalse(r1.isRepeatable());
		final Holder<String> contentHolder = new Holder<String>();
		r1.read((in) -> contentHolder.set(Streams.read(in, StandardCharsets.UTF_8)));
		final String contents = contentHolder.get();
		assertEquals(f.length(), contents.getBytes(StandardCharsets.UTF_8).length);
		assertFalse(r1.isReadable());
		assertFalse(r1.isRepeatable());
		try {
			r1.read((FailableConsumer<InputStream,?>) null);
			fail("Expected Exception");
		} catch (NoLongerReadableException e) {
			assertEquals("This IReadable has already been read: pom.xml", e.getMessage());
		}
	}

	@Test
	public void testFileRepeatable() throws Exception {
		final File f = new File("pom.xml");
		assertTrue(f.isFile());
		final IReadable r1 = IReadable.of(f).repeatable();
		validateRepeatable(r1, "pom.xml", f.length());
	}

	@Test
	public void testUrl() throws Exception {
		final Path p = Paths.get("pom.xml");
		assertTrue(Files.isRegularFile(p));
		final URL url = p.toUri().toURL();
		final IReadable r = IReadable.of(url);
		validateNonRepeatable(r, url.toExternalForm(), Files.size(p));
	}

	@Test
	public void testUrlRepeatable() throws Exception {
		final Path p = Paths.get("pom.xml");
		assertTrue(Files.isRegularFile(p));
		final URL url = p.toUri().toURL();
		final IReadable r = IReadable.of(url).repeatable();
		validateRepeatable(r, url.toExternalForm(), Files.size(p));
	}
}
