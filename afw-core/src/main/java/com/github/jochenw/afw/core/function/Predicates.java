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
package com.github.jochenw.afw.core.function;

import java.util.function.Predicate;


/**
 * Utility class for working with instances of {@link Predicate}.
 */
public class Predicates {
	/** Creates a new instance. Private constructor, because
	 * all methods are static.
	 */
	private Predicates() {}

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

	/** Tests, whether any of the given predicates is true.
	 * @param <O> Type, on which the predicates are being applied to.
	 * @param pPredicates List of predicates, that are being tested.
	 * @param pValue The test value.
	 * @return True, if any of the predicates in the list {@code pPredicates}
	 *   returned true, when applied to the value {@code pValue}.
	 */
	public static <O> boolean anyOf(Iterable<Predicate<O>> pPredicates, O pValue) {
		for (Predicate<O> predicate : pPredicates) {
			if (predicate.test(pValue)) {
				return true;
			}
		}
		return false;
	}

	/** Tests, whether any of the given predicates is true.
	 * @param <O> Type, on which the predicates are being applied to.
	 * @param pPredicates List of predicates, that are being tested.
	 * @param pValue The test value.
	 * @return True, if any of the predicates in the list {@code pPredicates}
	 *   returned true, when applied to the value {@code pValue}.
	 */
	@SafeVarargs
	public static <O> boolean anyOf(O pValue, Predicate<O>... pPredicates) {
		for (Predicate<O> predicate : pPredicates) {
			if (predicate.test(pValue)) {
				return true;
			}
		}
		return false;
	}

	/** Tests, whether all of the given predicates are true.
	 * @param <O> Type, on which the predicates are being applied to.
	 * @param pPredicates List of predicates, that are being tested.
	 * @param pValue The test value.
	 * @return True, if all of the predicates in the list {@code pPredicates}
	 *   returned true, when applied to the value {@code pValue}.
	 */
	public static <O> boolean allOf(Iterable<Predicate<O>> pPredicates, O pValue) {
		for (Predicate<O> predicate : pPredicates) {
			if (!predicate.test(pValue)) {
				return false;
			}
		}
		return true;
	}

	/** Tests, whether all of the given predicates are true.
	 * @param <O> Type, on which the predicates are being applied to.
	 * @param pPredicates List of predicates, that are being tested.
	 * @param pValue The test value.
	 * @return True, if all of the predicates in the list {@code pPredicates}
	 *   returned true, when applied to the value {@code pValue}.
	 */
	@SafeVarargs
	public static <O> boolean allOf(O pValue, Predicate<O>... pPredicates) {
		for (Predicate<O> predicate : pPredicates) {
			if (!predicate.test(pValue)) {
				return false;
			}
		}
		return true;
	}

	/** Creates a new predicate, which implements a logical OR on all of
	 * the given predicates.
	 * @param pPredicates The terms of the logical OR.
	 * @param <O> Type of the terms, and the created predicate.
	 * @return The created predicate, which implements a logical OR
	 *   on all of the terms.
	 */
	@SafeVarargs
	public static <O> Predicate<O> or(Predicate<O>... pPredicates) {
		return (o) -> {
			for (Predicate<O> pr : pPredicates) {
				if (pr.test(o)) {
					return true;
				}
			}
			return false;
		};
	}

	/** Creates a new predicate, which implements a logical OR on all of
	 * the given predicates.
	 * @param pPredicates The terms of the logical OR.
	 * @param <O> Type of the terms, and the created predicate.
	 * @return The created predicate, which implements a logical OR
	 *   on all of the terms.
	 */
	public static <O> Predicate<O> or(Iterable<Predicate<O>> pPredicates) {
		return (o) -> {
			for (Predicate<O> pr : pPredicates) {
				if (pr.test(o)) {
					return true;
				}
			}
			return false;
		};
	}

	/** Creates a new predicate, which implements a logical AND on all of
	 * the given predicates.
	 * @param pPredicates The terms of the logical AND.
	 * @param <O> Type of the terms, and the created predicate.
	 * @return The created predicate, which implements a logical AND
	 *   on all of the terms.
	 */
	@SafeVarargs
	public static <O> Predicate<O> and(Predicate<O>... pPredicates) {
		return (o) -> {
			for (Predicate<O> pr : pPredicates) {
				if (pr.test(o)) {
					return false;
				}
			}
			return true;
		};
	}

	/** Creates a new predicate, which evaluates to true, if any of
	 * the given predicates does. (In other words, a logical OR on all
	 * of the predicates.
	 * @param pPredicates The predicates, on which the created
	 *   predicate depends.
	 * @param <O> Type of the terms, and the created predicate.
	 * @return The created predicate, which implements a logical AND
	 *   on all of the terms.
	 */
	public static <O> Predicate<O> anyOf(Iterable<Predicate<O>> pPredicates) {
		return or(pPredicates);
	}

	/** Creates a new predicate, which evaluates to true, if all of
	 * the given predicates do. (In other words, a logical AND on all
	 * of the predicates.
	 * @param pPredicates The predicates, on which the created
	 *   predicate depends.
	 * @param <O> Type of the terms, and the created predicate.
	 * @return The created predicate, which implements a logical AND
	 *   on all of the terms.
	 */
	public static <O> Predicate<O> allOf(Iterable<Predicate<O>> pPredicates) {
		return and(pPredicates);
	}

	/** Creates a new predicate, which implements a logical AND on all of
	 * the given predicates.
	 * @param pPredicates The terms of the logical AND.
	 * @param <O> Type of the terms, and the created predicate.
	 * @return The created predicate, which implements a logical AND
	 *   on all of the terms.
	 */
	public static <O> Predicate<O> and(Iterable<Predicate<O>> pPredicates) {
		return (o) -> {
			for (Predicate<O> pr : pPredicates) {
				if (!pr.test(o)) {
					return false;
				}
			}
			return true;
		};
	}
}
