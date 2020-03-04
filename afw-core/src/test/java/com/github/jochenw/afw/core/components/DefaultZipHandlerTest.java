/**
 * 
 */
package com.github.jochenw.afw.core.components;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
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
import com.github.jochenw.afw.core.util.Functions.FailableFunction;
import com.github.jochenw.afw.core.util.ImmutableDouble;
import com.github.jochenw.afw.core.util.Tests;

/**
 * @author jwi
 *
 */
public class DefaultZipHandlerTest {
	@Test
	public void testCreateAndValidateWithBaseDir() throws Exception {
		createAndValidate(true);
	}
	@Test
	public void testCreateAndValidateWithoutBaseDir() throws Exception {
		createAndValidate(false);
	}

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

	protected void assertEntry(List<String> pEntries, String pSrcSubDir, Class<?> pClass, boolean pBaseDirIncluded) {
		final String className = pClass.getName();
		final String entry = (pBaseDirIncluded ? "src/" : "") + pSrcSubDir + "/java/"
				+ className.replace('.', '/') + ".java";
		assertTrue(entry, pEntries.contains(entry));
	}
	
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
			com.github.jochenw.afw.core.util.Files.removeDirectory(workSrcDir);
		}
		zfh.extractZipFile(workDir, zipFile);
		final AbstractFileVisitor fv = new AbstractFileVisitor(true) {
			@Override
			protected void visitFile(String pPath, Path pFile, BasicFileAttributes pAttrs) throws IOException {
				final Path srcFile = Paths.get(pPath);
				final Path targetFile = workDir.resolve(pPath);
				assertTrue(Files.isRegularFile(srcFile));
				assertTrue(Files.isRegularFile(targetFile));
				Tests.assertSameContent(srcFile, targetFile);
			}
			
			@Override
			protected void visitDirectory(String pPath, Path pDir, BasicFileAttributes pAttrs) throws IOException {
				final Path srcDir = Paths.get(pPath);
				final Path targetDir = workDir.resolve(pPath);
				assertTrue(srcDir.toString(), Files.isDirectory(srcDir));
				assertTrue(targetDir.toString(), Files.isDirectory(targetDir));
			}
		};
		Files.deleteIfExists(zipFile);
		Files.walkFileTree(workSrcDir, fv);
	}

	@Test
	public void testOpenEntry() throws Exception {
		final IZipFileHandler zfh = new DefaultZipHandler();
		final Path workDir = Paths.get("target/unit-tests/DefaultZipHandlerTest");
		final Path zipFile = workDir.resolve("src.zip");
		final Path srcDir = Paths.get("src");
		Files.createDirectories(workDir);
		zfh.createZipFile(srcDir, zipFile, true);
		final Function<Class<?>,Path> inputStreamSupplier = (c) -> {
			final String p = c.getName().replace('.', '/') + ".java";
			final Path mainPath = Paths.get("src/main/java", p);
			if (Files.isRegularFile(mainPath)) {
				return mainPath;
			} else {
				final Path testPath = Paths.get("src/test/java", p);
				if (Files.isRegularFile(testPath)) {
					return testPath;
				} else {
					throw new IllegalStateException("File not found: " + mainPath + ", or " + testPath);
				}
			}
		};
		final Path id1 = inputStreamSupplier.apply(IZipFileHandler.class);
		final Path id2 = inputStreamSupplier.apply(DefaultZipHandler.class);
		final Path id3 = inputStreamSupplier.apply(DefaultZipHandlerTest.class);
		/** Test, whether we can open, and read a single entry.
		 */
		try (InputStream in1 = zfh.openEntry(zipFile, id1.toString())) {
			Tests.assertSameContent(id1, in1);
		}
		/** Test, whether we can open, and read multiple entries concurrently.
		 */
		try (InputStream in1 = zfh.openEntry(zipFile, id1.toString())) {
			try (InputStream in2 = zfh.openEntry(zipFile, id2.toString())) {
				try (InputStream in3 = zfh.openEntry(zipFile, id3.toString())) {
					Tests.assertSameContent(id1, in1);
					Tests.assertSameContent(id2, in2);
					Tests.assertSameContent(id3, in3);
				}
			}
		}
	}
}
