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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.junit.Test;

import com.github.jochenw.afw.core.io.AbstractFileVisitor;
import com.github.jochenw.afw.core.util.Tests;

/**
 * @author jwi
 *
 */
public class DefaultZipHandlerTest {
	@Test
	public void testCreateAndValidate() throws Exception {
		final IZipFileHandler zfh = new DefaultZipHandler();
		final Path workDir = Paths.get("target/unit-tests/DefaultZipHandlerTest");
		final Path zipFile = workDir.resolve("src.zip");
		final Path srcDir = Paths.get("src");
		Files.createDirectories(workDir);
		zfh.createZipFile(srcDir, zipFile);
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
		assertEntry(entries, "main", IZipFileHandler.class);
		assertEntry(entries, "main", DefaultZipHandler.class);
		assertEntry(entries, "test", DefaultZipHandlerTest.class);
	}

	protected void assertEntry(List<String> pEntries, String pSrcSubDir, Class<?> pClass) {
		final String className = pClass.getName();
		final String entry = "src/" + pSrcSubDir + "/java/"
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
		zfh.createZipFile(srcDir, zipFile);
		final Path workSrcDir = workDir.resolve("src");
		if (Files.exists(workSrcDir)) {
			com.github.jochenw.afw.core.util.Files.removeDirectory(workSrcDir);
		}
		zfh.extractZipFile(workDir, zipFile);
		final AbstractFileVisitor fv = new AbstractFileVisitor() {
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

}
