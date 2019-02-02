package com.github.jochenw.afw.core.util;

public class AbstractMutable {
	private boolean immutable;
	protected void assertMutable() {
		if (immutable) {
			throw new IllegalStateException("This object is no longer mutable.");
		}
	}

	public boolean isMutable() {
		return !immutable;
	}

	protected void makeImmutable() {
		assertMutable();
		immutable = true;
	}
}
