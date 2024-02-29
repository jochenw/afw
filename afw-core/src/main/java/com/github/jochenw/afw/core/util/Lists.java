/*
 * Copyright 2024 Jochen Wiedmann
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Utility methods for working with lists.
 */
public class Lists {
	/** Converts the given parameter array into a list.
	 * Compared to {@link Arrays#asList(Object...)}, this
	 * method has the advantage, that the created list is
	 * mutable.
	 * @param <O> Element type of the parameter array, and the generated list.
	 * @param pArgs The parameter array, which is being converted.
	 * @return The created list.
	 */
	public static <O> @NonNull List<O> of(@SuppressWarnings("unchecked") O... pArgs) {
		if (pArgs == null) {
			return new ArrayList<>();
		} else {
			final List<O> list = new ArrayList<>(pArgs.length);
			for (O o : pArgs) {
				list.add(o);
			}
			return list;
		}
	}

	/** Converts the given list into another list by
	 * mapping the list elements.
	 * @param pMapper A mapper function, which is being
	 *   invoked for every element in the input list to create
	 *   an element in the output list.
	 * @param pList The input list.
	 * @return The created output list, with the same
	 *   number of elements than the input list. Every
	 *   element in the output list has been created by
	 *   invoking the mapper function for an element in
	 *   the input list.
	 * @param <I> Element type of the input list.
	 * @param <O> Element type of the output list.
	 */
	public static <I,O> @NonNull List<O> of(@NonNull Function<I,O> pMapper, @Nullable Collection<I> pList) {
		if (pList == null) {
			return Objects.requireNonNull(Collections.emptyList());
		}
		final @NonNull Function<I,O> mapper = Objects.requireNonNull(pMapper, "Mapper function");
		final List<O> outputList = new ArrayList<>(pList.size());
		pList.forEach((i) -> outputList.add(mapper.apply(i)));
		return outputList;
	}

	/** Converts the given parameter array into a list by
	 * mapping the list elements.
	 * @param pMapper A mapper function, which is being
	 *   invoked for every element in the parameter array
	 *   to create an element in the output list.
	 * @param pArgs The input parameter array
	 * @return The created output list, with the same
	 *   number of elements than the input list. Every
	 *   element in the output list has been created by
	 *   invoking the mapper function for an element in
	 *   the input list.
	 * @param <I> Element type of the input list.
	 * @param <O> Element type of the output list.
	 */
	public static <I,O> List<O> of(Function<I,O> pMapper, @SuppressWarnings("unchecked") I... pArgs) {
		if (pArgs == null) {
			return Collections.emptyList();
		}
		final @NonNull Function<I,O> mapper = Objects.requireNonNull(pMapper, "Mapper function");
		final List<O> outputList = new ArrayList<>(pArgs.length);
		for (int i = 0;  i < pArgs.length;  i++) {
			outputList.add(mapper.apply(pArgs[i]));
		}
		return outputList;
	}

	/** Converts the given list, or collection, into an array with the given element type.
	 *  @param pList The list, which is being converted.
	 *  @param pType The element type.
	 *  @param <O> The element type.
	 *  @return The result array.
	 */
	public static <O> O[] toArray(@NonNull Collection<?> pList, @NonNull Class<O> pType) {
		final @NonNull Collection<?> list = Objects.requireNonNull(pList, "List");
		final @NonNull Class<O> type = Objects.requireNonNull(pType, "Type");
		@SuppressWarnings("unchecked")
		final O[] array = (O[]) Array.newInstance(type, list.size());
		int index = 0;
		for (Object object : list) {
			if (object == null) {
				array[index++] = Objects.fakeNonNull();
			} else {
				if (pType.isAssignableFrom(object.getClass())) {
					array[index++] = pType.cast(object);
				} else {
					throw new ClassCastException("List[" + index + "]: " + object.getClass().getName());
				}
			}
		}
		return array;
	}

	/** Maps the given list to another list by mapping the element.
	 * @param <I> Element type of the input list.
	 * @param <O> Element type of the output list.
	 * @param pList The input list.
	 * @param pMapper A mapping function, which converts a single element
	 * of the input list into a single element of the output list.
	 * @return The created list.
	 */
	public static <I,O> List<O> map(@NonNull List<I> pList, @NonNull Function<I,O> pMapper) {
		final List<I> list = Objects.requireNonNull(pList, "List");
		final Function<I,O> mapper = Objects.requireNonNull(pMapper, "Mapper");
		final List<O> result = new ArrayList<>(list.size());
		for (I i : list) {
			result.add(mapper.apply(i));
		}
		return result;
	}
}
