package com.github.jochenw.afw.core.jdbc;

import static org.junit.Assert.*;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import org.junit.Test;

import com.github.jochenw.afw.core.components.Application;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.Scopes;
import com.github.jochenw.afw.core.inject.guice.GuiceComponentFactoryBuilder;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.MutableBoolean;
import com.github.jochenw.afw.core.util.Streams;

/** Test suite for the {@link JdbcHelper}.
 */
public class JdbcHelperTest {
	private Application getApplication(Module pModule) {
		return new Application((b) -> {
			b.bind(JdbcHelper.class).in(Scopes.SINGLETON);
			b.bind(Worker.class).in(Scopes.SINGLETON);
			b.bind(ZoneId.class).toInstance(ZoneId.of("Europe/Berlin"));
			b.bind(ZoneId.class, "db").toInstance(ZoneId.of("GMT"));
			b.bind(Dialect.class, "h2").to(H2Dialect.class);
			b.bind(ConnectionProvider.class).to(DefaultConnectionProvider.class).in(Scopes.SINGLETON);
			if (pModule != null) {
				pModule.configure(b);
			}
		}, () -> new SimpleLogFactory(System.out), () -> {
			final String uri = "com/github/jochenw/afw/core/jdbc/db-test.properties";
			final URL url = Thread.currentThread().getContextClassLoader().getResource(uri);
			if (url == null) {
				throw new IllegalStateException("Unable to locate resource: " + uri);
			}
			final Properties props = Streams.load(url);
			return new DefaultPropertyFactory(props);
		}) {

			@Override
			protected ComponentFactoryBuilder<?> newComponentFactoryBuilder() {
				return new GuiceComponentFactoryBuilder();
			}
			
		};
	}

	/** Test for {@link Worker.Context#getConnection()}.
	 */
	@Test
	public void testOpenConnection() {
		final Application application = getApplication(null);
		final Worker worker = application.getComponentFactory().requireInstance(Worker.class);
		final MutableBoolean success = new MutableBoolean();
		worker.run((c) -> {
			assertNotNull(c);
			final Connection conn = c.getConnection();
			assertNotNull(conn);
			assertNotNull(conn.getMetaData());
			success.set();
		});
		assertTrue(success.isSet());
	}

	/** Test for {@link Worker.Context#executeUpdate()}.
	 */
	@Test
	public void testCreateTable() {
		final String sqlCreate = "CREATE TABLE table_one ("
				+ " id BIGINT NOT NULL PRIMARY KEY,"
				+ " valid TINYINT NOT NULL"
				+ ");";
		final String sqlDrop = "DROP TABLE table_one";
		final Application application = getApplication(null);
		final Worker worker = application.getComponentFactory().requireInstance(Worker.class);
		final MutableBoolean success = new MutableBoolean();
		worker.run((c) -> {
			try {
				c.executeUpdate(sqlDrop);
			} catch (Throwable t) {
				final SQLException cause = Exceptions.getCause(t, SQLException.class);
				if (cause != null) {
					final Dialect dialect = c.getDialect();
					if (!dialect.isDroppedTableDoesnExistError(cause)) {
						throw Exceptions.show(t);
					}
				} else {
					throw Exceptions.show(t);
				}
			}
			final int affectedRows = c.executeUpdate(sqlCreate);
			assertEquals(0, affectedRows);
			success.set();
		});
		assertTrue(success.isSet());
	}

	/** Test for INSERT, and SELECT.
	 */
	@Test
	public void testInsertAndSelect() {
		final String sqlCreate = "CREATE TABLE table_two ("
				+ " id BIGINT NOT NULL PRIMARY KEY,"
				+ " tinyIntColumn TINYINT,"
				+ " smallIntColumn SMALLINT,"
				+ " intColumn INTEGER,"
				+ " bigIntColumn BIGINT,"
				+ " varCharColumn VARCHAR(64),"
				+ " varBinaryColumn VARBINARY(64),"
				+ " timeStampColumn TIMESTAMP,"
				+ " dateColumn DATE,"
				+ " timeColumn TIME,"
				+ " zonedDateTimeColumn TIMESTAMP,"
				+ " localDateTimeColumn TIMESTAMP,"
				+ " localDateColumn DATE,"
				+ " localTimeColumn TIME"
				+ ");";
		final String sqlDrop = "DROP TABLE table_two";
		final Application application = getApplication(null);
		final Worker worker = application.getComponentFactory().requireInstance(Worker.class);
		final MutableBoolean success = new MutableBoolean();
		worker.run((c) -> {
			try {
				c.executeUpdate(sqlDrop);
			} catch (Throwable t) {
				final SQLException cause = Exceptions.getCause(t, SQLException.class);
				if (cause != null) {
					final Dialect dialect = c.getDialect();
					if (!dialect.isDroppedTableDoesnExistError(cause)) {
						throw Exceptions.show(t);
					}
				} else {
					throw Exceptions.show(t);
				}
			}
			final int affectedRowsForCreate = c.executeUpdate(sqlCreate);
			assertEquals(0, affectedRowsForCreate);
			final byte byteColumnValue = (byte) 31;
			final short shortColumnValue = (short) 42;
			final int intColumnValue = 53;
			final long bigIntColumnValue = Integer.MAX_VALUE + 1;
			final String varCharColumnValue = "FooBar\u00DC\u00D6";
			final byte[] varBinaryColumnValue = varCharColumnValue.getBytes(StandardCharsets.UTF_8);
			final ZoneId zoneId = ZoneId.of("Europe/Berlin");
			final ZonedDateTime zonedDateTimeValue = ZonedDateTime.of(2021, 11, 21, 12, 56, 0, 0, zoneId);
			final Timestamp timeStampColumnValue = Timestamp.valueOf(zonedDateTimeValue.toLocalDateTime());
			final Date dateColumnValue = Date.valueOf(zonedDateTimeValue.toLocalDate());
			final Time timeColumnValue = Time.valueOf(zonedDateTimeValue.toLocalTime());
			final ZonedDateTime zonedDateTimeColumnValue = zonedDateTimeValue;
			final LocalDateTime localDateTimeColumnValue = zonedDateTimeValue.toLocalDateTime();
			final LocalDate localDateColumnValue = zonedDateTimeValue.toLocalDate();
			final LocalTime localTimeColumnValue = zonedDateTimeValue.toLocalTime();
			c.executeUpdate("INSERT INTO table_two (id, tinyIntColumn, smallIntColumn,"
			        + " intColumn, bigIntColumn, varCharColumn, varBinaryColumn,"
			        + " timeStampColumn, dateColumn, timeColumn, zonedDateTimeColumn,"
			        + " localDateTimeColumn, localDateColumn, localTimeColumn) VALUES"
			        + " (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
			        Long.valueOf(1), byteColumnValue, shortColumnValue, intColumnValue,
			        bigIntColumnValue, varCharColumnValue, varBinaryColumnValue,
			        timeStampColumnValue, dateColumnValue, timeColumnValue,
			        zonedDateTimeColumnValue, localDateTimeColumnValue, localDateColumnValue,
			        localTimeColumnValue);
			c.executeUpdate("INSERT INTO table_two (id) VALUES"
			        + " (?)",
			        Long.valueOf(2));
			c.executeQuery("SELECT * FROM table_two WHERE id=?", (row) -> {
				assertEquals(1, row.nextLong());
				assertEquals(byteColumnValue, row.nextByte());
				assertEquals(shortColumnValue, row.nextShort());
				assertEquals(intColumnValue, row.nextInt());
				assertEquals(bigIntColumnValue, row.nextLong());
				assertEquals(varCharColumnValue, row.nextStr());
				assertArrayEquals(varBinaryColumnValue, row.nextBytes());
				assertEquals(timeStampColumnValue, row.nextTimestamp());
				assertEquals(dateColumnValue, row.nextDate());
				assertEquals(timeColumnValue, row.nextTime());
				assertEquals(zonedDateTimeColumnValue, row.nextZonedDateTime(zoneId));
				assertEquals(localDateTimeColumnValue, row.nextLocalDateTime(zoneId));
				assertEquals(localDateColumnValue, row.nextLocalDate(zoneId));
				assertEquals(localTimeColumnValue, row.nextLocalTime(zoneId));
				row.reset();
				row
				  .nextLongObj((l) -> assertEquals(1, l.longValue()))
				  .nextByte((b) -> assertEquals(byteColumnValue, b))
				  .nextShort((s) -> assertEquals(shortColumnValue, s))
				  .nextInt((i) -> assertEquals(intColumnValue, i))
				  .nextLong((l) -> assertEquals(bigIntColumnValue, l))
				  .nextStr((s) -> assertEquals(varCharColumnValue, s))
				  .nextBytes((b) -> assertArrayEquals(varBinaryColumnValue, b))
				  .nextTimestamp((t) -> assertEquals(timeStampColumnValue, t))
				  .nextDate((d) -> assertEquals(dateColumnValue, d))
				  .nextTime((t) -> assertEquals(timeColumnValue, t))
				  .nextZonedDateTime((z) -> assertEquals(zonedDateTimeColumnValue, z), zoneId)
				  .nextLocalDateTime((l) -> assertEquals(localDateTimeColumnValue, l), zoneId)
				  .nextLocalDate((l) -> assertEquals(localDateColumnValue, l), zoneId)
				  .nextLocalTime((l) -> assertEquals(localTimeColumnValue, l), zoneId);
			}, Long.valueOf(1));
			c.executeQuery("SELECT * FROM table_two WHERE id=?", (row) -> {
				assertEquals(2, row.nextLong());
				assertNull(row.nextByteObj());
				assertNull(row.nextShortObj());
				assertNull(row.nextIntObj());
				assertNull(row.nextLongObj());
				assertNull(row.nextStr());
				assertNull(row.nextBytes());
				assertNull(row.nextTimestamp());
				assertNull(row.nextDate());
				assertNull(row.nextTime());
				assertNull(row.nextZonedDateTime(zoneId));
				assertNull(row.nextLocalDateTime(zoneId));
				assertNull(row.nextLocalDate(zoneId));
				assertNull(row.nextLocalTime(zoneId));
				row.reset();
				row
				  .nextLongObj((l) -> assertEquals(2, l.longValue()))
				  .nextByteObj((b) -> assertNull(b))
				  .nextShortObj((s) -> assertNull(s))
				  .nextIntObj((i) -> assertNull(i))
				  .nextLongObj((l) -> assertNull(l))
				  .nextStr((s) -> assertNull(s))
				  .nextBytes((b) -> assertNull(b))
				  .nextTimestamp((t) -> assertNull(t))
				  .nextDate((d) -> assertNull(d))
				  .nextTime((t) -> assertNull(t))
				  .nextZonedDateTime((z) -> assertNull(z), zoneId)
				  .nextLocalDateTime((l) -> assertNull(l), zoneId)
				  .nextLocalDate((l) -> assertNull(l), zoneId)
				  .nextLocalTime((l) -> assertNull(l), zoneId);
			}, Long.valueOf(2));
			success.set();
		});
		assertTrue(success.isSet());
	}
}