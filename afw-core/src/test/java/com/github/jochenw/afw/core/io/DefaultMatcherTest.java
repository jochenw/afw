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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Test for the {@link DefaultMatcher}.
 */
public class DefaultMatcherTest {
	/** Test case for case sensitive matching.
	 */
	@Test
	public void testCaseSensitive() {
		final DefaultMatcher dm = new DefaultMatcher("**/*.java");
		assertFalse(dm.isMatchingAll());
		assertTrue(dm.test("com/foo/bar.java"));
		assertTrue(dm.test("com/Foo/bar.java"));
		assertFalse(dm.test("com/Foo/bar.javax"));
		assertFalse(dm.test("com/Foo/bar.Java"));
		assertTrue(dm.test("My.java"));
	}

	/** Test case for case insensitive matching.
	 */
	@Test
	public void testCaseInsensitive() {
		final DefaultMatcher dm = new DefaultMatcher("**/*.java", false);
		assertFalse(dm.isMatchingAll());
		assertTrue(dm.test("com/foo/bar.java"));
		assertTrue(dm.test("com/Foo/bar.java"));
		assertFalse(dm.test("com/Foo/bar.javax"));
		assertTrue(dm.test("com/Foo/bar.Java"));
		assertTrue(dm.test("My.java"));
	}

	/** Test case for matching directory patterns without a directory in the path.
	 */
	@Test
	public void testNoDirectoryInPath() {
		final DefaultMatcher dm = new DefaultMatcher("**/.classpath");
		assertTrue(dm.test("foo/.classpath"));
		assertTrue(dm.test(".classpath"));
		final DefaultMatcher dm2 = new DefaultMatcher("**/.settings/**/*");
		assertTrue(dm2.test(".settings/org.eclipse.core.resources.prefs"));
	}
	/** Test case for matching with a prefix.
	 */
	@Test
	public void testPrefix() {
		final DefaultMatcher dm = new DefaultMatcher("main/**/*");
		assertTrue(dm.test("main/java/com/github/jochenw/afw/core/csv/CsvFormatter.java"));
	}
}
