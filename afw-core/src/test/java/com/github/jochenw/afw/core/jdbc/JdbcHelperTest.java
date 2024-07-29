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
import java.util.Properties;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.MutableBoolean;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.di.api.Application;
import com.github.jochenw.afw.di.api.Module;
import com.github.jochenw.afw.di.api.Scopes;


/** Test suite for the {@link JdbcHelper}.
 */
public class JdbcHelperTest {
	private static final @NonNull ZoneId ZONEID_EUROPE_BERLIN = Objects.requireNonNull(ZoneId.of("Europe/Berlin"));
	private static final @NonNull ZoneId ZONEID_GMT = Objects.requireNonNull(ZoneId.of("GMT"));

	private Application getApplication(Module pModule) {
		return Application.of((b) -> {
			b.bind(JdbcHelper.class).in(Scopes.SINGLETON);
			b.bind(Worker.class).in(Scopes.SINGLETON);
			b.bind(ZoneId.class).toInstance(ZONEID_EUROPE_BERLIN);
			b.bind(ZoneId.class, "db").toInstance(ZONEID_GMT);
			b.bind(Dialect.class, "h2").to(H2Dialect.class);
			b.bind(ConnectionProvider.class).to(DefaultConnectionProvider.class).in(Scopes.SINGLETON);
			if (pModule != null) {
				pModule.configure(b);
			}
			b.bind(ILogFactory.class).toInstance(SimpleLogFactory.ofSystemOut());
			b.bind(IPropertyFactory.class).toSupplier(() -> {
				final String uri = "com/github/jochenw/afw/core/jdbc/db-test.properties";
				final URL url = JdbcHelperTest.class.getResource("db-test.properties");
				if (url == null) {
					throw new IllegalStateException("Unable to locate resource: " + uri);
				}
				final Properties props = Streams.load(url);
				return new DefaultPropertyFactory(props);
			});
		});
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

	/** Test for conversion of local time objects to database objects, and back.
	 */
	@Test
	public void testTimeConversions() {
		final Application application = getApplication(null);
		final Worker worker = application.getComponentFactory().requireInstance(Worker.class);
		final JdbcHelper helper = worker.getJdbcHelper();
		final ZoneId zoneId = ZoneId.of("Europe/Berlin");
		final ZonedDateTime expectedZonedDateTimeValue = ZonedDateTime.of(2021, 11, 21, 12, 56, 0, 0, zoneId);
		final LocalDateTime expectedLocalDateTimeValue = expectedZonedDateTimeValue.toLocalDateTime();
		final Timestamp timeStamp = helper.asTimestamp(expectedLocalDateTimeValue);
		final LocalDateTime actualLocalDateTimeValue = helper.asLocalDateTime(timeStamp);
		assertEquals(expectedLocalDateTimeValue, actualLocalDateTimeValue);
		final LocalDate expectedLocalDate = expectedLocalDateTimeValue.toLocalDate();
		final Date date = helper.asDate(expectedLocalDate);
		final LocalDate actualLocalDate = helper.asLocalDate(date);
		assertEquals(expectedLocalDate, actualLocalDate);
		final LocalTime expectedLocalTime = expectedLocalDateTimeValue.toLocalTime();
		final Time time = helper.asTime(expectedLocalTime);
		final LocalTime actualLocalTime = helper.asLocalTime(time);
		assertEquals(expectedLocalTime, actualLocalTime);
	}
	
	/** Test for {@link Worker.Context#query(String, Object[])}.
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
				c.query(sqlDrop).run();
			} catch (DroppedTableDoesntExistException e) {
				// Ignore this.
			}
			final int affectedRows = c.query(sqlCreate).affectedRows();
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
				c.query(sqlDrop).run();
			} catch (DroppedTableDoesntExistException dtde) {
				// Ignore this.
			}
			final int affectedRowsForCreate = c.query(sqlCreate).affectedRows();
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
			c.query("INSERT INTO table_two (id, tinyIntColumn, smallIntColumn,"
			        + " intColumn, bigIntColumn, varCharColumn, varBinaryColumn,"
			        + " timeStampColumn, dateColumn, timeColumn, zonedDateTimeColumn,"
			        + " localDateTimeColumn, localDateColumn, localTimeColumn) VALUES"
			        + " (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
			        Long.valueOf(1), Byte.valueOf(byteColumnValue),
			        Short.valueOf(shortColumnValue), Integer.valueOf(intColumnValue),
			        Long.valueOf(bigIntColumnValue), varCharColumnValue, varBinaryColumnValue,
			        timeStampColumnValue, dateColumnValue, timeColumnValue,
			        zonedDateTimeColumnValue, localDateTimeColumnValue, localDateColumnValue,
			        localTimeColumnValue).run();
			c.query("INSERT INTO table_two (id) VALUES (?)",
			        Long.valueOf(2)).run();
			c.query("SELECT * FROM table_two WHERE id=?", Long.valueOf(1))
			    .withRows((row) -> {
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
				assertEquals(localDateTimeColumnValue, row.nextLocalDateTime());
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
				  .nextLocalDateTime((l) -> assertEquals(localDateTimeColumnValue, l))
				  .nextLocalDate((l) -> assertEquals(localDateColumnValue, l), zoneId)
				  .nextLocalTime((l) -> assertEquals(localTimeColumnValue, l), zoneId);
			}).run();
			c.query("SELECT * FROM table_two WHERE id=?", Long.valueOf(2))
			    .withRows((row) -> {
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
			    	assertNull(row.nextLocalDateTime());
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
			    	.nextLocalDateTime((l) -> assertNull(l))
			    	.nextLocalDate((l) -> assertNull(l), zoneId)
			    	.nextLocalTime((l) -> assertNull(l), zoneId);
			    }).run();
			success.set();
		});
		assertTrue(success.isSet());
	}

	/** Test case for {@link JdbcHelper#query(Connection, Dialect, String, Object...)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testQueryConnection() throws Exception {
		final String sqlCreate = "CREATE TABLE table_three (id INT NOT NULL PRIMARY KEY, userId VARCHAR(16) NOT NULL)";
		final String sqlDrop = "DROP TABLE table_three";
		final Application application = getApplication(null);
		final JdbcHelper jh = application.getComponentFactory().requireInstance(JdbcHelper.class);
		final ConnectionProvider connectionProvider = application.getComponentFactory().requireInstance(ConnectionProvider.class);
		try (Connection conn = connectionProvider.open()) {
			conn.prepareStatement(sqlCreate).executeUpdate();
			jh.query(conn, null, sqlDrop).run();
			jh.query(conn, null, sqlCreate).run();
			assertEquals(0l, jh.query(conn, null, "SELECT COUNT(*) FROM table_three").count());
		}
	}
}
