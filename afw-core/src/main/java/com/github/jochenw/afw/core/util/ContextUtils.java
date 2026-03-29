package com.github.jochenw.afw.core.util;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;


/** A helper class, which provides static utility methods for
 * working with context objects.
 */
public class ContextUtils {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	public ContextUtils() {}

	/** Interface of void method, which is being called with a
	 * context object as the sole argument.
	 * @param <C> Type of the context object.
	 */
	@FunctionalInterface
	public interface IRunnable<C> {
		/** Called to make the runnable execute.
		 * @param pCtx The context object.
		 * @throws Throwable The runnable has failed.
		 */
		public void run(C pCtx) throws Throwable;
	}
	/** Interface of a method, which is being called with a
	 * context object as the sole argument, producing an output
	 * object.
	 * @param <C> Type of the context object.
	 * @param <O> Type of the output object.
	 */
	@FunctionalInterface
	public interface ICallable<C,O> {
		/** Called to execute the callable, and
		 * have it return the output object.
		 * @param pCtx The context object.
		 * @return The output object.
		 * @throws Throwable The callable has failed.
		 */
		public O call(C pCtx) throws Throwable;
	}

	/** Interface of an object, which can provide context objects,
	 * that are encapsulated in method calls.
	 * @param <C> Type of the context object. 
	 */
	public interface IContextProvider<C> {
		/** Calls the given runnable, providing the context object
		 * as argument.
		 * @param pRunnable The runnable, which is being invoked.
		 */
		public void run(IRunnable<C> pRunnable);
		/** Calls the given callable, providing the context object
		 * as argument, and returns the callables output object
		 * as a result.
		 * @param pCallable The runnable, which is being invoked.
		 * @param <O> Type of the output object.
		 * @return The output object, which has been returned
		 *   by invoking the callable.
		 */
		public <O> O call(ICallable<C,O> pCallable);
	}

	/** Creates a new context provider. If necessary, the context
	 * provider will invoke the given supplier to obtain the
	 * context object. The context provider will ensure, that
	 * the context objects {@link AutoCloseable#close()} method
	 * will be invoked.
	 * @param <C> Type of the context objects.
	 * @param pSupplier The supplier of the context object, which
	 *   will be invoked, whenever the context providers
	 *   {@link IContextProvider#run(ContextUtils.IRunnable) run}, or
	 *   {@link IContextProvider#call(ContextUtils.ICallable) call}
	 *   methods are being invoked.
	 * @return The created context provider.
	 */
	public static <C extends AutoCloseable> IContextProvider<C> of(FailableSupplier<C,?> pSupplier) {
		return new IContextProvider<C>() {
			@Override
			public void run(IRunnable<C> pRunnable) {
				try (C c = pSupplier.get()) {
					pRunnable.run(c);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}

			@Override
			public <O> O call(ICallable<C, O> pCallable) {
				try (C c = pSupplier.get()) {
					return pCallable.call(c);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
		};
	}

	/** Creates a new context provider. If necessary, the context
	 * provider will invoke the given supplier to obtain the
	 * context object. The context provider will ensure, that
	 * the terminator is invoked, when the context object is no
	 * longer required.
	 * @param <C> Type of the context objects.
	 * @param pSupplier The supplier of the context object, which
	 *   will be invoked, whenever the context providers
	 *   {@link IContextProvider#run(ContextUtils.IRunnable) run}, or
	 *   {@link IContextProvider#call(ContextUtils.ICallable) call}
	 *   methods are being invoked.
	 * @param pTerminator An optional resource deallocator,
	 *   which will be called when the context object
	 *   is being disposed.
	 * @return The created context provider.
	 */
	public static <C> IContextProvider<@NonNull C> of(FailableSupplier<@NonNull C,?> pSupplier, FailableConsumer<C,?> pTerminator) {
		return new IContextProvider<@NonNull C>() {
			@Override
			public void run(IRunnable<@NonNull C> pRunnable) {
				call((c) -> { pRunnable.run(c); return null; });
			}

			@Override
			public <O> O call(ICallable<@NonNull C, O> pCallable) {
				@Nullable O o = null;
				Throwable th = null;
				boolean close = false;
				@Nullable C c = null;
				try {
					c = pSupplier.get();
					close = true;
					o = pCallable.call(c);
				} catch (Throwable t) {
					th = t;
				} finally {
					if (close) {
						try {
							pTerminator.accept(Objects.requireNonNull(c));
						} catch (Throwable t) {
							// In case of more than one Exception: Throw the first.
							if (th == null) {
								th = t;
							}
						}
					}
				}
				if (th != null) {
					throw Exceptions.show(th);
				}
				@SuppressWarnings("null")
				final O result = (O) o;
				return result;
			}
		};
	}
}
