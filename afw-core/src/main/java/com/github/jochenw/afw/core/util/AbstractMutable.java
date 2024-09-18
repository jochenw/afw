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

/**
 * Abstract base class for objects, that are initially mutable, but may become immutable.
 */
public class AbstractMutable {
	/** Creates a new instance.
	 */
	protected AbstractMutable() {}

	private boolean immutable;
	/**
	 * Asserts, that this object is still mutable.
	 * @throws IllegalStateException The instance is already immutable.
	 */
	public void assertMutable() {
		if (immutable) {
			throw new IllegalStateException("This object is no longer mutable.");
		}
	}

	/** Returns, whether the instance is still mutable.
	 * @return True, if this instance is still mutable, otherwise false.
	 */
	public boolean isMutable() {
		return !immutable;
	}

	/** Makes this object immutable.
	 * @throws IllegalStateException The instance is already immutable.
	 */
	protected void makeImmutable() {
		assertMutable();
		immutable = true;
	}
}
