/**
 * 
 */
package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.components.DefaultZipHandler;
import com.github.jochenw.afw.core.components.DefaultZipHandlerTest;
import com.github.jochenw.afw.core.components.IZipFileHandler;

/**
 * @author jwi
 *
 */
public class AbstractFileVisitorTest {
	protected List<String> expectedFiles() {
		final List<Class<?>> mainList = Arrays.asList(AbstractFileVisitor.class, IZipFileHandler.class,
				                                   DefaultZipHandler.class);
		final List<Class<?>> testList = Arrays.asList(DefaultZipHandlerTest.class, AbstractFileVisitorTest.class);
		final List<String> list = new ArrayList<>();
		mainList.forEach((c) -> list.add("main/java/" + c.getName().replace('.', '/') + ".java"));
		testList.forEach((c) -> list.add("test/java/" + c.getName().replace('.', '/') + ".java"));
		return list;
	}
	@Test
	public void testWithBasedir() throws Exception {
		final List<String> expectedNames = expectedFiles().stream().map((s) -> "src/" + s).collect(Collectors.toList());
		final List<String> rejectedNames = expectedFiles();
		final FileVisitor<Path> fv = new AbstractFileVisitor(true) {
			@Override
			protected void visitFile(String pPath, Path pFile, BasicFileAttributes pAttrs) throws IOException {
				if (rejectedNames.contains(pPath)) {
					throw new IllegalStateException("Unexpected name: " + pPath);
				}
				expectedNames.remove(pPath);
			}
			
			@Override
			protected void visitDirectory(String pPath, Path pDir, BasicFileAttributes pAttrs) throws IOException {
				// Do nothing
			}
		};
		Files.walkFileTree(Paths.get("src"), fv);
		Assert.assertTrue(expectedNames.isEmpty());
	}

	@Test
	public void testWithoutBasedir() throws Exception {
		final List<String> rejectedNames = expectedFiles().stream().map((s) -> "src/" + s).collect(Collectors.toList());
		final List<String> expectedNames = expectedFiles();
		final FileVisitor<Path> fv = new AbstractFileVisitor(false) {
			@Override
			protected void visitFile(String pPath, Path pFile, BasicFileAttributes pAttrs) throws IOException {
				if (rejectedNames.contains(pPath)) {
					throw new IllegalStateException("Unexpected name: " + pPath);
				}
				expectedNames.remove(pPath);
			}
			
			@Override
			protected void visitDirectory(String pPath, Path pDir, BasicFileAttributes pAttrs) throws IOException {
				// Do nothing
			}
		};
		Files.walkFileTree(Paths.get("src"), fv);
		Assert.assertTrue(expectedNames.isEmpty());
	}

}
