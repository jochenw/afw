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
package com.github.jochenw.afw.core.io;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.junit.Assert;
import org.junit.Test;


/** Test for the {@link DirectoryScanner}.
 */
public class DirectoryScannerTest {
	private static final DirectoryScanner ds = new DirectoryScanner();

	/**
	 * Test for finding all files (no includes, or excludes specified).
	 */
	@Test
	public void testAllFiles() {
		final Set<String> files = new HashSet<>();
		@SuppressWarnings("null")
		final @NonNull Path srcPath = Paths.get("src");
		ds.scan(srcPath, null, null, (c) -> {
			Assert.assertTrue(files.add(c.getUri()));
		});
		Assert.assertTrue(files.contains("main/java/com/github/jochenw/afw/core/util/Sax.java"));
		Assert.assertTrue(files.contains("main/resources/com/github/jochenw/afw/core/plugins/plugin-list-100.xsd"));
		Assert.assertTrue(files.contains("test/java/com/github/jochenw/afw/core/util/StringsTest.java"));
		Assert.assertTrue(files.contains("test/resources/com/github/jochenw/afw/test/mod/test.properties"));
	}

	/**
	 * Test for removing the prefix.
	 */
	@Test
	public void testSrcMain() {
		final Set<String> files = new HashSet<>();
		final IMatcher[] includes = new IMatcher[] {
			new DefaultMatcher("main/**/*")
		};
		@SuppressWarnings("null")
		final @NonNull Path srcPath = Paths.get("src");
		ds.scan(srcPath, includes, null, (c) -> {
			Assert.assertTrue(files.add(c.getUri()));
		});
		Assert.assertTrue(files.contains("main/java/com/github/jochenw/afw/core/util/Sax.java"));
		Assert.assertTrue(files.contains("main/resources/com/github/jochenw/afw/core/plugins/plugin-list-100.xsd"));
		Assert.assertFalse(files.contains("test/java/com/github/jochenw/afw/core/util/StringsTest.java"));
		Assert.assertFalse(files.contains("test/resources/com/github/jochenw/afw/test/mod/test.properties"));
	}
}
