package com.github.jochenw.afw.core.jdbc;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.BooleanConsumer;
import com.github.jochenw.afw.core.function.Functions.ByteConsumer;
import com.github.jochenw.afw.core.function.Functions.DoubleConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.function.Functions.FloatConsumer;
import com.github.jochenw.afw.core.function.Functions.ShortConsumer;
import com.github.jochenw.afw.core.util.Exceptions;

/** A helper object for working with JDBC connections.
 */
public class JdbcHelper {
	/** Creates a new instance.
	 */
	public JdbcHelper() {}

	private static final @NonNull ZoneId UTC = Objects.requireNonNull(ZoneId.of("GMT"));
	private @NonNull ZoneId dbZoneId = UTC;
	private @NonNull ZoneId appZoneId = Objects.requireNonNull(ZoneId.systemDefault());

	/** Returns the database time zone id.
	 * @return The database time zone id.
	 */
	public @NonNull ZoneId getDbZoneId() {
		return dbZoneId;
	}

	/** Sets the database time zone id.
	 * @param pDbZoneId The database time zone id.
	 */
	public @Inject void setDbZoneId(@Named(value="db") ZoneId pDbZoneId) {
		dbZoneId = Objects.requireNonNull(pDbZoneId, "DbZoneId");
	}

	/** Returns the application time zone id.
	 * @return The application time zone id.
	 */
	public @NonNull ZoneId getAppZoneId() {
		return appZoneId;
	}

	/** Sets the application time zone id.
	 * @param pAppZoneId The application time zone id.
	 */
	public @Inject void setAppZoneId(ZoneId pAppZoneId) {
		appZoneId = Objects.requireNonNull(pAppZoneId, "AppZoneId");
	}

	/**
	 * An object, that can execute a query, and process the result.
	 */
	public static class Executor {
		private final String query;
		private final Object[] parameters;
		private final JdbcHelper helper;
		private final Dialect dialect;
		private final FailableSupplier<Connection,?> connectionProvider;

		/** Creates a new instance.
		 * @param pHelper The {@link JdbcHelper}, that is creating this object.
		 * @param pDialect The {@link Dialect SQL Dialect}, if available,
		 *   for support in error handling.
		 * @param pConnProvider The connection provider.
		 * @param pQuery The SQL query, that is being executed.
		 * @param pParameters The query parameters.
		 */
		public Executor(@NonNull JdbcHelper pHelper,
				        @Nullable Dialect pDialect,
				        @NonNull FailableSupplier<Connection,?> pConnProvider,
				        @NonNull String pQuery,
				        @Nullable Object... pParameters) {
			helper = Objects.requireNonNull(pHelper, "JdbcHelper");
			dialect = pDialect;
			connectionProvider = Objects.requireNonNull(pConnProvider, "Connection Provider");
			query = Objects.requireNonNull(pQuery, "Query");
			parameters = pParameters;
		}


		/** Executes a "count query", and returns the result. A "count query" is
		 * defined to be a query, which returns exactly one row with exactly
		 * one long integer column.
		 * @return The result of the "count query": An integer, or long, which
		 * was returned in the first column of the first row in the result set.
		 * @throws IllegalStateException The query is no "count query", because
		 * it returned zero, or more than one result row, or because it returned
		 * null, rather than an integer, or long value.
		 */
		public int count() {
			try (Connection conn = connectionProvider.get();
				 PreparedStatement stmt = Objects.requireNonNull(conn.prepareStatement(query))) {
			    helper.setParameters(stmt, parameters);
			    try (ResultSet rs = stmt.executeQuery()) {
			    	if (!rs.next()) {
			    		throw new IllegalStateException("The query did not return a result row.");
			    	}
			    	final int i = rs.getInt(1);
			    	if (rs.wasNull()) {
			    		throw new IllegalStateException("The query returned a null object.");
			    	}
			    	if (rs.next()) {
			    		throw new IllegalStateException("The query returned more than one result row.");
			    	}
			    	return i;
			    }
			} catch (Throwable t) {
				throw handleError(t);
			}
		}
	
		/** Executes a "count query", and returns the result. A "count query" is
		 * defined to be a query, which returns exactly one row with exactly
		 * one long integer column.
		 * @return The result of the "count query": An integer, or long, which
		 * was returned in the first column of the first row in the result set.
		 * @throws IllegalStateException The query is no "count query", because
		 * it returned zero, or more than one result row, or because it returned
		 * null, rather than an integer, or long value.
		 */
		public long countLong() {
			try (Connection conn = connectionProvider.get();
				 PreparedStatement stmt = Objects.requireNonNull(conn.prepareStatement(query))) {
			    helper.setParameters(stmt, parameters);
			    try (ResultSet rs = stmt.executeQuery()) {
			    	if (!rs.next()) {
			    		throw new IllegalStateException("The query did not return a result row.");
			    	}
			    	final long l = rs.getLong(1);
			    	if (rs.wasNull()) {
			    		throw new IllegalStateException("The query returned a null object.");
			    	}
			    	if (rs.next()) {
			    		throw new IllegalStateException("The query returned more than one result row.");
			    	}
			    	return l;
			    }
			} catch (Throwable t) {
				throw handleError(t);
			}
		}

		/** Executes a "count query", and returns the nullable result.
		 * A "count query" is defined to be a query, which returns exactly one
		 * row with exactly one integer column.
		 * @return The result of the "count query": An integer, or long, which
		 * was returned in the first column of the first row in the result set.
		 * @throws IllegalStateException The query is no "count query", because
		 * it returned zero, or more than one result row, or because it returned
		 * null, rather than an integer, or long value.
		 */
		public Long countNullable() {
			try (Connection conn = connectionProvider.get();
				 PreparedStatement stmt = Objects.requireNonNull(conn.prepareStatement(query))) {
			    helper.setParameters(stmt, parameters);
			    try (ResultSet rs = stmt.executeQuery()) {
			    	if (!rs.next()) {
			    		throw new IllegalStateException("The query did not return a result row.");
			    	}
			    	final long l = rs.getLong(1);
			    	if (rs.wasNull()) {
			    		return null;
			    	}
			    	if (rs.next()) {
			    		throw new IllegalStateException("The query returned more than one result row.");
			    	}
			    	return Long.valueOf(l);
			    }
			} catch (Throwable t) {
				throw handleError(t);
			}
		}

		/**
		 * Executes the configured query, and returns the number of affected rows.
		 * @return The number of affected rows (The result of
		 * {@link PreparedStatement#executeUpdate()}.
		 */
		public int run() {
			try (Connection conn = connectionProvider.get();
				 PreparedStatement stmt = Objects.requireNonNull(conn.prepareStatement(query))) {
				helper.setParameters(stmt, parameters);
				return stmt.executeUpdate();
			} catch (Throwable t) {
				throw handleError(t);
			}
		}

		/** Executes the query, creates a {@link ResultSet},
		 * and invokes the given function to process the
		 * {@link ResultSet}. Returns the functions result.
		 * @param <O> Type of the result object.
		 * @param pFunction The consumer, which is being invoked to process the {@link ResultSet}.
		 * @return The result object, which was returned by the function invocation.
		 */
		public <O> O call(FailableFunction<ResultSet,O,?> pFunction) {
			try (Connection conn = connectionProvider.get();
				 PreparedStatement stmt = Objects.requireNonNull(conn.prepareStatement(query))) {
				helper.setParameters(stmt, parameters);
				try (ResultSet rs = stmt.executeQuery()) {
					return pFunction.apply(rs);
				}
			} catch (Throwable t) {
				throw handleError(t);
			}
		}
	
		/** Executes the query, creates a {@link Rows} object,
		 * and invokes the given function to process the
		 * {@link Rows} object. Returns the functions result.
		 * @param <O> Type of the result object.
		 * @param pFunction The consumer, which is being invoked to process the {@link Rows} object.
		 * @return The result object, which was returned by the function invocation.
		 */
		public <O> O callWithRows(FailableFunction<Rows,O,?> pFunction) {
			final FailableFunction<ResultSet,O,?> function = (rs) -> {
				return pFunction.apply(helper.newRows(rs));
			};
			return call(function);
		}
	
		/** Executes the query, creates a {@link ResultSet},
		 * and invokes the given consumer to process the
		 * {@link ResultSet}. Returns no result.
		 * @param pConsumer The consumer, which is being invoked to process the {@link ResultSet}.
		 */
		public void run(FailableConsumer<ResultSet,?> pConsumer) {
			call((FailableFunction<ResultSet, Object, ?>) (rs) -> {
				pConsumer.accept(rs);
				return null;
			});
		}

		/** Executes the query, creates a {@link Rows} object,
		 * and invokes the given consumer to process the
		 * {@link Rows} object. Returns no result.
		 * @param pConsumer The consumer, which is being invoked to process the {@link Rows} objects.
		 */
		public void runWithRows(FailableConsumer<Rows,?> pConsumer) {
			callWithRows((FailableFunction<Rows, Object, ?>) (rows) -> {
				pConsumer.accept(rows);
				return null;
			});
		}

		/** Executes a query, which returns exactly one row, and exactly one
		 * column, and returns the result object.
		 * @param <O> Type of the result object.
		 * @return The query's single result object, possibly null (depending on the query).
		 */
		public <O> O singleObject() {
			final FailableFunction<Rows,O,?> function = (rows) -> {
				if (rows.next()) {
					final O o = rows.nextObject();
					if (rows.next()) {
						throw new IllegalStateException("The query returned more than one result row.");
					}
					return o;
				} else {
					throw new IllegalStateException("The query did not return a result row.");
				}
			};
			return callWithRows(function);
		}

		/** Called to handle an error, that has occurred.
		 * @param pError The error, that has occurred, typically
		 * an {@link SQLException}.
		 * @return The error, that is being reported. This might be the
		 *   same as the parameter {@code pError}, but not necessarily.
		 *   The return value null indicates, that no error is being
		 *   reported.
		 */
		protected RuntimeException handleError(Throwable pError) {
			final Throwable error = Objects.requireNonNull(pError, "Error");
			if (pError instanceof SQLException) {
				if (dialect != null) {
					final SQLException se = (SQLException) error;
					if (dialect.isDroppedTableDoesnExistError(se)) {
						throw new DroppedTableDoesntExistException(se);
					}
				}
			}
			throw Exceptions.show(error);
		}
	}

	/** Abstract representation of a row in the {@link ResultSet}. Allows a code style, that is
	 * independent from JDBC.
	 */
	public class Rows {
		private final ResultSet rs;
		private ResultSetMetaData rsmd;
		private int index;

		/** Creates a new instance with the given result set.
		 * @param pResultSet The result set, which is encapsulated by this object.
		 */
		public Rows(ResultSet pResultSet) {
			rs = pResultSet;
		}

		
		/** Returns the next object in the column list.
		 * @param <O> The expected object type.
		 * @return The next object in the column list.
		 */
		public <O> O nextObject() {
			return getObject(++index);
		}

		/** Returns the object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @param <O> Expected column type.
		 * @return The object with the given index in the column list.
		 */
		public <O> O getObject(int pIndex) {
			try {
				if (rsmd == null) {
					rsmd = rs.getMetaData();
				}
				final int type = rsmd.getColumnType(pIndex);
				Object object;
				switch (type) {
				case Types.INTEGER:
					object = getIntObj(pIndex);
					break;
				case Types.SMALLINT:
					object = getShortObj(pIndex);
					break;
				case Types.TINYINT:
					object = getByteObj(pIndex);
					break;
				case Types.BIGINT:
					object = getLongObj(pIndex);
					break;
				case Types.VARCHAR:
				case Types.CHAR:
				case Types.CLOB:
				case Types.LONGVARCHAR:
				case Types.LONGNVARCHAR:
				case Types.NVARCHAR:
				case Types.NCHAR:
				case Types.NCLOB:
					object = getStr(pIndex);
					break;
				case Types.BOOLEAN:
					object = getBoolObj(pIndex);
					break;
				case Types.BLOB:
				case Types.BINARY:
				case Types.VARBINARY:
					object = getBytes(pIndex);
					break;
				case Types.NUMERIC:
					object = getBigDecimal(pIndex);
					break;
				case Types.TIMESTAMP:
					object = getZonedDateTime(pIndex, appZoneId);
					break;
				case Types.TIME:
					object = getTime(pIndex);
					break;
				case Types.DATE:
					object = getDate(pIndex);
					break;
				case Types.DOUBLE:
					object = getDoubleObj(pIndex);
					break;
				case Types.FLOAT:
					object = getFloatObj(pIndex);
				default:
					throw new IllegalStateException("Invalid column type: " + type);
				}
				@SuppressWarnings("unchecked")
				final O o = (O) object;
				return o;
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the underlying {@link ResultSet}.
		 * @return The underlying {@link ResultSet}.
		 */
		public ResultSet getResultSet() { return rs; }
		
		/** Returns, whether there is another row available.
		 * @return True, if there is another row available, otherwise false.
		 * @throws SQLException The check for a next row failed.
		 */
		public boolean next() throws SQLException {
			reset();
			return rs.next();
		}

		/** Resets the column counter to 0. The next invocations of any
		 * {@code next*} method will return the values for column 1, 2, ...
		 */
		public void reset() {
			index = 0;
		}

		/** Returns the next big decimal in the column list.
		 * @return The next big decimal in the column list.
		 */
		public BigDecimal nextBigDecimal() {
			return getBigDecimal(++index);
		}

		/** Returns the big decimal with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The big decimal with the given index in the column list.
		 */
		public BigDecimal getBigDecimal(int pIndex) {
			try {
				return rs.getBigDecimal(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Passes the next big decimal in the column list to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the big decimal.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextBigDecimal(Consumer<BigDecimal> pConsumer) {
			final Consumer<BigDecimal> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getBigDecimal(++index));
			return this;
		}

		/** Returns the next big integer in the column list.
		 * @return The next big integer in the column list.
		 */
		public BigInteger nextBigInteger() {
			return getBigInteger(++index);
		}

		/** Returns the big integer with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The big decimal with the given index in the column list.
		 */
		public BigInteger getBigInteger(int pIndex) {
			try {
				final BigDecimal bd = rs.getBigDecimal(pIndex);
				return bd == null ? null : bd.toBigInteger();
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Passes the next big decimal in the column list to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the big decimal.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextBigInteger(Consumer<BigInteger> pConsumer) {
			final Consumer<BigInteger> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getBigInteger(++index));
			return this;
		}

		/** Returns the next string in the column list.
		 * @return The next string in the column list.
		 */
		public String nextStr() {
			return getStr(++index);
		}

		/** Passes the next string in the column list to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the string.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextStr(Consumer<String> pConsumer) {
			final Consumer<String> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getStr(++index));
			return this;
		}

		/** Returns the string with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The string with the given index in the column list.
		 */
		public String getStr(int pIndex) {
			try {
				return rs.getString(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next long in the column list.
		 * @return The next long in the column list.
		 */
		public long nextLong() {
			return getLong(++index);
		}

		/** Passes the next value in the column list as a long to the
		 * given {@link LongConsumer}.
		 * @param pConsumer The {@link LongConsumer}, that should receive the string.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextLong(LongConsumer pConsumer) {
			final LongConsumer consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getLong(++index));
			return this;
		}

		/** Returns the long with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The long with the given index in the column list.
		 */
		public long getLong(int pIndex) {
			try {
				return rs.getLong(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next Long in the column list.
		 * @return The next Long in the column list, possibly null.
		 */
		public Long nextLongObj() {
			return getLongObj(++index);
		}

		/** Passes the next value in the column list as a Long to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link LongConsumer}, that should receive the string.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextLongObj(Consumer<Long> pConsumer) {
			final Consumer<Long> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getLongObj(++index));
			return this;
		}

		/** Returns the Long with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The Long with the given index in the column list, possibly null.
		 */
		public Long getLongObj(int pIndex) {
			try {
				final long l = rs.getLong(pIndex);
				if (rs.wasNull()) {
					return null;
				} else {
					return Long.valueOf(l);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		
		/** Returns the next integer in the column list.
		 * @return The next integer in the column list.
		 */
		public int nextInt() {
			return getInt(++index);
		}

		/** Passes the next value in the column list as an integer to the
		 * given {@link LongConsumer}.
		 * @param pConsumer The {@link LongConsumer}, that should receive the string.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextInt(IntConsumer pConsumer) {
			final IntConsumer consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getInt(++index));
			return this;
		}

		/** Returns the long with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The long with the given index in the column list.
		 */
		public int getInt(int pIndex) {
			try {
				return rs.getInt(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next integer object in the column list, possibly null.
		 * @return The next integer object in the column list, possibly null.
		 */
		public Integer nextIntObj() {
			return getIntObj(++index);
		}

		/** Passes the next value in the column list as an integer object (possibly null)
		 * to the given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the string.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextIntObj(Consumer<Integer> pConsumer) {
			final Consumer<Integer> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getIntObj(++index));
			return this;
		}

		/** Returns the integer object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The integer object with the given index in the column list,
		 *   possibly null.
		 */
		public Integer getIntObj(int pIndex) {
			try {
				final int i = rs.getInt(pIndex);
				if (rs.wasNull()) {
					return null;
				} else {
					return Integer.valueOf(i);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next short in the column list.
		 * @return The next integer in the column list.
		 */
		public short nextShort() {
			return getShort(++index);
		}

		/** Passes the next value in the column list as a short to the
		 * given {@link ShortConsumer}.
		 * @param pConsumer The {@link ShortConsumer}, that should receive the string.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextShort(ShortConsumer pConsumer) {
			final ShortConsumer consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getShort(++index));
			return this;
		}

		/** Returns the short with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The short with the given index in the column list.
		 */
		public short getShort(int pIndex) {
			try {
				return rs.getShort(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}
		/** Returns the next short object in the column list.
		 * @return The next short object in the column list, possibly null.
		 */
		public Short nextShortObj() {
			return getShortObj(++index);
		}

		/** Passes the next value in the column list as a short object 
		 * (possibly null) to the given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the short object.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextShortObj(Consumer<Short> pConsumer) {
			final Consumer<Short> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getShortObj(++index));
			return this;
		}

		/** Returns the short object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The short with the given index in the column list, possibly null.
		 */
		public Short getShortObj(int pIndex) {
			try {
				final short sh = rs.getShort(pIndex);
				if (rs.wasNull()) {
					return null;
				} else {
					return Short.valueOf(sh);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next byte in the column list.
		 * @return The next byte in the column list.
		 */
		public short nextByte() {
			return getByte(++index);
		}
		

		/** Passes the next value in the column list as a byte to the
		 * given {@link ByteConsumer}.
		 * @param pConsumer The {@link ShortConsumer}, that should receive the byte.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextByte(ByteConsumer pConsumer) {
			final ByteConsumer consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getByte(++index));
			return this;
		}

		/** Returns the byte with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The byte with the given index in the column list.
		 */
		public byte getByte(int pIndex) {
			try {
				return rs.getByte(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next byte object in the column list.
		 * @return The next byte object in the column list.
		 */
		public Byte nextByteObj() {
			return getByteObj(++index);
		}
		

		/** Passes the next value in the column list as a byte object
		 * (possibly null) to the given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the byte.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextByteObj(Consumer<Byte> pConsumer) {
			final Consumer<Byte> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getByteObj(++index));
			return this;
		}

		/** Returns the byte object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The byte with the given index in the column list,
		 *   possibly null.
		 */
		public Byte getByteObj(int pIndex) {
			try {
				final byte bt = rs.getByte(pIndex);
				if (rs.wasNull()) {
					return null;
				} else {
					return Byte.valueOf(bt);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}


		/** Returns the next float in the column list.
		 * @return The next float in the column list.
		 */
		public float nextFloat() {
			return getFloat(++index);
		}
		

		/** Passes the next value in the column list as a float to the
		 * given {@link FloatConsumer}.
		 * @param pConsumer The {@link FloatConsumer}, that should receive the string.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextFloat(FloatConsumer pConsumer) {
			final FloatConsumer consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getFloat(++index));
			return this;
		}

		/** Returns the float with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The byte with the given index in the column list.
		 */
		public float getFloat(int pIndex) {
			try {
				return rs.getFloat(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next boolean object in the column list.
		 * @return The next boolean object in the column list, possibly null.
		 */
		public Float nextFloatObj() {
			return getFloatObj(++index);
		}
		

		/** Passes the next value in the column list as a float object to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) float object.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextFloatObj(Consumer<Float> pConsumer) {
			final Consumer<Float> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getFloatObj(++index));
			return this;
		}

		/** Returns the float object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The float object with the given index in the column list,
		 *   possibly null.
		 */
		public Float getFloatObj(int pIndex) {
			try {
				final float f = rs.getFloat(pIndex);
				if (rs.wasNull()) {
					return null;
				} else {
					return Float.valueOf(f);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next boolean in the column list.
		 * @return The next boolean in the column list.
		 */
		public boolean nextBool() {
			return getBool(++index);
		}
		

		/** Passes the next value in the column list as a boolean to the
		 * given {@link ByteConsumer}.
		 * @param pConsumer The {@link BooleanConsumer}, that should receive the string.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextBool(BooleanConsumer pConsumer) {
			final BooleanConsumer consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getBool(++index));
			return this;
		}

		/** Returns the boolean with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The byte with the given index in the column list.
		 */
		public boolean getBool(int pIndex) {
			try {
				return rs.getBoolean(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next boolean object in the column list.
		 * @return The next boolean object in the column list, possibly null.
		 */
		public boolean nextBoolObj() {
			return getBool(++index);
		}
		

		/** Passes the next value in the column list as a boolean to the
		 * given {@link ByteConsumer}.
		 * @param pConsumer The {@link BooleanConsumer}, that should receive the string.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public Rows nextBoolObj(BooleanConsumer pConsumer) {
			final BooleanConsumer consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getBool(++index));
			return this;
		}

		/** Returns the boolean with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The byte with the given index in the column list.
		 */
		public Boolean getBoolObj(int pIndex) {
			try {
				final boolean b = rs.getBoolean(pIndex);
				if (rs.wasNull()) {
					return null;
				} else {
					return Boolean.valueOf(b);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next double in the column list.
		 * @return The next double in the column list.
		 */
		public double nextDouble() {
			return getFloat(++index);
		}
		

		/** Passes the next value in the column list as a double to the
		 * given {@link DoubleConsumer}.
		 * @param pConsumer The {@link DoubleConsumer}, that should receive the double.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextDouble(@NonNull DoubleConsumer pConsumer) {
			final DoubleConsumer consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getDouble(++index));
			return this;
		}

		/** Returns the double with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The double with the given index in the column list.
		 */
		public double getDouble(int pIndex) {
			try {
				return rs.getDouble(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next double object in the column list.
		 * @return The next double object in the column list, possibly null.
		 */
		public @Nullable Double nextDoubleObj() {
			return getDoubleObj(++index);
		}
		

		/** Passes the next value in the column list as a double object to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) double object.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextDoubleObj(@NonNull Consumer<Double> pConsumer) {
			final Consumer<Double> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getDoubleObj(++index));
			return this;
		}

		/** Returns the double object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The double object with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable Double getDoubleObj(int pIndex) {
			try {
				final double d = rs.getFloat(pIndex);
				if (rs.wasNull()) {
					return null;
				} else {
					return Double.valueOf(d);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next ZonedDateTime in the column list.
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return The next ZonedDateTime in the column list.
		 */
		public @Nullable ZonedDateTime nextZonedDateTime(@Nullable ZoneId pZoneId) {
			return getZonedDateTime(++index, pZoneId);
		}

		/** Passes the next value in the column list as a ZonedDateTime to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) ZonedDateTime object.
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextZonedDateTime(@NonNull Consumer<ZonedDateTime> pConsumer,
				                              @Nullable ZoneId pZoneId) {
			final Consumer<ZonedDateTime> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getZonedDateTime(++index, pZoneId));
			return this;
		}

		/** Returns the {@link ZonedDateTime} object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @param pZoneId The returned objects time zone id. Defaults to UTC.
		 * @return The double object with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable ZonedDateTime getZonedDateTime(int pIndex, @Nullable ZoneId pZoneId) {
			try {
				final Timestamp timestamp = rs.getTimestamp(pIndex);
				if (timestamp == null) {
					return null;
				} else {
					ZoneId zoneId = com.github.jochenw.afw.core.util.Objects.notNull(pZoneId, UTC);
					ZonedDateTime zdtDb = ZonedDateTime.ofInstant(timestamp.toInstant(), dbZoneId);
					ZonedDateTime zdt = zdtDb
							.withZoneSameInstant(zoneId);
					return zdt;
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next LocalDateTime in the column list.
		 * @return The next ZonedDateTime in the column list.
		 */
		public @Nullable LocalDateTime nextLocalDateTime() {
			return getLocalDateTime(++index);
		}

		/** Passes the next value in the column list as a LocalDateTime to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) LocalDateTime object.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextLocalDateTime(@NonNull Consumer<LocalDateTime> pConsumer) {
			final Consumer<LocalDateTime> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getLocalDateTime(++index));
			return this;
		}

		/** Returns the {@link LocalDateTime} object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The double object with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable LocalDateTime getLocalDateTime(int pIndex) {
			try {
				final Timestamp timestamp = rs.getTimestamp(pIndex);
				if (timestamp == null) {
					return null;
				} else {
					return asLocalDateTime(timestamp);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next Timestamp in the column list.
		 * @return The next ZonedDateTime in the column list.
		 */
		public @Nullable Timestamp nextTimestamp() {
			return getTimestamp(++index);
		}

		/** Passes the next value in the column list as a Timestamp to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) Timestamp object.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextTimestamp(@NonNull Consumer<Timestamp> pConsumer) {
			final Consumer<Timestamp> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getTimestamp(++index));
			return this;
		}

		/** Returns the {@link Timestamp} object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The Timestamp object with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable Timestamp getTimestamp(int pIndex) {
			try {
				return rs.getTimestamp(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next Timestamp in the column list.
		 * @return The next ZonedDateTime in the column list.
		 */
		public @Nullable Date nextDate() {
			return getDate(++index);
		}

		/** Passes the next value in the column list as a Date object to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) Date object.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextDate(@NonNull Consumer<Date> pConsumer) {
			final Consumer<Date> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getDate(++index));
			return this;
		}

		/** Returns the {@link Date} object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The Date object with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable Date getDate(int pIndex) {
			try {
				return rs.getDate(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next Time in the column list.
		 * @return The next Time in the column list.
		 */
		public @Nullable Time nextTime() {
			return getTime(++index);
		}

		/** Passes the next value in the column list as a Time object to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) Time object.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextTime(@NonNull Consumer<Time> pConsumer) {
			final Consumer<Time> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getTime(++index));
			return this;
		}

		/** Returns the {@link Time} object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The Time object with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable Time getTime(int pIndex) {
			try {
				return rs.getTime(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}


		/** Returns the next LocalDate in the column list.
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return The next ZonedDateTime in the column list.
		 */
		public @Nullable LocalDate nextLocalDate(@Nullable ZoneId pZoneId) {
			return getLocalDate(++index, pZoneId);
		}

		/** Passes the next value in the column list as a LocalDate to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) LocalDate object.
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextLocalDate(@NonNull Consumer<LocalDate> pConsumer,
				                          @Nullable ZoneId pZoneId) {
			final Consumer<LocalDate> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getLocalDate(++index, pZoneId));
			return this;
		}

		/** Returns the {@link LocalDate} object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return The LocalDate object with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable LocalDate getLocalDate(int pIndex, @Nullable ZoneId pZoneId) {
			try {
				final Date date = rs.getDate(pIndex);
				if (date == null) {
					return null;
				} else {
					return asLocalDate(date);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next LocalTime in the column list.
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return The next LocalTime in the column list.
		 */
		public @Nullable LocalTime nextLocalTime(@Nullable ZoneId pZoneId) {
			return getLocalTime(++index, pZoneId);
		}

		/** Passes the next value in the column list as a LocalTime to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) LocalTime object.
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextLocalTime(@NonNull Consumer<LocalTime> pConsumer,
				                          @Nullable ZoneId pZoneId) {
			final Consumer<LocalTime> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getLocalTime(++index, pZoneId));
			return this;
		}

		/** Returns the {@link LocalTime} object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return The LocalTime object with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable LocalTime getLocalTime(int pIndex, @Nullable ZoneId pZoneId) {
			try {
				final Time time = rs.getTime(pIndex);
				if (time == null) {
					return null;
				} else {
					return asLocalTime(time);
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next byte array in the column list.
		 * @return The next byte array in the column list.
		 */
		public byte[] nextBytes() {
			return getBytes(++index);
		}

		/** Passes the next value in the column list as a byte array to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) byte array.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextBytes(@NonNull Consumer<byte[]> pConsumer) {
			final Consumer<byte[]> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getBytes(++index));
			return this;
		}

		/** Returns the byte array with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The byte array with the given index in the column list,
		 *   possibly null.
		 */
		public byte[] getBytes(int pIndex) {
			try {
				return rs.getBytes(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next {@link InputStream} in the column list.
		 * @return The next {@link InputStream} in the column list.
		 */
		public @Nullable InputStream nextInputStream() {
			return getInputStream(++index);
		}

		/** Passes the next value in the column list as an {@link InputStream} to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) {@link InputStream}.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @NonNull Rows nextInputStream(@NonNull Consumer<InputStream> pConsumer) {
			final Consumer<InputStream> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getInputStream(++index));
			return this;
		}

		/** Returns the byte array with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The byte array with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable InputStream getInputStream(int pIndex) {
			try {
				return rs.getBinaryStream(pIndex);
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}
	}

	/** Creates a new instance of {@link Rows}, which encapsulates the given {@link ResultSet}.
	 * @param pResultSet The {@link ResultSet result set}, which is being encapsulated.
	 * @return The created {@link Rows row}.
	 */
	protected Rows newRows(ResultSet pResultSet) {
		return new Rows(pResultSet);
	}

	/** Applies the given parameter list to the given {@link PreparedStatement}.
	 * @param pStmt The statement, that is being executed.
	 * @param pParams The parameter list, that is being applied.
	 * @throws SQLException Applying the parameter list failed.
	 */
	public void setParameters(@NonNull PreparedStatement pStmt, Object... pParams)
	        throws SQLException {
		if (pParams != null) {
			for (int i = 0;  i < pParams.length;  i++) {
				setParameter(pStmt, i+1, pParams[i]);
			}
		}
	}

	/** Applies the given parameter to the given {@link PreparedStatement},
	 * using the given parameter index (The index is 1-based.)
	 * @param pStmt The statement, that is being executed.
	 * @param pInd The parameter index.
	 * @param pParam The parameter list, that is being applied.
	 * @throws SQLException Applying the parameter failed.
	 */
	public void setParameter(@NonNull PreparedStatement pStmt, int pInd, @Nullable Object pParam)
			throws SQLException {
		if (pParam == null) {
			pStmt.setNull(pInd, Types.NULL);
		} else if (pParam instanceof String) {
			pStmt.setString(pInd, (String) pParam);
		} else if (pParam instanceof Long) {
			try {
				pStmt.setLong(pInd, ((Long) pParam).longValue());
			} catch (SQLException se) {
				throw se;
			}
		} else if (pParam instanceof Integer) {
			pStmt.setInt(pInd, ((Integer) pParam).intValue());
		} else if (pParam instanceof Short) {
			pStmt.setShort(pInd, ((Short) pParam).shortValue());
		} else if (pParam instanceof Byte) {
			pStmt.setByte(pInd, ((Byte) pParam).byteValue());
		} else if (pParam instanceof Boolean) {
			pStmt.setBoolean(pInd, ((Boolean) pParam).booleanValue());
		} else if (pParam instanceof Enum) {
			final Enum<?> en = (Enum<?>) pParam;
			pStmt.setInt(pInd, en.ordinal());
		} else if (pParam instanceof Double) {
			pStmt.setDouble(pInd, ((Double) pParam).doubleValue());
		} else if (pParam instanceof Float) {
			pStmt.setFloat(pInd, ((Float) pParam).floatValue());
		} else if (pParam instanceof ZonedDateTime) {
			final ZonedDateTime zdt = (ZonedDateTime) pParam;
			ZonedDateTime zdtDb = zdt.withZoneSameInstant(getDbZoneId());
			final Timestamp ts = Timestamp.from(zdtDb.toInstant());
			pStmt.setTimestamp(pInd, ts);
		} else if (pParam instanceof LocalDateTime) {
			pStmt.setTimestamp(pInd, asTimestamp((LocalDateTime) pParam));
		} else if (pParam instanceof LocalDate) {
			pStmt.setDate(pInd, asDate((LocalDate) pParam));
		} else if (pParam instanceof LocalTime) {
			pStmt.setTime(pInd, asTime((LocalTime) pParam));
		} else if (pParam instanceof Timestamp) {
			pStmt.setTimestamp(pInd, (Timestamp) pParam);
		} else if (pParam instanceof Date) {
			pStmt.setDate(pInd, (Date) pParam);
		} else if (pParam instanceof Time) {
			pStmt.setTime(pInd, (Time) pParam);
		} else if (pParam instanceof InputStream) {
			pStmt.setBinaryStream(pInd, (InputStream) pParam);
		} else if (pParam instanceof byte[]) {
			pStmt.setBytes(pInd, (byte[]) pParam);
		} else {
			throw new IllegalStateException("Invalid parameter type: " + pParam.getClass().getName());
		}
	}

	/** Converts a local dateTime value from the {@link #getAppZoneId() applications time zone}
	 * to a timestamp, which is suitable for storage into the database.
	 * @param pLocalDateTimeValue A local dateTime value, in the applications time zone.
	 * @return A timestamp, which represents the same local dateTime value, and is suitable for
	 *   storage into the database.
	 */
	public Timestamp asTimestamp(LocalDateTime pLocalDateTimeValue) {
		final ZonedDateTime zdtApp = ZonedDateTime.of(pLocalDateTimeValue, getAppZoneId());
		final ZonedDateTime zdtDb = zdtApp.withZoneSameInstant(getDbZoneId());
		return Timestamp.from(zdtDb.toInstant());
	}

	/** Converts a database timestamp into a local dateTime value in the
	 * {@link #getAppZoneId() applications time zone}.
	 * @param pTimestamp A database timestamp.
	 * @return A local dateTime value, which represents the same timestamp.
	 */
	public LocalDateTime asLocalDateTime(Timestamp pTimestamp) {
		final ZonedDateTime zdtDb = ZonedDateTime.ofInstant(pTimestamp.toInstant(), getDbZoneId());
		final ZonedDateTime zdtApp = zdtDb.withZoneSameInstant(getAppZoneId());
		return zdtApp.toLocalDateTime();
	}

	/** Converts a local date value from the {@link #getAppZoneId() applications time zone}
	 * to a date, which is suitable for storage into the database.
	 * @param pLocalDateValue A local date value, in the applications time zone.
	 * @return A date, which represents the same local dateTime value, and is suitable for
	 *   storage into the database.
	 */
	public Date asDate(LocalDate pLocalDateValue) {
		return Date.valueOf(pLocalDateValue);
	}

	/** Converts a database date into a local date value in the
	 * {@link #getAppZoneId() applications time zone}.
	 * @param pDate A database date.
	 * @return A local dateTime value, which represents the same date.
	 */
	public LocalDate asLocalDate(Date pDate) {
		return pDate.toLocalDate();
	}

	/** Converts a local time value from the {@link #getAppZoneId() applications time zone}
	 * to a time, which is suitable for storage into the database.
	 * @param pLocalTimeValue A local time value, in the applications time zone.
	 * @return A time, which represents the same local time value, and is suitable for
	 *   storage into the database.
	 */
	public Time asTime(LocalTime pLocalTimeValue) {
		final ZonedDateTime zdtApp = ZonedDateTime.of(ZERO_DATE, pLocalTimeValue, getAppZoneId());
		final ZonedDateTime zdtDb = zdtApp.withZoneSameInstant(getDbZoneId());
		return new Time(zdtDb.toInstant().toEpochMilli());
	}
	private static final LocalDate ZERO_DATE = LocalDate.of(2021, 1, 1);

	/** Converts a database time into a local time value in the
	 * {@link #getAppZoneId() applications time zone}.
	 * @param pTime A database time.
	 * @return A local time value, which represents the same date.
	 */
	public LocalTime asLocalTime(Time pTime) {
		final ZonedDateTime zdtDb = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pTime.getTime()), getDbZoneId());
		final ZonedDateTime zdtApp = zdtDb.withZoneSameInstant(getAppZoneId());
		return zdtApp.toLocalTime();
	}

	/** Prepares a {@link JdbcHelper.Executor query executor} with the given SQL
	 * statement, and parameters, and the given dialect.
	 * The query will be executed by invocation of a suitable method on the
	 * {@link JdbcHelper.Executor query executor}.
	 * @param pConnectionSupplier A database connection provider. The
	 * connection, which is returned by the provider, will be closed.
	 * If you need the connection to remain open, use
	 * {@link #query(Connection, Dialect, String, Object...)}.
	 * @param pDialect The SQL dialect, if available, for support in
	 *   error handling.
	 * @param pStatement The SQL statement, which is being executed.
	 * @param pParameters The numbered statement parameters.
	 * @return The created {@link JdbcHelper.Executor query executor}.
	 */
	public Executor query(@NonNull FailableSupplier<Connection,?> pConnectionSupplier,
			              @Nullable Dialect pDialect,
			              @NonNull String pStatement, @Nullable Object... pParameters) {
		return new Executor(this, pDialect, pConnectionSupplier, pStatement, pParameters);
	}

	/** Prepares a {@link JdbcHelper.Executor query executor} with the given SQL
	 * statement, and parameters, and no dialect.
	 * The query will be executed by invocation of a suitable method on the
	 * {@link JdbcHelper.Executor query executor}.
	 * @param pConnectionSupplier A database connection provider. The
	 * connection, which is returned by the provider, will be closed.
	 * If you need the connection to remain open, use
	 * {@link #query(Connection, Dialect, String, Object...)}.
	 * @param pStatement The SQL statement, which is being executed.
	 * @param pParameters The numbered statement parameters.
	 * @return The created {@link JdbcHelper.Executor query executor}.
	 */
	public Executor query(@NonNull FailableSupplier<Connection,?> pConnectionSupplier,
			              @NonNull String pStatement, @Nullable Object... pParameters) {
		return new Executor(this, null, pConnectionSupplier, pStatement, pParameters);
	}

	/** Prepares a {@link JdbcHelper.Executor query executor} with the given SQL
	 * statement, and parameters, and the given dialect.
	 * The query will be executed by invocation of a suitable method on the
	 * {@link JdbcHelper.Executor query executor}.
	 * @param pConnection An open database connection. This method will
	 * <em>not</em> close the connection. If this doesn't suit, you may
	 * use {@link #query(Functions.FailableSupplier, Dialect, String, Object...)}
	 * instead.
	 * @param pDialect The SQL dialect, if available, for support in
	 *   error handling.
	 * @param pStatement The SQL statement, which is being executed.
	 * @param pParameters The numbered statement parameters.
	 * @return The created {@link JdbcHelper.Executor query executor}.
	 */
	public Executor query(@NonNull Connection pConnection,
					      @Nullable Dialect pDialect,
			              @NonNull String pStatement, @Nullable Object... pParameters) {
		final Connection conn = uncloseableConnection(pConnection);
		return new Executor(this, null, () -> conn, pStatement, pParameters);
	}

	/** Prepares a {@link JdbcHelper.Executor query executor} with the given SQL
	 * statement, and parameters, and no dialect.
	 * The query will be executed by invocation of a suitable method on the
	 * {@link JdbcHelper.Executor query executor}.
	 * @param pConnection An open database connection. This method will
	 * <em>not</em> close the connection. If this doesn't suit, you may
	 * use {@link #query(Functions.FailableSupplier, Dialect, String, Object...)}
	 * instead.
	 * @param pStatement The SQL statement, which is being executed.
	 * @param pParameters The numbered statement parameters.
	 * @return The created {@link JdbcHelper.Executor query executor}.
	 */
	public Executor query(@NonNull Connection pConnection,
			              @NonNull String pStatement, @Nullable Object... pParameters) {
		final Connection conn = uncloseableConnection(pConnection);
		return new Executor(this, null, () -> conn, pStatement, pParameters);
	}

	/** Creates a new connection object, which acts as a wrapper for the
	 * given database connection. However, the created connection has a
	 * do-nothing {@link Connection#close()} method. In other words:
	 * Invoking {@link Connection#close()} on the created connection
	 * object will not close the wrapped database connection.
	 * @param pConnection The database connection, which is being
	 * wrapped.
	 * @return The created, uncloseable, connection object.
	 */
	public Connection uncloseableConnection(Connection pConnection) {
		final InvocationHandler ih = new InvocationHandler() {
			@Override
			public Object invoke(Object pProxy, Method pMethod, Object[] pArgs) throws Throwable {
				if ("close".equals(pMethod.getName())) {
					return null; // Do nothing.
				} else {
					return pMethod.invoke(pConnection, pArgs);
				}
			}
		};
		final Class<?>[] classes = (Class<?>[]) Array.newInstance(Class.class, 1);
		classes[0] = Connection.class;
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return (Connection) Proxy.newProxyInstance(cl, classes, ih);
	}
}
