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


/** Abstract base class for creating builders.
 * @param <S> Type of the object, that's being created by the builder.
 * @param <T> Actual type of the builder.
 */
public abstract class AbstractBuilder<S,T extends AbstractBuilder<S,T>> extends AbstractMutable {
	private S instance;

	protected T self() {
		@SuppressWarnings("unchecked")
		final T t = (T) this;
		return t;
	}

	protected abstract S newInstance();

	/**
	 * Makes this builder immutable, and returns the created instance.
	 * @return The created instance
	 */
	public S build() {
		if (isMutable()) {
			S s = newInstance();
			makeImmutable();
			instance = s;
		}
		return instance;
	}
}
