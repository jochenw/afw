package com.github.jochenw.afw.core.util;

public class ImmutableDouble<O1 extends Object,O2 extends Object> {
	private final O1 o1;
	private final O2 o2;

	public ImmutableDouble(O1 pO1, O2 pO2) {
		o1 = pO1;
		o2 = pO2;
	}

	public O1 getO1() {
		return o1;
	}
	public O2 getO2() {
		return o2;
	}
}
