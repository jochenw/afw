package com.github.jochenw.afw.core.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the two-arity specialization of {@link Function}, where the
 * first argument is an integer.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(int, Object)}.
 *
 * @param <I> the type of the non-integer argument to the function
 * @param <O> the type of the result of the function
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface BiIntFunction<I,O> {
    /**
     * Applies this function to the given arguments.
     *
     * @param pI1 the integer function argument
     * @param pI2 the second function argument
     * @return the function result
     */
    O apply(int pI1, I pI2);

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code pAfter} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code pAfter} function, and of the
     *           composed function
     * @param pAfter the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <V> BiIntFunction<I, V> andThen(Function<? super O, ? extends V> pAfter) {
        Objects.requireNonNull(pAfter);
        return (int i1, I i2) -> pAfter.apply(apply(i1, i2));
    }
}
