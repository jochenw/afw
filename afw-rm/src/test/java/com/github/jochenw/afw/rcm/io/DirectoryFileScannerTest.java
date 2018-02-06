package com.github.jochenw.afw.rcm.io;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rcm.api.RmResourceRef;
import com.github.jochenw.afw.rcm.impl.DirectoryResourceRefRepository;
import com.github.jochenw.afw.rcm.impl.FileRmResourceRef;
import com.github.jochenw.afw.rcm.io.DirectoryFileScanner;

public class DirectoryFileScannerTest {
	@Test
	public void testFiles() {
		final File file = new File("src/main/java");
		final List<FileRmResourceRef> files = new DirectoryFileScanner().scan(file);
		final List<RmResourceRef> list = files.stream().map(r -> (RmResourceRef) r).collect(Collectors.toList());
		validate(list);
	}

	@Test
	public void testPaths() {
		final Path path = Paths.get("src/main/java");
		final List<FileRmResourceRef> files = new DirectoryFileScanner().scan(path);
		final List<RmResourceRef> list = files.stream().map(r -> (RmResourceRef) r).collect(Collectors.toList());
		validate(list);
	}

	protected void validate(List<RmResourceRef> pResources) {
		Assert.assertFalse(pResources.isEmpty());
		boolean resourceRefGuesserFound = false;
		for (RmResourceRef resource : pResources) {
			final FileRmResourceRef res = (FileRmResourceRef) resource; 
			if ("com/github/jochenw/afw/rcm/api/RmResourceRefGuesser.java".equals(res.getUri())) {
				resourceRefGuesserFound = true;
			}
			final File file = res.getFile();
			final Path path = res.getPath();
			if (file == null) {
				Assert.assertNotNull(path);
				Assert.assertTrue(Files.isRegularFile(path));
			} else {
				Assert.assertTrue(file.isFile());
			}
		}
		Assert.assertTrue(resourceRefGuesserFound);
	}

	@Test
	public void testDirRefRepositoryFiles() {
		final File file = new File("src/main/java");
		final DirectoryResourceRefRepository repo = new DirectoryResourceRefRepository(file);
		validate(repo.getResources(null));
	}

	@Test
	public void testDirRefRepositoryPaths() {
		final Path path = Paths.get("src/main/java");
		final DirectoryResourceRefRepository repo = new DirectoryResourceRefRepository(path);
		validate(repo.getResources(null));
	}
}
