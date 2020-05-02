/**
 * 
 */
package com.github.jochenw.afw.core.components;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assume;
import org.junit.Test;

import com.github.jochenw.afw.core.util.SymbolicLinks;
import com.github.jochenw.afw.core.util.Systems;
import com.github.jochenw.afw.core.util.Tests;

/**
 * @author jwi
 *
 */
public class SymbolicLinksHandlerTest {
	/** Test case for the {@link WindowsCmdSymbolicLinksHandler}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testWindowsCmdSymbolicLinksHandler() throws Exception {
		Assume.assumeTrue(Systems.isWindows());
		runTest(new WindowsCmdSymbolicLinksHandler());
	}

	/**
	 * Test case for the {@link JavaNioSymbolicLinksHandler}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testJavaNioSymbolicLinksHandler() throws Exception {
		Assume.assumeTrue(Systems.isLinuxOrUnix());
		runTest(new JavaNioSymbolicLinksHandler());
	}

	/**
	 * Test case for the {@link DefaultSymbolicLinksHandler}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testDefaultSymbolicLinksHandler() throws Exception {
		runTest(new DefaultSymbolicLinksHandler());
	}

	/**
	 * Test case for the {@link SymbolicLinks} class.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testSymbolicLinks() throws Exception {
		final ISymbolicLinksHandler h = new ISymbolicLinksHandler() {
			@Override
			public void removeLink(Path pPath) {
				SymbolicLinks.removeLink(pPath);
			}
			
			@Override
			public void createFileLink(Path pTarget, Path pLink) throws UnsupportedOperationException {
				SymbolicLinks.createFileLink(pTarget, pLink);
			}
			
			@Override
			public void createDirectoryLink(Path pTarget, Path pLink) {
				SymbolicLinks.createDirectoryLink(pTarget, pLink);
			}
			
			@Override
			public Path checkLink(Path pPath) {
				return SymbolicLinks.checkLink(pPath);
			}
		};
		runTest(h);
	}
	
	protected void runTest(ISymbolicLinksHandler pHandler) throws IOException{
		runCreateDirectoryTest(pHandler);
	}

	protected void runCreateDirectoryTest(ISymbolicLinksHandler pHandler) throws IOException {
		final Path testDir = Paths.get("target/unit-tests/SymbolicLinksHandlerTest");
		Files.createDirectories(testDir);
		final Path targetDir = Files.createTempDirectory(testDir, "src");
		Files.createDirectories(targetDir);
		final Path linkDir = Files.createTempDirectory(testDir, "trgt");
		Files.deleteIfExists(linkDir);
		pHandler.createDirectoryLink(targetDir, linkDir);
		final Path targetFile = targetDir.resolve("pom.xml");
		final Path linkFile = linkDir.resolve("pom.xml");
		try (InputStream in = Files.newInputStream(Paths.get("pom.xml"))) {
			Files.copy(in, targetFile);
		}
		assertEquals(targetDir.toAbsolutePath(), pHandler.checkLink(linkDir));
		assertTrue(Files.exists(linkDir));
		assertTrue(Files.isDirectory(linkDir));
		Tests.assertSameContent(targetFile, linkFile);
		Files.delete(linkFile);
		pHandler.removeLink(linkDir);
		assertFalse(Files.exists(linkDir));
		assertFalse(Files.isDirectory(linkDir));
		assertNull(pHandler.checkLink(linkDir));
	}
}
