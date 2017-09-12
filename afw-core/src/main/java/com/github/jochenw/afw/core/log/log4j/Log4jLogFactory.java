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
