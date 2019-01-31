/**
 * Copyright 2018 Jochen Wiedmann
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

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Functions {
	/** Failable version of {@link Consumer}.
	 * @see Consumer
	 */
	@FunctionalInterface
	public interface FailableConsumer<T1 extends Object, T2 extends Throwable> {
		/** @see Consumer#accept(Object)
		 */
		public void accept(T1 pObject) throws T2;
	}

	/** Failable version of {@link BiConsumer}.
	 * @see BiConsumer
	 */
	@FunctionalInterface
	public interface FailableBiConsumer<T1 extends Object, T2 extends Object, T3 extends Throwable> {
		/** @see BiConsumer#accept(Object, Object)
		 */
		public void accept(T1 pObject1, T2 pObject2) throws T3;
	}

	/** Failable version of {@link Function}.
	 * @see Function
	 */
	@FunctionalInterface
	public interface FailableFunction<I extends Object, O extends Object, T extends Throwable> {
		/** @see Function#apply(Object)
		 */
		public O apply(I pInput) throws T;
	}

	/** Failable version of {@link BiFunction}.
	 * @see BiFunction
	 */
	@FunctionalInterface
	public interface FailableBiFunction<I1 extends Object, I2 extends Object, O extends Object, T extends Throwable> {
		/** @see BiFunction#apply(Object, Object)
		 */
		public O apply(I1 pInput1, I2 pInput2) throws T;
	}

	/** Failable version of {@link Runnable}.
	 * @see Runnable
	 */
	@FunctionalInterface
	public interface FailableRunnable<T extends Throwable> {
		/** @see Runnable#run()
		 */
		public void run() throws T;
	}

	/** Failable version of {@link Callable}.
	 * @see Callable
	 */
	@FunctionalInterface
	public interface FailableCallable<O extends Object,T extends Throwable> {
		/** @see Callable#call()
		 */
		public O call() throws T;
	}

	/** Failable version of {@link Predicate}.
	 * @see Predicate
	 */
	@FunctionalInterface
	public interface FailablePredicate<O extends Object,T extends Throwable> {
		/** @see Predicate#test(Object)
		 */
		public boolean test(O pObject) throws T;
	}

	/** Failable version of {@link BiPredicate}.
	 * @see BiPredicate
	 */
	@FunctionalInterface
	public interface FailableBiPredicate<O1 extends Object,O2 extends Object,T extends Throwable> {
		/** @see BiPredicate#test(Object, Object)
		 */
		public boolean test(O1 pObject1, O2 pObject2) throws T;
	}

	/** Failable version of {@link Supplier}.
	 * @see Supplier
	 */
	@FunctionalInterface
	public interface FailableSupplier<O extends Object,T extends Throwable> {
		/** @see Supplier#get()
		 */
		public O get() throws T;
	}

	/**
	 * Executes the given {@link FailableRunnable}, rethrowing checked
	 * exceptions, if necessary.
	 * @param pRunnable The {@link FailableRunnable} to execute.
	 */
	public static void run(FailableRunnable<? extends Throwable> pRunnable) {
		try {
			pRunnable.run();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Calls the given {@link FailableCallable}, and returns the result.
	 * Rethrows checked exceptions, if necessary.
	 * @param pCallable The {@link FailableCallable} to execute.
	 */
	public static <O> O call(FailableCallable<O,? extends Throwable> pCallable) {
		try {
			return pCallable.call();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Applies the given {@link FailableFunction}, and returns the result.
	 * Rethrows checked exceptions, if necessary.
	 * @param pFunction The {@link FailableFunction} to apply.
	 * @param pInput The input parameter, on which the {@link FailableFunction}
	 *   is being applied.
	 * @see Function#apply(Object)
	 */
	public static <I,O> O apply(FailableFunction<I,O,? extends Throwable> pFunction, I pInput) {
		try {
			return pFunction.apply(pInput);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Applies the given {@link FailableBiFunction}, and returns the result.
	 * Rethrows checked exceptions, if necessary.
	 * @param pFunction The {@link FailableBiFunction} to apply.
	 * @param pInput1 The first input parameter, on which the {@link FailableFunction}
	 *   is being applied.
	 * @param pInput2 The second input parameter, on which the {@link FailableFunction}
	 *   is being applied.
	 * @see BiFunction#apply(Object, Object)
	 */
	public static <I1,I2,O> O apply(FailableBiFunction<I1,I2,O,? extends Throwable> pBiFunction, I1 pInput1, I2 pInput2) {
		try {
			return pBiFunction.apply(pInput1, pInput2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Makes the given {@link FailableConsumer} accept the given object.
	 * Rethrows checked exceptions, if necessary.
	 * @param pConsumer The {@link FailableConsumer}, which is to accept the object.
	 * @param pObject The object being accepted.
	 * @see Consumer#accept(Object)
	 */
	public static <O> void accept(FailableConsumer<O,? extends Throwable> pConsumer, O pObject) {
		try {
			pConsumer.accept(pObject);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Makes the given {@link FailableBiConsumer} accept the given objects.
	 * Rethrows checked exceptions, if necessary.
	 * @param pConsumer The {@link FailableBiConsumer}, which is to accept the objects.
	 * @param pObject1 The first object being accepted.
	 * @param pObject2 The second object being accepted.
	 * @see BiConsumer#accept(Object, Object)
	 */
	public static <O1,O2> void accept(FailableBiConsumer<O1,O2,? extends Throwable> pBiConsumer, O1 pObject1, O2 pObject2) {
		try {
			pBiConsumer.accept(pObject1, pObject2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Applies the given {@link FailablePredicate} on the given object, and returns the
	 * result.
	 * @param pPredicate The {@link FailablePredicate} being applied.
	 * @param pObject The object being tested.
	 * @return The predicates result.
	 * @see Predicate#test(Object)
	 */
	public static <O> boolean test(FailablePredicate<O,? extends Throwable> pPredicate, O pObject) {
		try {
			return pPredicate.test(pObject);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Applies the given {@link FailableBiPredicate} on the given object, and returns the
	 * result.
	 * @param pPredicate The {@link FailableBiPredicate} being applied.
	 * @param pObject1 The first object being tested.
	 * @param pObject2 The second object being tested.
	 * @return The predicates result.
	 * @see BiPredicate#test(Object, Object)
	 */
	public static <O1,O2> boolean test(FailableBiPredicate<O1,O2,? extends Throwable> pPredicate, O1 pObject1, O2 pObject2) {
		try {
			return pPredicate.test(pObject1, pObject2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Invokes the given {@link FailableSupplier}, and returns the result.
	 * @param pSupplier The {@link FailableSupplier} being invoked.
	 * @return The result, as returned by invoking the {@link FailableSupplier}.
	 * @see Supplier#get()
	 */
	public static <O> O get(FailableSupplier<O,? extends Throwable> pSupplier) {
		try {
			return pSupplier.get();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Lambda version of try-with-resources.
	 * The given {@code pAction} is being executed. Afterwards, <em>all</em> of
	 * the given {@code pResources} are being executed, in the given order.
	 * If an exception is thrown by either the original action, or by the
	 * resource actions, then the <em>first</em> exception is being rethrown.
	 */
	@SafeVarargs
	public static void tryWithResources(FailableRunnable<? extends Throwable> pAction,
			                            FailableRunnable<? extends Throwable>... pResources) {
		tryWithResources(pAction, null, pResources);
	}

	/**
	 * Lambda version of try-with-resources.
	 * The given {@code pAction} is being executed. Afterwards, <em>all</em> of
	 * the given {@code pResources} are being executed, in the given order.
	 * If an exception is thrown by either the original action, or by the
	 * resource actions, then the <em>first</em> exception is being rethrown.
	 */
	@SafeVarargs
	public static void tryWithResources(FailableRunnable<? extends Throwable> pAction,
			                            FailableConsumer<Throwable,? extends Throwable> pErrorHandler,
			                            FailableRunnable<? extends Throwable>... pResources) {
		Throwable th = null;
		try {
			pAction.run();
		} catch (Throwable t) {
			th = t;
		}
		if (pResources != null) {
			for (FailableRunnable<? extends Throwable> resource : pResources) {
				try {
					resource.run();
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
		}
		if (th != null) {
			if (pErrorHandler == null) {
				throw Exceptions.show(th);
			} else {
				try {
					pErrorHandler.accept(th);
				} catch (Throwable t) {
					throw Exceptions.show(th);
				}
			}
		}
	}
}
