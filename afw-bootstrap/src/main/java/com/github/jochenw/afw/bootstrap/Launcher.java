package com.github.jochenw.afw.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

import com.github.jochenw.afw.bootstrap.cli.Args.Options;
import com.github.jochenw.afw.bootstrap.log.FileLogger;
import com.github.jochenw.afw.bootstrap.log.Logger;
import com.github.jochenw.afw.bootstrap.log.Logger.Level;
import com.github.jochenw.afw.bootstrap.util.Versions;
import com.github.jochenw.afw.bootstrap.util.Versions.Version;
import com.github.jochenw.afw.bootstrap.log.SystemOutLogger;


public class Launcher {
	private Options options;
	private Logger logger;
	private Properties localProperties, remoteProperties;

	public void run(Options pOptions) {
		options = pOptions;
		try (final Logger log = newLogger(pOptions)) {
			logger = log;
			localProperties = readProperties();
			run();
		}
	}

	// All local resources have been allocated. Do the actual work.
	protected void run() {
		final String currentVersionStr = requireProperty(localProperties, "version.number");
		final Version currentVersion = Versions.valueOf(currentVersionStr,
				                                        () -> "Invalid value for property version.number: " + currentVersionStr + " (Not a valid version number)");
		String baseDir = requireProperty(localProperties, "base.dir");
		if (baseDir == null) {
			baseDir = ".."; // Assume, that we're inside the "bin" directory, and the base directory is one level above.
		}
		final String remoteUrl = getProperty(localProperties, "update.property.url");
		if (remoteUrl == null) {
			logger.info("Property update.property.url not set, Autoupdate is not being performed.");
		} else {
			remoteProperties = getRemoteProperties(baseDir, remoteUrl);
			if (remoteProperties == null) {
				final String msg = "Property update.property.url is neither a valid URL, nor a file: " + remoteUrl;
				logger.error(msg);
				throw new IllegalStateException(msg);
			}
			final String remoteVersionStr = requireProperty(remoteProperties, "version.number");
			final Version remoteVersion = Versions.valueOf(remoteVersionStr,
					                                       () -> "Invalid value for remote property version.number: " + remoteVersionStr + " (Not a valid version number)");
			if (currentVersion.isGreaterOrEqual(remoteVersion)) {
				logger.info("Update from current version " + currentVersion + " to version " + remoteVersion + " required.");
				runUpdate(remoteUrl, baseDir);
			} else {
				logger.debug("Remote version " + remoteVersion + " is ");
			}
		}
	}

	protected void runUpdate(String pRemoteUrl, String pBaseDir) {
		final Path baseDir = Paths.get(pBaseDir);
		if (!Files.isDirectory(baseDir)) {
			throw new IllegalStateException("Invalid base directory " + baseDir
					+ " (Doesn't exist, or is not a directory.)");
		}
	}

	protected Path downLoad(String pRemoteUrl, Path pBaseDir) {
		final URL url;
		try {
			url = new URL(pRemoteUrl);
			return download(url, pBaseDir);
		} catch (MalformedURLException e) {
			final Path file = Paths.get(pRemoteUrl);
			if (Files.isRegularFile(file)) {
				return file;
			} else {
				throw new IllegalStateException("Invalid remote URL: " + pRemoteUrl
						+ " (Neither a valid URL, nor an existig file, so no download.)");
			}
		}
	}

	protected Path download(URL pUrl, Path pBaseDir) {
		String fileName = pUrl.getFile();
		Path file;
		final int offset = Math.max(fileName.indexOf('/'), fileName.indexOf('\\'));
		if (offset == -1) {
			file = pBaseDir.resolve(fileName);
		} else {
			file = pBaseDir.resolve(fileName.substring(offset+1));
		}
		try (InputStream in = pUrl.openStream();
			 OutputStream out = Files.newOutputStream(file)) {
			final byte[] buffer = new byte[8192];
			for (;;) {
				final int res = in.read(buffer);
				if (res == -1) {
					break;
				} else {
					out.write(buffer, 0, res);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return file;
	}
	protected String getProperty(Properties pProperties, String pKey) {
		return pProperties.getProperty(pKey);
	}

	protected String requireProperty(Properties pProperties, String pKey) {
		final String value = getProperty(pProperties, pKey);
		if (value == null) {
			throw new IllegalStateException("Required property missing: " + pKey);
		}
		if (value.length() == 0) {
			throw new IllegalStateException("Required property empty: " + pKey);
		}
		return value;
	}

	protected Logger newLogger(Options pOptions) {
		final Level level = pOptions.getLogLevel() == null ? Logger.Level.INFO : pOptions.getLogLevel();
		final Path logFile = pOptions.getLogFile();
		if (logFile == null) {
			return new SystemOutLogger(level);
		} else {
			return new FileLogger(logFile, level);
		}
	}

	protected interface StreamSupplier {
		InputStream get() throws IOException;
	}

	protected Properties readProperties() {
		final Path path = Objects.requireNonNull(options.getPropertyFile(), "propertyFile");
		final Properties props = readProperties(() -> Files.newInputStream(path), path.toString());
		return props;
	}

	protected Properties readProperties(final StreamSupplier pStreamSupplier, String pUri) {
		final Properties props = new Properties();
		try (InputStream in = pStreamSupplier.get()) {
			if (pUri.endsWith(".xml")) {
				props.loadFromXML(in);
			} else {
				props.load(in);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return props;
	}

	protected Properties getRemoteProperties(String pBaseDir, String pRemoteUrl) {
		final URL url;
		try {
			url = new URL(pRemoteUrl);
		} catch (MalformedURLException e) {
			final Path path = Paths.get(pRemoteUrl);
			if (Files.isRegularFile(path)) {
				logger.debug("Reading remote properties from file: " + path);
				return readProperties(() -> Files.newInputStream(path), path.getFileName().toString());
			} else {
				return null;
			}
		}
		logger.debug("Reading remote properties from URL: " + url);
		return readProperties(() -> url.openStream(), url.getFile());
	}
}
