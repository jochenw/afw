package com.github.jochenw.afw.bootstrap.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;

public class Paths {
	public static Path pathOf(Class<?> pClass) {
		final URL url = pClass.getResource(pClass.getSimpleName() + ".class");
		try {
			URLConnection conn = url.openConnection();
			if (conn instanceof JarURLConnection) {
				final JarURLConnection juc = (JarURLConnection) conn;
				final Path path = get(juc.getJarFile().getName());
				if (!Files.isRegularFile(path)) {
					throw new IllegalStateException("Expected jar file not found: " + path);
				}
				return path;
			} else {
				throw new IllegalStateException("Class " + pClass.getName() + " is not located in a jar file: " + url);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static Path get(String pPath, String... pMore) {
		return java.nio.file.Paths.get(pPath, pMore);
	}

}
