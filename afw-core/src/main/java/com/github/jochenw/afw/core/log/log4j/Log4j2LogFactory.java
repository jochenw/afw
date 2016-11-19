package com.github.jochenw.afw.core.log.log4j;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
		Class<?> configurationSourceClass = null;
		Class<?> configuratorClass = null;
		try {
			configurationSourceClass = Class.forName("org.apache.logging.log4j.core.config.ConfigurationSource");
			configuratorClass = Class.forName("org.apache.logging.log4j.core.config.Configurator");
		} catch (ClassNotFoundException e) {
			configurationSourceClass = null;
			configuratorClass = null;
		}

		if (configurationSourceClass == null  ||  configuratorClass == null) {
			// log4j-core is not in the class path
			logger = LogManager.getLogger(Log4j2LogFactory.class);
			logger.warn("Log4j-Core not found, configuration is hopefully done elsewhere.");
		} else {
			final URL url = getResourceLocator().getResource("log4j2.xml");

			final Constructor<?> cons;
			try {
				cons = configurationSourceClass.getConstructor(InputStream.class, URL.class);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}

			final Method initializeMethod;
			try {
				initializeMethod = configuratorClass.getMethod("initialize", ClassLoader.class, configurationSourceClass);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}

			final Object configurationSource;
			try (InputStream istream = url.openStream()) {
				configurationSource = cons.newInstance(istream, url);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			try {
				initializeMethod.invoke(null, null, configurationSource);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			logger = LogManager.getLogger(Log4j2LogFactory.class);
			logger.info("Log4j2 configured from " + url);
		}
	}
}
