package com.github.jochenw.afw.vdn.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;

/** Interface of an object, which handles persistence. (Creating new beans,
 * Updating, or deleting existent beans.)
 */
public abstract class Persistor<T,V> {
	/** Interface of a listener, which is being notified about
	 * persistence related events.
	 */
	public static abstract class Listener<T> {
		/** Creates a new instance.
		 */
		protected Listener() {}

		/** Called, if a new bean has been inserted into the
		 * persistent storage.
		 * @param pBean The newly inserted bean, with a non-null id.
		 */
		public void inserted(T pBean) {}
		/** Called, if a new bean has been updated in the
		 * persistent storage.
		 * @param pBean The recently updated bean, with the new
		 *   field values.
		 */
		public void updated(T pBean) {}
		/** Called, if a new bean has been removed from the
		 * persistent storage.
		 * @param pBean The recently deleted bean.
		 */
		public void deleted(T pBean) {}
	}
	/** Creates a new instance.
	 */
	protected Persistor() {}
	/** Returns the Id of a bean of type T.
	 * @param pBean The bean, for which to return the id.
	 * @return The given bean's id, if any, or null.
	 *   The null value indicates, that the bean has
	 *   not yet been persisted.
	 */
	public abstract V getId(T pBean);
	/** Inserts the given bean into the persistent storage.
	 * The given bean is supposed to have no id.
	 * ({@link #getId(Object)} would return null.)
	 * Returns a representation of the persisted bean,
	 * with a generated id. The caller is supposed to
	 * discard the given bean, and replace it with the
	 * result.
	 * @param pBean The bean, which is being inserted, with
	 *   {@link #getId(Object) id} null. The caller is
	 *   supposed to discard this input bean, and use
	 *   the methods result object instead.
	 * @return The persisted bean, with a non-null
	 * {@link #getId(Object)}. This may, or may not be
	 * the same instance than {@code pBean}.
	 */
	public abstract T insert(T pBean);
	/** Updates the given bean in the persistent stage.
	 * The beans id <em>cannot</em> be changed.
	 * @param pBean The bean, which is being changed, with the
	 *   new attribute values.
	 * @throws NoSuchElementException The given bean's id
	 *   is not found in the persistent storage.
	 */
	public abstract void update(T pBean);
	/** Deletes the given bean from the persistent stage.
	 * @param pBean The bean, which is being changed, with the
	 *   new attribute values. The caller is supposed
	 *   to discard the bean after calling, because it
	 *   is assumed not to exist any longer.
	 * @throws NoSuchElementException The given bean's id
	 *   is not found in the persistent storage.
	 */
	public abstract void delete(T pBean);
	/** Adds a new listener to the persistor, which is
	 * being notified in the case of persistence related
	 * events.
	 * @param pListener The listener, which is being
	 *   added.
	 */
	public abstract void add(Listener<T> pListener);
	/** Removes a listener from the persistor.
	 * @param pListener The listener, which is being
	 *   removed.
	 */
	public abstract void remove(Listener<T> pListener);
	/** Returns, whether this {@link Persistor}
	 * is read-only. If so, then the write operations
	 * {@link #insert(Object)}, {@link #update(Object)},
	 * and {@link #delete(Object)} will throw an
	 * {@link UnsupportedOperationException}.
	 * A read-only persistor can easily be created
	 * by using #of(Functions.FailableConsumer).
	 * @see #readAll(FailableConsumer)
	 * @return True, if this persistor supports
	 *   the write operations
	 *   {@link #insert(Object)},
	 *   {@link #update(Object)}, and
	 *   {@link #delete(Object)}.
	 */
	public abstract boolean isReadOnly();

	/** Returns a list of all items from the persistent
	 * storage. Basically, this is a convenience frontend
	 * for {@link #readAll(FailableConsumer)}.
	 * @return The list of all items, that have been
	 *   read.
	 */
	public List<T> getAllItems() {
		final List<T> list = new ArrayList<>();
		readAll((t) -> list.add(t));
		return list;
	}

	/** Reads all items, and passes them to
	 * the given {@link FailableConsumer}.
	 * @param pConsumer A consumer, which
	 *   is being invoked, in turn, for all
	 *   items in the persistent storage.
	 * @see #getAllItems()
	 */
	public abstract void readAll(FailableConsumer<T,?> pConsumer);

	/** Creates a new, blank instance of T,
	 * with a null id.
	 * @return The created instance.
	 */
	public abstract T newBean();

	/** Creates a new persistor, which is read-only: Only
	 * {@link #getAllItems()}, and {@link #readAll(FailableConsumer)},
	 * are supported, and {@link #isReadOnly()} will return true.
	 * @param <T> Type of the beans, which are handled by the
	 *   created persistor.
	 * @param <I> Type of the keys, which identify the beans,
	 *   that are handled by the created persistor.
	 * @param pReader The implementation for {@link #readAll(FailableConsumer)},
	 *   thus indirectly the implementation for {@link #getAllItems()}. 
	 * @param pIdMapper A {@link Function}, which maps the persisted
	 *   items to their id. Internally, this implements
	 *   {@link #getId(Object)}.
	 * @return The created persistor.
	 */
	public static <T,I> Persistor<T,I> of (@NonNull FailableConsumer<FailableConsumer<T,?>, ?> pReader,
			                               @NonNull Function<T,I> pIdMapper) {
		final Builder<T,I> builder = builder();
		return builder.reader(pReader).idMapper(pIdMapper).get();
	}

	/** Creates a new {@link Builder}, as a means to implement a
	 * {@link Persistor}.
	 * @param <T> Type of the beans, which are handled by the
	 *   created persistor.
	 * @param <I> Type of the keys, which identify the beans,
	 *   that are handled by the created persistor.
	 * @return The created builder.
	 */
	public static <T,I> Builder<T,I> builder() {
		return new Builder<T,I>();
	}

	/** A builder class for persistors. Use {@link Persistor#builder()}
	 * to obtain one.
	 */
	public static class Builder<T,I> {
		/** Creates a new instance.
		 */
		protected Builder() {}

		private @Nullable FailableConsumer<FailableConsumer<T,?>,?> reader;
		private @Nullable Function<T,I> idMapper;
		private @Nullable Supplier<T> supplier;
		private @Nullable FailableFunction<T,T,?> inserter;
		private @Nullable FailableConsumer<T,?> updater, deleter;


		/** Sets the reader, as an implementation of
		 * {@link Persistor#readAll(FailableConsumer)}.
		 * @param pReader The reader, which implements
		 *   {@link Persistor#readAll(FailableConsumer)}.
		 * @return This builder.
		 */
		public Builder<T,I> reader(FailableConsumer<FailableConsumer<T,?>, ?> pReader) {
			reader = pReader;
			return this;
		}

		/** Returns the reader, which has been supplied as
		 * the implementation of {@link Persistor#readAll(FailableConsumer)}.
		 * @return The reader, which has previously been supplied
		 *   by invoking {@link #insert(FailableFunction)},
		 *   or null.
		 */
		public FailableConsumer<FailableConsumer<T, ?>, ?> getReader() {
			return reader;
		}

		/** Sets the supplier, as an implementation of
		 * {@link Persistor#newBean()}.
		 * @param pSupplier The supplier, which implements
		 *   {@link Persistor#newBean()}.
		 * @return This builder.
		 */
		public Builder<T,?> supplier(Supplier<T> pSupplier) {
			supplier = pSupplier;
			return this;
		}

		/** Returns the supplier, which has been specified as
		 * the implementation of {@link Persistor#newBean()}.
		 * @return The supplier, which has previously been specified
		 *   by invoking {@link #supplier(Supplier)},
		 *   or null.
		 */
		public Supplier<T> getSupplier() {
			return supplier;
		}

		/** Sets the mapper, as an implementation of
		 * {@link Persistor#getId(Object)}.
		 * @param pMapper The mapper, which implements
		 *   {@link Persistor#getId(Object)}.
		 * @return This builder.
		 */
		public Builder<T,I> idMapper(Function<T,I> pMapper) {
			idMapper = pMapper;
			return this;
		}

		/** Returns the mapper, which has been supplied as
		 * the implementation of {@link Persistor#getId(Object)}.
		 * @return The mapper, which has previously been supplied
		 *   by invoking {@link #idMapper(Function)},
		 *   or null.
		 */
		public Function<T, I> getIdMapper() {
			return idMapper;
		}

		/** Sets the inserter, as an implementation of
		 * {@link Persistor#insert(Object)}.
		 * @param pInserter The inserter, which implements
		 *   {@link Persistor#insert(Object)}.
		 * @return This builder.
		 */
		public Builder<T,I> insert(FailableFunction<T,T,?> pInserter) {
			inserter = pInserter;
			return this;
		}

		/** Returns the inserter, which has been supplied as
		 * the implementation of {@link Persistor#insert(Object)}.
		 * @return The inserter, which has previously been supplied
		 *   by invoking {@link #insert(Functions.FailableFunction)},
		 *   or null.
		 */
		public FailableFunction<T, T, ?> getInserter() {
			return inserter;
		}

		/** Sets the updater, as an implementation of
		 * {@link Persistor#update(Object)}.
		 * @param pUpdater The updater, which implements
		 *   {@link Persistor#update(Object)}.
		 * @return This builder.
		 */
		public Builder<T,I> update(FailableConsumer<T,?> pUpdater) {
			updater = pUpdater;
			return this;
		}


		/** Returns the updater, which has been supplied as
		 * the implementation of {@link Persistor#update(Object)}.
		 * @return The updater, which has previously been supplied
		 *   by invoking {@link #update(FailableConsumer)}.
		 *   or null.
		 */
		public FailableConsumer<T, ?> getUpdater() {
			return updater;
		}

		/** Sets the deleter, as an implementation of
		 * {@link Persistor#delete(Object)}.
		 * @param pDeleter The deleter, which implements
		 *   {@link Persistor#delete(Object)}.
		 * @return This builder.
		 */
		public Builder<T,I> delete(FailableConsumer<T,?> pDeleter) {
			deleter = pDeleter;
			return this;
		}


		/** Returns the deleter, which has been supplied as
		 * the implementation of {@link Persistor#delete(Object)}.
		 * @return The deleter, which has previously been supplied
		 *   by invoking {@link #delete(FailableConsumer)}.
		 *   or null.
		 */
		public FailableConsumer<T, ?> getDeleter() {
			return deleter;
		}


		/** Creates a new persistor, applying the builders
		 * configuration, and returns the created instance.
		 * @return The created persistor, with the builders
		 *   configuration applied.
		 */
		public Persistor<T,I> get() {
			if (reader == null  ||  idMapper == null) {
				throw new IllegalStateException("A Persistor needs to implement, "
						+ "at least the methods readAll(FailableConsumer<T,?>), "
						+ "and getId(T), but the respective implementations "
						+ "have not been supplied. Did you invoke "
						+ "the methods reader(), and idGetter() on this "
						+ "builder?");
			}
			return new Persistor<T,I>(){
				private final List<Listener<T>> listeners = new ArrayList<>();

				@Override
				public I getId(T pBean) { return idMapper.apply(pBean); }
				@Override
				public T insert(T pBean) {
					if (inserter == null) {
						throw new UnsupportedOperationException(
								"No implementation for insert has been specified. "
								+ "Did you invoke the insert method on the "
								+ "builder?");
					}
					if (getId(pBean) != null) {
						throw new IllegalStateException("The bean has a non-null id. "
								+ "It can be updated, but not inserted.");
					}
					final T result = Functions.apply(inserter, pBean);
					if (result == null) {
						throw new NullPointerException("The insert implementation, "
								+ "which has been specified by invoking insert "
								+ "on the builder has returned null. It is "
								+ "supposed to return a non-null instance "
								+ "with a non-null, generated id.");
					}
					if (getId(result) == null) {
						throw new NullPointerException("The insert implementation, "
								+ "which has been specified by invoking insert "
								+ "on the builder has returned a result with a "
								+ " null id. It is supposed to return a non-null"
								+ " instance with a non-null, generated id.");
					}
					return result;
				}

				@Override
				public void update(T pBean) {
					if (updater == null) {
						throw new UnsupportedOperationException(
								"No implementation for update has been specified. "
								+ "Did you invoke the update method on the "
								+ "builder?");
					}
					Functions.accept(updater, pBean);
				}

				@Override
				public void delete(T pBean) {
					if (deleter == null) {
						throw new UnsupportedOperationException(
								"No implementation for delete has been specified. "
								+ "Did you invoke the delete method on the "
								+ "builder?");
					}
					Functions.accept(updater, pBean);
				}

				@Override
				public T newBean() {
					if (supplier == null) {
						throw new UnsupportedOperationException(
								"No implementation for newBean has been specified. "
								+ "Did you invoke the supplier method on the "
								+ "builder?");
					}
					final T result = supplier.get();
					if (getId(result) != null) {
						throw new IllegalStateException("The supplier returned an "
								+ " object with a non-null id. It is supposed"
								+ " to return an object with a null id, "
								+ " suitable as an argument for insert(T).");
					}
					return result;
				}
				@Override
				public void add(Listener<T> pListener) {
					synchronized (listeners) {
						listeners.add(pListener);
					}
				}

				@Override
				public void remove(Listener<T> pListener) {
					synchronized (listeners) {
						listeners.add(pListener);
					}
				}

				@Override
				public void readAll(FailableConsumer<T, ?> pConsumer) {
					Functions.accept(reader, pConsumer);
				}

				@Override
				public boolean isReadOnly() {
					return inserter == null  &&  updater == null  &&  deleter == null;
				}
			};
		}
	}
}
