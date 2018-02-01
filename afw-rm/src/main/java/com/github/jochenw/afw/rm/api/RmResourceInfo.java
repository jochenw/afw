package com.github.jochenw.afw.rm.api;

public interface RmResourceInfo {
	String getTitle();
	String getType();
	String getDescription();
	String getUri();
	String getLocation();
	RmVersion getVersion();
}
