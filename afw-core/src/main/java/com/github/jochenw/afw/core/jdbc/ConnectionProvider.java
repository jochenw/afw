package com.github.jochenw.afw.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface of a provider for JDBC connections.
 */
@FunctionalInterface
public interface ConnectionProvider {
	/** Opens a new database connection.
	 * @return The new database connection, that has been opened.
	 * @throws SQLException Creating the database connection has failed.
	 */
	public Connection open() throws SQLException;

	/** Returns the dialect id.
	 * @return The dialect id.
	 */
	public default String getDialectId() { return null; }

	/** Returns the actual dialect.
	 * @return The actual dialect.
	 */
	public default Dialect getDialect() { return null; }
}
