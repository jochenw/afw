package com.github.jochenw.afw.rm.impl;

import java.io.File;

import com.github.jochenw.afw.rm.api.ClassInfo;
import com.github.jochenw.afw.rm.api.RmResourceRef;

public class ClassPathResourceRef implements RmResourceRef {
	private final File zipFile;
	private final String uri;
	private final ClassInfo classInfo;

	public ClassPathResourceRef(File pZipFile, String pUri, ClassInfo pClassInfo) {
		zipFile = pZipFile;
		uri = pUri;
		classInfo = pClassInfo;
	}

	public ClassPathResourceRef(File pZipFile, String pUri) {
		this(pZipFile, pUri, null);
	}

	@Override
	public String getUri() {
		return uri;
	}

	@Override
	public String getLocation() {
		return "zip:file:/" + zipFile.getPath() + "!" + uri;
	}

	public File getZipFile() {
		return zipFile;
	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}
}
