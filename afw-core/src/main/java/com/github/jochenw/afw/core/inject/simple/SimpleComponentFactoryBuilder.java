package com.github.jochenw.afw.core.inject.simple;

import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder;
import com.github.jochenw.afw.core.inject.DefaultOnTheFlyBinder;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.Key;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactory.Binding;


public class SimpleComponentFactoryBuilder extends ComponentFactoryBuilder<SimpleComponentFactoryBuilder> {
	public SimpleComponentFactoryBuilder() {
		componentFactoryClass(SimpleComponentFactory.class);
		onTheFlyBinder(new DefaultOnTheFlyBinder());
	}

	@Override
	protected void createBindings(IComponentFactory pComponentFactory, List<BindingBuilder<?>> pBindings,
			Set<Class<?>> pStaticInjectionClasses) {
		final SimpleComponentFactory scf = (SimpleComponentFactory) pComponentFactory;
		scf.setOnTheFlyBinder(getOnTheFlyBinder());
		for (BindingBuilder<?> bb : pBindings) {
			final Key<?> key = bb.getKey();
			final Provider<?> supplier = asProvider(bb);
			final Binding<?> binding = asBinding(scf, bb, supplier);
			scf.addBinding(key, binding);
		}
		scf.setStaticInjectionPredicate((c) -> pStaticInjectionClasses.contains(c));
	}

	private Binding<?> asBinding(SimpleComponentFactory pCf, BindingBuilder<?> pBb, Provider<?> pProvider) {
		return pCf.newBinding(pProvider, pBb.getScope());
	}
}
