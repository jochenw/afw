package com.github.jochenw.afw.core.util;

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
			Locks.run(stampedLock, false, pRunnable);
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
			return Locks.call(stampedLock, false, pCallable);
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
			return Locks.call(stampedLock, true, pCallable);
		}
		/**
		 * Creates an exclusive lock (write lock) on this {@link Lockable}, and
		 * executes the given {@link FailableRunnable}, while still holding the
		 * lock. Guarantees, that the read lock is released afterwards.
		 * @param pRunnable The runnable being executed.
		 */
		public void runWriteLocked(FailableRunnable<?> pRunnable) {
			Locks.run(stampedLock, true, pRunnable);
		}
	}

	/** Creates a new instance, Private, to avoid accidental instantiation.
	 * This constructor might be removed, it is mainly present to avoid a Javadoc
	 * warning with JDK 21.
	 */
	private Locks() {}

	/** Creates a new {@link Lockable}.
	 * @return A new {@link Lockable}.
	 */
	public static @NonNull Lockable newLockable() {
		return new Lockable();
	}

	/**
	 * Obtains a lock on an object by invoking the given
	 * {@link FailableSupplier} lock supplier, and
	 * executes the given {@link FailableRunnable}, while
	 * still holding the lock. Guarantees, that the read
	 * lock is released afterwards.
	 * @param pRunnable The runnable being executed.
	 * @param pLock The lock object.
	 * @param pExclusive True, if the created lock should be exclusive. 
	 */
	public static void run(StampedLock pLock, boolean pExclusive, FailableRunnable<?> pRunnable) {
		long lock = 0l;
		boolean locked = false;
		Throwable th = null;
		try {
			if (pExclusive) {
				lock = pLock.writeLock();
			} else {
				lock = pLock.readLock();
			}
			locked = true;
			pRunnable.run();
			pLock.unlock(lock);
			locked = false;
		} catch (Throwable t) {
			th = t;
		} finally {
			if (locked) {
				try {
					pLock.unlock(lock);
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
	 * Returns the callable's result.
	 * @param <O> The return type (both of the callable, and the method).
	 * @param pCallable The callable being called.
	 * @param pLock The lock object.
	 * @param pExclusive True, if the created lock should be exclusive. 
	 * @return The result object, which has been obtained by
	 *   invoking the callable.
	 */
	public static <O> @Nullable O call(StampedLock pLock, boolean pExclusive, FailableCallable<@Nullable O,?> pCallable) {
		long lock = 0l;
		boolean locked = false;
		@Nullable Throwable th = null;
		@Nullable O o = null;
		try {
			if (pExclusive) {
				lock = pLock.writeLock();
			} else {
				lock = pLock.readLock();
			}
			locked = true;
			o = pCallable.call();
			pLock.unlock(lock);
			locked = false;
		} catch (Throwable t) {
			th = t;
		} finally {
			if (locked) {
				try {
					pLock.unlock(lock);
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
