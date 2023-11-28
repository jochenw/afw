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


/** Test for the {@link DefaultMatcher}.
 */
public class DefaultMatcherTest {
	/** Test case for case sensitive matching.
	 */
	@Test
	public void testCaseSensitive() {
		final DefaultMatcher dm = new DefaultMatcher("**/*.java");
		Assert.assertFalse(dm.isMatchingAll());
		Assert.assertTrue(dm.test("com/foo/bar.java"));
		Assert.assertTrue(dm.test("com/Foo/bar.java"));
		Assert.assertFalse(dm.test("com/Foo/bar.javax"));
		Assert.assertFalse(dm.test("com/Foo/bar.Java"));
		Assert.assertTrue(dm.test("My.java"));
	}

	/** Test case for case insensitive matching.
	 */
	@Test
	public void testCaseInsensitive() {
		final DefaultMatcher dm = new DefaultMatcher("**/*.java", false);
		Assert.assertFalse(dm.isMatchingAll());
		Assert.assertTrue(dm.test("com/foo/bar.java"));
		Assert.assertTrue(dm.test("com/Foo/bar.java"));
		Assert.assertFalse(dm.test("com/Foo/bar.javax"));
		Assert.assertTrue(dm.test("com/Foo/bar.Java"));
		Assert.assertTrue(dm.test("My.java"));
	}

	/** Test case for matching directory patterns without a directory in the path.
	 */
	@Test
	public void testNoDirectoryInPath() {
		final DefaultMatcher dm = new DefaultMatcher("**/.classpath");
		Assert.assertTrue(dm.test("foo/.classpath"));
		Assert.assertTrue(dm.test(".classpath"));
		final DefaultMatcher dm2 = new DefaultMatcher("**/.settings/**/*");
		Assert.assertTrue(dm2.test(".settings/org.eclipse.core.resources.prefs"));
	}
	/** Test case for matching with a prefix.
	 */
	@Test
	public void testPrefix() {
		final DefaultMatcher dm = new DefaultMatcher("main/**/*");
		Assert.assertTrue(dm.test("main/java/com/github/jochenw/afw/core/csv/CsvFormatter.java"));
	}
}
