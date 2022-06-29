package com.github.jochenw.afw.di.impl.simple;

import javax.inject.Provider;

public class ProviderBinding implements Binding {
	private final Binding parentBinding;

	public ProviderBinding(Binding pParentBinding) {
		this.parentBinding = pParentBinding;
	}

	@Override
	public Object apply(SimpleComponentFactory pScf) {
		return new Provider<Object>() {
			@Override
			public Object get() {
				return parentBinding.apply(pScf);
			}
		};
	}
}
