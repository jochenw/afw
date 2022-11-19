package com.github.jochenw.afw.core.function;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts three input arguments and returns no
 * result.  This is the three-arity specialization of {@link Consumer}.
 * Unlike most other functional interfaces, {@code TriConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object)}.
 *
 * @param <I1> the type of the first argument to the operation
 * @param <I2> the type of the second argument to the operation
 * @param <I3> the type of the third argument to the operation
 *
 * @see Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface TriConsumer<I1,I2,I3> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param pI1 the first input argument
     * @param pI2 the second input argument
     * @param pI3 the third input argument
     */
    void accept(I1 pI1, I2 pI2, I3 pI3);

    /**
     * Returns a composed {@code TriConsumer} that performs, in sequence, this
     * operation followed by the {@code pAfter} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code pAfter} operation will not be performed.
     *
     * @param pAfter the operation to perform after this operation
     * @return a composed {@code TriConsumer} that performs in sequence this
     * operation followed by the {@code pAfter} operation
     * @throws NullPointerException if {@code pAfter} is null
     */
    default TriConsumer<I1,I2,I3> andThen(TriConsumer<? super I1, ? super I2, ? super I3> pAfter) {
        Objects.requireNonNull(pAfter);

        return (i1, i2, i3) -> {
            accept(i1, i2, i3);
            pAfter.accept(i1, i2, i3);
        };
    }
}
