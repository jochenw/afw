package com.github.jochenw.afw.core.util;

import java.util.concurrent.locks.StampedLock;

import org.jspecify.annotations.NonNull;


/** A wrapper for a hidden object, that is being protected from external access.
 * @param <T> Type of the hidden object.
 */
public class Locked<T> {
	/** Interface of an accessor for the hidden object. Access to the given
	 * object (which is in fact the hidden object) is valid only within
	 * the scope of the {@link Locked.Runnable#run(Object)} method.
	 * @param <O> Type of the argument for {@link Locked.Runnable#run(Object)}.
	 * @see Locked.Callable
	 */
	public interface Runnable<O> {
		/**
		 * Called to access the hidden object.
		 * @param pObject The idden object, which may be accessed only
		 *   within the cope of the method.
		 * @throws Exception The {@link Runnable} has failed.
		 */
		public void run(O pObject) throws Exception;
	}
	/** Interface of an accessor for the hidden object, that provides a result.
	 * Access to the given object (which is in fact the hidden object) is valid
	 * only within the scope of the {@link Locked.Runnable#run(Object)} method.
	 * @param <O> Type of the argument for {@link Locked.Runnable#run(Object)}.
	 * @param <Out> Type of result object.
	 * @see Locked.Runnable
	 */
	public interface Callable<O,Out> {
		/**
		 * Called to access the hidden object.
		 * @param pObject The idden object, which may be accessed only
		 *   within the cope of the method.
		 * @throws Exception The {@link Runnable} has failed.
		 * @return The provided result object.
		 */
		public Out call(O pObject) throws Exception;
	}
	private T object;
	private final StampedLock lock = new StampedLock();

	/**Creates a new instance with the given hidden object. 
	 * @param pObject The hidden object.
	 */
	public Locked(T pObject) {
		object = pObject;
	}

	/** Called to access the hidden object with shared privileges
	 * by invoking the given runnable.
	 * @param pRunnable The accessor, which will be invoked with the
	 * hidden object.
	 */
	public void runReadLocked(@NonNull Runnable<T> pRunnable) {
		final long l = lock.readLock();
		try {
			pRunnable.run(object);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		} finally {
			lock.unlock(l);
		}
	}

	/** Called to access the hidden object with exclusive privileges
	 * by invoking the given runnable.
	 * @param pRunnable The accessor, which will be invoked with the
	 * hidden object.
	 */
	public void runWriteLocked(@NonNull Runnable<T> pRunnable) {
		final long l = lock.readLock();
		try {
			pRunnable.run(object);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		} finally {
			lock.unlock(l);
		}
	}

	/** Called to access the hidden object with shared privileges,
	 * and provide a result object, by invoking the given callable.
	 * @param pCallable The accessor, which will be invoked with the
	 * hidden object.
	 * @param <O> Type of the result object.
	 * @return The result object, that has been provided by invoking
	 * the given callable.
	 */
	public <O> O callReadLocked(@NonNull Callable<T,O> pCallable) {
		final long l = lock.readLock();
		try {
			return pCallable.call(object);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		} finally {
			lock.unlock(l);
		}
	}

	/** Called to access the hidden object with exclusive privileges,
	 * and provide a result object, by invoking the given callable.
	 * @param pCallable The accessor, which will be invoked with the
	 * hidden object.
	 * @param <O> Type of the result object.
	 * @return The result object, that has been provided by invoking
	 * the given callable.
	 */
	public <O> O callWriteLocked(@NonNull Callable<T,O> pCallable) {
		final long l = lock.writeLock();
		try {
			return pCallable.call(object);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		} finally {
			lock.unlock(l);
		}
	}

	/** Called to create a clone of the hidden object. Access to the
	 * clone is unrestricted.
	 * @param pCloner The object that will be invoked to perform the
	 * cloning.
	 * @return The cloned object, which may be accessed without any
	 * limitations.
	 */
	public T get(Callable<T,T> pCloner) {
		return callReadLocked(pCloner);
	}

	/** Called to replace the hidden object with the given.
	 * @param pObject The new object.
	 * @return The previous hidden object.
	 */
	public T set(T pObject) {
		return callWriteLocked((t) -> {
			object = pObject;
			return t;
		});
	}
}
