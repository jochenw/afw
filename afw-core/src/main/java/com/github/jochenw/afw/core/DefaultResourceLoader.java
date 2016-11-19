package com.github.jochenw.afw.core;

import java.net.URL;

public class DefaultResourceLoader extends ResourceLocator {
	public DefaultResourceLoader() {
		this(null, null);
	}

	public DefaultResourceLoader(String pApplicationName, String pInstanceName) {
		setApplicationName(pApplicationName);
		setInstanceName(pInstanceName);
	}

	@Override
	public URL getResource(ClassLoader pClassLoader, String pUri) {
		final String instanceName = getInstanceName();
		if (instanceName != null  &&  instanceName.length() > 0) {
			final String uri = instanceName + "/" + pUri;
			final URL url = pClassLoader.getResource(uri);
			if (url != null) {
				return url;
			}
		}
		return pClassLoader.getResource(pUri);
	}
}
