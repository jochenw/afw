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
package com.github.jochenw.afw.core.inject.guice;

import com.github.jochenw.afw.core.inject.InjectTests;

import junit.framework.Test;
import junit.framework.TestCase;

/** Test for the {@link GuiceComponentFactory}.
 */
public class GuiceComponentFactoryTckTest extends TestCase {
	/**
	 * Returns a test suite for running tests on on the {@link GuiceComponentFactoryBuilder}. 
	 * @return A test suite for running tests on on the {@link GuiceComponentFactoryBuilder}.
	 */
	public static Test suite() {
		return InjectTests.testTckCompliance(new GuiceComponentFactoryBuilder());
	}
}
