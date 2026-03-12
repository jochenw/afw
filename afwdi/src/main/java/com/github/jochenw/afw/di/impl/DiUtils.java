package com.github.jochenw.afw.di.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** A small utility class with reusable helper methods.
 */
public class DiUtils {
	/** Converts a {@link Throwable} into an Exception,
	 * which can be thrown without polluting the method
	 * signature, and throws the converted exception.
	 * @param pTh The Exception, which is being converted.
	 * @return Nothing. This method will <em>always</em>
	 *   throw an exception. However declaring it as
	 *   returning an exception, permits using it like
	 *   this
	 *   {@snippet id="Recommeded" :
	 *     try {
	 *       // Do something
	 *     } catch (Exception e) {
	 *       throw DiUtils.show(e); // Recommended way of using DiUtils.show
	 *     }
	 *   }
	 *   rather than this
	 *   {@snippet id="Discouraged" :
	 *     try {
	 *       // Do something
	 *     } catch (Exception e) {
	 *       DiUtils.show(e);  // Discouraged way of using DiUtils.show
	 *     }
	 *   }
	 *   The problem with the latter is that the compilers code flow analyzer will
	 *   not detect, what happpens.
	 */
	@SuppressWarnings("javadoc")
	public static RuntimeException show(Throwable pTh) {
		Objects.requireNonNull(pTh, "Throwable");
		if (pTh instanceof RuntimeException rte) {
			throw rte;
		} else if (pTh instanceof Error e) {
			throw e;
		} else if (pTh instanceof IOException ioe) {
			throw new UncheckedIOException(ioe);
		} else {
			throw new UndeclaredThrowableException(pTh);
		}
	}

	/** Creates an object, which implements a lazy supplier
	 * for another object.
	 * @param pSupplierProvider An object, which provides the
	 *   supplier of the actual instance. This object will
	 *   be invoked when the lazy supplier is invoked for
	 *   the first time.
	 * @return The created, lazy, suppplier.
	 */
	public static <I,O> Function<I,O> deferredSupplier(Function<I,Supplier<O>> pSupplierProvider) {
		return new DeferredSupplier<I,O>(pSupplierProvider);
	}

	/** Implementation of a lazy supplier, as returned by
	 * {@link DiUtils#deferredSupplier(Function)}.
	 * @param <I> The lazy suppliers input type.
	 * @param <O> The lazy suppliers output type.
	 */
	public static class DeferredSupplier<I,O> implements Function<I,O> {
		private final Function<I,Supplier<O>> supplierProvider;
		private Supplier<O> supplier;
		/** Creates a new instance.
		 * @param pSupplierProvider The provider, which is being
		 *   used internally to create the actual supplier. The
		 *   provider will be invoked upon the first invocation of
		 *   of {@link DeferredSupplier this supplier}.
		 */
		public DeferredSupplier(Function<I,Supplier<O>> pSupplierProvider) {
			supplierProvider = pSupplierProvider;
		}
		@Override
		public O apply(I pInput) {
			final Supplier<O> supp;
			synchronized(this) {
				if (supplier == null) {
					supp = supplierProvider.apply(pInput);
					supplier = supp;
				} else {
					supp = supplier;
				}
			}
			return supp.get();
		}
	}

	@SuppressWarnings("deprecation")
	public static void assertAccessible(AccessibleObject pObject) {
		if (!pObject.isAccessible()) {
			pObject.setAccessible(true);
		}
	}
}
