package com.github.jochenw.afw.rm.impl;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

import com.github.jochenw.afw.rm.api.RmResourceRef;

public class FileRmResourceRef implements RmResourceRef {
	private final File file;
	private final Path path;
	private final String uri;

	public FileRmResourceRef(File pFile, String pUri) {
		Objects.requireNonNull(pFile, "File");
		Objects.requireNonNull(pUri, "Uri");
		file = pFile;
		path = null;
		uri = pUri;
	}

	public FileRmResourceRef(Path pPath, String pUri) {
		Objects.requireNonNull(pPath, "Path");
		Objects.requireNonNull(pUri, "Uri");
		file = null;
		path = pPath;
		uri = pUri;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public String getLocation() {
		return file.getAbsolutePath();
	}

	public File getFile() {
		return file;
	}

	public Path getPath() {
		return path;
	}
}
