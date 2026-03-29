/**
 * Copyright 2025 Jochen Wiedmann
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
package com.github.jochenw.afw.core.vdn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Predicates;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Strings;


/** Utility class for creating, and using filters.
 */
public class Filters {
	/** Private constructor, because this class contains only static methods.
	 */
	private Filters() {}

	/** Builder class for creating a comparator, which combines other
	 * comparators (so-called sub comparators) into a single one.
	 * This is typically used for sorting over several properties,
	 * based on precedence.
	 * @param <O> The comparators type. (The type, which is being compared.)
	 */
	public static class ComparatorBuilder<O> {
		private List<@NonNull Comparator<O>> comparators;

		/** Creates a new instance.
		 */
		public ComparatorBuilder(){}

		/** Adds a new sub comparator.
		 * @param pComparator The sub comparator.
		 * @return This builder.
		 */
		public ComparatorBuilder<O> add(Comparator<O> pComparator) {
			final Comparator<O> comparator = Objects.requireNonNull(pComparator, "Comparator");
			if (comparators == null) {
				comparators = new ArrayList<>();
			}
			comparators.add(comparator);
			return this;
		}

		/** Adds a new sub comparator by creating a new comparator as follows:
		 * The created comparator will use the given {@code Mapper} to convert
		 * the compared objects into string calues, that are actually compared
		 * using {@link String#compareToIgnoreCase(String)}.
		 *
		 * The typical use case is to compare beans, based on property values:
		 * The given {@code pMapper} will extract the property values, which
		 * are then compared by applying the given {@code pComparator}.
		 * @param pMapper The mapper, which is used to extract the property
		 *    values.
		 * @param pAscending True, if the comparator should sort in ascending
		 *   order; otherwise false (descending order).
		 * @return This builder.
		 */
		public ComparatorBuilder<O> addNullSafeStringComparator(@NonNull Function<O,String> pMapper,
				boolean pAscending) {
			final Comparator<String> comp = (s1,s2) -> { return s1.compareToIgnoreCase(s2); };
			final Comparator<O> cmp = Filters.asNullSafeComparator(pMapper, pAscending, comp);
			return add(cmp);
		}

		/** Adds a new sub comparator by creating a new comparator, as follows:
		 * The created comparator will use the given {@code pMapper} to convert
		 * the compared objects into values, that are actually compared by
		 * applying the given {@code pComparator} on them.
		 *
		 * The typical use case is to compare beans, based on property values:
		 * The given {@code pMapper} will extract the property values, which
		 * are then compared by applying the given {@code pComparator}.
		 * @param <T> The mappers result type. Also, the type of the comparator.
		 * @param pMapper The mapper, which is used to extract the property
		 *    values.
		 * @param pComparator The comparator, which compares the extracted
		 *   values.
		 * @return This builder.
		 */
		public <T> ComparatorBuilder<O> add(@NonNull Function<O,T> pMapper,
				                            @NonNull Comparator<T> pComparator) {
			final @NonNull Function<O,T> mapper = Objects.requireNonNull(pMapper, "Mapper");
			final @NonNull Comparator<T> comparator = Objects.requireNonNull(pComparator, "Comparator");
			return add((o1,o2) -> comparator.compare(mapper.apply(o1), mapper.apply(o2)));
		}

		/** Builds the actual comparator from all the added sub comparators.
		 * @return The created comparator, or null, if no sub comparators have been
		 * added.
		 */
		public Comparator<O> build() {
			if (comparators == null  ||  comparators.isEmpty()) {
				return null;
			} else {
				return (o1,o2) -> {
					for (Comparator<O> comp : comparators) {
						final int result = comp.compare(o1, o2);
						if (result != 0) {
							return result;
						}
					}
					return 0;
				};
			}
		}
	}
	/** Builds a predicate, which combines other predicates (so-called
	 * predicate terms) by appplying a logical AND on them.
	 * @param <O> The predicates type. (The type, which is being tested.)
	 */
	public static class PredicateBuilder<O> {
		private List<Predicate<O>> predicates;
		/** Creates a new instance.
		 */
		public PredicateBuilder() {}
		/** Adds a new predicate term to the builder.
		 * @param pPredicate The predicate term, which is being added.
		 * @return This builder.
		 * @see #add(Function, Predicate)
		 */
		public PredicateBuilder<O> add(Predicate<O> pPredicate) {
			if (predicates == null) {
				predicates = new ArrayList<>();
			}
			predicates.add(pPredicate);
			return this;
		}
		/** Adds a new predicate term to the builder. The predicate term
		 * is specified as applying the given {@code pMapper} on the tested
		 * object, and then testing the result by applying the given
		 * {@code pTest}.
		 * The typical use case is a predicate on a bean property: The mapper
		 * extracts the property value from the bean, and the given predicate
		 * is being applied on that value.
		 * @param <T> The mappers result type. Also, the type of the predicate.
		 * @param pPredicate The predicate, which is being applied on the
		 *   mappers result.
		 * @param pMapper The mapper, which is applied to retrieve the value,
		 *   that is being tested.
		 * @return This builder.
		 */
		public <T> PredicateBuilder<O> add(Function<O,T> pMapper, Predicate<T> pPredicate) {
			return add((o) -> pPredicate.test(pMapper.apply(o)));
		}

		/** Adds a new predicate term to the builder. The predicate term
		 * is specified as applying the given {@code Mapper} on the tested object,
		 * and then testing the result by applying the given filter string.
		 * The filter string may contain wildcard characters, like '%', '*', or
		 * '?'. If it does, then a predicate will be constructed by invoking
		 * {@link Strings#matcher(String)}. Otherwise, a predicate will be
		 * constructed by invoking {@link String#contains(CharSequence)}.
		 * @param pFilterStr The filter string, which is being applied on the mappers
		 *   result. If the filter string is null, or empty, then this operation
		 *   is being skipped.
		 * @param pMapper The mapper, which is applied to retrieve the value
		 *   for testing.
		 * @return This builder.
		 */
		public PredicateBuilder<O> addNullsafeLikeOrContains(Function<O,String> pMapper,
				String pFilterStr) {
			final Function<O,String> mapper = Objects.requireNonNull(pMapper, "Mapper");
			final Predicate<String> predicate = asStringPredicate(pFilterStr);
			if (predicate != null) {
				return add((o) -> {
					final String s = o == null ? null : mapper.apply(o);
					if (s == null) {
						return false;
					} else {
						return predicate.test(s);
					}
				});
			}
			/* Do nothing */
			return this;
		}

		/** Called to convert the predicate terms into a single result predicate
		 * by combining them via a logical AND.
		 * @return The created predicate.
		 */
		public Predicate<O> build() {
			if (predicates == null  ||  predicates.isEmpty()) {
				return Predicates.alwaysTrue();
			} else if (predicates.size() == 1) {
				return predicates.get(0);
			} else {
				return Predicates.allOf(predicates);
			}
		}
	}

	/** Creates a new {@link PredicateBuilder}, and returns it.
	 * @param <O> Type of the created {@link PredicateBuilder}.
	 * @param pType Type of the created {@link PredicateBuilder}.
	 * @return The created builder.
	 */
	public static <O> PredicateBuilder<O> predicate(Class<O> pType) {
		return new PredicateBuilder<>();
	}

	/** Creates a new {@link ComparatorBuilder}, and returns it.
	 * @param <O> Bean type of the created {@link ComparatorBuilder}.
	 * @return The created builder.
	 */
	public static <O> ComparatorBuilder<O> comparator() {
		return new ComparatorBuilder<>();
	}

	/** Creates a new predicate, which tests, if an object in a collection
	 * objects is within the specified limit.
	 * @param <O> The bean type, on which the generated predicate
	 *   is being applied to.
	 * @param pOffset Specifies, that the given number of objects should
	 *   be skipped. Use 0, or -1, for no offset.
	 * @param pLimit Specifies the maximum number of permitted objects.
	 *   Use 0, or -1 for unlimited.
	 * @return The created predicate.
	 */
	public static <O> Predicate<O> limit(final int pOffset, final int pLimit) {
		return new Predicate<O>() {
			int offset = pOffset;
			int limit = pLimit;
			@Override
			public boolean test(O t) {
				if (offset > 0) {
					--offset;
					return false;
				}
				if (limit > 0) {
					return limit-- > 0;
				}
				return limit == -1;
			}
		};
	}

	/** Creates a string predicate by applying the given filter string:
	 * If the filter string is null, or empty, then a null predicate
	 * is being returned.
	 * Otherwise, if the filter string contains either of the characters
	 * '*', '%', or '?', then the predicate is created using
	 * {@link Strings#matcher(String)}.
	 * Otherwise, a case-insensitive predicate is created using
	 * {@link String#contains(CharSequence)}.
	 * @param pFilterStr The filter string (A description of the
	 *   created preducate).
	 * @return The created predicate, if any, or null.
	 */
	public static Predicate<String> asStringPredicate(String pFilterStr) {
		if (pFilterStr != null) {
			final String filterStr = pFilterStr.trim().replace('%', '*');
			if (filterStr.length() > 0) {
				if (filterStr.indexOf('*') >= 0  ||  filterStr.indexOf('?') >= 0) {
					if (filterStr.endsWith("/i")) {
						return Strings.matcher(filterStr);
					} else {
						return Strings.matcher(filterStr + "/i");
					}
				} else {
					final String matchStr = filterStr.toLowerCase();
					return (s) -> s.toLowerCase().contains(matchStr);
				}
			}
		}
		return null;
	}

	/** Creates a nullsafe comparator, which applies the given
	 * {@code pMapper} to extract property values from the compared
	 * objects, and applying the given {@code pComparator} on the
	 * extracted property values.
	 * @param <T> Result type of the mapper. Also, the type of the
	 *   comparator.
	 * @param <O> Bean type, on which the mapper is being applied.
	 * @param pMapper The mapper, which is used to extract the property
	 *    values.
	 * @param pAscending True, if the created comparator shall be used
	 *   for an ascending order: otherwise false (descending).
	 * @param pComparator The comparator, which compares the extracted
	 *   values.
	 * @return The created comparator.
	 */
	public static <O,T> Comparator<O> asNullSafeComparator(Function<O,T> pMapper,
			boolean pAscending, Comparator<T> pComparator) {
		final @NonNull Function<O,T> mapper = Objects.requireNonNull(pMapper, "Mapper");
		@SuppressWarnings("null")
		final @NonNull Comparator<@NonNull T> comparator = Objects.requireNonNull(pComparator, "Comparator");
		return (o1,o2) -> {
			final @Nullable T t1 = o1 == null ? null : mapper.apply(o1);
			final @Nullable T t2 = o2 == null ? null : mapper.apply(o2);
			if (t1 == null) {
				if (t2 == null) {
					return 0;
				} else {
					return pAscending ? -1 : 1;
				}
			} else {
				if (t2 == null) {
					return pAscending ? 1 : -1;
				} else {
					return pAscending ? comparator.compare(t1,t2) : comparator.compare(t2,t1);
				}
			}
		};
	}
}
