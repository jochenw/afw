package com.github.jochenw.afw.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Implementation of {@link IResourceRepository}, which uses a {@link ClassLoader},
 * providing access to the class loaders resource files. Internally, the resource
 * repository will search for MANIFEST.MF files. For every such file, an instance
 * of {@link DirResourceRepository}, or {@link ZipFileResourceRepository} will be
 * created, to which this repository delegates.
 */
public class ClassLoaderResourceRepository implements IResourceRepository {
	private final ClassLoader classLoader;

	/**
	 * Creates a new instance, using the given {@link ClassLoader}.
	 * @param pClassLoader The {@link ClassLoader} to use.
	 */
	public ClassLoaderResourceRepository(ClassLoader pClassLoader) {
		classLoader = pClassLoader;
	}

	/**
	 * Creates a new instance, using the {@link Thread#getContextClassLoader() current threads context class loader}.
	 */
	public ClassLoaderResourceRepository() {
		this(Thread.currentThread().getContextClassLoader());
	}
	
	@Override
	public void list(Consumer<IResource> pConsumer) {
		try {
			final String uri = "META-INF/MANIFEST.MF";
			final Enumeration<URL> en = classLoader.getResources(uri);
			while (en.hasMoreElements()) {
				final URL url = en.nextElement();
				if ("jar".equals(url.getProtocol())) {
					final String fileUri = url.getFile();
					final int offset = fileUri.indexOf('!');
					if (offset == -1) {
						throw new IllegalStateException("Unable to handle jar URL: " + url);
					} else {
						final String filePart = fileUri.substring(0, offset);
						if (filePart.startsWith("file:")) {
							final File file = new File(filePart.substring("file:".length()));
							if (!file.isFile()) {
								throw new IllegalStateException("No such zip file: " + file);
							} else {
								final ZipFileResourceRepository zfrr = new ZipFileResourceRepository(file);
								zfrr.list(pConsumer);
							}
						}
					}
				} else if ("file".equals(url.getProtocol())) {
					final File manifestFile = new File(url.getFile());
					if (!manifestFile.isFile()) {
						throw new IllegalStateException("Manifest file not found: " + manifestFile);
					}
					File manifestDir = manifestFile.getParentFile();
					if (manifestDir == null) {
						manifestDir = manifestFile.getAbsoluteFile().getParentFile();
					}
					if (!manifestDir.isDirectory()) {
						throw new IllegalStateException("Manifest directory not found: " + manifestDir);
					}
					File resourceDir = manifestDir.getParentFile();
					if (resourceDir == null) {
						resourceDir = manifestDir.getAbsoluteFile().getParentFile();
					}
					if (!resourceDir.isDirectory()) {
						throw new IllegalStateException("Resource directory not found: " + manifestDir);
					}
					new DirResourceRepository(resourceDir).list(pConsumer);
				} else {
					// Ignore this URL, can't handle it.
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public InputStream open(IResource pResource) throws IOException {
		final IResource res = Objects.requireNonNull(pResource, "Resource");
		if (DirResourceRepository.isValidResource(res)) {
			return DirResourceRepository.openResource(res);
		} else if (ZipFileResourceRepository.isValidResource(res)) {
			return ZipFileResourceRepository.openResource(res);
		} else {
			throw new IllegalArgumentException("Invalid resource type: " + res.getClass().getName());
		}
	}
}
