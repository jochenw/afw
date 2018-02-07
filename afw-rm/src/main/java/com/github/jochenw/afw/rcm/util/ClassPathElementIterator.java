package com.github.jochenw.afw.rcm.util;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

public abstract class ClassPathElementIterator {
	public void iterate(ClassLoader pClassLoader, String pUri, int pPathElementsBelowRoot) {
		try {
			final Enumeration<URL> en = pClassLoader.getResources(pUri);
			while (en.hasMoreElements()) {
				final URL url = en.nextElement();
				final String protocol = url.getProtocol();
				if ("file".equals(protocol)) {
					final File f = new File(url.getFile());
					File d = f;
					for (int i = 0;  i < pPathElementsBelowRoot; i++) {
						if (d == null) {
							throw new IllegalStateException("Unable to determine root directory for resource="
									+ f.getAbsolutePath() + ", and pathElementsBelowRoot=" + pPathElementsBelowRoot);
						}
						d = d.getParentFile();
					}
					if (d == null) {
						throw new IllegalStateException("Unable to determine root directory for resource="
								+ f.getAbsolutePath() + ", and pathElementsBelowRoot=" + pPathElementsBelowRoot);
					}
					
					iterateDirectory(d, pUri, url);
				} else if ("zip".equals(protocol)  ||  "jar".equals(protocol)) {
					final String uri = url.getFile();
					if (!uri.startsWith("file:/")) {
						throw new IllegalStateException("Invalid ZIP URL: " + url);
					}
					final int offset = uri.indexOf('!');
					if (offset == -1) {
						throw new IllegalStateException("Unable to locate ZIP file for resource=" + url);
					}
					final String zipFilePath = uri.substring("file:/".length(), offset);
					final File zipFile = new File(zipFilePath);
					if (!zipFile.isFile()) {
						throw new IllegalStateException("Cannot open ZIP File " + zipFile.getAbsolutePath() +
								" for resource=" + url);
					}
					iterateZipFile(zipFile, pUri, url);
				}
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected abstract void iterateDirectory(File pDirectory, String pUri, URL pUrl);
	protected abstract void iterateZipFile(File pZipFile, String pUri, URL pLocation);
}
