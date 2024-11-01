/*
 * Copyright 2022 Jochen Wiedmann
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.jspecify.annotations.NonNull;

/** Utility class for working with {@link Enumeration enumerations}.
 */
public class Enumerations {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	private Enumerations() {}

	/**
	 * Converts the enumeration into a list with the same elements.
	 * @param pEnum The enumeration, that is being converted.
	 *   <em>Note:</em> The enumeration
	 *   is being completely consumed by this method, and
	 *   {@link Enumeration#hasMoreElements()} will return
	 *   false, after executing this method.
	 *   If that is a problem, use {@link #asIterator(Enumeration)},
	 *   or {@link #asIterable(Enumeration)}.
	 * @param <O> The enumerations element type.
	 * @return A list with the same elements.
	 */
	public static <O> List<O> asList(Enumeration<O> pEnum) {
		final List<O> list = new ArrayList<>();
		while (pEnum.hasMoreElements()) {
			list.add(pEnum.nextElement());
		}
		return list;
	}

	/**
	 * Converts the enumeration into a list with the same elements.
	 * @param pEnum The enumeration, that is being converted.
	 *   <em>Note:</em> The enumeration is <em>not</em> consumed
	 *   by this method. However, using the iterator will affect
	 *   the state of the enumeration.
	 * @param <O> The enumerations element type.
	 * @return An iterator over the same elements.
	 */
	public static <O> Iterator<O> asIterator(Enumeration<O> pEnum) {
		return new Iterator<O>(){
			@Override
			public boolean hasNext() {
				return pEnum.hasMoreElements();
			}

			@Override
			public O next() {
				return pEnum.nextElement();
			}
			
		};
	}

	/**
	 * Converts the enumeration into an iterable with the same elements.
	 * @param pEnum The enumeration, that is being converted.
	 *   <em>Note:</em> The enumeration is <em>not</em> consumed
	 *   by this method. However, using the iterable, or the
	 *   iterator, that it creates, will affect the state of
	 *   the enumeration.
	 * @param <O> The enumerations element type.
	 * @return An iterable over the same elements.
	 */
	public static <O> Iterable<O> asIterable(Enumeration<O> pEnum) {
		return new Iterable<O>(){
			@Override
			public Iterator<O> iterator() {
				return asIterator(pEnum);
			}
		};
	}

	/** Converts the given iterator into an enumeration with the same elements.
	 * @param pIterator The iterator, which is being converted.
	 *   <em>Note:</em> The iterator is <em>not</em> consumed
	 *   by this method. However, using the iterator, or the created
	 *   enumeration, will affect the state of
	 *   the iterator.
	 * @param <O> Element type of the iterator, also element type of the created
	 *   enumeration.
	 * @return An enumeration returning the same elements.
	 */
	public static @NonNull <O> Enumeration<O> asEnumeration(Iterator<O> pIterator) {
		return new Enumeration<O>() {
			@Override
			public boolean hasMoreElements() {
				return pIterator.hasNext();
			}

			@Override
			public O nextElement() {
				return pIterator.next();
			}
		};
	}

	/** Converts the given iterable into an enumeration with the same elements.
	 * @param pIterable The iterable, which is being converted.
	 *   <em>Note:</em> The iterable is <em>not</em> altered
	 *   by this method. However, modifications of the iterable will
	 *   render the created enumeration invalid.
	 * @param <O> Element type of the iterable, also element type of the created
	 *   enumeration.
	 * @return An enumeration returning the same elements.
	 */
	public static @NonNull <O> Enumeration<O> asEnumeration(Iterable<O> pIterable) {
		return asEnumeration(pIterable.iterator());
	}

	/** Converts the given items into an enumeration with the same elements.
	 * @param pItems The array of items, which are being converted.
	 * @param <O> Element type of the array, also element type of the created
	 *   enumeration.
	 * @return An enumeration returning the same elements.
	 */
	public static @NonNull <O> Enumeration<O> asEnumeration(@SuppressWarnings("unchecked") O... pItems) {
		return asEnumeration(Arrays.asList(pItems));
	}
}
