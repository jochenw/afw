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

import java.io.InputStream;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;

import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.AbstractLogFactory;
import com.github.jochenw.afw.core.util.Exceptions;

public class Log4j2LogFactory extends AbstractLogFactory {
	private Logger logger;

	@Override
    protected AbstractLog newLog(String pId) {
        return new Log4j2Log(this, pId);
    }

	@Override
	protected void init() {
		final URL url = getResourceLocator().getResource("log4j2.xml");
		try (InputStream in = url.openStream()) {
			final ConfigurationSource csource = new ConfigurationSource(in, url);
			Configurator.initialize(Thread.currentThread().getContextClassLoader(), csource);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		logger = LogManager.getLogger(Log4j2LogFactory.class);
		logger.info("Log4j2 configured from " + url);
	}
}
