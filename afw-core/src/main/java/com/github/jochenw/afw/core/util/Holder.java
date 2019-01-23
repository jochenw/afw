package com.github.jochenw.afw.core.util;

public class Holder<T> {
	private T value;

	public T get() {
		return value;
	}

	public void set(T pValue) {
		value = pValue;
	}

	public <O> Holder<O> synchronizedHolder(Holder<O> pHolder) {
		return new Holder<O>() {
			@Override
			public O get() {
				synchronized(pHolder) {
					return pHolder.get();
				}
			}

			@Override
			public void set(O pValue) {
				synchronized(pHolder) {
					pHolder.set(pValue);
				}
			}
			
		};
	}
}
