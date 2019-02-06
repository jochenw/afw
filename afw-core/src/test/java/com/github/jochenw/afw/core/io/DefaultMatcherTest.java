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

import org.junit.Assert;
import org.junit.Test;

public class DefaultMatcherTest {
	@Test
	public void tesCaseSensitive() {
		final DefaultMatcher dm = new DefaultMatcher("**/*.java");
		Assert.assertFalse(dm.isMatchingAll());
		Assert.assertTrue(dm.matches("com/foo/bar.java"));
		Assert.assertTrue(dm.matches("com/Foo/bar.java"));
		Assert.assertFalse(dm.matches("com/Foo/bar.javax"));
		Assert.assertFalse(dm.matches("com/Foo/bar.Java"));
		Assert.assertFalse(dm.matches("My.java"));
	}

	@Test
	public void testCaseInsensitive() {
		final DefaultMatcher dm = new DefaultMatcher("**/*.java", false);
		Assert.assertFalse(dm.isMatchingAll());
		Assert.assertTrue(dm.matches("com/foo/bar.java"));
		Assert.assertTrue(dm.matches("com/Foo/bar.java"));
		Assert.assertFalse(dm.matches("com/Foo/bar.javax"));
		Assert.assertTrue(dm.matches("com/Foo/bar.Java"));
		Assert.assertFalse(dm.matches("My.java"));
	}

	@Test
	public void testPrefix() {
		final DefaultMatcher dm = new DefaultMatcher("main/**/*");
		Assert.assertTrue(dm.matches("main/java/com/github/jochenw/afw/core/csv/CsvFormatter.java"));
	}
}
