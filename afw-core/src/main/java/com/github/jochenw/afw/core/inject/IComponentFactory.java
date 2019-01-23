package com.github.jochenw.afw.core.inject;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

public interface IComponentFactory {
	public default <O> Key<O> asKey(@Nonnull Type pType) {
		return new Key<O>(pType);
	}
	public default <O> Key<O> asKey(@Nonnull Type pType, @Nonnull String pName) {
		if (pName == null  ||  pName.length() == 0) {
			return new Key<O>(pType);
		} else {
			final Named named = Names.named(pName);
			return new Key<O>(pType, named);
		}
	}
	public void init(Object pObject);
	public @Nullable <O> O getInstance(Key<O> pKey);
	public default @Nullable <O> O getInstance(@Nonnull Class<O> pType) {
		return getInstance(asKey(pType));
	}
	public default @Nullable <O> O getInstance(@Nonnull Class<O> pType, @Nonnull String pName) {
		return getInstance(asKey(pType, pName));
	}
	public default @Nonnull <O> O requireInstance(Key<O> pKey) throws NoSuchElementException {
		final O o = getInstance(pKey);
		if (o == null) {
			throw new NoSuchElementException("No such instance: " + pKey.getDescription());
		}
		return o;
	}
	public default @Nonnull <O> O requireInstance(@Nonnull Class<O> pType) {
		return requireInstance(asKey(pType));
	}
	public default @Nonnull <O> O requireInstance(@Nonnull Class<O> pType, @Nonnull String pName) {
		return requireInstance(asKey(pType, pName));
	}
	public <O> O newInstance(Class<? extends O> pImplClass);
}
