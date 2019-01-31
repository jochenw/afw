/**
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

import java.lang.annotation.Annotation;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.Key;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.name.Names;


public class GuiceComponentFactory implements IComponentFactory {
	private Injector injector;

	void setInjector(@Nonnull Injector pInjector) {
		injector = Objects.requireNonNull(pInjector, "Injector");
	}

	public @Nullable Injector getInjector() {
		return injector;
	}
	@Override
	public void init(Object pObject) {
		injector.injectMembers(pObject);
	}

	@Override
	public <O> O getInstance(Key<O> pKey) {
		final com.google.inject.Key<O> key = asGKey(pKey);
		final O o = injector.getInstance(key);
		return o;
	}

	protected <O> com.google.inject.Key<O> asGKey(Key<O> pKey) {
		final com.google.inject.Key<O> key;
		final Annotation annotation = pKey.getAnnotation();
		if (annotation == null) {
			if (pKey.getAnnotationClass() == null) {
				@SuppressWarnings("unchecked")
				final com.google.inject.Key<O> k = (com.google.inject.Key<O>) com.google.inject.Key.get(pKey.getType());
				key = k;
			} else {
				@SuppressWarnings("unchecked")
				final com.google.inject.Key<O> k = (com.google.inject.Key<O>) com.google.inject.Key.get(pKey.getType(), pKey.getAnnotationClass());
				key = k;
			}
		} else if (annotation instanceof Named) {
			final Named named = (Named) annotation;
			final String value = named.value();
			@SuppressWarnings("unchecked")
			final com.google.inject.Key<O> k = (com.google.inject.Key<O>) com.google.inject.Key.get(pKey.getType(), Names.named(value));
			key = k;
		} else {
			@SuppressWarnings("unchecked")
			final com.google.inject.Key<O> k = (com.google.inject.Key<O>) com.google.inject.Key.get(pKey.getType(), annotation);
			key = k;
		}
		return key;
	}

	@Override
	public <O> O newInstance(Class<? extends O> pImplClass) {
		@SuppressWarnings("unchecked")
		final Binding<O> binding = (Binding<O>) injector.getBinding(pImplClass);
		return binding.getProvider().get();
	}
}
