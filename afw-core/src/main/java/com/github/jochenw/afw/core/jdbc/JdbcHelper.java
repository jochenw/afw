package com.github.jochenw.afw.core.jdbc;

import java.io.InputStream;
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
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

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
	private static final ZoneId UTC = ZoneId.of("GMT");
	private @Nonnull ZoneId dbZoneId = UTC;
	private @Nonnull ZoneId appZoneId = ZoneId.systemDefault();

	/** Returns the database time zone id.
	 * @return The database time zone id.
	 */
	public @Nonnull ZoneId getDbZoneId() {
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
	public @Nonnull ZoneId getAppZoneId() {
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
		public Executor(@Nonnull JdbcHelper pHelper,
				        @Nullable Dialect pDialect,
				        @Nonnull FailableSupplier<Connection,?> pConnProvider,
				        @Nonnull String pQuery,
				        @Nullable Object... pParameters) {
			helper = Objects.requireNonNull(pHelper, "JdbcHelper");
			dialect = pDialect;
			connectionProvider = Objects.requireNonNull(pConnProvider, "Connection Provider");
			query = Objects.requireNonNull(pQuery, "Query");
			parameters = pParameters;
		}

		/**
		 * Creates a {@link Callable}, which prepares, and executes, the query,
		 * with the parameters applied. The {@link ResultSet}, that is created,
		 * is being passed to the given function, that processes the result,
		 * and returns the result object. 
		 * @param <O> Type of the result object.
		 * @param pFunction The function, which is being invoked to 
		 *   the {@link ResultSet}.
		 * @return The result object, that has been obtained by invoking the function.
		 */
		public @Nonnull <O> Callable<O> withResultSet(@Nonnull FailableFunction<ResultSet,O,?> pFunction) {
			final @Nonnull FailableFunction<ResultSet,O,?> function = Objects.requireNonNull(pFunction, "Function");
			return () -> {
				try (Connection conn = connectionProvider.get();
					 PreparedStatement stmt = conn.prepareStatement(query)) {
					helper.setParameters(stmt, parameters);
					try (ResultSet rs = stmt.executeQuery()) {
						return function.apply(rs);
					}
				} catch (Throwable t) {
					throw handleError(t);
				}
			};
		}

		/**
		 * Creates a {@link Runnable}, which prepares, and executes, the query,
		 * with the parameters applied. The {@link ResultSet}, that is created,
		 * is being passed to the given consumer, that processes the result,
		 * without any result object.
		 * @param pConsumer The consumer, which is being invoked to process
		 *   the {@link ResultSet}.
		 * @return The result object, that has been obtained by invoking the function.
		 */
		public @Nonnull Runnable withResultSet(@Nonnull FailableConsumer<ResultSet,?> pConsumer) {
			final @Nonnull FailableConsumer<ResultSet,?> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			return () -> {
				try (Connection conn = connectionProvider.get();
					 PreparedStatement stmt = conn.prepareStatement(query)) {
					helper.setParameters(stmt, parameters);
					try (ResultSet rs = stmt.executeQuery()) {
						consumer.accept(rs);
					}
				} catch (Throwable t) {
					throw handleError(t);
				}
			};
		}

		/**
		 * Creates a {@link Callable}, which prepares, and executes, the query,
		 * with the parameters applied. The {@link ResultSet}, that is created,
		 * is being passed as a {@link Rows} to the given function, which processes
		 * the result, and returns the result object.
		 * @param <O> Type of the result object.
		 * @param pFunction The function, which is being invoked to 
		 *   the {@link ResultSet}.
		 * @return The result object, that has been obtained by invoking the function.
		 */
		public @Nonnull <O> Callable<O> withRows(@Nonnull FailableFunction<Rows,O,?> pFunction) {
			final @Nonnull FailableFunction<Rows,O,?> function = Objects.requireNonNull(pFunction, "Function");
			return () -> {
				try (Connection conn = connectionProvider.get();
					 PreparedStatement stmt = conn.prepareStatement(query)) {
					helper.setParameters(stmt, parameters);
					try (ResultSet rs = stmt.executeQuery()) {
						return function.apply(helper.newRows(rs));
					}
				} catch (Throwable t) {
					throw handleError(t);
				}
			};
		}

		/**
		 * Creates a {@link Runnable}, which prepares, and executes, the query,
		 * with the parameters applied. The {@link ResultSet}, that is created,
		 * is being passed as a {@link Rows} object to the given consumer, which processes
		 * the result. No result object is being produced.
		 * @param pConsumer The function, which is being invoked to 
		 *   the {@link ResultSet}.
		 * @return The result object, that has been obtained by invoking the function.
		 */
		public @Nonnull Runnable withRows(@Nonnull FailableConsumer<Rows,?> pConsumer) {
			final @Nonnull FailableConsumer<Rows,?> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			return () -> {
				try (Connection conn = connectionProvider.get();
					 PreparedStatement stmt = conn.prepareStatement(query)) {
					helper.setParameters(stmt, parameters);
					try (ResultSet rs = stmt.executeQuery()) {
						while (rs.next()) {
							consumer.accept(helper.newRows(rs));
						}
					}
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};
		}

		/**
		 * Creates a {@link Callable}, which prepares, and executes, the query,
		 * with the parameters applied. The query is supposed to return a single
		 * row, and a single column. The object from the single row, and single
		 * column is returned as a result object.
		 * 
		 * A typical use case would be a query like "SELECT COUNT(*) ...". For
		 * such a query, we know, that it will return a single integer object.
		 * @param <O> Type of the result object.
		 * @return The result object, that has been obtained from the query result.
		 */
		public @Nonnull <O> Callable<O> singleObject() {
			return () -> {
				try (Connection conn = connectionProvider.get();
					 PreparedStatement stmt = conn.prepareStatement(query)) {
					helper.setParameters(stmt, parameters);
					try (ResultSet rs = stmt.executeQuery()) {
						if (!rs.next()) {
							throw new IllegalStateException("The query did not return any result.");
						}
						final Rows rows = helper.newRows(rs);
						final O o = rows.nextObject();
						return o;
					}
				} catch (Throwable t) {
					throw handleError(t);
				}
			};
		}

		/**
		 * Executes the configured query, executes it, and returns the number
		 * of affected rows.
		 * @return The number of rows, that have been affected by the query.
		 */
		public int affectedRows() {
			try (Connection conn = connectionProvider.get();
					PreparedStatement stmt = conn.prepareStatement(query)) {
				helper.setParameters(stmt, parameters);
				return stmt.executeUpdate();
			} catch (Throwable t) {
				throw handleError(t);
			}
		}

		/**
		 * Executes the configured query. No result is being returned.
		 */
		public void run() {
			affectedRows();
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
		 * @return The next object in the column list.
		 */
		public <O> O nextObject() {
			return getObject(++index);
		}

		/** Returns the object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
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
		public @Nonnull Rows nextDouble(@Nonnull DoubleConsumer pConsumer) {
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
		public @Nonnull Rows nextDoubleObj(@Nonnull Consumer<Double> pConsumer) {
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
		public @Nonnull Rows nextZonedDateTime(@Nonnull Consumer<ZonedDateTime> pConsumer,
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
		public @Nonnull Rows nextLocalDateTime(@Nonnull Consumer<LocalDateTime> pConsumer) {
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
		public @Nonnull Rows nextTimestamp(@Nonnull Consumer<Timestamp> pConsumer) {
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
		public @Nonnull Rows nextDate(@Nonnull Consumer<Date> pConsumer) {
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
		public @Nonnull Rows nextTime(@Nonnull Consumer<Time> pConsumer) {
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
		public @Nonnull Rows nextLocalDate(@Nonnull Consumer<LocalDate> pConsumer,
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
		public @Nonnull Rows nextLocalTime(@Nonnull Consumer<LocalTime> pConsumer,
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
		public @Nullable byte[] nextBytes() {
			return getBytes(++index);
		}

		/** Passes the next value in the column list as a byte array to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) byte array.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @Nonnull Rows nextBytes(@Nonnull Consumer<byte[]> pConsumer) {
			final Consumer<byte[]> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getBytes(++index));
			return this;
		}

		/** Returns the byte array with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @return The byte array with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable byte[] getBytes(int pIndex) {
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
		public @Nonnull Rows nextInputStream(@Nonnull Consumer<InputStream> pConsumer) {
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
	public void setParameters(@Nonnull PreparedStatement pStmt, Object... pParams)
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
	public void setParameter(@Nonnull PreparedStatement pStmt, int pInd, @Nullable Object pParam)
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
		final ZonedDateTime zdtApp = ZonedDateTime.of(pLocalDateValue, ZERO_TIME, getAppZoneId());
		final ZonedDateTime zdtDb = zdtApp.withZoneSameInstant(getDbZoneId());
		return new Date(zdtDb.toInstant().toEpochMilli());
	}
	private static final LocalTime ZERO_TIME = LocalTime.of(0, 0, 0, 0);

	/** Converts a database date into a local date value in the
	 * {@link #getAppZoneId() applications time zone}.
	 * @param pDate A database date.
	 * @return A local dateTime value, which represents the same date.
	 */
	public LocalDate asLocalDate(Date pDate) {
		final ZonedDateTime zdtDb = ZonedDateTime.ofInstant(Instant.ofEpochMilli(pDate.getTime()), getDbZoneId());
		final ZonedDateTime zdtApp = zdtDb.withZoneSameInstant(getAppZoneId());
		return zdtApp.toLocalDate();
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
	 * statement, and parameters.
	 * The query will be executed by invocation of a suitable method on the
	 * {@link JdbcHelper.Executor query executor}.
	 * @param pConnectionSupplier A database connection provider.
	 * @param pDialect The SQL dialect, if available, for support in
	 *   error handling.
	 * @param pStatement The SQL statement, which is being executed.
	 * @param pParameters The numbered statement parameters.
	 * @return The created {@link JdbcHelper.Executor query executor}.
	 */
	public Executor query(@Nonnull FailableSupplier<Connection,?> pConnectionSupplier,
			              @Nullable Dialect pDialect,
			              @Nonnull String pStatement, @Nullable Object... pParameters) {
		return new Executor(this, pDialect, pConnectionSupplier, pStatement, pParameters);
	}
}
