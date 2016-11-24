package com.github.jochenw.afw.lc.impl;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.lc.IConnectionProvider;

public class DefaultConnectionProvider implements IConnectionProvider {
	@Inject private Properties properties;
	private final String propertyPrefix;
	private String driverClassName;
	private String url, shutdownUrl, user, password, shutdownCommand;
	private Driver driver;

	DefaultConnectionProvider() {
		this("");
	}

	@PostConstruct
	public void start() {
		driverClassName = requireProperty("jdbc.driver");
		url = requireProperty("jdbc.url");
		shutdownUrl = getProperty("jdbc.shutdownUrl");
		shutdownCommand = getProperty("jdbc.shutdownCommand");
		user = getProperty("jdbc.user");
		password = getProperty("jdbc.password");
		final Class<?> cl;
		try {
			cl = Class.forName(driverClassName);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Driver class not found: " + driverClassName);
		}
		final Driver dr;
		try {
			dr = (Driver) cl.newInstance();
			DriverManager.registerDriver(dr);
		} catch (InstantiationException e) {
			throw new IllegalStateException("Unable to instantiate driver class " + cl.getName() + ": " + e.getMessage(), e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Illegal access, while instantiating driver class " + cl.getName() + ": " + e.getMessage(), e);
		} catch (SQLException e) {
			throw new IllegalStateException("Unable to register SQL driver " + cl.getName() + ": " + e.getMessage(), e);
		}
		driver = dr;
	}

	@PreDestroy
	public void shutdown() {
		final Driver dr = driver;
		if (dr != null) {
			try {
				if (shutdownUrl != null) {
					try (Connection conn = DriverManager.getConnection(shutdownUrl, user, password)) {
						// Do nothing, connection is closed immediately.
					}
				} else if (shutdownCommand != null) {
					try (Connection conn = newConnection();
						 PreparedStatement stmt = conn.prepareStatement(shutdownCommand)) {
						stmt.executeUpdate();
					}
				}
				driver = null;
				DriverManager.deregisterDriver(dr);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
	}
	
	protected String getProperty(String pKey) {
		final String key = propertyPrefix + pKey;
		return properties.getProperty(key);
	}

	protected String requireProperty(String pKey) {
		final String value = getProperty(pKey);
		if (value == null  ||  value.length() == 0) {
			final String key = propertyPrefix + pKey;
			throw new IllegalStateException("Property " + key + " is undefined, or empty.");
		}
		return value;
	}
	
	DefaultConnectionProvider(String pPropertyPrefix) {
		propertyPrefix = pPropertyPrefix;
	}
	
	@Override
	public Connection newConnection() {
		try {
			return DriverManager.getConnection(url, user, password);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public void close(Connection pConnection) {
		try {
			pConnection.close();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

}
