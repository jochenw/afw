package com.github.jochenw.afw.core.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a function that accepts two arguments and produces a result.
 * This is the three-arity specialization of {@link Function}.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #apply(Object, Object, Object)}.
 *
 * @param <I1> the type of the first argument to the function
 * @param <I2> the type of the second argument to the function
 * @param <I3> the type of the third argument to the function
 * @param <O> the type of the result of the function
 *
 * @see Function
 * @since 1.8
 */
@FunctionalInterface
public interface TriFunction<I1,I2,I3,O> {
    /**
     * Applies this function to the given arguments.
     *
     * @param pI1 the first function argument
     * @param pI2 the second function argument
     * @param pI3 the third function argument
     * @return the function result
     */
    O apply(I1 pI1, I2 pI2, I3 pI3);

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
    default <V> TriFunction<I1, I2, I3, V> andThen(Function<? super O, ? extends V> pAfter) {
        Objects.requireNonNull(pAfter);
        return (I1 i1, I2 i2, I3 i3) -> pAfter.apply(apply(i1, i2, i3));
    }
}
