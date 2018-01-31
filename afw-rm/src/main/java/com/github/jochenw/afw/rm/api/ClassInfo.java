package com.github.jochenw.afw.rm.api;

import java.io.Serializable;

public class ClassInfo implements Serializable {
	private static final long serialVersionUID = -8164972489867956426L;
	private final String className, type, title, description, version;

	public ClassInfo(String pClassName, String pType, String pTitle, String pDescription, String pVersion) {
		className = pClassName;
		type = pType;
		title = pTitle;
		description = pDescription;
		version = pVersion;
	}

	public String getClassName() {
		return className;
	}
	
	public String getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getVersion() {
		return version;
	}
}