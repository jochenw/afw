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
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;


/** Utility methods for working with lists.
 */
public class Lists {
	/** Creates a new instance.
	 */
	public Lists() {}

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

	/** A collector is basically a builder for lists.
	 * @param <O> The lists element type.
	 */
	public static class Collector<O> {
		private final Class<O> type;
		private final List<O> list = new ArrayList<>();
		/** Creates a new collector.
		 * @param pType The element type. May be null, if using
		 * {@link #toList()}, and {@link #toMutableList()} is sufficient.
		 * Must be non-null, if you intend to use {@link #toArray()}.
		 */
		Collector(Class<O> pType) {
			type = pType;
		}
		/** Adds a new element to the list, which is being created.
		 * @param pElement The element, which is being added.
		 * @return This collector.
		 */
		public Collector<O> add(O pElement) {
			list.add(pElement);
			return this;
		}
		/** Adds new elements to the list, which is being created.
		 * @param pElements The elements, which are being added.
		 * @return This collector.
		 */
		public Collector<O> add(@SuppressWarnings("unchecked") O... pElements) {
			if (pElements != null) {
				for(O element : pElements) {
					add(element);
				}
			}
			return this;
		}
		/** Converts the collected elements into an
		 * immutable list.
		 * @return The created list.
		 */
		public List<O> toList() {
			return Collections.unmodifiableList(list);
		}
		/** Converts the collected elements into a
		 * mutable list.
		 * @return The created list.
		 */
		public List<O> toMutableList() {
			return new ArrayList<>(list);
		}
		/** Converts the collected elements into an
		 * array.
		 * @return The created array.
		 */
		public O[] toArray() {
			if (type == null) {
				throw new IllegalStateException("The element type is null.");
			}
			@SuppressWarnings("unchecked")
			final O[] array = (O[]) Array.newInstance(type, list.size());
			return list.toArray(array);
		}
		/** Invokes the given consumer for every collected element.
		 * @param pConsumer The consumer, which is being invoked
		 *   for every element.
	     * @throws NullPointerException The parameter is null.
		 */
		public void forEach(Consumer<O> pConsumer) {
			final Consumer<O> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			list.forEach(consumer);
		}
		/** Invokes the given consumer for every collected element.
		 * @param pConsumer The consumer, which is being invoked
		 *   for every element.
		 * @param <T> Type of the exceptions, that the consumer
		 *   may throw.
	     * @throws NullPointerException The parameter is null.
	     * @throws T An exception, which may be thrown by the
	     *   consumer.
		 */
		public <T extends Throwable> void forEach(FailableConsumer<O,T> pConsumer) throws T {
			final FailableConsumer<O,T> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			for (O o : list) {
				consumer.accept(o);
			}
		}
	}

	/** Creates a new {@link Collector}. The method
	 * {@link Collector#toArray()} won't work for the created
	 * collector.
	 * @param <O> The collectors element type.
	 * @return The created collector.
	 */
	public static <O> Collector<O> collect() {
		return new Collector<>(null);
	}

	/** Creates a new {@link Collector}. The method
	 * {@link Collector#toArray()} will be supported
	 * by the created collector.
	 * @param pType The collectors element type. Must not be null.
	 * @param <O> The collectors element type.
	 * @return The created collector.
	 * @throws NullPointerException The element type parameter is null.
	 */
	public static <O> Collector<O> collect(Class<O> pType) {
		return new Collector<>(Objects.requireNonNull(pType, "Type"));
	}

	/** Returns a mutable, non-null list with the given elements.
	 * @param pValues The list elements.
	 * @param <O> Type of the list elements.
	 * @return A mutable, non-null list with the given elements.
	 */
	@SafeVarargs
	public static <O >@NonNull List<O> asList(O... pValues) {
		if (pValues == null) {
			return new ArrayList<>();
		} else {
			final List<O> list = new ArrayList<>(pValues.length);
			for (O o : pValues) {
				list.add(o);
			}
			return list;
		}
	}

	/** Converts the given array of values into an {@link Iterable}.
	 * @param <O> The element type of the input array, and of the
	 * created {@link Iterable}.
	 * @param pValues The elements, over which the created
	 *   {@link Iterable} should iterate.
	 * @return The created {@link Iterable}.
	 */
	public static <O> Iterable<O> iter(@SuppressWarnings("unchecked") O... pValues) {
		return iter((o) -> o, pValues);
	}

	/** Converts the given array of values into an {@link Iterable}.
	 * @param <O> The element type of the input array.
	 * @param <E> The element type of the created {@link Iterable}.
	 * @param pMapper A mapping function, which converts the
	 *   array elements into the elements of the created
	 *   {@link Iterable}. The mapping function will be invoked
	 *   for every element of the input array.
	 * @param pValues The input elements. The mapping function will
	 *   be invoked for every element.
	 * @return The created {@link Iterable}.
	 */
	public static <O,E> Iterable<E> iter(Function<O,E> pMapper, @SuppressWarnings("unchecked") O... pValues) {
		final Function<O,E> mapper = Objects.requireNonNull(pMapper, "Mapper");
		final O[] values = Objects.requireNonNull(pValues, "Values");
		return new Iterable<E>() {
			@Override
			public Iterator<E> iterator() {
				return new Iterator<E>() {
					int i;

					@Override
					public boolean hasNext() {
						return values.length > i;
					}

					@Override
					public E next() {
						return mapper.apply(values[i++]);
					}
				};
			}
		};
	}

	/** Converts the given set of values into an {@link Iterable}.
	 * @param <O> The element type of the set of input values.
	 * @param <E> The element type of the created {@link Iterable}.
	 * @param pMapper A mapping function, which converts the
	 *   input elements into the elements of the created
	 *   {@link Iterable}. The mapping function will be invoked
	 *   for every element of the input array.
	 * @param pValues The input elements. The mapping function will
	 *   be invoked for every element.
	 * @return The created {@link Iterable}.
	 */
	public static <O,E> Iterable<E> iter(Function<O,E> pMapper, Iterable<O> pValues) {
		final Function<O,E> mapper = Objects.requireNonNull(pMapper, "Mapper");
		final Iterable<O> values = Objects.requireNonNull(pValues, "Values");
		return new Iterable<E>() {
			final Iterator<O> itertr = values.iterator();
			@Override
			public Iterator<E> iterator() {
				return new Iterator<E>() {
					@Override
					public boolean hasNext() {
						return itertr.hasNext();
					}

					@Override
					public E next() {
						return mapper.apply(itertr.next());
					}
				};
			}
		};
	}

	/** For each of the given items: Applies the given mapping
	 * function, and invokes the given action with the mapped
	 * item (the mapping functions result).
	 * @param pMapper The mapping function, which will be invoked
	 *   for every one of the given input values. The mapping
	 *   functions result will be used to invoke the action.
	 * @param pAction The action, which will be invoked for each
	 *   of the mapped values.
	 * @param pValues The input values. Each of these values will
	 *   be used to invoke the mapping function, and the mapping
	 *   functions result will be used to invoke the action.
	 * @param <O> Type of the input values.
	 * @param <E> Type of the mapping functions result. Also type
	 *   of the actions parameter.
	 * @throws NullPointerException Either of the parameters is null.
	 */
	public static <O,E> void forEach(Function<O,E> pMapper, Consumer<E> pAction,
			                         @SuppressWarnings("unchecked") O... pValues) {
		final Function<O,E> mapper = Objects.requireNonNull(pMapper, "Mapper");
		final Consumer<E> action = Objects.requireNonNull(pAction, "Action");
		final O[] values = Objects.requireNonNull(pValues, "Values");
		for (int i = 0;  i < values.length;  i++) {
			action.accept(mapper.apply(values[i]));
		}
	}

	/** For each of the given items: Invokes the given action with the
	 * item. In other words, this is equivalent to invoking
	 * {@link #forEach(Function, Consumer, Object[])} with an
	 * identity mapping function.
	 * @param pAction The action, which will be invoked for each
	 *   of the mapped values.
	 * @param pValues The input values. Each of these values will
	 *   be used to invoke the mapping function, and the mapping
	 *   functions result will be used to invoke the action.
	 * @param <O> Type of the input values.
	 * @throws NullPointerException Either of the parameters is null.
	 */
	public static <O> void forEach(Consumer<O> pAction,
			                       @SuppressWarnings("unchecked") O... pValues) {
		final Consumer<O> action = Objects.requireNonNull(pAction, "Action");
		final O[] values = Objects.requireNonNull(pValues, "Values");
		for (int i = 0;  i < values.length;  i++) {
			action.accept(values[i]);
		}
	}

	/** For each of the given items: Applies the given mapping
	 * function, and invokes the given action with the mapped
	 * item (the mapping functions result).
	 * @param pMapper The mapping function, which will be invoked
	 *   for every one of the given input values. The mapping
	 *   functions result will be used to invoke the action.
	 * @param pAction The action, which will be invoked for each
	 *   of the mapped values.
	 * @param pValues The input values. Each of these values will
	 *   be used to invoke the mapping function, and the mapping
	 *   functions result will be used to invoke the action.
	 * @param <O> Type of the input values.
	 * @param <E> Type of the mapping functions result. Also type
	 *   of the actions parameter.
	 * @throws NullPointerException Either of the parameters is null.
	 */
	public static <O,E> void forEach(Function<O,E> pMapper, Consumer<E> pAction,
			                         Iterable<O> pValues) {
		final Function<O,E> mapper = Objects.requireNonNull(pMapper, "Mapper");
		final Consumer<E> action = Objects.requireNonNull(pAction, "Action");
		final Iterable<O> values = Objects.requireAllNonNull(pValues, "Values");
		for (O o : values) {
			action.accept(mapper.apply(o));
		}
	}

}
