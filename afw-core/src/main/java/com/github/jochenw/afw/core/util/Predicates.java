package com.github.jochenw.afw.core.util;

import java.util.function.Predicate;

public class Predicates {
	/** A {@link Predicate}, which is always true, or false.
	 */
	public static <O> Predicate<O> always(final boolean pValue) {
		return new Predicate<O>() {
			@Override
			public boolean test(O pT) {
				return pValue;
			}
		};
	}
	/** A {@link Predicate}, which is always true.
	 */
	public static <O> Predicate<O> alwaysTrue() {
		return always(true);
	}
	/** A {@link Predicate}, which is always false.
	 */
	public static <O> Predicate<O> alwaysFalse() {
		return always(false);
	}
}
