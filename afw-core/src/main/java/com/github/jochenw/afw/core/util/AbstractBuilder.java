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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Abstract base class for creating builders.
 * @param <S> Type of the object, that's being created by the builder.
 * @param <T> Actual type of the builder.
 */
public abstract class AbstractBuilder<S,T extends AbstractBuilder<S,T>> extends AbstractMutable {
	/** Creates a new instance.
	 */
	protected AbstractBuilder() {}

	private @Nullable S instance;

	/** Returns this builder.
	 * @return This builder.
	 */
	protected @NonNull T self() {
		@SuppressWarnings("unchecked")
		final @NonNull T t = (T) this;
		return t;
	}

	/** Creates the instance, that the builder returns.
	 * @return The created instance, with the builders configuration applied.
	 */
	protected abstract @NonNull S newInstance();

	/**
	 * Makes this builder immutable, and returns the created instance.
	 * @return The created instance
	 */
	public @NonNull S build() {
		if (isMutable()) {
			@NonNull S s = newInstance();
			makeImmutable();
			instance = s;
		}
		return Objects.requireNonNull(instance);
	}
}
