/**
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

import java.net.URL;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.AbstractLogFactory;

public class Log4jLogFactory extends AbstractLogFactory {
	private Logger logger;

	@Override
    protected AbstractLog newLog(String pId) {
        return new Log4jLog(this, pId);
    }

	@Override
	protected void init() {
		final URL url = getResourceLocator().getResource("log4j.xml");
		if (url == null) {
			BasicConfigurator.configure();
			Logger.getRootLogger().setLevel(Level.DEBUG);
			logger = Logger.getLogger(Log4jLogFactory.class);
			logger.warn("Log4j.xml not found, log4j configured with default settings.");
		} else {
			DOMConfigurator.configure(url);
			logger = Logger.getLogger(Log4jLogFactory.class);
			logger.info("Log4j configured from " + url);
		}
	}
}
