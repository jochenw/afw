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

import java.lang.reflect.Type;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;


public interface IComponentFactory {
	public default <O> Key<O> asKey(@Nonnull Type pType) {
		return new Key<O>(pType);
	}
	public default <O> Key<O> asKey(@Nonnull Type pType, @Nonnull String pName) {
		if (pName == null  ||  pName.length() == 0) {
			return new Key<O>(pType);
		} else {
			final Named named = Names.named(pName);
			return new Key<O>(pType, named);
		}
	}
	public void init(Object pObject);
	public @Nullable <O> O getInstance(Key<O> pKey);
	public default @Nullable <O> O getInstance(@Nonnull Class<O> pType) {
		return getInstance(asKey(pType));
	}
	public default @Nullable <O> O getInstance(@Nonnull Class<O> pType, @Nonnull String pName) {
		return getInstance(asKey(pType, pName));
	}
	public default @Nonnull <O> O requireInstance(Key<O> pKey) throws NoSuchElementException {
		final O o = getInstance(pKey);
		if (o == null) {
			throw new NoSuchElementException("No such instance: " + pKey.getDescription());
		}
		return o;
	}
	public default @Nonnull <O> O requireInstance(@Nonnull Class<O> pType) {
		return requireInstance(asKey(pType));
	}
	public default @Nonnull <O> O requireInstance(@Nonnull Class<O> pType, @Nonnull String pName) {
		return requireInstance(asKey(pType, pName));
	}
	public <O> O newInstance(Class<? extends O> pImplClass);
	public default <O> O getInstance(@Nonnull Type pType) {
		final Key<O> key = new Key<O>(pType);
		return getInstance(key);
	}
	public default <O> O requireInstance(@Nonnull Type pType) {
		final Key<O> key = new Key<O>(pType);
		return requireInstance(key);
	}
	public default <O> O getInstance(@Nonnull Types.Type<O> pType) {
		final Key<O> key = new Key<O>(pType.getRawType());
		return getInstance(key);
	}
	public default <O> O requireInstance(@Nonnull Types.Type<O> pType) {
		final Key<O> key = new Key<O>(pType.getRawType());
		return requireInstance(key);
	}
}
