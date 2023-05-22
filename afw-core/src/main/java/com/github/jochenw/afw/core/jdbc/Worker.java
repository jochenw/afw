package com.github.jochenw.afw.core.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.jdbc.JdbcHelper.Row;
import com.github.jochenw.afw.core.util.Exceptions;


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

		/** Called to execute an SQL query, and process the results by repeatably calling a
		 * {@link FailableConsumer consumer}, that produces no result.
		 * @param pSql The SQL query, that is being executed.
		 * @param pConsumer The consumer, that is being called to process the result.
		 * @param pParams The query parameters, if any.
		 * @see #executeQueryCall(String, FailableFunction, Object...)
		 */
		public void executeQuery(String pSql, FailableConsumer<JdbcHelper.Row,?> pConsumer, Object... pParams) {
			try (Connection conn = getConnectionProvider().open();
				 PreparedStatement stmt = conn.prepareStatement(pSql)) {
				final JdbcHelper jh = getJdbcHelper();
				jh.setParameters(stmt, pParams);
				try (final ResultSet rs = stmt.executeQuery()) {
					final Row row = jh.newRow(rs);
					while (rs.next()) {
						try {
							pConsumer.accept(row);
						} catch (Throwable t) {
							throw Exceptions.show(t, SQLException.class);
						}
					}
				}
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Called to execute an SQL query, process the results, and return an output object,
		 * which is created by invoking the given {@link FailableFunction}.
		 * @param pSql The SQL query, that is being executed.
		 * @param pFunction The function, that is being called to process the result, and
		 *   produce the output object. The function should <em>not</em> close the given
		 *   {@link ResultSet} result set, because that's done by the caller.
		 * @param pParams The query parameters, if any.
		 * @param <O> Type of the output object.
		 * @return The output object, which has been produced by calling the
		 *   given {@link FailableFunction} function.
		 * @see #executeQuery(String, FailableConsumer, Object...)
		 * @see #executeSingleRowQuery(String, FailableFunction, Object...)
		 * @see #executeCountQuery(String, Object...)
		 */
		public <O> O executeQueryCall(String pSql, FailableFunction<ResultSet,O,?> pFunction, Object... pParams) {
			try (Connection conn = getConnectionProvider().open();
				 PreparedStatement stmt = conn.prepareStatement(pSql)) {
				getJdbcHelper().setParameters(stmt, pParams);
				try (ResultSet rs = stmt.executeQuery()) {
					return pFunction.apply(rs);
				}
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Called to execute an SQL query, that produces a single result row, process the results,
		 * and return an output object, which is created by invoking the given {@link FailableFunction}.
		 * @param pSql The SQL query, that is being executed.
		 * @param pFunction The function, that is being called to process the result, and
		 *   produce the output object. The function should <em>not</em> close the given
		 *   {@link ResultSet} result set, because that's done by the caller.
		 * @param pParams The query parameters, if any.
		 * @param <O> Type of the output object.
		 * @return The output object, which has been produced by calling the
		 *   given {@link FailableFunction} function.
		 * @see #executeQueryCall(String, FailableFunction, Object...)
		 * @see #executeCountQuery(String, Object...)
		 */
		public <O> O executeSingleRowQuery(String pSql, FailableFunction<JdbcHelper.Row,O,?> pFunction, Object... pParams) {
			try (Connection conn = getConnectionProvider().open();
				 PreparedStatement stmt = conn.prepareStatement(pSql)) {
				getJdbcHelper().setParameters(stmt, pParams);
				try (ResultSet rs = stmt.executeQuery()) {
					JdbcHelper.Row row = getJdbcHelper().newRow(rs);
					if (row.next()) {
						return pFunction.apply(row);
					} else {
						throw new IllegalStateException("Expected result row not available.");
					}
				}
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Called to execute an SQL update statement.
		 * @param pSql The statement, that is being executed.
		 * @param pParams The parameters, if any.
		 * @return The number of affected rows.
		 * @see #executeQuery(String, FailableConsumer, Object...)
		 */
		public int executeUpdate(String pSql, Object... pParams) {
			try (Connection conn = getConnectionProvider().open();
				 PreparedStatement stmt = conn.prepareStatement(pSql)) {
				getJdbcHelper().setParameters(stmt, pParams);
				return stmt.executeUpdate();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Called to execute an SQL query, which returns exactly one row, and one column,
		 * which has an integer, or long value.
		 * @param pSql The statement, that is being executed.
		 * @param pParams The parameters, if any.
		 * @return The value of the single result cell.
		 * @throws IllegalStateException The query did not return a result row.
		 * @see #executeSingleRowQuery(String, FailableFunction, Object...)
		 * @see #executeQueryCall(String, FailableFunction, Object...)
		 */
		public long executeCountQuery(String pSql, Object... pParams) {
			return executeSingleRowQuery(pSql, (r) -> {
				return r.nextLongObj();
			}, pParams).longValue();
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
	 * @param <O> Type of the result object, that is returned by the database.
	 */
	public <O> O call(FailableFunction<Context,O,?> pCallable) {
		try (Context ctx = new Context()) {
			return pCallable.apply(ctx);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
