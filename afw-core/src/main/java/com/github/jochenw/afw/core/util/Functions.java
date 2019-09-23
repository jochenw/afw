/*
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
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Functions {
	/** Failable version of {@link Consumer}.
	 * @param <T1> The type being consumed.
	 * @param <T2> The exception type being thrown by the consumer.
	 * @see Consumer
	 */
	@FunctionalInterface
	public interface FailableConsumer<T1 extends Object, T2 extends Throwable> {
		/** Called to consume the given object.
		 * @param pObject The object being consumed.
		 * @see Consumer#accept(Object)
		 * @throws T2 The exception type being thrown by the consumer.
		 */
		public void accept(T1 pObject) throws T2;
	}

	/** Failable version of {@link BiConsumer}.
	 * @param <T1> The first type being consumed.
	 * @param <T2> The first type being consumed.
	 * @param <T3> The exception type being thrown by the consumer.
	 * @see BiConsumer
	 */
	@FunctionalInterface
	public interface FailableBiConsumer<T1 extends Object, T2 extends Object, T3 extends Throwable> {
		/** Called to consume the given objects.
		 * @param pObject1 The first object being consumed.
		 * @param pObject2 The first object being consumed.
		 * @see BiConsumer#accept(Object,Object)
		 * @throws T3 The exception type being thrown by the consumer.
		 */
		public void accept(T1 pObject1, T2 pObject2) throws T3;
	}

	/** Failable version of {@link Function}.
	 * @param <I> The functions input type.
	 * @param <O> The functions output type (result type).
	 * @param <T> The exception type being thrown by the function.
	 * @see Function
	 */
	@FunctionalInterface
	public interface FailableFunction<I extends Object, O extends Object, T extends Throwable> {
		/** @param pInput The functions input object
		 * @return The functions result object
	     * @throws T The exception type being thrown by the function.
		 * @see Function#apply(Object)
		 */
		public O apply(I pInput) throws T;
	}

	/** Failable version of {@link BiFunction}.
	 * @param <I1> The functions input type.
	 * @param <I2> The functions input type.
	 * @param <O> The functions output type (result type).
	 * @param <T> The exception type being thrown by the function.
	 * @see BiFunction
	 */
	@FunctionalInterface
	public interface FailableBiFunction<I1 extends Object, I2 extends Object, O extends Object, T extends Throwable> {
		/** @param pInput1 The functions first input object
		 * @param pInput2 The functions first input object
		 * @return The functions result object
	     * @throws T The exception type being thrown by the function.
		 * @see Function#apply(Object)
		 */
		public O apply(I1 pInput1, I2 pInput2) throws T;
	}

	/** Failable version of {@link Runnable}.
	 * @param <T> The exception type being thrown by the runnable.
	 * @see Runnable
	 */
	@FunctionalInterface
	public interface FailableRunnable<T extends Throwable> {
		/** @see Runnable#run()
		 * @throws T The exception type being thrown by the runnable.
		 */
		public void run() throws T;
	}

	/** Failable version of {@link Callable}.
	 * @param <O> The callables argument type.
	 * @param <T> The exception type being thrown by the callable.
	 * @see Callable
	 */
	@FunctionalInterface
	public interface FailableCallable<O extends Object,T extends Throwable> {
		/**
		 * @return The result object.
		 * @throws T The exception type being thrown by the callable.
		 * @see Callable#call()
		 */
		public O call() throws T;
	}

	/** Failable version of {@link Predicate}.
	 * @param <O> The predicates argument type.
	 * @param <T> The exception type being thrown by the predicate.
	 * @see Predicate
	 */
	@FunctionalInterface
	public interface FailablePredicate<O extends Object,T extends Throwable> {
		/**
		 * @param pObject The predicates argument (The object being tested.)
		 * @return True, if the predicate accepts the object. (The object matches) Otherwise false.
		 * @throws T The exception type being thrown by the predicate.
		 * @see Predicate#test(Object)
		 */
		public boolean test(O pObject) throws T;
	}

	/** Failable version of {@link IntPredicate}.
	 * @param <T> The exception type being thrown by the predicate.
	 * @see Predicate
	 */
	@FunctionalInterface
	public interface FailableIntPredicate<T extends Throwable> {
		/**
		 * @param pValue The predicates argument (The object being tested.)
		 * @return True, if the predicate accepts the object. (The object matches) Otherwise false.
		 * @throws T The exception type being thrown by the predicate.
		 * @see Predicate#test(Object)
		 */
		public boolean test(int pValue) throws T;
	}

	/** Failable version of {@link LongPredicate}.
	 * @param <T> The exception type being thrown by the predicate.
	 * @see Predicate
	 */
	@FunctionalInterface
	public interface FailableLongPredicate<T extends Throwable> {
		/**
		 * @param pValue The predicates argument (The object being tested.)
		 * @return True, if the predicate accepts the object. (The object matches) Otherwise false.
		 * @throws T The exception type being thrown by the predicate.
		 * @see Predicate#test(Object)
		 */
		public boolean test(long pValue) throws T;
	}

	/** Failable version of {@link BiPredicate}.
	 * @param <O1> The predicates first argument type.
	 * @param <O2> The predicates second argument type.
	 * @param <T> The exception type being thrown by the predicate.
	 * @see BiPredicate
	 */
	@FunctionalInterface
	public interface FailableBiPredicate<O1 extends Object,O2 extends Object,T extends Throwable> {
		/**
		 * @param pObject1 The predicates first argument (The first object being tested.)
		 * @param pObject2 The predicates second argument (The second object being tested.)
		 * @return True, if the predicate accepts the object. (The object matches) Otherwise false.
		 * @throws T The exception type being thrown by the predicate.
		 * @see BiPredicate#test(Object,Object)
		 */
		public boolean test(O1 pObject1, O2 pObject2) throws T;
	}

	/** Failable version of {@link BiPredicate}, with a primitive integer as the first argument type.
	 * @param <O> The predicates second argument type.
	 * @param <T> The exception type being thrown by the predicate.
	 * @see BiPredicate
	 */
	@FunctionalInterface
	public interface FailableBiIntPredicate<O extends Object,T extends Throwable> {
		/**
		 * @param pValue1 The predicates first argument (The integer value being tested.)
		 * @param pObject2 The predicates second argument (The second object being tested.)
		 * @return True, if the predicate accepts the object. (The object matches) Otherwise false.
		 * @throws T The exception type being thrown by the predicate.
		 * @see BiPredicate#test(Object,Object)
		 */
		public boolean test(int pValue1, O pObject2) throws T;
	}

	/** Failable version of {@link BiPredicate}, with a primitive long as the first argument type.
	 * @param <O> The predicates second argument type.
	 * @param <T> The exception type being thrown by the predicate.
	 * @see BiPredicate
	 */
	@FunctionalInterface
	public interface FailableBiLongPredicate<O extends Object,T extends Throwable> {
		/**
		 * @param pValue1 The predicates first argument (The integer value being tested.)
		 * @param pObject2 The predicates second argument (The second object being tested.)
		 * @return True, if the predicate accepts the object. (The object matches) Otherwise false.
		 * @throws T The exception type being thrown by the predicate.
		 * @see BiPredicate#test(Object,Object)
		 */
		public boolean test(long pValue1, O pObject2) throws T;
	}

	/** Failable version of {@link Supplier}.
	 * @param <O> The suppliers result type.
	 * @param <T> The exception type being thrown by the supplier.
	 * @see Supplier
	 */
	@FunctionalInterface
	public interface FailableSupplier<O extends Object,T extends Throwable> {
		/**
		 * @return The result object, as created by the supplier.
		 * @throws T The exception type being thrown by the supplier.
		 * @see Supplier#get()
		 */
		public O get() throws T;
	}

	/**
	 * Converts the given {@link FailableRunnable} into a standard {@link Runnable}.
	 */
	public static Runnable asRunnable(FailableRunnable<?> pRunnable) {
		return () -> {
			try {
				pRunnable.run();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
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
	 * Converts the given {@link FailableCallable} into a standard {@link Callable}.
	 */
	public static <O extends Object> Callable<O> asCallable(FailableCallable<O,?> pCallable) {
		return () -> {
			try {
				return pCallable.call();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}

	/**
	 * Calls the given {@link FailableCallable}, and returns the result.
	 * Rethrows checked exceptions, if necessary.
	 * @param pCallable The {@link FailableCallable} to execute.
	 * @return The result object, which has been returned by invoking the callable.
	 * @param <O> The callables return type.
	 * @param <T> The exception type, which may be thrown by the callable.
	 */
	public static <O, T extends Throwable> O call(FailableCallable<O,T> pCallable) {
		try {
			return pCallable.call();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Converts the given {@link FailableFunction} into a standard {@link Function}.
	 */
	public static <I,O> Function<I,O> asFunction(FailableFunction<I,O,?> pFunction) {
		return (pInput) -> {
			try {
				return pFunction.apply(pInput);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}
	
	/**
	 * Applies the given {@link FailableFunction}, and returns the result.
	 * Rethrows checked exceptions, if necessary.
	 * @param pFunction The {@link FailableFunction} to apply.
	 * @param pInput The input parameter, on which the {@link FailableFunction}
	 *   is being applied.
	 * @return The result object, which has been returned by invoking the function.
	 * @see Function#apply(Object)
	 * @param <I> The functions input type.
	 * @param <O> The functions output type.
	 * @param <T> The exception type, which may be thrown by the function.
	 */
	public static <I,O,T extends Throwable> O apply(FailableFunction<I,O,T> pFunction, I pInput) {
		try {
			return pFunction.apply(pInput);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Converts the given {@link FailableBiFunction} into a standard {@link BiFunction}.
	 */
	public static <I1,I2,O> BiFunction<I1,I2,O> asBiFunction(FailableBiFunction<I1,I2,O,?> pFunction) {
		return (pInput1, pInput2) -> {
			try {
				return pFunction.apply(pInput1, pInput2);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}

	/**
	 * Applies the given {@link FailableBiFunction}, and returns the result.
	 * Rethrows checked exceptions, if necessary.
	 * @param pBiFunction The {@link FailableBiFunction} to apply.
	 * @param pInput1 The first input parameter, on which the {@link FailableFunction}
	 *   is being applied.
	 * @param pInput2 The second input parameter, on which the {@link FailableFunction}
	 *   is being applied.
	 * @return The result object, which has been returned by invoking the function.
	 * @see BiFunction#apply(Object, Object)
	 * @param <I1> The functions first input type.
	 * @param <I2> The functions second input type.
	 * @param <O> The functions output type.
	 * @param <T> The exception type, which may be thrown by the function.
	 */
	public static <I1,I2,O,T extends Throwable> O apply(FailableBiFunction<I1,I2,O,T> pBiFunction, I1 pInput1, I2 pInput2) {
		try {
			return pBiFunction.apply(pInput1, pInput2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Converts the given {@link FailableConsumer} into a standard {@link Consumer}.
	 */
	public static <I> Consumer<I> asConsumer(FailableConsumer<I,?> pConsumer) {
		return (pInput) -> {
			try {
				pConsumer.accept(pInput);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}

	/** Makes the given {@link FailableConsumer} accept the given object.
	 * Rethrows checked exceptions, if necessary.
	 * @param pConsumer The {@link FailableConsumer}, which is to accept the object.
	 * @param pObject The object being accepted.
	 * @param <O> The consumers argument type.
	 * @param <T> The exception type, which may be thrown by the consumer.
	 * @see Consumer#accept(Object)
	 */
	public static <O,T extends Throwable> void accept(FailableConsumer<O,T> pConsumer, O pObject) {
		try {
			pConsumer.accept(pObject);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Converts the given {@link FailableBiConsumer} into a standard {@link BiConsumer}.
	 */
	public static <I1,I2> BiConsumer<I1,I2> asConsumer(FailableBiConsumer<I1,I2,?> pConsumer) {
		return asBiConsumer(pConsumer);
	}

	/**
	 * Converts the given {@link FailableBiConsumer} into a standard {@link BiConsumer}.
	 */
	public static <I1,I2> BiConsumer<I1,I2> asBiConsumer(FailableBiConsumer<I1,I2,?> pConsumer) {
		return (pInput1, pInput2) -> {
			try {
				pConsumer.accept(pInput1, pInput2);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}

	/** Makes the given {@link FailableBiConsumer} accept the given objects.
	 * Rethrows checked exceptions, if necessary.
	 * @param pBiConsumer The {@link FailableBiConsumer}, which is to accept the objects.
	 * @param pObject1 The first object being accepted.
	 * @param pObject2 The second object being accepted.
	 * @param <O1> The consumers first argument type.
	 * @param <O2> The consumers first argument type.
	 * @param <T> The exception type, which may be thrown by the consumer.
	 * @see BiConsumer#accept(Object, Object)
	 */
	public static <O1,O2,T extends Throwable> void accept(FailableBiConsumer<O1,O2,T> pBiConsumer, O1 pObject1, O2 pObject2) {
		try {
			pBiConsumer.accept(pObject1, pObject2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Converts the given {@link FailablePredicate} into a standard {@link Predicate}.
	 */
	public static <I> Predicate<I> asPredicate(FailablePredicate<I,?> pPredicate) {
		return (pInput) -> {
			try {
				return pPredicate.test(pInput);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}

	/** Tests the given object, using the given {@link FailablePredicate}, and returns the
	 * result.
	 * @param pPredicate The {@link FailablePredicate} being applied.
	 * @param pObject The object being tested.
	 * @param <O> The predicates argument type (Type of the object being tested).
	 * @param <T> The exception type, which may be thrown by the predicate.
	 * @return The predicates result.
	 * @see Predicate#test(Object)
	 */
	public static <O,T extends Throwable> boolean test(FailablePredicate<O,T> pPredicate, O pObject) {
		try {
			return pPredicate.test(pObject);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Tests the given object, using the given {@link FailableIntPredicate}, and returns the
	 * result.
	 * @param pPredicate The {@link FailableIntPredicate} being applied.
	 * @param pValue The value being tested.
	 * @param <T> The exception type, which may be thrown by the predicate.
	 * @return The predicates result.
	 * @see IntPredicate#test(int)
	 */
	public static <T extends Throwable> boolean test(FailableIntPredicate<T> pPredicate, int pValue) {
		try {
			return pPredicate.test(pValue);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Tests the given object, using the given {@link FailableLongPredicate}, and returns the
	 * result.
	 * @param pPredicate The {@link FailableLongPredicate} being applied.
	 * @param pValue The value being tested.
	 * @param <T> The exception type, which may be thrown by the predicate.
	 * @return The predicates result.
	 * @see LongPredicate#test(long)
	 */
	public static <T extends Throwable> boolean test(FailableLongPredicate<T> pPredicate, long pValue) {
		try {
			return pPredicate.test(pValue);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Converts the given {@link FailableBiPredicate} into a standard {@link BiPredicate}.
	 */
	public static <I1,I2> BiPredicate<I1,I2> asBiPredicate(FailableBiPredicate<I1,I2,?> pPredicate) {
		return (pInput1, pInput2) -> {
			try {
				return pPredicate.test(pInput1, pInput2);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}

	/** Tests the given objects, using the given {@link FailableBiPredicate}, and returns the
	 * result.
	 * @param pPredicate The {@link FailableBiPredicate} being applied.
	 * @param pObject1 The first object being tested.
	 * @param pObject2 The second object being tested.
	 * @param <O1> The predicates first argument type (Type of the first object being tested).
	 * @param <O2> The predicates second argument type (Type of the second object being tested).
	 * @param <T> The exception type, which may be thrown by the predicate.
	 * @return The predicates result.
	 * @see BiPredicate#test(Object, Object)
	 */
	public static <O1,O2,T extends Throwable> boolean test(FailableBiPredicate<O1,O2,T> pPredicate, O1 pObject1, O2 pObject2) {
		try {
			return pPredicate.test(pObject1, pObject2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Tests the given objects, using the given {@link FailableBiIntPredicate}, and returns the
	 * result.
	 * @param pPredicate The {@link FailableBiIntPredicate} being applied.
	 * @param pValue1 The first value being tested (the primitive integer value).
	 * @param pObject2 The second object being tested.
	 * @param <O> The predicates second argument type (Type of the second object being tested).
	 * @param <T> The exception type, which may be thrown by the predicate.
	 * @return The predicates result.
	 * @see BiPredicate#test(Object, Object)
	 */
	public static <O,T extends Throwable> boolean test(FailableBiIntPredicate<O,T> pPredicate, int pValue1, O pObject2) {
		try {
			return pPredicate.test(pValue1, pObject2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Tests the given objects, using the given {@link FailableBiLongPredicate}, and returns the
	 * result.
	 * @param pPredicate The {@link FailableBiLongPredicate} being applied.
	 * @param pValue1 The first value being tested (the primitive long value).
	 * @param pObject2 The second object being tested.
	 * @param <O> The predicates second argument type (Type of the second object being tested).
	 * @param <T> The exception type, which may be thrown by the predicate.
	 * @return The predicates result.
	 * @see BiPredicate#test(Object, Object)
	 */
	public static <O,T extends Throwable> boolean test(FailableBiLongPredicate<O,T> pPredicate, long pValue1, O pObject2) {
		try {
			return pPredicate.test(pValue1, pObject2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Converts the given {@link FailableSupplier} into a standard {@link Supplier}.
	 */
	public static <O> Supplier<O> asSupplier(FailableSupplier<O,?> pSupplier) {
		return () -> {
			try {
				return pSupplier.get();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
	}

	/**
	 * Invokes the given {@link FailableSupplier}, and returns the result.
	 * @param pSupplier The {@link FailableSupplier} being invoked.
	 * @param <O> The suppliers result type (Type of the object being created).
	 * @param <T> The exception type, which may be thrown by the supplier.
	 * @return The result, as returned by invoking the {@link FailableSupplier}.
	 * @see Supplier#get()
	 */
	public static <O,T extends Throwable> O get(FailableSupplier<O,T> pSupplier) {
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
	 * Equivalent to invoking {@link #tryWithResources(FailableRunnable, FailableConsumer, FailableRunnable...)}
	 * with a null error handler.
	 * @param pAction The action to execute.
	 * @param pResources The actions to execute for closing the acquired resources.
	 * @see #tryWithResources(FailableRunnable, FailableConsumer, FailableRunnable...)
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
	 * @param pAction The action to execute.
	 * @param pErrorHandler The error handler to invoke in case of errors. May be null,
	 *   in which case the error is simply rethrown.
	 * @param pResources The actions to execute for closing the acquired resources.
	 * @see #tryWithResources(FailableRunnable, FailableRunnable...)
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
