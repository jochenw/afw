package com.github.jochenw.afw.core.jdbc;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.BooleanConsumer;
import com.github.jochenw.afw.core.util.Functions.ByteConsumer;
import com.github.jochenw.afw.core.util.Functions.DoubleConsumer;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Functions.FloatConsumer;
import com.github.jochenw.afw.core.util.Functions.ShortConsumer;

/** A helper object for working with JDBC connections.
 */
public class JdbcHelper {
	private static final ZoneId UTC = ZoneId.of("GMT");
	private ZoneId dbZoneId = UTC;

	/** Returns the database time zone id.
	 * @return The database time zone id.
	 */
	public @Nonnull ZoneId getDbZoneId() {
		return Objects.requireNonNull(dbZoneId, "DbZoneId");
	}

	/** Sets the database time zone id.
	 * @param pDbZoneId The database time zone id.
	 */
	public @Inject void setDbZoneId(@Named(value="db") ZoneId pDbZoneId) {
		dbZoneId = Objects.requireNonNull(pDbZoneId, "DbZoneId");
	}

	/** Abstract representation of a row in the {@link ResultSet}. Allows a code style, that is
	 * independent from JDBC.
	 */
	public class Row {
		private final ResultSet rs;
		private int index;

		/** Creates a new instance with the given result set.
		 * @param pResultSet The result set, which is encapsulated by this object.
		 */
		public Row(ResultSet pResultSet) {
			rs = pResultSet;
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
		public Row nextStr(Consumer<String> pConsumer) {
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
		public Row nextLong(LongConsumer pConsumer) {
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
		public Row nextLongObj(Consumer<Long> pConsumer) {
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
		public Row nextInt(IntConsumer pConsumer) {
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
		public Row nextIntObj(Consumer<Integer> pConsumer) {
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
		public Row nextShort(ShortConsumer pConsumer) {
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
		public Row nextShortObj(Consumer<Short> pConsumer) {
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
		public Row nextByte(ByteConsumer pConsumer) {
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
		public Row nextByteObj(Consumer<Byte> pConsumer) {
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
		public Row nextFloat(FloatConsumer pConsumer) {
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
		public Row nextFloatObj(Consumer<Float> pConsumer) {
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
		public Row nextBool(BooleanConsumer pConsumer) {
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
		public Row nextBoolObj(BooleanConsumer pConsumer) {
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
		public @Nonnull Row nextDouble(@Nonnull DoubleConsumer pConsumer) {
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
		public @Nonnull Row nextDoubleObj(@Nonnull Consumer<Double> pConsumer) {
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
		public @Nonnull Row nextZonedDateTime(@Nonnull Consumer<ZonedDateTime> pConsumer,
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
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return The next ZonedDateTime in the column list.
		 */
		public @Nullable LocalDateTime nextLocalDateTime(@Nullable ZoneId pZoneId) {
			return getLocalDateTime(++index, pZoneId);
		}

		/** Passes the next value in the column list as a LocalDateTime to the
		 * given {@link Consumer}.
		 * @param pConsumer The {@link Consumer}, that should receive the
		 *   (possibly null) LocalDateTime object.
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
		 * @return This row object. (Allows for builder-style code.)
		 */
		public @Nonnull Row nextLocalDateTime(@Nonnull Consumer<LocalDateTime> pConsumer,
				                              @Nullable ZoneId pZoneId) {
			final Consumer<LocalDateTime> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			consumer.accept(getLocalDateTime(++index, pZoneId));
			return this;
		}

		/** Returns the {@link LocalDateTime} object with the given index in the column list.
		 * @param pIndex A JDBC-style column index (1-based).
		 * @param pZoneId The returned objects time zone id. Defaults to UTC.
		 * @return The double object with the given index in the column list,
		 *   possibly null.
		 */
		public @Nullable LocalDateTime getLocalDateTime(int pIndex, @Nullable ZoneId pZoneId) {
			try {
				final Timestamp timestamp = rs.getTimestamp(pIndex);
				if (timestamp == null) {
					return null;
				} else {
					return asZonedDateTime(timestamp, pZoneId).toLocalDateTime();
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		protected ZonedDateTime asZonedDateTime(java.util.Date pDate, ZoneId pZoneId) {
			ZoneId zoneId = com.github.jochenw.afw.core.util.Objects.notNull(pZoneId, UTC);
			return ZonedDateTime.ofInstant(pDate.toInstant(), dbZoneId)
					.withZoneSameInstant(zoneId);
		}

		/** Returns the next Timestamp in the column list.
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
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
		public @Nonnull Row nextTimestamp(@Nonnull Consumer<Timestamp> pConsumer) {
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
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
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
		public @Nonnull Row nextDate(@Nonnull Consumer<Date> pConsumer) {
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
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
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
		public @Nonnull Row nextTime(@Nonnull Consumer<Time> pConsumer) {
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
		public @Nonnull Row nextLocalDate(@Nonnull Consumer<LocalDate> pConsumer,
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
					ZoneId zoneId = com.github.jochenw.afw.core.util.Objects.notNull(pZoneId, UTC);
					final ZonedDateTime zdtDb = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), dbZoneId);
					final ZonedDateTime zdt = zdtDb.withZoneSameInstant(zoneId);
					return zdt.toLocalDate();
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
		public @Nonnull Row nextLocalTime(@Nonnull Consumer<LocalTime> pConsumer,
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
					final ZoneId zoneId = com.github.jochenw.afw.core.util.Objects.notNull(pZoneId, UTC);
					final ZonedDateTime zdtDb = ZonedDateTime.ofInstant(Instant.ofEpochMilli(time.getTime()), dbZoneId);
					final ZonedDateTime zdt = zdtDb.withZoneSameInstant(zoneId);
					return zdt.toLocalTime();
				}
			} catch (SQLException e) {
				throw Exceptions.show(e);
			}
		}

		/** Returns the next byte array in the column list.
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
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
		public @Nonnull Row nextBytes(@Nonnull Consumer<byte[]> pConsumer) {
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
		 * @param pZoneId The requested objects time zone id. Defaults to UTC.
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
		public @Nonnull Row nextInputStream(@Nonnull Consumer<InputStream> pConsumer) {
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

	/** Called to execute an SQL query, and to process the result set.
	 * @param pConnection The database connection, which is being used.
	 * @param pTracker A tracker object, which is responsible for closing resource objects.
	 * @param pSql The SQL statement, that is being executed. The SQL statement may contain
	 *   parameter indicators ('?'), that are given by the parameter {@code pParameters}.
	 * @param pConsumer An action object, that is being invoked to process the results.
	 * @param pParameters The statement parameters.
	 * @throws SQLException Executing the query has failed.
	 */
	public void executeQuery(@Nonnull Connection pConnection, @Nonnull Consumer<AutoCloseable> pTracker,
			                 @Nonnull String pSql, @Nonnull FailableConsumer<JdbcHelper.Row,?> pConsumer,
			                 @Nullable Object... pParameters) throws SQLException {
		PreparedStatement stmt = pConnection.prepareStatement(pSql);
		pTracker.accept(stmt);
		setParameters(stmt, pParameters);
		final ResultSet rs = stmt.executeQuery();
		pTracker.accept(rs);
		final Row row = new Row(rs);
		while (row.next()) {
			try {
				pConsumer.accept(row);
			} catch (Throwable t) {
				throw Exceptions.show(t, SQLException.class);
			}
		}
	}

	/** Called to execute an SQL update statement, and to return the number of affected rows.
	 * @param pConnection The database connection, which is being used.
	 * @param pTracker A tracker object, which is responsible for closing resource objects.
	 * @param pSql The SQL statement, that is being executed. The SQL statement may contain
	 *   parameter indicators ('?'), that are given by the parameter {@code pParameters}.
	 * @param pParameters The statement parameters.
	 * @return The number of affected rows, if applicable, or 0.
	 * @throws SQLException Executing the query has failed.
	 */
	public int executeUpdate(@Nonnull Connection pConnection, @Nonnull Consumer<AutoCloseable> pTracker,
			                 @Nonnull String pSql, 
			                 @Nullable Object... pParameters) throws SQLException {
		PreparedStatement stmt = pConnection.prepareStatement(pSql);
		pTracker.accept(stmt);
		setParameters(stmt, pParameters);
		return stmt.executeUpdate();
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
			final LocalDateTime ldt = (LocalDateTime) pParam;
			pStmt.setTimestamp(pInd, Timestamp.valueOf(ldt));
		} else if (pParam instanceof LocalDate) {
			final LocalDate ld = (LocalDate) pParam;
			pStmt.setDate(pInd, Date.valueOf(ld));
		} else if (pParam instanceof LocalTime) {
			final LocalTime lt = (LocalTime) pParam;
			pStmt.setTime(pInd, Time.valueOf(lt));
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
}