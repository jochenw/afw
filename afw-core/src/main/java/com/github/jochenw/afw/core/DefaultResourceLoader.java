package com.github.jochenw.afw.core;

import java.net.URL;

import org.apache.logging.log4j.util.Strings;

public class DefaultResourceLoader extends ResourceLocator {
	String resourcePrefix;

	public DefaultResourceLoader() {
		this(null, null);
	}

	public DefaultResourceLoader(String pApplicationName, String pInstanceName) {
		this(pApplicationName, pInstanceName, null);
	}

	public DefaultResourceLoader(String pApplicationName, String pInstanceName, String pResourcePrefix) {
		setApplicationName(pApplicationName);
		setInstanceName(pInstanceName);
		resourcePrefix = pResourcePrefix;
	}

	public String getResourcePrefix() {
		return resourcePrefix;
	}

	public void setResourcePrefix(String pResourcePrefix) {
		resourcePrefix = pResourcePrefix;
	}
	
	@Override
	public URL getResource(ClassLoader pClassLoader, String pUri) {
		final String instanceName = getInstanceName();
		if (instanceName != null  &&  instanceName.length() > 0) {
			final String uri = instanceName + "/" + pUri;
			final URL url = findResource(uri, pClassLoader);
			if (url != null) {
				return url;
			}
		}
		return findResource(pUri, pClassLoader);
	}

	protected URL findResource(String pUri, ClassLoader pCl) {
		final String rp = getResourcePrefix();
		if (Strings.isEmpty(rp)) {
			return pCl.getResource(pUri);
		} else {
			if (rp.endsWith("/")) {
				return pCl.getResource(rp + pUri);
			} else {
				return pCl.getResource(rp + "/" + pUri);
			}
		}
	}
}
