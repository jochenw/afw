package com.github.jochenw.afw.core.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Functions.FailableFunction;


/** A helper object for implementing JDBC based applications.
 */
public class Worker {
	/** A context object, which is designed for use in application code.
	 */
	public class Context implements AutoCloseable {
		private final List<AutoCloseable> resources = new ArrayList<>();
		private Connection connection;

		/** Adds a resource for tracking. This resource will be closed automatically, when
		 * {@link #close()} is being invoked.
		 * @param pCloseable The resource, which is being tracked.
		 */
		public void add(AutoCloseable pCloseable) {
			resources.add(pCloseable);
		}

		/**
		 * Opens a new database connection, or returns an already opened connection.
		 * @return A database connection, that may be used by the caller. This
		 *    connection will be automatically closed, because it has already been
		 *    added to the resource tracking by invoking {@link #add(AutoCloseable)}.
		 * @throws SQLException Opening a new database connection has failed.
		 */
		public Connection getConnection() throws SQLException {
			if (connection == null) {
				connection = connectionProvider.open();
				add(connection);
			}
			return connection;
		}

		@Override
		public void close() throws Exception {
			Throwable th = null;
			while (!resources.isEmpty()) {
				final AutoCloseable ac = resources.remove(resources.size()-1);
				try {
					ac.close();
				} catch (Throwable t) {
					// In case of multiple exceptions: Throw the first.
					if (th == null) {
						th = t;
					}
				}
			}
			if (th != null) {
				throw Exceptions.show(th);
			}
		}

		/** Called to execute an SQL query, and process the results.
		 * @param pSql 
		 * @param pConsumer
		 * @param pParams
		 */
		public void executeQuery(String pSql, FailableConsumer<JdbcHelper.Row,?> pConsumer, Object... pParams) {
			try {
				getJdbcHelper().executeQuery(getConnection(), this::add, pSql, pConsumer, pParams);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Called to execute an SQL update statement.
		 * @param pSql The statement, that is being executed.
		 * @param pParams The parameters, if any.
		 * @return The number of affected rows.
		 */
		public int executeUpdate(String pSql, Object... pParams) {
			try {
				return getJdbcHelper().executeUpdate(getConnection(), this::add, pSql, pParams);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Returns the database dialect.
		 * @return The database dialect.
		 */
		public Dialect getDialect() {
			return connectionProvider.getDialect();
		}
	}

	private ConnectionProvider connectionProvider;
	private JdbcHelper jdbcHelper = new JdbcHelper();

	/** Creates a new instance with the given {@link ConnectionProvider}.
	 * @param pConnectionProvider The connection provider, which is being used.
	 */
	public Worker(ConnectionProvider pConnectionProvider) {
		connectionProvider = pConnectionProvider;
	}
	
	/** Creates a new instance without {@link ConnectionProvider}.
	 * @see #setConnectionProvider(ConnectionProvider)
	 */
	public Worker() {
	}

	/** Sets the {@link ConnectionProvider}, which is being used by the {@link Worker}.
	 * @param pConnectionProvider The {@link ConnectionProvider}, which is being used
	 * by the {@link Worker}.
	 * @see #getConnectionProvider()
	 */
	public @Inject void setConnectionProvider(ConnectionProvider pConnectionProvider) {
		connectionProvider = pConnectionProvider;
	}

	/** Returns the connection provider.
	 * @return The connection provider, never null.
	 * @see #setConnectionProvider(ConnectionProvider)
	 * @throws NullPointerException No connection provider has been cnfigured.
	 */
	public @Nonnull ConnectionProvider getConnectionProvider() {
		return Objects.requireNonNull(connectionProvider, "ConnectionProvider");
	}

	/** Sets the {@link JdbcHelper}, which is going to be used internally.
	 * @param pHelper The {@link JdbcHelper}, which is going to be used internally.
	 * @see #getJdbcHelper()
	 */
	public @Inject void setJdbcHelper(@Nonnull JdbcHelper pHelper) {
		jdbcHelper = Objects.requireNonNull(pHelper, "JdbcHelper");
	}

	/** Returns the {@link JdbcHelper}, which is being used internally.
	 * @return The {@link JdbcHelper}, which is being used internally. Never null.
	 * @throws NullPointerException No {@link JdbcHelper} has been configured.
	 * @see #setJdbcHelper(JdbcHelper)
	 */
	public @Nonnull JdbcHelper getJdbcHelper() {
		return Objects.requireNonNull(jdbcHelper, "JdbcHelper");
	}

	/** Called to perform an action on the database. The action is implemented by
	 * invoking the given {@code pRunnable runnable}.
	 * @param pRunnable The action, which is being executed.
	 */
	public void run(FailableConsumer<Context,?> pRunnable) {
		try (Context ctx = new Context()) {
			pRunnable.accept(ctx);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Called to perform an action on the database. The action is implemented by
	 * invoking the given {@code pCallable}, and returning the result.
	 * @param pCallable The action, which is being executed.
	 * @return The result object, that has been returned by the action.
	 */
	public <O> O call(FailableFunction<Context,O,?> pCallable) {
		try (Context ctx = new Context()) {
			return pCallable.apply(ctx);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
