package com.github.jochenw.afw.rm.impl;

import java.io.File;

import com.github.jochenw.afw.rm.api.ClassInfo;
import com.github.jochenw.afw.rm.api.RmResourceRef;

public class ClassPathResourceRef implements RmResourceRef {
	private final File zipFile;
	private final File file;
	private final String uri;

	public ClassPathResourceRef(File pZipFile, String pUri) {
		zipFile = pZipFile;
		file = null;
		uri = pUri;
	}

	public ClassPathResourceRef(String pUri, File pFile) {
		zipFile = null;
		file = pFile;
		uri = pUri;
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public String getLocation() {
		if (zipFile == null) {
			return file.getPath();
		} else {
			return "zip:file:/" + zipFile.getPath() + "!" + uri;
		}
	}

	public File getZipFile() {
		return zipFile;
	}

	public File getFile() {
		return file;
	}
}
