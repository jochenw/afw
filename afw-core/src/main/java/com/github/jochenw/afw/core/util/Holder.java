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

import javax.annotation.Nullable;

/** A {@link Holder} is an envelope for another object. Holders are typically used in Lambda's, when
 * you need a final object (the Holder), that is mutable.
 * @param <T> Type of the wrapped object.
 */
public class Holder<T> {
	private T value;

	/** Returns the wrapped object. May be null, if there the wrapped object hasn't been set.
	 * @return The wrapped object. May be null, if there the wrapped object hasn't been set.
	 */
	public @Nullable T get() {
		return value;
	}

	/** Sets the wrapped object.
	 * @param pValue The wrapped object. 
	 */
	public void set(T pValue) {
		value = pValue;
	}

	/** Converts the Holder into a thread safe version, that wraps the same object.
	 * @param <O> Type of the wrapped object.
	 * @param pHolder The Holder, that is being converted.
	 * @return A thread safe Holder, which is wrapping the same object.
	 */
	public static <O> Holder<O> synchronizedHolder(Holder<O> pHolder) {
		return new Holder<O>() {
			@Override
			public O get() {
				synchronized(pHolder) {
					return pHolder.get();
				}
			}

			@Override
			public void set(O pValue) {
				synchronized(pHolder) {
					pHolder.set(pValue);
				}
			}
			
		};
	}
}
