package com.github.jochenw.afw.rcm.impl;

import java.io.File;

import com.github.jochenw.afw.rcm.api.RmResourceInfo;
import com.github.jochenw.afw.rcm.api.RmVersion;


public class ClassPathResourceRef extends SimpleClassPathResourceRef implements RmResourceInfo {
	private final RmVersion version;
	private final String title, description, type;

	public ClassPathResourceRef(File pZipFile, String pUri, String pLocation, RmVersion pVersion, String pType, String pTitle, String pDescription) {
		super(pZipFile, pUri, pLocation);
		version = pVersion;
		type = pType;
		title = pTitle;
		description = pDescription;
	}

	public ClassPathResourceRef(File pFile, String pUri, RmVersion pVersion, String pType, String pTitle, String pDescription) {
		super(pFile, pUri);
		version = pVersion;
		type = pType;
		title = pTitle;
		description = pDescription;
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

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public RmVersion getVersion() {
		return version;
	}
}
