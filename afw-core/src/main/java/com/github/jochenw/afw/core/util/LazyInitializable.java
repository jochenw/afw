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

import java.util.function.Consumer;
import java.util.function.Supplier;


/** A wrapper for another object. The wrapped object will be initialized lazily
 * (upon first access).
 * @param <O> Type of the wrapped object.
 */
public class LazyInitializable<O extends Object> implements Supplier<O> {
	private final Supplier<O> supplier;
	private final Consumer<O> initializer;
	private volatile O instance;

	/**
	 * Creates a new instance.
	 * @param pSupplier A supplier, which provides the wrapped objects.
	 * @param pInitializer A consumer, which initializes the wrapped object.
	 *   The wrapped object is not considered to be usable, until the
	 *   initializer's invocation has finished.
	 */
	public LazyInitializable(Supplier<O> pSupplier, Consumer<O> pInitializer) {
		Objects.requireNonNull(pSupplier, "Supplier");
		supplier = pSupplier;
		initializer = pInitializer;
	}

	/**
	 * Returns the wrapped object. Upon the first invocation, this will invoke
	 * the supplier, and the initializer.
	 */
	@Override
	public O get() {
		if (instance == null) {
			synchronized(this) {
				if (instance == null) {
					O o = supplier.get();
					Objects.requireNonNull(o, "Instance");
					if (initializer != null) {
						initializer.accept(o);
					}
					instance = o;
				}
			}
		}
		return instance;
	}
}
