package com.github.jochenw.afw.lc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.inject.Inject;

import com.github.jochenw.afw.core.ResourceLocator;
import com.github.jochenw.afw.core.util.Exceptions;


public class PropertyLoader {
	@Inject private ResourceLocator resourceLocator;

	public PropertyLoader() {
		this(null);
	}

	public PropertyLoader(ResourceLocator pLocator) {
		resourceLocator = pLocator;
	}
	
	public Properties load(URL pUrl) {
		final Properties props = new Properties();
		try (InputStream istream = pUrl.openStream()) {
			if (pUrl.toExternalForm().endsWith(".xml")) {
				props.loadFromXML(istream);
			} else {
				props.load(istream);
			}
		} catch (IOException e) {
			throw Exceptions.newUncheckedIOException(e);
		}
		return props;
	}

	public Properties load(String pUri) {
		if (pUri.endsWith(".properties")) {
			final String xmlUri = pUri + ".xml";
			final URL url = resourceLocator.getResource(xmlUri);
			if (url != null) {
				return load(url);
			}
		}
		final URL url = resourceLocator.requireResource(pUri);
		return load(url);
	}

	public Properties load(ClassLoader pClassLoader, String pUri) {
		final URL url;
		if (resourceLocator == null) {
			url = pClassLoader.getResource(pUri);
			if (url == null) {
				throw new NoSuchElementException("Unable to locate resource: " + pUri
						+ " via ClassLoader " + pClassLoader);
			}
		} else {
			url = resourceLocator.getResource(pClassLoader, pUri);
		}
		return load(url);
	}
}