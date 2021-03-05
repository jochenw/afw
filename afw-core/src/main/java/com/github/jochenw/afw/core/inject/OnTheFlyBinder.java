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
package com.github.jochenw.afw.core.inject;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.inject.Provider;



/** The {@link com.github.jochenw.afw.core.inject.OnTheFlyBinder} is used by the
 * {@link com.github.jochenw.afw.core.inject.simple.SimpleComponentFactory} to
 * inject loggers, properties, etc. dynamically.
 */
public interface OnTheFlyBinder {
	/** Interface of a provider/supplier, that has a scope.
	 * @param <O> Type of the object, that the provider/supplier
	 *   returns.
	 */
	public static interface ScopedProvider<O> extends Provider<O> {
		/**
		 * Returns the providers scope.
		 * @return The providers scope.
		 */
		public Scope getScope();
	}
	/**
	 * Returns a provider, that can be invoked to inject a value
	 * into the given field. Returns null, if the field isn't
	 * injectable.
	 * @param <O> Type of the value, that is being injected.
	 * @param pCf The component factory, which may be queried
	 *    by the provider in order to retrieve a value.
	 * @param pField The field, that is being injected.
	 * @return A provider, that can be invoked to inject a value
	 * into the given field. Null, if the field isn't
	 * injectable.
	 */
	public <O> Provider<O> getProvider(IComponentFactory pCf, Field pField);
	/**
	 * Called to create a set of consumers, that are participating in the
	 * creation of an instance of the given type.
	 * @param <O> Type of the instance, that is being created.
	 * @param pCf The component factory, which may be queried
	 *    by the consumers in order to retrieve values.
	 * @param pType Type of the instance, that is being created.
	 * @param pConsumerSink A listener, that collects the created
	 *   consumers.
	 */
	public <O> void findConsumers(IComponentFactory pCf, Class<?> pType, Consumer<Consumer<O>> pConsumerSink);
	/**
	 * Called to create a set of additional bindings.
	 * @param <O> Type, for which to create additional bindings.
	 * @param pCf The component factory, which may be queried
	 *    by the consumers in order to retrieve values.
	 * @param pType Type, for which to create additional bindings.
	 * @param pBindingSink A listener, that collects the created
	 *   bindings.
	 */
	public <O> void findBindings(IComponentFactory pCf, Class<?> pType, BiConsumer<Key<O>, ScopedProvider<O>> pBindingSink);
}
