/**
 * 
 */
package com.github.jochenw.afw.core.components;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import com.github.jochenw.afw.core.io.AbstractFileVisitor;
import com.github.jochenw.afw.core.util.Tests;

/** Test suite for the {@link DefaultZipHandler} class.
 */
public class DefaultZipHandlerTest {
	/** Test for {@link DefaultZipHandler#createZipFile(Path, Path, boolean)},
	 * with constructor argument true.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testCreateAndValidateWithBaseDir() throws Exception {
		createAndValidate(true);
	}
	/** Test for {@link DefaultZipHandler#createZipFile(Path, Path, boolean)},
	 * with constructor argument false.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testCreateAndValidateWithoutBaseDir() throws Exception {
		createAndValidate(false);
	}

	/** Implementation of {@link #testCreateAndValidateWithBaseDir()}, and
	 * {@link #testCreateAndValidateWithoutBaseDir()}.
	 * @param pBaseDirIncluded Whether to include the base directory in the
	 * zip file entry names.
	 * @throws IOException The respective test failed.
	 */
	protected void createAndValidate(boolean pBaseDirIncluded) throws IOException {
		final IZipFileHandler zfh = new DefaultZipHandler();
		final Path workDir = Paths.get("target/unit-tests/DefaultZipHandlerTest");
		final Path zipFile = workDir.resolve("src.zip");
		final Path srcDir = Paths.get("src");
		Files.createDirectories(workDir);
		zfh.createZipFile(srcDir, zipFile, pBaseDirIncluded);
		final List<String> entries = new ArrayList<String>();
		try (InputStream is = Files.newInputStream(zipFile);
			 BufferedInputStream bis = new BufferedInputStream(is);
			 ZipInputStream zis = new ZipInputStream(bis)) {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				entries.add(ze.getName());
				ze = zis.getNextEntry();
			}
		}
		assertTrue(entries.size() >= 3);
		assertEntry(entries, "main", IZipFileHandler.class, pBaseDirIncluded);
		assertEntry(entries, "main", DefaultZipHandler.class, pBaseDirIncluded);
		assertEntry(entries, "test", DefaultZipHandlerTest.class, pBaseDirIncluded);
	}

	/** Asserts, that the given list of entries contains a specific one.
	 * @param pEntries The list of entries, that is being tested.
	 * @param pSrcSubDir The source directory, that is supposed to contain the
	 *   expected class
	 * @param pClass The expected class.
	 * @param pBaseDirIncluded Whether the base directory is included in the entry names.
	 */
	protected void assertEntry(List<String> pEntries, String pSrcSubDir, Class<?> pClass, boolean pBaseDirIncluded) {
		final String className = pClass.getName();
		final String entry = (pBaseDirIncluded ? "src/" : "") + pSrcSubDir + "/java/"
				+ className.replace('.', '/') + ".java";
		assertTrue(entry, pEntries.contains(entry));
	}

	/** Test for {@link DefaultZipHandler#extractZipFile(Path, Path)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testCreateAndExtract() throws Exception {
		final IZipFileHandler zfh = new DefaultZipHandler();
		final Path workDir = Paths.get("target/unit-tests/DefaultZipHandlerTest");
		final Path zipFile = workDir.resolve("src.zip");
		final Path srcDir = Paths.get("src");
		Files.createDirectories(workDir);
		zfh.createZipFile(srcDir, zipFile, true);
		final Path workSrcDir = workDir.resolve("src");
		if (Files.exists(workSrcDir)) {
			com.github.jochenw.afw.core.util.FileUtils.removeDirectory(workSrcDir);
		}
		zfh.extractZipFile(workDir, zipFile);
		final AbstractFileVisitor fv = new AbstractFileVisitor(true) {
			@Override
			public void visitFile(String pPath, Path pFile, BasicFileAttributes pAttrs) throws IOException {
				final Path srcFile = Paths.get(pPath);
				final Path targetFile = workDir.resolve(pPath);
				assertTrue(Files.isRegularFile(srcFile));
				assertTrue(Files.isRegularFile(targetFile));
				Tests.assertSameContent(srcFile, targetFile);
			}
			
			@Override
			public void visitDirectory(String pPath, Path pDir, BasicFileAttributes pAttrs) throws IOException {
				final Path srcDir = Paths.get(pPath);
				final Path targetDir = workDir.resolve(pPath);
				assertTrue(srcDir.toString(), Files.isDirectory(srcDir));
				assertTrue(targetDir.toString(), Files.isDirectory(targetDir));
			}
		};
		//Files.deleteIfExists(zipFile);
		Files.walkFileTree(workSrcDir, fv);
	}

	/**
	 * Test for {@link DefaultZipHandler#openEntry(Path, String)}
	 * @throws Exception The test failed.
	 */
	@Test
	public void testOpenEntry() throws Exception {
		final IZipFileHandler zfh = new DefaultZipHandler();
		final Path workDir = Paths.get("target/unit-tests/DefaultZipHandlerTest");
		final Path zipFile = workDir.resolve("src.zip");
		final Path srcDir = Paths.get("src");
		Files.createDirectories(workDir);
		zfh.createZipFile(srcDir, zipFile, true);
		final Function<Class<?>,String> inputStreamSupplier = (c) -> {
			final String p = c.getName().replace('.', '/') + ".java";
			final String mainPath = "src/main/java/" + p;
			if (Files.isRegularFile(Paths.get(mainPath))) {
				return mainPath;
			} else {
				final String testPath = "src/test/java/" + p;
				if (Files.isRegularFile(Paths.get(testPath))) {
					return testPath;
				} else {
					throw new IllegalStateException("File not found: " + mainPath + ", or " + testPath);
				}
			}
		};
		final String id1 = inputStreamSupplier.apply(IZipFileHandler.class);
		final String id2 = inputStreamSupplier.apply(DefaultZipHandler.class);
		final String id3 = inputStreamSupplier.apply(DefaultZipHandlerTest.class);
		/** Test, whether we can open, and read a single entry.
		 */
		try (InputStream in1 = zfh.openEntry(zipFile, id1.toString())) {
			Tests.assertSameContent(Paths.get(id1), in1);
		}
		/** Test, whether we can open, and read multiple entries concurrently.
		 */
		try (InputStream in1 = zfh.openEntry(zipFile, id1.toString())) {
			try (InputStream in2 = zfh.openEntry(zipFile, id2.toString())) {
				try (InputStream in3 = zfh.openEntry(zipFile, id3.toString())) {
					Tests.assertSameContent(Paths.get(id1), in1);
					Tests.assertSameContent(Paths.get(id2), in2);
					Tests.assertSameContent(Paths.get(id3), in3);
				}
			}
		}
	}
}
