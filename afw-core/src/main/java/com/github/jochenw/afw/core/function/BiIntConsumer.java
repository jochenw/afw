package com.github.jochenw.afw.core.function;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Represents an operation that accepts three input arguments and returns no
 * result.  This is the two-arity specialization of {@link Consumer}, where
 * the first argument is an integer.
 * Unlike most other functional interfaces, {@code BiIntConsumer} is expected
 * to operate via side-effects.
 *
 * <p>This is a <a href="package-summary.html">functional interface</a>
 * whose functional method is {@link #accept(Object, Object, Object)}.
 *
 * @param <I> the type of the first argument to the operation
 *
 * @see Consumer
 * @since 1.8
 */
@FunctionalInterface
public interface BiIntConsumer<I> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param pI1 the first input argument (the integer value)
     * @param pI2 the second input argument
     */
    void accept(int pI1, I pI2);

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
    default BiIntConsumer<I> andThen(BiIntConsumer<? super I> pAfter) {
        Objects.requireNonNull(pAfter);

        return (i1, i2) -> {
            accept(i1, i2);
            pAfter.accept(i1, i2);
        };
    }
}
