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

import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** A {@link Holder} is an envelope for another object. Holders are typically used in Lambda's, when
 * you need a final object (the Holder), that is mutable.
 * @param <T> Type of the wrapped object.
 */
public class Holder<T> implements Supplier<@Nullable T> {
	/** Creates a new instance..
	 */
	public Holder() {}

	private @Nullable T value;

	/** Returns the wrapped object. May be null, if there the wrapped object hasn't been set.
	 * @return The wrapped object. May be null, if there the wrapped object hasn't been set.
	 * @see #require()
	 * @see #set(Object)
	 */
	public @Nullable T get() {
		return value;
	}

	/** Returns the wrapped object. Unlike the {@link #get()} method, this one guarantees
	 * to return a non-null value, because it throws a {@link NoSuchElementException}, if
	 * no wrapped object has been set.
	 * @return The wrapped object. Never null.
	 * @throws NoSuchElementException No wrapped object has been set.
	 * @see #get()
	 * @see #set(Object)
	 */
	public @NonNull T require() throws NoSuchElementException {
		final @Nullable T v = get();
		if (v == null) {
			throw new NoSuchElementException("No value has been given.");
		}
		return v;
	}

	/** Sets the wrapped object.
	 * @param pValue The wrapped object. 
	 */
	public void set(T pValue) {
		value = pValue;
	}

	/** Creates a new Holder, which wraps the given object.
	 * @param pObject The object, that is being wrapped by the created
	 *   Holder.
	 * @param <O> Type of the wrapped object.
	 * @return The created instance.
	 */
	public static <O> Holder<O> of(O pObject) {
		final Holder<O> holder = new Holder<O>();
		holder.set(pObject);
		return holder;
	}

	/** Creates a new Holder without wrapped object.
	 * @param <O> Type of the wrapped object.
	 * @return The created instance.
	 */
	public static <O> Holder<O> of() {
		return new Holder<O>();
	}

	/** Converts the Holder into a thread safe version, that wraps the same object.
	 * @param <O> Type of the wrapped object.
	 * @param pObject The wrapped object.
	 * @return A thread safe Holder, which is wrapping the same object.
	 */
	public static <O> Holder<O> ofSynchronized(O pObject) {
		final Holder<O> holder = new Holder<O>() {
			@Override
			public synchronized @Nullable O get() {
				return super.get();
			}

			@Override
			public synchronized void set(O pValue) {
				super.set(pValue);
			}
		};
		holder.set(pObject);
		return holder;
	}
}
