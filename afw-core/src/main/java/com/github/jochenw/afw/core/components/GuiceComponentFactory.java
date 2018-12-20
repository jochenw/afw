package com.github.jochenw.afw.core.components;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;


public class GuiceComponentFactory extends AbstractComponentFactory {
	public class Binding<O extends Object> implements Supplier<O>,Provider<O> {
		private final Supplier<O> supplier;
		private boolean initialized;
		private O instance;
		Binding(Supplier<O> pSupplier) {
			supplier = pSupplier;
		}
		@Override
		public O get() {
			if (!initialized) {
				synchronized(this) {
					if (!initialized) {
						instance = supplier.get();
						initialize(instance);
						initialized = true;
					}
				}
			}
			return instance;
		}
	}
	
	private Injector injector;
	private Map<Key,Binding<Object>> bindings = new HashMap<>();

	@Override
	public void initialize(Object pObject) {
		injector.injectMembers(pObject);
	}

	
	
	@Override
	public void setBindings(Map<Key, Supplier<Object>> pBindings) {
		super.setBindings(pBindings);
		bindings.clear();
		for (Map.Entry<Key,Supplier<Object>> en : pBindings.entrySet()) {
			final Binding<Object> binding = new Binding<Object>(en.getValue());
			bindings.put(en.getKey(), binding);
		}
		final Module module = new Module() {
			@Override
			public void configure(Binder pBinder) {
				for (Map.Entry<Key,Binding<Object>> en : bindings.entrySet()) {
					final Key key = en.getKey();
					@SuppressWarnings("unchecked")
					final Class<Object> cl = (Class<Object>) key.getType();
					final Binding<Object> binding = en.getValue();
					if (key.getName().length() == 0) {
						pBinder.bind(cl).toProvider(binding).in(Scopes.SINGLETON);
					} else {
						pBinder.bind(cl).annotatedWith(Names.named(key.getName())).toProvider(binding).in(Scopes.SINGLETON);
					}
				}
				pBinder.bind(IComponentFactory.class).toInstance(GuiceComponentFactory.this);
			}
		};
		injector = Guice.createInjector(module);
	}

	@Override
	public <O> Supplier<O> getSupplier(Class<?> pType, String pName) {
		final Key key = new Key(pType, pName);
		@SuppressWarnings("unchecked")
		final Supplier<O> supplier = (Supplier<O>) bindings.get(key);
		return supplier;
	}
}
