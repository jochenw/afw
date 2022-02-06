package com.github.jochenw.afw.core.util;

import java.lang.reflect.Array;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Functions.FailableBiIntConsumer;

/** Interface of an object, that may be used for iterations over lists of elements,
 * like lists, or arrays.
 * @param <O> Type of an element 
 */
public interface IListable<O> {
	/** Returns the number of elements in the {@link IListable}.
	 * @return The number of elements in the {@link IListable}.
	 */
	public int getSize();
	/** Returns, whether the {@link IListable} is empty.
	 * @return True, if the {@link IListable} is empty, otherwise false.
	 */
	public default boolean isEmpty() {
		return getSize() == 0;
	}
	/** Returns a single element of the {@link IListable}.
	 * @param pIndex The element in the {@link IListable} with the given index.
	 * @throws IndexOutOfBoundsException The index is invalid (negative, or too large),
	 *   and there is no matching element.
	 * @return The requested element, possibly null.
	 */
	public @Nullable O getItem(int pIndex);
	/** Iterates over all the elements, invoking the given {@link FailableBiIntConsumer} for every
	 * element.
	 * @param pConsumer The consumer, that is being invoked. On every call, it receives two
	 *   parameters: The first parameter is the current index, and the second is the actual
	 *   element. The indexes will range from 0 to {@code getSize()-1}, included.
	 */
	public void forEach(FailableBiIntConsumer<O, ?> pConsumer);

	/** Creates a new {@link IListable}. The elements in the {@link IListable} will be the
	 * elements in the list.
	 * @param pList The list, that supplies the elements.
	 * @return The created {@link IListable}.
	 * @throws NullPointerException The parameter {@code pList} is null.
	 */
	public static <T> @Nonnull IListable<T> of(@Nonnull List<T> pList) {
		final List<T> list = Objects.requireNonNull(pList, "List");
		return new IListable<T>() {
			@Override
			public int getSize() {
				return list.size();
			}

			
			@Override
			public boolean isEmpty() {
				return list.isEmpty();
			}


			@Override
			public T getItem(int pIndex) {
				return list.get(pIndex);
			}

			@Override
			public void forEach(FailableBiIntConsumer<T, ?> pConsumer) {
				final FailableBiIntConsumer<T, ?> consumer = Objects.requireNonNull(pConsumer, "Consumer");
				for (int i = 0;  i < list.size();  i++) {
					try {
						consumer.accept(i, list.get(i));
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				}
			}
		};
	}

	/** Creates a new {@link IListable}. The elements in the {@link IListable} are given
	 * as method arguments.
	 * @param pValues The elements.
	 * @return The created {@link IListable}.
	 * @throws NullPointerException The parameter {@code pList} is null.
	 */
	public static <T> @Nonnull IListable<T> of(@SuppressWarnings("unchecked") T... pValues) {
		final T[] values = Objects.requireNonNull(pValues, "Values");
		return new IListable<T>() {
			@Override
			public int getSize() {
				return values.length;
			}

			
			@Override
			public boolean isEmpty() {
				return values.length == 0;
			}


			@Override
			public T getItem(int pIndex) {
				return values[pIndex];
			}

			@Override
			public void forEach(FailableBiIntConsumer<T, ?> pConsumer) {
				final FailableBiIntConsumer<T, ?> consumer = Objects.requireNonNull(pConsumer, "Consumer");
				for (int i = 0;  i < values.length;  i++) {
					try {
						consumer.accept(i, values[i]);
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				}
			}
		};
	}

	/** Creates a new {@link IListable}. The elements in the {@link IListable} are given
	 * as an array parameter. Arbitrary arrays are supported, including primitive values.
	 * @param pArray The element array.
	 * @return The created {@link IListable}.
	 * @throws NullPointerException The parameter {@code pArray} is null.
	 * @throws IllegalArgumentException The parameter {@code pArray} is no array.
	 */
	public static <T> @Nonnull IListable<T> of(Object pArray) {
		final Object array = Objects.requireNonNull(pArray, "Array");
		if (!array.getClass().isArray()) {
			throw new IllegalArgumentException("The parameter pArray is not an array, but a "
					+ array.getClass().getName());
		}
		final int length = Array.getLength(array);
		return new IListable<T>() {
			@Override
			public int getSize() {
				return length;
			}

			
			@Override
			public boolean isEmpty() {
				return length == 0;
			}


			@Override
			public T getItem(int pIndex) {
				@SuppressWarnings("unchecked")
				final T t = (T) Array.get(array, pIndex);
				return t;
			}

			@Override
			public void forEach(FailableBiIntConsumer<T, ?> pConsumer) {
				final FailableBiIntConsumer<T, ?> consumer = Objects.requireNonNull(pConsumer, "Consumer");
				for (int i = 0;  i < length;  i++) {
					@SuppressWarnings("unchecked")
					final T t = (T) Array.get(array, i);
					try {
						consumer.accept(i, t);
					} catch (Throwable th) {
						throw Exceptions.show(th);
					}
				}
			}
		};
	}
}
