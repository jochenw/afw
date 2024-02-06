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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Utility methods for working with lists.
 */
public class Lists {
	/** Converts the given parameter array into a list.
	 * @param <O> Element type of the parameter array, and the generated list.
	 * @param pArgs The parameter array, which is being converted.
	 * @return The created list.
	 */
	public static <O> @NonNull List<O> of(@SuppressWarnings("unchecked") O... pArgs) {
		if (pArgs == null) {
			return Objects.requireNonNull(Collections.emptyList());
		}
		return Objects.requireNonNull(Arrays.asList(pArgs));
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
	public static <I,O> @NonNull List<O> of(@NonNull Function<I,O> pMapper, @Nullable List<I> pList) {
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
}
