package com.github.jochenw.afw.lc.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.IConnectionProvider;
import com.github.jochenw.afw.lc.InjLog;

public class DbInitializer {
	private static final ThreadLocal<ComponentFactory> cf = new ThreadLocal<ComponentFactory>();
	@Inject @Named(value="flyway.schemaPackage") private String schemaPackage;
	@Inject ComponentFactory componentFactory;
	@Inject private IConnectionProvider connectionProvider;
	@InjLog() private ILog log;

	@PostConstruct
	public void start() {
		cf.set(componentFactory);
		try {
			try (Connection conn = connectionProvider.newConnection()) {
				final DatabaseMetaData dbmd = conn.getMetaData();
				final String dbId = getDbId(dbmd);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			final Flyway flyway = new Flyway();
			flyway.setBaselineOnMigrate(true);
			String pkg = schemaPackage;
			flyway.setLocations(pkg);
			flyway.setDataSource(newDataSource());
			flyway.migrate();
		} finally {
			cf.set(null);
		}
	}

	public static ComponentFactory getComponentFactory() {
		final ComponentFactory c = cf.get();
		if (c == null) {
			throw new IllegalStateException("No ComponentFactory available.");
		}
		return c;
	}
	
	protected String getDbId(DatabaseMetaData pDbmd) throws SQLException {
		final String databaseProductName = pDbmd.getDatabaseProductName();
        if (databaseProductName.startsWith("Apache Derby")) {
            return "derby";
        }
        if (databaseProductName.startsWith("SQLite")) {
            return "sqlite";
        }
        if (databaseProductName.startsWith("H2")) {
        	return "h2";
        }
        if (databaseProductName.contains("HSQL Database Engine")) {
        	return "hsqldb";
        }
        if (databaseProductName.startsWith("Microsoft SQL Server")) {
        	return "sqlserver";
        }
        if (databaseProductName.contains("MySQL")) {
        	return "mysql";
        }
        if (databaseProductName.startsWith("Oracle")) {
        	return "oracle";
        }
        if (databaseProductName.startsWith("PostgreSQL 8")) {
        	return "redshift";
        }
        if (databaseProductName.startsWith("PostgreSQL")) {
        	return "postgresql";
        }
        if (databaseProductName.startsWith("DB2")) {
        	return "db2";
        }
        if (databaseProductName.startsWith("Vertica")) {
        	return "vertica";
        }
        if (databaseProductName.contains("solidDB")) {
        	return "solid";
        }
        if (databaseProductName.startsWith("Phoenix")) {
        	return "phoenix";
        }
        throw new IllegalStateException("Invalid database product name");
	}
	
	protected DataSource newDataSource() {
		return new DataSource(){
			@Override
			public PrintWriter getLogWriter() throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public void setLogWriter(PrintWriter out) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public void setLoginTimeout(int seconds) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public int getLoginTimeout() throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public Logger getParentLogger() throws SQLFeatureNotSupportedException {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public <T> T unwrap(Class<T> iface) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public boolean isWrapperFor(Class<?> iface) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public Connection getConnection() throws SQLException {
				return connectionProvider.newConnection();
			}

			@Override
			public Connection getConnection(String username, String password) throws SQLException {
				throw new IllegalStateException("Not implemented");
			}
		};
	}
}
