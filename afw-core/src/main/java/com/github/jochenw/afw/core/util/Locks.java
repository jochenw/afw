package com.github.jochenw.afw.core.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions.FailableCallable;
import com.github.jochenw.afw.core.function.Functions.FailableLongSupplier;
import com.github.jochenw.afw.core.function.Functions.FailableRunnable;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;


/**
 * Utility class for working with locks.
 */
public class Locks {
	/** An object, which can be locked. Use {@link Locks#newLockable()} to obtain it.
	 */
	public static class Lockable {
		private final StampedLock stampedLock = new StampedLock();

		/** Creates a new instance,
		 * This constructor might be removed, it is mainly present to avoid a Javadoc
		 * warning with JDK 21.
		 */
		Lockable() {}

		/**
		 * Creates a shared lock (read lock) on this {@link Lockable}, and executes
		 * the given {@link FailableRunnable}, while still holding the lock. Guarantees,
		 * that the read lock is released afterwards.
		 * @param pRunnable The runnable being executed.
		 */
		public void runReadLocked(FailableRunnable<?> pRunnable) {
			runLocked(() -> stampedLock.readLock(), pRunnable);
		}

		/**
		 * Creates an exclusive lock (write lock) on this {@link Lockable},
		 * and returns the result of calling the given {@link FailableCallable}.
		 * The call is performed, while still holding the lock. Guarantees, that
		 * the write lock is released afterwards.
		 * @return The result object, which has been returned by the callable.
		 * @param <O> The result type (of the callable, and this method)
		 * @param pCallable The {@link FailableCallable} to call.
		 */
		public <O> @Nullable O callReadLocked(FailableCallable<@Nullable O,?> pCallable) {
			return callLocked(() -> stampedLock.readLock(), pCallable);
		}
		/**
		 * Creates an exclusive lock (write lock) on this {@link Lockable},
		 * and returns the result of calling the given {@link FailableCallable}.
		 * The call is performed, while still holding the lock. Guarantees, that
		 * the write lock is released afterwards.
		 * @return The result object, which has been returned by the callable.
		 * @param <O> The result type (of the callable, and this method)
		 * @param pCallable The {@link FailableCallable} to call.
		 */
		public <O> @Nullable O callWriteLocked(FailableCallable<@Nullable O,?> pCallable) {
			return callLocked(() -> stampedLock.writeLock(), pCallable);
		}
		/**
		 * Creates an exclusive lock (write lock) on this {@link Lockable}, and
		 * executes the given {@link FailableRunnable}, while still holding the
		 * lock. Guarantees, that the read lock is released afterwards.
		 * @param pRunnable The runnable being executed.
		 */
		public void runWriteLocked(FailableRunnable<?> pRunnable) {
			runLocked(() -> stampedLock.writeLock(), pRunnable);
		}
		/**
		 * Obtains a lock on an object by invoking the given
		 * {@link FailableSupplier} lock supplier, and
		 * executes the given {@link FailableRunnable}, while
		 * still holding the lock. Guarantees, that the read
		 * lock is released afterwards.
		 * @param pRunnable The runnable being executed.
		 * @param pSupplier The supplier, which creates the lock.
		 */
		public void runLocked(FailableRunnable<?> pRunnable, FailableSupplier<Lock,?> pSupplier) {
			Locks.runLocked(pRunnable, pSupplier);
		}
		/**
		 * Obtains a lock on an object by invoking the given
		 * {@link FailableSupplier} lock supplier, and
		 * executes the given {@link FailableCallable}, while
		 * still holding the lock. Guarantees, that the read
		 * lock is released afterwards. Returns the callables
		 * result.
		 * @return The result object, which has been returned
		 *   by calling the callable.
		 * @param <O> The return type (of the callable, and
		 *   of this method).
		 * @param pCallable The callable being called.
		 * @param pSupplier The supplier, which creates the lock.
		 */
		public <O> @Nullable O callLocked(FailableCallable<@Nullable O,?> pCallable, FailableSupplier<Lock,?> pSupplier) {
			return Locks.callLocked(pCallable, pSupplier);
		}
		/**
		 * Obtains a lock on an object by invoking the given
		 * {@link FailableSupplier} lock supplier, and
		 * executes the given {@link FailableRunnable}, while
		 * still holding the lock. Guarantees, that the read
		 * lock is released afterwards.
		 * @param pRunnable The runnable being executed.
		 * @param pSupplier The supplier, which creates the lock.
		 */
		protected void runLocked(FailableLongSupplier<?> pSupplier, FailableRunnable<?> pRunnable) {
			Long lock = null;
			Throwable th = null;
			try {
				lock = Long.valueOf(pSupplier.get());
				pRunnable.run();
				stampedLock.unlock(lock.longValue());
				lock = null;
			} catch (Throwable t) {
				th = t;
			} finally {
				if (lock != null) {
					try {
						stampedLock.unlock(lock.longValue());
					} catch (Throwable t) {
						if (th == null) {
							th = t;
						}
					}
				}
			}
			if (th != null) {
				throw Exceptions.show(th);
			}
		}
		/**
		 * Obtains a lock on an object by invoking the given
		 * {@link FailableLongSupplier} lock supplier, and
		 * calls the given {@link FailableCallable}, while
		 * still holding the lock. Guarantees, that the lock
		 * is released afterwards.
		 * Returns the callables result.
		 * @param <O> The return type (both of the callable, and the method).
		 * @param pCallable The callable being called.
		 * @param pSupplier The supplier, which creates the lock.
		 * @return The result object, which has been obtained by
		 *   invoking the callable.
		 */
		protected <O> @Nullable O callLocked(FailableLongSupplier<?> pSupplier, FailableCallable<@Nullable O,?> pCallable) {
			@Nullable Long lock = null;
			@Nullable Throwable th = null;
			@Nullable O o = null;
			try {
				@SuppressWarnings("null")
				final @NonNull Long l = Long.valueOf(pSupplier.get());
				lock = l;
				o = pCallable.call();
				final long lck = lock.longValue();
				lock = null;
				stampedLock.unlock(lck);
			} catch (Throwable t) {
				th = t;
			} finally {
				if (lock != null) {
					try {
						stampedLock.unlock(lock.longValue());
					} catch (Throwable t) {
						if (th == null) {
							th = t;
						}
					}
				}
			}
			if (th != null) {
				throw Exceptions.show(th);
			}
			return o;
		}
	}

	/** Creates a new instance, Private, to avoid accidental instantiation.
	 * This constructor might be removed, it is mainly present to avoid a Javadoc
	 * warning with JDK 21.
	 */
	private Locks() {}

	/**
	 * Obtains a lock on an object by invoking the given
	 * {@link FailableSupplier} lock supplier, and
	 * executes the given {@link FailableRunnable}, while
	 * still holding the lock. Guarantees, that the read
	 * lock is released afterwards.
	 * @param pRunnable The runnable being executed.
	 * @param pSupplier The supplier, which creates the lock.
	 */
	public static void runLocked(FailableRunnable<?> pRunnable, FailableSupplier<Lock,?> pSupplier) {
		@Nullable Lock lock = null;
		@Nullable Throwable th = null;
		try {
			lock = Objects.requireNonNull(pSupplier.get(),
					                      "The lock supplier returned a null value.");
			lock.lock();
			pRunnable.run();
			final Lock lck = lock;
			lock = null;
			lck.unlock();
		} catch (Throwable t) {
			th = t;
		} finally {
			if (lock != null) {
				try {
					lock.unlock();
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
		}
		if (th != null) {
			throw Exceptions.show(th);
		}
	}

	/**
	 * Obtains a lock on an object by invoking the given
	 * {@link FailableSupplier} lock supplier, and
	 * calls the given {@link FailableCallable}, while
	 * still holding the lock. Guarantees, that the lock
	 * is released afterwards. Returns the callables
	 * result.
	 * @return The result object, which has been
	 *   returned by the callable.
	 * @param <O> The result type (both, of the callable, and this method)
	 * @param pCallable The callable being called.
	 * @param pSupplier The supplier, which creates the lock.
	 */
	public static <O> @Nullable O callLocked(FailableCallable<@Nullable O,?> pCallable, FailableSupplier<Lock,?> pSupplier) {
		@Nullable Lock lock = null;
		@Nullable Throwable th = null;
		@Nullable O o = null;
		try {
			lock = Objects.requireNonNull(pSupplier.get(),
					                      "The supplier returned a non-null value.");
			lock.lock();
			o = pCallable.call();
			final Lock lck = lock;
			lock = null;
			lck.unlock();
		} catch (Throwable t) {
			th = t;
		} finally {
			if (lock != null) {
				try {
					lock.unlock();
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
		}
		if (th != null) {
			throw Exceptions.show(th);
		}
		return o;
	}

	/** Creates a new {@link Lockable}.
	 * @return A new {@link Lockable}.
	 */
	public static @NonNull Lockable newLockable() {
		return new Lockable();
	}
}
