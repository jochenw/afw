package com.github.jochenw.afw.core.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;

import com.github.jochenw.afw.core.util.Functions.FailableCallable;
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
		 * Creates an exclusive lock (write lock) on this {@link Lockable},
		 * and returns the result of calling the given {@link FailableCallable}.
		 * The call is performed, while still holding the lock. Guarantees, that
		 * the write lock is released afterwards.
		 * @return The result object, which has been returned by the callable.
		 * @param pCallable The {@link FailableCallable} to call.
		 */
		public <O,T> O callReadLocked(FailableCallable<O,?> pCallable) {
			return callLocked(() -> stampedLock.readLock(), pCallable);
		}
		/**
		 * Creates an exclusive lock (write lock) on this {@link Lockable},
		 * and returns the result of calling the given {@link FailableCallable}.
		 * The call is performed, while still holding the lock. Guarantees, that
		 * the write lock is released afterwards.
		 * @return The result object, which has been returned by the callable.
		 * @param pCallable The {@link FailableCallable} to call.
		 */
		public <O,T> O callWriteLocked(FailableCallable<O,?> pCallable) {
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
		 * @param pCallable The callable being called.
		 * @param pSupplier The supplier, which creates the lock.
		 */
		public <O> O callLocked(FailableCallable<O,?> pCallable, FailableSupplier<Lock,?> pSupplier) {
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
		 * @param pCallable The callable being called.
		 * @param pSupplier The supplier, which creates the lock.
		 */
		protected <O> O callLocked(FailableLongSupplier<?> pSupplier, FailableCallable<O,?> pCallable) {
			Long lock = null;
			Throwable th = null;
			O o = null;
			try {
				lock = Long.valueOf(pSupplier.get());
				o = pCallable.call();
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
			return o;
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

	/**
	 * Obtains a lock on an object by invoking the given
	 * {@link FailableSupplier} lock supplier, and
	 * calls the given {@link FailableCallable}, while
	 * still holding the lock. Guarantees, that the lock
	 * is released afterwards. Returns the callables
	 * result.
	 * @return The result object, which has been
	 *   returned by the callable.
	 * @param pCallable The callable being called.
	 * @param pSupplier The supplier, which creates the lock.
	 */
	public static <O> O callLocked(FailableCallable<O,?> pCallable, FailableSupplier<Lock,?> pSupplier) {
		Lock lock = null;
		boolean locked = false;
		Throwable th = null;
		O o = null;
		try {
			lock = pSupplier.get();
			lock.lock();
			locked = true;
			o = pCallable.call();
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
		return o;
	}

	/** Creates a new {@link Lockable}.
	 * @return A new {@link Lockable}.
	 */
	public static Lockable newLockable() {
		return new Lockable();
	}
}
