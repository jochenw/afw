/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import com.github.jochenw.afw.core.props.DefaultInterpolator;

/** Test for the {@link DefaultInterpolator}.
 */
public class DefaultInterpolatorTest {
	private DefaultInterpolator interpolator;
	private Properties properties;

	/** Initializes the internal variables.
	 */
	@Before
	public void init() {
		properties = newProperties();
		interpolator = newInterpolator();
	}

	/** Test case for interpolating a simple string.
	 */
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

	/** Test case for interpolating properties.
	 */
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
