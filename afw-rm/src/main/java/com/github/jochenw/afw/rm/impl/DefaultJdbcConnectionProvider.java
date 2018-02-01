package com.github.jochenw.afw.rm.impl;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import com.github.jochenw.afw.rm.api.AbstractInitializable;
import com.github.jochenw.afw.rm.api.ComponentFactory;
import com.github.jochenw.afw.rm.api.JdbcConnectionProvider;


public class DefaultJdbcConnectionProvider extends AbstractInitializable implements JdbcConnectionProvider {
	private final String propertyPrefix;
	private String driver, url, user, password;

	public DefaultJdbcConnectionProvider(String pPropertyPrefix) {
		Objects.requireNonNull(pPropertyPrefix, "Property Prefix");
		propertyPrefix = pPropertyPrefix;
	}

	public DefaultJdbcConnectionProvider() {
		this("jdbc.connection.");
	}

	@Override
	public Connection open() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}

	@Override
	public void close(Connection pConnection) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(ComponentFactory pComponentFactory) {
		super.init(pComponentFactory);
		final ClassLoader cl = pComponentFactory.requireInstance(ClassLoader.class);
		final Properties properties = pComponentFactory.requireInstance(Properties.class);
		driver = requireProperty(properties, "driver");
		url = requireProperty(properties, "url");
		user = getProperty(properties, "user");
		password = getProperty(properties, "password");
		try {
			cl.loadClass(driver);
		} catch (Throwable t) {
			throw new UndeclaredThrowableException(t, "Unable to load driver class " + driver + ": " + t.getMessage());
		}
	}

	protected String requireProperty(Properties pProperties, String pSuffix) {
		final String value = getProperty(pProperties, pSuffix);
		if (value == null  ||  value.length() == 0) {
			throw new IllegalStateException("Missing, or empty property: " + propertyPrefix + pSuffix);
		}
		return value;
	}

	protected String getProperty(Properties pProperties, String pSuffix) {
		final String key = propertyPrefix + pSuffix;
		return pProperties.getProperty(key);
	}
}
