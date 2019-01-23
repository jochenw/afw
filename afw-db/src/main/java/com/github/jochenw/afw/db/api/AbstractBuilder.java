package com.github.jochenw.afw.db.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractBuilder<S extends Object,T extends AbstractBuilder<S,T>> {
	private boolean immutable;
	@SuppressWarnings("null")
	private @Nullable S instance;

	protected void assertMutable() {
		if (immutable) {
			throw new IllegalStateException("This object is no longer mutable.");
		}
	}

	protected void makeImmutable() {
		if (immutable) {
			throw new IllegalStateException("This object is no longer mutable.");
		}
		immutable = true;
	}

	public boolean isMutable() {
		return !immutable;
	}

	protected abstract @Nonnull S newInstance();

	public @Nonnull S build() {
		if (instance == null) {
			if (!immutable) {
				makeImmutable();
			}
			instance = newInstance();
		}
		return instance;
	}
	
	T self() {
		@SuppressWarnings("unchecked")
		final T t = (T) this;
		return t;
	}
}
