package com.github.jochenw.afw.rcm.impl;

import java.io.File;
import java.util.Objects;

import com.github.jochenw.afw.rcm.api.RmResourceRef;

public class SimpleClassPathResourceRef implements RmResourceRef {
	public final File file;
	public final File zipFile;
	public final String uri;
	public final String location;

	public SimpleClassPathResourceRef(File pFile, String pUri) {
		Objects.requireNonNull(pFile, "File");
		file = pFile;
		uri = pUri;
		zipFile = null;
		location = pFile.getAbsolutePath();
	}
	public SimpleClassPathResourceRef(File pZipFile, String pUri, String pLocation) {
		Objects.requireNonNull(pZipFile, "ZipFile");
		file = null;
		uri = pUri;
		zipFile = pZipFile;
		location = pLocation;
	}
	
	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public String getLocation() {
		return location;
	}

	public File getFile() {
		return file;
	}

	public File getZipFile() {
		return zipFile;
	}
}
