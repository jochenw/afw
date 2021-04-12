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
package com.github.jochenw.afw.core.util;

import java.util.function.Predicate;


/**
 * Utility class for working with instances of {@link Predicate}.
 */
public class Predicates {
	/** A {@link Predicate}, which is always true, or false.
	 * @param pValue The created predicates result.
	 * @param <O> The predicates argument type.
	 * @return A predicate returning a constant value, as given by the argument {@code pValue}.
	 * @see #alwaysTrue()
	 * @see #alwaysFalse()
	 */
	public static <O> Predicate<O> always(final boolean pValue) {
		return new Predicate<O>() {
			@Override
			public boolean test(O pT) {
				return pValue;
			}
		};
	}
	/** A {@link Predicate}, which is always true. Equivalent to {@link #always(boolean)} with the value true.
	 * @param <O> The predicates argument type.
	 * @return A predicate returning the constant value true.
	 * @see #always(boolean)
	 * @see #alwaysFalse()
	 */
	public static <O> Predicate<O> alwaysTrue() {
		return always(true);
	}
	/** A {@link Predicate}, which is always false. Equivalent to {@link #always(boolean)} with the value false.
	 * @param <O> The predicates argument type.
	 * @return A predicate returning the constant value false.
	 * @see #always(boolean)
	 * @see #alwaysTrue()
	 */
	public static <O> Predicate<O> alwaysFalse() {
		return always(false);
	}
}
