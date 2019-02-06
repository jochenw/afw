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

import com.google.inject.Provider;


public interface OnTheFlyBinder {
	public static interface ScopedProvider<O> extends Provider<O> {
		public Scope getScope();
	}
	public <O> Provider<O> getProvider(IComponentFactory pCf, Field pField);
	public <O> void findConsumers(IComponentFactory pCf, Class<?> pType, Consumer<Consumer<O>> pConsumerSink);
	public <O> void findBindings(IComponentFactory pCf, Class<?> pType, BiConsumer<Key<O>, ScopedProvider<O>> pBindingSink);
}
