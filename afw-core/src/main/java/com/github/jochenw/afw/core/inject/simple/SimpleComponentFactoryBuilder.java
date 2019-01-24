package com.github.jochenw.afw.core.inject.simple;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.inject.Provider;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder;
import com.github.jochenw.afw.core.inject.DefaultOnTheFlyBinder;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.Key;
import com.github.jochenw.afw.core.inject.OnTheFlyBinder;
import com.github.jochenw.afw.core.inject.OnTheFlyBinder.ScopedProvider;
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
		final List<Key<?>> keys = new ArrayList<>();
		for (BindingBuilder<?> bb : pBindings) {
			final Key<?> key = bb.getKey();
			keys.add(key);
			final Provider<?> supplier = asProvider(bb);
			final Binding<?> binding = asBinding(scf, bb, supplier);
			scf.addBinding(key, binding, true);
		}
		final OnTheFlyBinder otfb = getOnTheFlyBinder();
		if (otfb != null) {
			final BiConsumer<Key<Object>,ScopedProvider<Object>> bindingSink = (k,p) -> {
				final Binding<?> binding = scf.newBinding(p, p.getScope());
				scf.addBinding(k, binding, false);
			};
			for (Key<?> key : keys) {
				final Type type = key.getType();
				if (type instanceof Class) {
					@SuppressWarnings("unchecked")
					final Class<Object> cl = (Class<Object>) type;
					otfb.findBindings(scf, cl, bindingSink);
				}
			}
		}
		scf.setStaticInjectionPredicate((c) -> pStaticInjectionClasses.contains(c));
	}

	private Binding<?> asBinding(SimpleComponentFactory pCf, BindingBuilder<?> pBb, Provider<?> pProvider) {
		return pCf.newBinding(pProvider, pBb.getScope());
	}
}
