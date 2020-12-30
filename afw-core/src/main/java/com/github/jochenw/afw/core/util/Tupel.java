package com.github.jochenw.afw.core.util;

import java.util.Objects;

public class Tupel<O1, O2> {
	private final O1 attribute1;
	private final O2 attribute2;

	public Tupel(O1 pAttribute1, O2 pAttribute2) {
		attribute1 = pAttribute1;
		attribute2 = pAttribute2;
	}

	public O1 getAttribute1() {
		return attribute1;
	}

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
}
