package org.apache.logging.log4j.util;

import java.util.function.Function;

/** Three-arguments version of a {@link BiFunction}.
 * Represents a function that accepts three arguments and produces a result.
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
     * Performs the operation given the specified arguments.
     * @param pI1 the first input argument
     * @param pI2 the second input argument
     * @param pI3 the second input argument
     * @return The result object
     */
    O apply(I1 pI1, I2 pI2, I3 pI3);
}
