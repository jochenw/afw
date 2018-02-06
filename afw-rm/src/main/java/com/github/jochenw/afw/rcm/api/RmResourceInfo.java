package com.github.jochenw.afw.rcm.api;

public interface RmResourceInfo {
	String getTitle();
	String getType();
	String getDescription();
	String getUri();
	String getLocation();
	RmVersion getVersion();
}
