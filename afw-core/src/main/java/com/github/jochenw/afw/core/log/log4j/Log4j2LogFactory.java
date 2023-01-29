/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.log.log4j;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.AbstractLogFactory;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.util.Exceptions;


/** Implementation of {@link ILogFactory}, which is based on Apache Log4j 2.
 */
public class Log4j2LogFactory extends AbstractLogFactory {
	private Logger logger;

	@Override
    protected AbstractLog newLog(String pId) {
        return new Log4j2Log(this, pId);
    }

	/** Returns the configuration URL.
	 * @return The config file's URL.
	 */
	protected URL getUrl() {
		return getResourceLocator().getResource("log4j2.xml");
	}

	@Override
	protected void init() {
		final URL url = getUrl();
		try (InputStream in = url.openStream()) {
			configure(in, url.toExternalForm());
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Called to perform configuration by reading a file from
	 * the given input stream.
	 * @param pIn The file to read.
	 * @param pUri The file's URI, for use in error messages.
	 */
	protected void configure(InputStream pIn, String pUri) {
		try {
			final ConfigurationSource csource = new ConfigurationSource(pIn);
			Configurator.initialize(Thread.currentThread().getContextClassLoader(), csource);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		logger = LogManager.getLogger(Log4j2LogFactory.class);
		logger.info("Log4j2 configured from " + pUri);
	}

	/** Creates a new instance, which is being configured by reading the given {@link IReadable readable}.
	 * @param pReadable The document, that provides the Log4j 2 configuration.
	 * @throws NullPointerException The parameter {@code pReadable} is null.
	 * @return The created instance.
	 */
	public static Log4j2LogFactory of (IReadable pReadable) {
		final IReadable readable = Objects.requireNonNull(pReadable, "Readable");
		final Log4j2LogFactory lf = new Log4j2LogFactory() {
			@Override
			protected void init() {
				readable.read((in) -> {
					configure(in, readable.getName());
				});
			}
		};
		lf.start();
		return lf;
	}

	/** Creates a new instance, which is being configured by reading the given {@link Path file}.
	 * @param pPath The file, that provides the Log4j 2 configuration.
	 * @throws NullPointerException The parameter {@code pPath} is null.
	 * @return The created instance.
	 */
	public static Log4j2LogFactory of (Path pPath) {
		return of(IReadable.of(Objects.requireNonNull(pPath, "Path")));
	}

	/** Creates a new instance, which is being configured by reading the given {@link File file}.
	 * @param pFile The file, that provides the Log4j 2 configuration.
	 * @throws NullPointerException The parameter {@code pFile} is null.
	 * @return The created instance.
	 */
	public static Log4j2LogFactory of (File pFile) {
		return of(IReadable.of(Objects.requireNonNull(pFile, "File")));
	}

	/** Creates a new instance, which is being configured by reading the given {@link URL url}.
	 * @param pUrl The URL, that provides the Log4j 2 configuration.
	 * @throws NullPointerException The parameter {@code pUrl} is null.
	 * @return The created instance.
	 */
	public static Log4j2LogFactory of (URL pUrl) {
		return of(IReadable.of(Objects.requireNonNull(pUrl, "URL")));
	}
}
