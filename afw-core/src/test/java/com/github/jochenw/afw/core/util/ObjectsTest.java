/*
 * Copyright 2023 Jochen Wiedmann
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.data.Data;
import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.util.Objects.CachedObjectSerializer;

/** Test suite for the {@link Objects} class.
 */
public class ObjectsTest {
	/** Test case for {@link Objects#getCacheableObject(Path, FailableSupplier)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testGetCacheableObject() throws Exception {
		final Map<String,Object> map = Data.asMap("foo", "bar", "answer", Integer.valueOf(42), "fact", Boolean.TRUE);
		final MutableBoolean invoked = new MutableBoolean();
		final FailableSupplier<Map<String,Object>,?> supplier = () -> {
			invoked.set();
			return map;
		};
		final Path testDir = Tests.requireTestDirectory(ObjectsTest.class);
		Files.createDirectories(testDir);
		@SuppressWarnings("null")
		final @NonNull Path cacheFile = Files.createTempFile("cache", ".file");
		Files.deleteIfExists(cacheFile);
		Assert.assertFalse(invoked.getValue());
		Assert.assertFalse(Files.isRegularFile(cacheFile));
		Map<String,Object> createdMap = Objects.getCacheableObject(cacheFile, supplier);
		Assert.assertTrue(Files.isRegularFile(cacheFile));
		Assert.assertTrue(invoked.getValue());
		Assert.assertSame(map, createdMap);
		invoked.unset();
		Assert.assertFalse(invoked.getValue());
		Map<String,Object> loadedMap = Objects.getCacheableObject(cacheFile, supplier);
		Assert.assertNotSame(map, loadedMap);
		Assert.assertTrue(map.equals(loadedMap));
	}

	/** Test case for {@link Objects#getCacheableObject(Path, CachedObjectSerializer, FailableSupplier)}
	 */
	/** Test case for {@link Objects#getCacheableObject(Path, FailableSupplier)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testGetCacheableObjectWithCustomSerializer() throws Exception {
		final CachedObjectSerializer cso = new CachedObjectSerializer() {
			@Override
			public void write(@NonNull Object pObject, @NonNull OutputStream pOut) throws IOException {
				@SuppressWarnings("unchecked")
				final Map<String,Object> map = (Map<String, Object>) pObject;
				try (final OutputStreamWriter osw = new OutputStreamWriter(pOut);
					 final BufferedWriter bw = new BufferedWriter(osw)) {
					bw.write(String.valueOf(map.size()));
					bw.newLine();
					final StringBuilder sb = new StringBuilder();
					for (Map.Entry<String, Object> en : map.entrySet()) {
						sb.setLength(0);
						final String k = en.getKey();
						if (k.indexOf('=') >= 0) {
							throw new IllegalStateException("Invalid map key: " + k);
						}
						sb.append(k);
						sb.append('=');
						final Object v = en.getValue();
						if (v instanceof String) {
							final String s = (String) v;
							if (s.indexOf(':') >= 0  ||  s.indexOf('\r') >= 0  ||  s.indexOf('\n') >= 0) {
								throw new IllegalStateException("Invalid map value: " + v);
							}
							sb.append(v);
						} else if (v instanceof Integer) {
							sb.append("I:");
							sb.append(v);
						} else if (v instanceof Boolean) {
							sb.append("B:");
							sb.append(v);
						} else {
							throw new IllegalStateException("Invalid value type: " + v.getClass().getName());
						}
						bw.write(sb.toString());
						bw.newLine();
					}
				}
			}

			@Override
			public @NonNull Object read(@NonNull InputStream pIn) throws Exception {
				final Map<String,Object> map = new HashMap<>();
				try (InputStreamReader isr = new InputStreamReader(pIn, StandardCharsets.UTF_8);
					 BufferedReader br = new BufferedReader(isr)) {
					final String sizeLine = br.readLine();
					final int size = Integer.valueOf(sizeLine);
					for (int i = 0;  i < size;  i++) {
						final String attrLine = br.readLine();
						final int offset = attrLine.indexOf('=');
						if (offset <= 0) {
							throw new IllegalStateException("Key not found in attribute line: " + attrLine);
						}
						final String key = attrLine.substring(0, offset);
						final String valueStr = attrLine.substring(offset+1);
						final Object o;
						if (valueStr.startsWith("I:")) {
							o = Integer.parseInt(valueStr.substring(2));
						} else if (valueStr.startsWith("B:")) {
							o = Boolean.parseBoolean(valueStr.substring(2));
						} else {
							o = valueStr;
						}
						map.put(key, o);
 					}
				}
				return map;
			}
		};
		
		final Map<String,Object> map = Data.asMap("foo", "bar", "answer", Integer.valueOf(42), "fact", Boolean.TRUE);
		final MutableBoolean invoked = new MutableBoolean();
		final FailableSupplier<Map<String,Object>,?> supplier = () -> {
			invoked.set();
			return map;
		};
		final Path testDir = Tests.requireTestDirectory(ObjectsTest.class);
		Files.createDirectories(testDir);
		@SuppressWarnings("null")
		final @NonNull Path cacheFile = Files.createTempFile(testDir, "cache", ".file");
		Files.deleteIfExists(cacheFile);
		Assert.assertFalse(invoked.getValue());
		Assert.assertFalse(Files.isRegularFile(cacheFile));
		Map<String,Object> createdMap = Objects.getCacheableObject(cacheFile, cso, supplier);
		Assert.assertTrue(Files.isRegularFile(cacheFile));
		Assert.assertTrue(invoked.getValue());
		Assert.assertSame(map, createdMap);
		invoked.unset();
		Assert.assertFalse(invoked.getValue());
		Map<String,Object> loadedMap = Objects.getCacheableObject(cacheFile, cso, supplier);
		Assert.assertNotSame(map, loadedMap);
		Assert.assertTrue(map.equals(loadedMap));
		Assert.assertTrue(Files.isRegularFile(cacheFile));
		try (@SuppressWarnings("null")
		     @NonNull InputStream is = Files.newInputStream(cacheFile)) {
			@SuppressWarnings("unchecked")
			final Map<String,Object> loadedMap2 = (Map<String, Object>) cso.read(is);
			Assert.assertNotSame(map, loadedMap2);
			Assert.assertNotSame(loadedMap, loadedMap2);
			Assert.assertTrue(map.equals(loadedMap2));
		}
	}

	/** Test case for {@link Objects#enumValues(Class)}.
	 */
	@Test
	public void testEnumValues() {
		final Level[] levels = Objects.enumValues(Level.class);
		final Level[] expectedLevels = Level.values();
		assertNotNull(levels);
		assertArrayEquals(expectedLevels, levels);
		try {
			Objects.enumValues(Objects.fakeNonNull());
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Type", e.getMessage());
		}
	}

	/** Test case for {@link Objects#enumNamesAsString(Class, String)}.
	 */
	@Test
	public void testEnumNamesAsString() {
		final List<String> names = new ArrayList<String>();
		for(Level level : Level.values()) {
			names.add(level.name());
		}
		
		assertEquals(String.join("|", names), Objects.enumNamesAsString(Level.class, "|"));
		assertEquals(String.join("", names), Objects.enumNamesAsString(Level.class, null));

		try {
			Objects.enumNamesAsString(Objects.fakeNonNull(), null);
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Type", e.getMessage());
		}
	}

	/** Test case for {@link Objects#valueOf(Class, String)}.
	 */
	@Test
	public void testValueOf() {
		assertSame(Level.DEBUG, Objects.valueOf(Level.class, "DEBUG"));
		assertSame(Level.DEBUG, Objects.valueOf(Level.class, "Debug"));
		assertSame(Level.TRACE, Objects.valueOf(Level.class, "trace"));
		Functions.assertFail(NullPointerException.class, "Enum Type",
	                         () -> Objects.valueOf(Objects.fakeNonNull(), "DEBUG"));
		Functions.assertFail(NullPointerException.class, "Enum Name",
	                         () -> Objects.valueOf(Level.class, Objects.fakeNonNull()));
		Functions.assertFail(IllegalArgumentException.class,
				             "Invalid name for an instance of "
						     + Level.class.getName() + ": Expected "
						     + Objects.enumNamesAsString(Level.class, "|") + ", got Dbug",
						     () -> Objects.valueOf(Level.class, "Dbug"));
		
		
	}
}
