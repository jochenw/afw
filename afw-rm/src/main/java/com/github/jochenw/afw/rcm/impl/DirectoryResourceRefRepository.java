package com.github.jochenw.afw.rcm.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jochenw.afw.rcm.api.RmLogger;
import com.github.jochenw.afw.rcm.api.RmResourceRef;
import com.github.jochenw.afw.rcm.api.RmResourceRefRepository;
import com.github.jochenw.afw.rcm.io.DirListingFileVisitor;
import com.github.jochenw.afw.rcm.io.DirectoryFileScanner;
import com.github.jochenw.afw.rcm.util.Exceptions;

public class DirectoryResourceRefRepository implements RmResourceRefRepository {
	private final File file;
	private final Path path;
	private List<RmResourceRef> resourceList;

	public DirectoryResourceRefRepository(File pFile) {
		file = pFile;
		path = null;
	}

	public DirectoryResourceRefRepository(Path pPath) {
		file = null;
		path = pPath;
	}

	@Override
	public List<RmResourceRef> getResources(RmLogger pLogger) {
		if (resourceList == null) {
			final Stream<FileRmResourceRef> stream;
			if (file == null) {
				stream = new DirectoryFileScanner().scan(path).stream();
			} else {
				stream = new DirectoryFileScanner().scan(file).stream();
			}
			resourceList = stream.map(r -> (RmResourceRef) r).collect(Collectors.toList());
		}
		return resourceList;
	}

	protected List<RmResourceRef> scan(Path pPath) {
		final List<RmResourceRef> list = new ArrayList<>();
		final FileVisitor<Path> fv = new DirListingFileVisitor();
		try {
			Files.walkFileTree(pPath, fv);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		return list;
	}

	
	@Override
	public InputStream open(RmResourceRef pResource) throws IOException {
		if (file == null) {
			return Files.newInputStream(path);
		} else {
			return new FileInputStream(file);
		}
	}

	public File getFile() {
		return file;
	}

	public Path getPath() {
		return path;
	}
}
