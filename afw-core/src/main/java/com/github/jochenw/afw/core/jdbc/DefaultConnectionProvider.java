package com.github.jochenw.afw.core.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Objects;

/** Default implementation of {@link ConnectionProvider}: Uses the properties
 * "_prefix_.driver", "_prefix_.dialect", "_prefix_.url",
 * "_prefix_.userName", and "_prefix_.password". The default value for
 * _prefix_ is "jdbc", but may be {@link #setPrefix(String) overwritten}.
 */
public class DefaultConnectionProvider implements ConnectionProvider {
	private @Nonnull String prefix = "jdbc";
	private IPropertyFactory propertyFactory;
	private IComponentFactory componentFactory;

	/** Returns the value of the property prefix.
	 * @return The value of the property prefix.
	 */
	public @Nonnull String getPrefix() {
		return prefix;
	}

	/** Sets the value of the property prefix.
	 * @param pPrefix The value of the property prefix.
	 */
	public void setPrefix(@Nonnull String pPrefix) {
		prefix = Objects.requireNonNull(pPrefix, "Prefix");
	}

	/** Sets the {@link IPropertyFactory}.
	 * @param pPropertyFactory The {@link IPropertyFactory property factory}.
	 */
	public @Inject void setPropertyFactory(IPropertyFactory pPropertyFactory) {
		propertyFactory = pPropertyFactory;
	}

	/** Sets the {@link IComponentFactory}.
	 * @param pComponentFactory The {@link IComponentFactory property factory}.
	 */
	public @Inject void setComponentFactory(IComponentFactory pComponentFactory) {
		componentFactory = pComponentFactory;
	}

	@Override
	public @Nonnull Connection open() throws SQLException {
		final String driverClassName = getProperty("driver");
		try {
			Class.forName(driverClassName);
		} catch (Throwable t) {
			throw new IllegalStateException("Unable to load driver class: " + driverClassName, t);
		}
		final String url = getProperty("url");
		final String userName = getProperty("userName");
		final String password = getProperty("password");
		final Connection conn = DriverManager.getConnection(url, userName, password);
		return Objects.requireNonNull(conn, "Connection");
	}

	@Override
	public String getDialectId() {
		return getProperty("dialect");
	}

	protected String getProperty(String pProperty) {
		final String prop = getPrefix() + "." + pProperty;
		final String value = propertyFactory.getPropertyValue(prop);
		if (value == null) {
			throw new NullPointerException("Missing property: " + prop);
		}
		if (value.length() == 0) {
			throw new IllegalArgumentException("Empty property: " + prop);
		}
		return value;
	}

	@Override
	public Dialect getDialect() {
		return componentFactory.requireInstance(Dialect.class, getDialectId());
	}
}