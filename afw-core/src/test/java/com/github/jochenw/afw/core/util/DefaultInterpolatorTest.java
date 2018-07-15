package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class DefaultInterpolatorTest {
	private DefaultInterpolator interpolator;
	private Properties properties;

	@Before
	public void init() {
		properties = newProperties();
		interpolator = newInterpolator();
	}
	
	@Test
	public void testInterpolateString() {
		assertFalse(interpolator.isInterpolatable("target"));
		assertTrue(interpolator.isInterpolatable("${targetDir}/test"));
		assertTrue(interpolator.isInterpolatable("${testDir}/db"));
		assertTrue(interpolator.isInterpolatable("hsql:file:${dbDir}/mydb"));
		assertEquals("target", interpolator.interpolate("${targetDir}"));
		assertEquals("target/test", interpolator.interpolate("${targetDir}/test"));
		assertEquals("target/test/db", interpolator.interpolate("${testDir}/db"));
		assertEquals("hsql:file:target/test/db/mydb", interpolator.interpolate("hsql:file:${dbDir}/mydb"));
	}

	@Test
	public void testInterpolateProperties() {
		final Properties props = new Properties();
		props.putAll(properties);
		final Properties interpolatedProps = interpolator.filter(props);
		assertEquals("target", interpolatedProps.get("targetDir"));
		assertEquals("target/test", interpolatedProps.get("testDir"));
		assertEquals("target/test/db", interpolatedProps.get("dbDir"));
		assertEquals("hsql:file:target/test/db/mydb", interpolatedProps.get("dbUrl"));
	}

	protected Properties newProperties() {
		final Properties props = new Properties();
		props.put("targetDir", "target");
		props.put("testDir", "${targetDir}/test");
		props.put("dbDir", "${testDir}/db");
		props.put("dbUrl", "hsql:file:${dbDir}/mydb");
		return props;
	}

	protected DefaultInterpolator newInterpolator() {
		try {
			final DefaultInterpolator ip = new DefaultInterpolator((s) -> properties.getProperty(s));
			return ip;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
