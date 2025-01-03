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
package com.github.jochenw.afw.core.function;

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

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Exceptions;


/** Utility class, which mimicks the java.util.function package, except that unchecked exceptions are
 * generally permitted.
 */
public class Functions {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	private Functions() {}

	/**
	 * Represents an operation that accepts a single {@code short}-valued argument and
	 * returns no result.  This is the primitive type specialization of
	 * {@link Consumer} for {@code short}.  Unlike most other functional interfaces,
	 * {@code ShortConsumer} is expected to operate via side-effects.
	 *
	 * <p>This is a <a href="package-summary.html">functional interface</a>
	 * whose functional method is {@link #accept(short)}.
	 *
	 * @see Consumer
	 * @since 0.6
	 */
	@FunctionalInterface
	public interface ShortConsumer {
	    /**
	     * Performs this operation on the given argument.
	     *
	     * @param pValue the input argument
	     */
		public void accept(short pValue);
	}

	/**
	 * Represents an operation that accepts a single {@code byte}-valued argument and
	 * returns no result.  This is the primitive type specialization of
	 * {@link Consumer} for {@code byte}.  Unlike most other functional interfaces,
	 * {@code ShortConsumer} is expected to operate via side-effects.
	 *
	 * <p>This is a <a href="package-summary.html">functional interface</a>
	 * whose functional method is {@link #accept(byte)}.
	 *
	 * @see Consumer
	 * @since 0.6
	 */
	@FunctionalInterface
	public interface ByteConsumer {
	    /**
	     * Performs this operation on the given argument.
	     *
	     * @param pValue the input argument
	     */
		public void accept(byte pValue);
	}

	/**
	 * Represents an operation that accepts a single {@code boolean}-valued argument and
	 * returns no result.  This is the primitive type specialization of
	 * {@link Consumer} for {@code boolean}.  Unlike most other functional interfaces,
	 * {@code ShortConsumer} is expected to operate via side-effects.
	 *
	 * <p>This is a <a href="package-summary.html">functional interface</a>
	 * whose functional method is {@link #accept(boolean)}.
	 *
	 * @see Consumer
	 * @since 0.6
	 */
	@FunctionalInterface
	public interface BooleanConsumer {
	    /**
	     * Performs this operation on the given argument.
	     *
	     * @param pValue the input argument
	     */
		public void accept(boolean pValue);
	}

	/**
	 * Represents an operation that accepts a two arguments, the first of which is a {@code boolean}.
	 * This is a primitive type specialization of {@link BiConsumer} for {@code boolean}.
	 * Unlike most other functional interfaces, {@code ShortConsumer} is expected to operate via side-effects.
	 *
	 * <p>This is a <a href="package-summary.html">functional interface</a>
	 * whose functional method is {@link #accept(boolean, Object)}.
	 * @param <I> Type of the second (the non-primitive) parameter.
	 *
	 * @see Consumer
	 * @since 0.6
	 */
	@FunctionalInterface
	public interface BooleanBiConsumer<I> {
	    /**
	     * Performs this operation on the given argument.
	     *
	     * @param pValue the first (boolean) input argument
	     * @param pNonPrimitiveValue The second (non-primitive) inp
	     */
		public void accept(boolean pValue, I pNonPrimitiveValue);
	}


	/**
	 * Represents an operation that accepts a single {@code float}-valued argument and
	 * returns no result.  This is the primitive type specialization of
	 * {@link Consumer} for {@code float}.  Unlike most other functional interfaces,
	 * {@code ShortConsumer} is expected to operate via side-effects.
	 *
	 * <p>This is a <a href="package-summary.html">functional interface</a>
	 * whose functional method is {@link #accept(float)}.
	 *
	 * @see Consumer
	 * @since 0.6
	 */
	@FunctionalInterface
	public interface FloatConsumer {
	    /**
	     * Performs this operation on the given argument.
	     *
	     * @param pValue the input argument
	     */
		public void accept(float pValue);
	}

	/**
	 * Represents an operation that accepts a single {@code double}-valued argument and
	 * returns no result.  This is the primitive type specialization of
	 * {@link Consumer} for {@code double}.  Unlike most other functional interfaces,
	 * {@code ShortConsumer} is expected to operate via side-effects.
	 *
	 * <p>This is a <a href="package-summary.html">functional interface</a>
	 * whose functional method is {@link #accept(double)}.
	 *
	 * @see Consumer
	 * @since 0.6
	 */
	@FunctionalInterface
	public interface DoubleConsumer {
	    /**
	     * Performs this operation on the given argument.
	     *
	     * @param pValue the input argument
	     */
		public void accept(double pValue);
	}

	/** Failable version of {@link Consumer}.
	 * @param <T1> Thes type being consumed.
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
		 * @param pObject2 The second object being consumed.
		 * @see BiConsumer#accept(Object,Object)
		 * @throws T3 The exception type being thrown by the consumer.
		 */
		public void accept(T1 pObject1, T2 pObject2) throws T3;
	}

	/** Failable version of {@link BiConsumer}, where the first argument is a primitive integer.
	 * @param <T2> The first type being consumed.
	 * @param <T3> The exception type being thrown by the consumer.
	 * @see BiConsumer
	 */
	@FunctionalInterface
	public interface FailableBiIntConsumer<T2 extends Object, T3 extends Throwable> {
		/** Called to consume the given objects.
		 * @param pObject1 The first object being consumed.
		 * @param pObject2 The second object being consumed.
		 * @see BiConsumer#accept(Object,Object)
		 * @throws T3 The exception type being thrown by the consumer.
		 */
		public void accept(int pObject1, T2 pObject2) throws T3;
	}

	/** Failable version of a {@link Consumer} with three parameters.
	 * @param <T1> The first type being consumed.
	 * @param <T2> The first type being consumed.
	 * @param <T3> The first type being consumed.
	 * @param <T4> The exception type being thrown by the consumer.
	 * @see BiConsumer
	 */
	@FunctionalInterface
	public interface FailableTriConsumer<T1 extends Object, T2 extends Object, T3 extends Object, T4 extends Throwable> {
		/** Called to consume the given objects.
		 * @param pObject1 The first object being consumed.
		 * @param pObject2 The second object being consumed.
		 * @param pObject3 The second object being consumed.
		 * @see BiConsumer#accept(Object,Object)
		 * @throws T4 The exception type being thrown by the consumer.
		 */
		public void accept(T1 pObject1, T2 pObject2, T3 pObject3) throws T4;
	}


    /** Failable version of {@link BiConsumer}, where the first parameter is a primitive long.
	 * @param <I> The first type being consumed.
	 * @param <T> The exception type being thrown by the consumer.
	 * @see BiConsumer
	 */
	@FunctionalInterface
	public interface FailableBiLongConsumer<I extends Object, T extends Throwable> {
		/** Called to consume the given objects.
		 * @param pObject1 The first input parameter being consumed, a primitive long.
		 * @param pObject2 The second input parameter being consumed.
		 * @throws T The exception type being thrown by the consumer.
		 */
		public void accept(long pObject1, I pObject2) throws T;
	}

	/** Extended version of a {@link BiConsumer}, accepting three arguments.
	 * @param <I1> First input parameters type.
	 * @param <I2> Second input parameters type.
	 * @param <I3> Third input parameters type.
	 */
	@FunctionalInterface
	public interface TriConsumer<I1 extends Object, I2 extends Object, I3 extends Object> {
		/** Called to process the input parameters.
		 * @param pI1 The first input parameter.
		 * @param pI2 The second input parameter.
		 * @param pI3 The  input parameter.
		 */
		public void accept(I1 pI1, I2 pI2, I3 pI3);
	}

	/** Failable version of {@link Function}.
	 * @param <I> The functions input type.
	 * @param <O> The functions output type (result type).
	 * @param <T> The exception type being thrown by the function.
	 * @see Function
	 */
	@FunctionalInterface
	public interface FailableFunction<I extends Object, O extends Object, T extends Throwable> {
		/** Called to apply the function on the given input value.
		 * @param pInput The functions input object
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
		/** Called to apply the function on the given input values.
		 * @param pInput1 The functions first input object
		 * @param pInput2 The functions first input object
		 * @return The functions result object
	         * @throws T The exception type being thrown by the function.
		 * @see Function#apply(Object)
		 */
		public O apply(I1 pInput1, I2 pInput2) throws T;
	}

        /** Failable version of {@link BiFunction}, where one of the arguments is a primitive integer.
	 * @param <I> The functions input type.
	 * @param <O> The functions output type (result type).
	 * @param <T> The exception type being thrown by the function.
	 * @see BiFunction
	 */
	@FunctionalInterface
	public interface FailableBiIntFunction<I extends Object, O extends Object, T extends Throwable> {
		/** Called to apply the function on the given input value.
		 * @param pInput1 The functions first input parameter, a primitive integer.
		 * @param pInput2 The functions second input parameter, an instance of I.
		 * @return The functions result object
	         * @throws T The exception type being thrown by the function.
		 * @see Function#apply(Object)
		 */
		public O apply(int pInput1, I pInput2) throws T;
	}

        /** Failable version of {@link BiFunction}, where one of the arguments is a primitive long.
	 * @param <I> The functions input type.
	 * @param <O> The functions output type (result type).
	 * @param <T> The exception type being thrown by the function.
	 * @see BiFunction
	 */
	@FunctionalInterface
	public interface FailableBiLongFunction<I extends Object, O extends Object, T extends Throwable> {
		/** Called to apply the function on the given input values.
		 * @param pInput1 The functions first input parameter, a primitive integer.
		 * @param pInput2 The functions second input parameter, an instance of I.
		 * @return The functions result object
	         * @throws T The exception type being thrown by the function.
		 * @see Function#apply(Object)
		 */
		public O apply(long pInput1, I pInput2) throws T;
	}

	/** Failable version of {@link Runnable}.
	 * @param <T> The exception type being thrown by the runnable.
	 * @see Runnable
	 */
	@FunctionalInterface
	public interface FailableRunnable<T extends Throwable> {
		/** Executes the {@link FailableRunnable}.
		 * @see Runnable#run()
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
		/** Invokes the callable, and returns the result.
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
		/** Applies the {@link FailablePredicate} on the given object.
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
		/** Applies the predicate on the given value.
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
		/** Applies the predicate on the given value.
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
		/** Called to test the given input objects.
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
		/** Called to test the given input values.
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
		/** Called to test the given input values.
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
		/** Returns the result object, as created by the supplier.
		 * @return The result object, as created by the supplier.
		 * @throws T The exception type being thrown by the supplier.
		 * @see Supplier#get()
		 */
		public O get() throws T;
	}

	/** Failable version of {@link Supplier Supplier&lt;Integer&gt;}.
	 * @param <T> The exception type being thrown by the supplier.
	 */
	@FunctionalInterface
	public interface FailableIntSupplier<T extends Throwable> {
		/** Returns the result value, as created by the supplier.
		 * @return The result value, as created by the supplier.
		 * @throws T The exception type being thrown by the supplier.
		 * @see Supplier#get()
		 */
		public int get() throws T;
	}

	/** Failable version of {@link Supplier Supplier&lt;Long&gt;}.
	 * @param <T> The exception type being thrown by the supplier.
	 */
	@FunctionalInterface
	public interface FailableLongSupplier<T extends Throwable> {
		/** Returns the result value, as created by the supplier.
		 * @return The result value, as created by the supplier.
		 * @throws T The exception type being thrown by the supplier.
		 * @see Supplier#get()
		 */
		public long get() throws T;
	}

	/**
	 * Converts the given {@link FailableRunnable} into a standard {@link Runnable}.
	 * @param pRunnable The failable runnable being executed.
	 * @return An instance of {@link Runnable}, which is internally invoking the failable runnable upon execution.
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
	 * @param <O> The callables return type (both callables, and the parameter).
	 * @param pCallable The callable to convert.
	 * @return A proxy callable, which is implemented by invoking {@code pCallable}.
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
	 * @param <O> The return type (both the callables, and this methods).
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
	 * @param <I> The functions input type (both functions, the parameter, and
	 *   the result).
	 * @param <O> The functions output type (both functions, the parameter, and
	 *   the result).
	 * @param pFunction The failable function to convert.
	 * @return A proxy function, which is implemented by invking {@code pFunction}.
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
         * @param <I1> The first input parameters type.
         * @param <I2> The second input parameters type.
         * @param <O> The output type.
         * @param pFunction The {@link FailableBiFunction} to convert.
         * @return A {@link BiFunction}, which is functionally equivalent to the input function, because
         *   it is implemented by invoking the latter.
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
	 * Applies the given {@link FailableBiIntFunction}, and returns the result.
	 * Rethrows checked exceptions, if necessary.
	 * @param pBiIntFunction The {@link FailableBiIntFunction} to apply.
	 * @param pInput1 The first input parameter, on which the {@link FailableFunction}
	 *   is being applied, a primitive integer.
	 * @param pInput2 The second input parameter, on which the {@link FailableFunction}
	 *   is being applied.
	 * @return The result object, which has been returned by invoking the function.
	 * @see BiFunction#apply(Object, Object)
	 * @param <I> The functions second input type.
	 * @param <O> The functions output type.
	 * @param <T> The exception type, which may be thrown by the function.
	 */
	public static <I,O,T extends Throwable> O apply(FailableBiIntFunction<I,O,T> pBiIntFunction, int pInput1, I pInput2) {
	    try {
		return pBiIntFunction.apply(pInput1, pInput2);
	    } catch (Throwable t) {
		throw Exceptions.show(t);
	    }
	}

        /**
	 * Applies the given {@link FailableBiIntFunction}, and returns the result.
	 * Rethrows checked exceptions, if necessary.
	 * @param pBiLongFunction The {@link FailableBiLongFunction} to apply.
	 * @param pInput1 The first input parameter, on which the {@link FailableFunction}
	 *   is being applied, a primitive long.
	 * @param pInput2 The second input parameter, on which the {@link FailableFunction}
	 *   is being applied.
	 * @return The result object, which has been returned by invoking the function.
	 * @see BiFunction#apply(Object, Object)
	 * @param <I> The functions second input type.
	 * @param <O> The functions output type.
	 * @param <T> The exception type, which may be thrown by the function.
	 */
	public static <I,O,T extends Throwable> O apply(FailableBiLongFunction<I,O,T> pBiLongFunction, long pInput1, I pInput2) {
	    try {
		return pBiLongFunction.apply(pInput1, pInput2);
	    } catch (Throwable t) {
		throw Exceptions.show(t);
	    }
	}

	/**
	 * Converts the given {@link FailableConsumer} into a standard {@link Consumer}.
	 * @param pConsumer The failable consumer being invoked.
	 * @param <I> The failable consumers parameter type.
	 * @return An instance of {@link Consumer}, which is functionally equivalent to
	 *   the input 
	 */
	public static <I> Consumer<I> asConsumer(@NonNull FailableConsumer<I,?> pConsumer) {
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
	 * @param <I1> The biconsumer's first input type (both the parameter consumer,
	 *   and the result consumer).
	 * @param <I2> The biconsumer's second input type (both the parameter consumer,
	 *   and the result consumer).
	 * @param pConsumer The failable consumer to convert.
	 * @return A proxy biconsumer, which is implemented by invoking {@code pConsumer}.
	 */
	public static <I1,I2> BiConsumer<I1,I2> asConsumer(FailableBiConsumer<I1,I2,?> pConsumer) {
		return asBiConsumer(pConsumer);
	}

	/**
	 * Converts the given {@link FailableBiConsumer} into a standard {@link BiConsumer}.
	 * @param <I1> The biconsumer's first input type (both the parameter consumer,
	 *   and the result consumer).
	 * @param <I2> The biconsumer's second input type (both the parameter consumer,
	 *   and the result consumer).
	 * @param pConsumer The failable consumer to convert.
	 * @return A proxy biconsumer, which is implemented by invoking {@code pConsumer}.
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

    /** Makes the given {@link FailableBiIntConsumer} accept the given parameters.
	 * Rethrows checked exceptions, if necessary.
	 * @param pBiIntConsumer The {@link FailableBiIntConsumer}, which is to accept the objects.
	 * @param pInt The first parameter being consumed, a primitive integer.
	 * @param pObject The second parameter being consumed, an instance of I.
	 * @param <I> The consumers first argument type.
	 * @param <T> The exception type, which may be thrown by the consumer.
	 * @see BiConsumer#accept(Object, Object)
	 */
	public static <I,T extends Throwable> void accept(FailableBiIntConsumer<I,T> pBiIntConsumer, int pInt, I pObject) {
	    try {
	        pBiIntConsumer.accept(pInt, pObject);
	    } catch (Throwable t) {
		throw Exceptions.show(t);
	    }
	}

    /** Makes the given {@link FailableBiIntConsumer} accept the given parameters.
	 * Rethrows checked exceptions, if necessary.
	 * @param pBiLongConsumer The {@link FailableBiLongConsumer}, which is to accept the objects.
	 * @param pLong The first parameter being consumed, a primitive long.
	 * @param pObject The second parameter being consumed, an instance of I.
	 * @param <I> The consumers first argument type.
	 * @param <T> The exception type, which may be thrown by the consumer.
	 * @see BiConsumer#accept(Object, Object)
	 */
	public static <I,T extends Throwable> void accept(FailableBiLongConsumer<I,T> pBiLongConsumer, long pLong, I pObject) {
	    try {
	        pBiLongConsumer.accept(pLong, pObject);
	    } catch (Throwable t) {
	        throw Exceptions.show(t);
	    }
	}

	/**
	 * Converts the given {@link FailablePredicate} into a standard {@link Predicate}.
	 * @param <I> The predicates input type (both, the parameter predicate, and the
	 *   result)
	 * @param pPredicate The failable predicate to convert.
	 * @return A proxy predicate, which is implemented by invoking {@code pPredicate}.
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
	 * @param <I1> The bipredicate's first input type (both the parameter,
	 *   and the result predicate).
	 * @param <I2> The bipredicate's second input type (both the parameter,
	 *   and the result predicate).
	 * @param pPredicate The failable predicate to convert.
	 * @return A bipredicate, which is implemented by invoking {@code pPredicate}.
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
	 * @param pSupplier The failable supplier to convert.
	 * @param <O> The suppliers return type (both the parameter, and the result
	 *   supplier)
	 * @return A supplier, which is implemented by invoking {@code pSupplier}.
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

	/** Asserts, that a {@link FailableRunnable} throws an expected Exception.
	 * @param pErrorType Type of the expected Exception.
	 * @param <T> Type of the expected Exception.
	 * @param pErrorMessage Expected error message.
	 * @param pRunnable The piece of code, that is being executed.
	 * @throws IllegalStateException The {@code pRunnable} was executed
	 *   without errors, or the error message was unexpected.
	 * @throws RuntimeException An unexpected error occurred, while executing the
	 *   {@code pRunnable}, and is being rethrown.
	 */
	public static <T extends Throwable> void assertFail(Class<T> pErrorType,
			                                            String pErrorMessage,
			                                            FailableRunnable<?> pRunnable) {
		try {
			pRunnable.run();
			throw new IllegalStateException("Expected Exception was not thrown.");
		} catch (Throwable th) {
			if (pErrorType == th.getClass()) {
				if (pErrorMessage != null  &&  !pErrorMessage.equals(th.getMessage())) {
					throw new IllegalStateException("Expected error message to be"
							+ "'" + pErrorMessage + "', got '" + th.getMessage() + "'");
				}
			} else {
				throw Exceptions.show(th);
			}
		}
	}

	/** Asserts, that a {@link FailableRunnable} throws an expected Exception.
	 * @param pError The expected error
	 * @param pRunnable The piece of code, that is being executed.
	 * @throws IllegalStateException The {@code pRunnable} was executed
	 *   without errors, or the error message was unexpected.
	 * @throws RuntimeException An unexpected error occurred, while executing the
	 *   {@code pRunnable}, and is being rethrown.
	 */
	public static <T extends Throwable> void assertFail(T pError,
			                                            FailableRunnable<?> pRunnable) {
		try {
			pRunnable.run();
			throw new IllegalStateException("Expected Exception was not thrown.");
		} catch (Throwable th) {
			if (pError != th) {
				throw Exceptions.show(th);
			}
		}
	}

	/** Asserts, that a {@link FailableRunnable} throws an expected Exception.
	 * @param pErrorType Type of the expected Exception.
	 * @param <T> Type of the expected Exception.
	 * @param pErrorValidator A function, which validates the error, and the
	 *   error message. If the validator returns null, then the error is
	 *   being ignored. Otherwise, an {@link IllegalStateException}
	 *   is thrown, and the validators result will be used as the
	 *   exceptions error message.
	 * @param pRunnable The piece of code, that is being executed.
	 * @throws IllegalStateException The {@code pRunnable} was executed
	 *   without errors, or the error message was unexpected.
	 * @throws RuntimeException An unexpected error occurred, while executing the
	 *   {@code pRunnable}, and is being rethrown.
	 */
	public static <T extends Throwable> void assertFail(Class<T> pErrorType,
			                                            BiFunction<T,String,String> pErrorValidator,
			                                            FailableRunnable<?> pRunnable) {
		try {
			pRunnable.run();
			throw new IllegalStateException("Expected Exception was not thrown.");
		} catch (Throwable th) {
			if (pErrorType == th.getClass()) {
				final String errorMessage = pErrorValidator.apply(pErrorType.cast(th), th.getMessage());
				if (errorMessage == null) {
					return;
				} else {
					throw new IllegalStateException(errorMessage);
				}
			} else {
				throw Exceptions.show(th);
			}
		}
	}
}
