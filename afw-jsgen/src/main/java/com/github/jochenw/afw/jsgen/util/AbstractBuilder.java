package com.github.jochenw.afw.jsgen.util;

public abstract class AbstractBuilder {
	private boolean immutable;

	protected void makeImmutable() {
		immutable = true;
	}

	protected void assertMutable() {
		assertMutable(this);
	}

	public boolean isMutable() {
		return !immutable;
	}

	public Object build() {
		makeImmutable();
		return this;
	}

	public static void assertMutable(AbstractBuilder pBuilder) {
		if (pBuilder.immutable) {
			throw new IllegalStateException("This object is no longer mutable.");
		}
	}
}
