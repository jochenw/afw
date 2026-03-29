package com.github.jochenw.afw.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
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
	private Consumer<String> logger;
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
		log("list: ->");
		try {
			final String uri = "META-INF/MANIFEST.MF";
			final Enumeration<URL> en = classLoader.getResources(uri);
			while (en.hasMoreElements()) {
				final URL url = en.nextElement();
				log("list: url=" + url);
				if ("jar".equals(url.getProtocol())) {
					log("list: Jar URL detected: host=" + url.getHost() + ", port=" + url.getPort() + ", file=" + url.getFile());
					final JarURLConnection juc = (JarURLConnection) url.openConnection();
					final File jarFile = new File(juc.getJarFile().getName());
					if (jarFile.isFile()) {
						log("list: Found jar file " + jarFile);
						final ZipFileResourceRepository zfrr = new ZipFileResourceRepository(jarFile);
						zfrr.list(pConsumer);
						log("list: Done with listing jar file " + jarFile);
					} else {
						throw new IllegalStateException("Jar file " + jarFile
								+ " not found for MANIFEST URL " + url);
					}
				} else if ("file".equals(url.getProtocol())) {
					log("list: File URL detected, looking for manifest file");
					final String manifestPath = URLDecoder.decode(url.getFile(), Charset.defaultCharset().name());
					final File manifestFile = new File(manifestPath);
					if (manifestFile.isFile()) {
						log("list: Manifest file detected, looking for manifest directory");
						File manifestDir = manifestFile.getParentFile();
						log("list: Found manifestDir=" + manifestDir);
						if (manifestDir == null) {
							manifestDir = manifestFile.getAbsoluteFile().getParentFile();
						}
						if (!manifestDir.isDirectory()) {
							throw new IllegalStateException("Manifest directory not found: " + manifestDir);
						}
						File resourceDir = manifestDir.getParentFile();
						log("list: Found resourceDir=" + resourceDir);
						if (resourceDir == null) {
							resourceDir = manifestDir.getAbsoluteFile().getParentFile();
						}
						if (!resourceDir.isDirectory()) {
							throw new IllegalStateException("Resource directory not found: " + manifestDir);
						}
						final DirResourceRepository drr = new DirResourceRepository(resourceDir);
						drr.setLogger(logger);
						drr.list(pConsumer);
					} else {
						log("list: Manifest file not found: " + manifestFile);
					}
				} else {
					// Ignore this URL, can't handle it.
					log("list: Ignoring URL, protocol=" + url.getProtocol());
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		log("list: <-");
	}

	/** Called to log a message by invoking the configured
	 * {@link #logger}. If the configured logger is null:
	 * Does nothing.
	 * @param pMsg The log message, which is being reported.
	 */
	protected void log(String pMsg) {
		if (logger != null) {
			logger.accept(pMsg);
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

	/**
	 * Sets a logger, that is being used to debug this object.
	 * By default, no logging is done.
	 * @param pLogger The logger, that is being invoked to emit
	 *   debugging information.
	 */
	public void setLogger(Consumer<String> pLogger) {
		logger = pLogger;
	}
}
