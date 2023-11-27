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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.data.Data;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;

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
		final Path cacheFile = Files.createTempFile("cache", ".file");
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

}
