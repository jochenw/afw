package com.github.jochenw.afw.core.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;

import com.github.jochenw.afw.core.util.Functions.FailableLongSupplier;
import com.github.jochenw.afw.core.util.Functions.FailableRunnable;
import com.github.jochenw.afw.core.util.Functions.FailableSupplier;


/**
 * Utility class for working with locks.
 */
public class Locks {
	/** An object, which can be locked. Use {@link Locks#newLockable()} to obtain it.
	 */
	public static class Lockable {
		private final StampedLock stampedLock = new StampedLock();

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
	public static void runLocked(FailableRunnable<?> pRunnable, FailableSupplier<Lock,?> pSupplier) {
		Lock lock = null;
		boolean locked = false;
		Throwable th = null;
		try {
			lock = pSupplier.get();
			lock.lock();
			locked = true;
			pRunnable.run();
			lock.unlock();
			locked = false;
			lock = null;
		} catch (Throwable t) {
			th = t;
		} finally {
			if (locked) {
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

	/** Creates a new {@link Lockable}.
	 * @return A new {@link Lockable}.
	 */
	public static Lockable newLockable() {
		return new Lockable();
	}
}
