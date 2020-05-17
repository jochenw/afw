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



/**
 * Interface of a dependency injection controller.
 */
public interface IComponentFactory {
	/**
	 * Creates a key, which represents the given {@link Type type}.
	 * The key can be used for registration, or retrieval of
	 * dependencies.
	 * @param <O> The key type.
	 * @param pType The {@link Type type}, which is being represented
	 *   by the created key.
	 * @return A key, which represents the given {@link Type type}.
	 * The key can be used for registration, or retrieval of
	 * dependencies.
	 */
	public default <O> Key<O> asKey(@Nonnull Type pType) {
		return new Key<O>(pType);
	}
	/**
	 * Creates a key, which represents the given {@link Type type},
	 * and the given name. The key can be used for registration,
	 * or retrieval of dependencies.
	 * @param <O> The key type.
	 * @param pType The {@link Type type}, which is being represented
	 *   by the created key.
	 * @param pName The keys name. It is possible, to register,
	 *   and retrieve multiple objects of the same type, if they
	 *   have different names.
	 * @return A key, which represents the given {@link Type type},
	 *   and the given name. The key can be used for registration, or
	 *   retrieval of dependencies.
	 */
	public default <O> Key<O> asKey(@Nonnull Type pType, @Nonnull String pName) {
		if (pName == null  ||  pName.length() == 0) {
			return new Key<O>(pType);
		} else {
			final Named named = Names.named(pName);
			return new Key<O>(pType, named);
		}
	}
	/**
	 * Called to initialize an object by injecting dependencies.
	 * @param pObject The object, which is being initialized. No
	 *   check is done, whether this object has been initialized
	 *   before.
	 */
	public void init(Object pObject);
	/**
	 * Retrieves a dependency, which has been registered with
	 * the given key.
	 * @param <O> The key type.
	 * @param pKey The key, which has been used to register the
	 *   requested dependency. (Not the same instance, but the
	 *   same key in the sense of {@link Object#equals(Object)}.)
	 * @return The requested dependency, if available, or null.
	 * @see #requireInstance(Key)
	 * @see #getInstance(Class)
	 * @see #getInstance(Class, String)
	 * @see #getInstance(com.github.jochenw.afw.core.inject.Types.Type)
	 */
	public @Nullable <O> O getInstance(Key<O> pKey);
	/**
	 * Retrieves a dependency, which has been registered with
	 * the given types key.
	 * @param <O> The key type.
	 * @param pType The key, which has been used to register the
	 *   requested dependency. (Not the same instance, but the
	 *   same key in the sense of {@link Object#equals(Object)}.)
	 * @return The requested dependency, if available, or null.
	 * @see #requireInstance(Class)
	 * @see #getInstance(Key)
	 * @see #getInstance(Class, String)
	 * @see #getInstance(Type)
	 * @see #getInstance(com.github.jochenw.afw.core.inject.Types.Type)
	 */
	public default @Nullable <O> O getInstance(@Nonnull Class<O> pType) {
		return getInstance(asKey(pType));
	}
	/**
	 * Retrieves a dependency, which has been registered with
	 * the given types key, and the given name.
	 * @param <O> The key type.
	 * @param pType The key, which has been used to register the
	 *   requested dependency. (Not the same instance, but the
	 *   same key in the sense of {@link Object#equals(Object)}.)
	 * @param pName The keys name. It is possible, to register,
	 *   and retrieve multiple objects of the same type, if they
	 *   have different names.
	 * @return The requested dependency, if available, or null.
	 * @see #requireInstance(Class, String)
	 * @see #getInstance(Key)
	 * @see #getInstance(Class)
	 * @see #getInstance(Type)
	 * @see #getInstance(com.github.jochenw.afw.core.inject.Types.Type)
	 */
	public default @Nullable <O> O getInstance(@Nonnull Class<O> pType, @Nonnull String pName) {
		return getInstance(asKey(pType, pName));
	}
	/**
	 * Retrieves a dependency, which has been registered with
	 * the given key.
	 * @param <O> The key type.
	 * @param pKey The key, which has been used to register the
	 *   requested dependency. (Not the same instance, but the
	 *   same key in the sense of {@link Object#equals(Object)}.)
	 * @return The requested dependency. Never null. A {@link NoSuchElementException}
	 *   is being thrown, if no such dependency has been registered.
	 * @see #getInstance(Key)
	 * @see #requireInstance(Class)
	 * @see #requireInstance(Class, String)
	 * @see #requireInstance(Type)
	 * @see #requireInstance(com.github.jochenw.afw.core.inject.Types.Type)
	 * @throws NoSuchElementException No such dependency has been registered.
	 */
	public default @Nonnull <O> O requireInstance(Key<O> pKey) throws NoSuchElementException {
		final O o = getInstance(pKey);
		if (o == null) {
			throw new NoSuchElementException("No such instance: " + pKey.getDescription());
		}
		return o;
	}
	/**
	 * Retrieves a dependency, which has been registered with
	 * the given types key.
	 * @param <O> The key type.
	 * @param pType The key, which has been used to register the
	 *   requested dependency. (Not the same instance, but the
	 *   same key in the sense of {@link Object#equals(Object)}.)
	 * @return The requested dependency. Never null. A {@link NoSuchElementException}
	 *   is being thrown, if no such dependency has been registered.
	 * @see #getInstance(Class)
	 * @see #requireInstance(Key)
	 * @see #requireInstance(Class, String)
	 * @see #requireInstance(Type)
	 * @see #requireInstance(com.github.jochenw.afw.core.inject.Types.Type)
	 * @throws NoSuchElementException No such dependency has been registered.
	 */
	public default @Nonnull <O> O requireInstance(@Nonnull Class<O> pType) {
		return requireInstance(asKey(pType));
	}
	/**
	 * Retrieves a dependency, which has been registered with
	 * the given types key, and the given name.
	 * @param <O> The key type.
	 * @param pType The key, which has been used to register the
	 *   requested dependency. (Not the same instance, but the
	 *   same key in the sense of {@link Object#equals(Object)}.)
	 * @param pName The keys name. It is possible, to register,
	 *   and retrieve multiple objects of the same type, if they
	 *   have different names.
	 * @return The requested dependency. Never null. A {@link NoSuchElementException}
	 *   is being thrown, if no such dependency has been registered.
	 * @see #getInstance(Class, String)
	 * @see #requireInstance(Key)
	 * @see #requireInstance(Class)
	 * @see #requireInstance(Type)
	 * @see #requireInstance(com.github.jochenw.afw.core.inject.Types.Type)
	 * @throws NoSuchElementException No such dependency has been registered.
	 */
	public default @Nonnull <O> O requireInstance(@Nonnull Class<O> pType, @Nonnull String pName) {
		return requireInstance(asKey(pType, pName));
	}
	/** Creates a new instance of the given type.
	 * @param <O> The expected result type.
	 * @param pImplClass The implementation class.
	 * @return The newly created instance.
	 */
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
