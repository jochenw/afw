package com.github.jochenw.afw.rm.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.github.jochenw.afw.rm.api.RmLogger;
import com.github.jochenw.afw.rm.api.RmResourceRef;
import com.github.jochenw.afw.rm.api.RmResourceRefRepository;

public class ClassPathResourceRefRepository implements RmResourceRefRepository {
	private final ClassLoader cl;
	private final String resourcePrefix;

	public ClassPathResourceRefRepository(ClassLoader pCl, String pResourcePrefix) {
		cl = pCl;
		resourcePrefix = pResourcePrefix;
	}

	@Override
	public InputStream open(RmResourceRef pResource) throws IOException {
		final ClassPathResourceRef cprr = (ClassPathResourceRef) pResource;
		final File zipFile = cprr.getZipFile();
		final String uri = cprr.getUri();
		@SuppressWarnings("resource")
		final ZipFile zFile = new ZipFile(zipFile);
		final ZipEntry entry = zFile.getEntry(uri);
		return zFile.getInputStream(entry);
	}
	
	@Override
	public List<RmResourceRef> getResources(RmLogger pLogger) {
		final List<RmResourceRef> resources = new ArrayList<>();
		Enumeration<URL> en;
		try {
			en = cl.getResources("META-INF/MANIFEST.MF");
			while (en.hasMoreElements()) {
				final URL url = en.nextElement();
				findResources(pLogger, resources, url);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return resources;
	}

	private void findResources(RmLogger pLogger, List<RmResourceRef> pList, URL pUrl) {
		String protocol = pUrl.getProtocol();
		if ("file".equals(protocol)) {
			final File manifestFile = new File(pUrl.getFile());
			final File metaInfDir = manifestFile.getParentFile();
			if (metaInfDir == null  || !metaInfDir.isDirectory()) {
				throw new IllegalStateException("Unable to locate parent directory for file: " + manifestFile);
			}
			final File classPathDir = metaInfDir.getParentFile();
			if (classPathDir == null  ||  !classPathDir.isDirectory()) {
				throw new IllegalStateException("Unable to locate parent directory for directory: " + metaInfDir);
			}
		} else if ("zip".equals(protocol)  ||  "jar".equals(protocol)) {
			final String url = pUrl.toExternalForm();
			String prefix = protocol + ":file:/";
			if (!url.startsWith(prefix)) {
				throw new IllegalStateException("Unable to parse URL: " + pUrl);
			}
			final String path = url.substring(prefix.length());
			final int offset = path.indexOf('!');
			if (offset == -1) {
				throw new IllegalStateException("Unable to parse file path from URL: " + pUrl);
			}
			final String file = path.substring(0, offset);
			final File zipFile = new File(file);
			if (!zipFile.isFile()) {
				throw new IllegalStateException("File " + zipFile + " does not exist for URL: " + pUrl);
			}
			try (InputStream is = new FileInputStream(zipFile);
				 BufferedInputStream bis = new BufferedInputStream(is);
				 final ZipInputStream zis = new ZipInputStream(bis)) {
				for (;;) {
					ZipEntry ze = zis.getNextEntry();
					if (ze == null) {
						break;
					}
					final String name = ze.getName();
					if (resourcePrefix == null  || name.startsWith(resourcePrefix)) {
						final ClassPathResourceRef cprr = new ClassPathResourceRef(zipFile, name);
						pList.add(cprr);
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			throw new IllegalStateException("Invalid protocol: " + protocol);
		}
	}
	
	public ClassLoader getClassLoader() {
		return cl;
	}

	public String getResourcePrefix() {
		return resourcePrefix;
	}
}
