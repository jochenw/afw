package com.github.jochenw.afw.core.util;

import java.util.function.Supplier;


public class LazyInitializable<O extends Object> implements Supplier<O> {
	private final Supplier<O> supplier;
	private final Consumer<O> initializer;
	private volatile O instance;

	public LazyInitializable(Supplier<O> pSupplier, Consumer<O> pInitializer) {
		Objects.requireNonNull(pSupplier, "Supplier");
		supplier = pSupplier;
		initializer = pInitializer;
	}

	@Override
	public O get() {
		if (instance == null) {
			synchronized(this) {
				O o = supplier.get();
				Objects.requireNonNull(o, "Instance");
				if (initializer != null) {
					initializer.accept(o);
				}
				instance = o;
			}
		}
		return instance;
	}
}
