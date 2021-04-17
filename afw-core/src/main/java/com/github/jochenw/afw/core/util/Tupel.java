package com.github.jochenw.afw.core.util;

import java.util.Objects;


/** A tupel of two objects (the attributes). Typically, this is used as a result type,
 * if a method is actually returning two values. In that case, the Tupel can act as a
 * wrapper object for both.
 * @param <O1> Type of the first attribute.
 * @param <O2> Type of the second attribute.
 */
public class Tupel<O1, O2> {
	private final O1 attribute1;
	private final O2 attribute2;

	/** Creates a new instance, which wraps the given objects.
	 * @param pAttribute1 The first attribute.
	 * @param pAttribute2 The second attribute.
c	 */
	public Tupel(O1 pAttribute1, O2 pAttribute2) {
		attribute1 = pAttribute1;
		attribute2 = pAttribute2;
	}

	/** Returns the first attribute.
	 * @return The first attribute.
	 */
	public O1 getAttribute1() {
		return attribute1;
	}

	/** Returns the second attribute.
	 * @return The second attribute.
	 */
	public O2 getAttribute2() {
		return attribute2;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getAttribute1(), getAttribute2());
	}

	@Override
	public boolean equals(Object pOther) {
		if (pOther == this) { return true; }
		if (pOther == null  ||  !getClass().equals(pOther.getClass())) { return false; }
		@SuppressWarnings("unchecked")
		final Tupel<O1,O2> other = (Tupel<O1,O2>) pOther;
		return Objects.equals(getAttribute1(), other.getAttribute1())  &&  Objects.equals(getAttribute2(), other.getAttribute2());
	}

	/**
	 * Creates a new instance with the given attributes.
	 * @param pAttribute1 The first attribute.
	 * @param pAttribute2 The second attribute.
	 * @param <O1> The first attribute's type.
	 * @param <O2> The second attribute's type.
	 * @return The created {@link Tupel}, which has the given attributes.
	 */
	public static <O1,O2> Tupel<O1,O2> of(O1 pAttribute1, O2 pAttribute2) {
		return new Tupel<O1,O2>(pAttribute1, pAttribute2);
	}
}
