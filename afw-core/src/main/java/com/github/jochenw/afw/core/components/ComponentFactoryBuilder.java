package com.github.jochenw.afw.core.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import com.github.jochenw.afw.core.components.IComponentFactory.Key;
import com.github.jochenw.afw.core.util.AbstractBuilder;
import com.github.jochenw.afw.core.util.Exceptions;

public class ComponentFactoryBuilder<T extends ComponentFactoryBuilder<T>> extends AbstractBuilder<T> {
	public interface Binder {
		<O extends Object> void bind(Class<O> pType, String pName, Class<? extends O> pImplementationClass);
		<O extends Object> void bind(Class<O> pType, String pName, O pSingleton);
		<O extends Object> void bind(Class<O> pType, String pName, Provider<? extends O> pProvider);
		<O extends Object> void bind(Class<O> pType, String pName, Supplier<? extends O> pSupplier);
		public default <O extends Object> void bind(Class<O> pType, Class<? extends O> pImplementationClass) {
			bind(pType, "", pImplementationClass);
		}
		public default <O extends Object> void bind(Class<O> pType, O pSingleton) {
			bind(pType, "", pSingleton);
		}
		public default <O extends Object> void bind(Class<O> pType, Provider<? extends O> pSingleton) {
			bind(pType, "", pSingleton);
		}
		public default <O extends Object> void bind(Class<O> pType, Supplier<? extends O> pSingleton) {
			bind(pType, "", pSingleton);
		}
	}
	public interface Module {
		void bind(Binder pBinder);
	}

	private IComponentFactory instance;
	private final List<Module> modules = new ArrayList<>();
	private Function<Map<Key,Supplier<Object>>,IComponentFactory> constructor;

	public @Nonnull T module(@Nonnull Module pModule) {
		assertMutable();
		modules.add(pModule);
		return self();
	}

	public @Nonnull T modules(@Nonnull Module... pModules) {
		assertMutable();
		if (pModules != null) {
			for (Module m : pModules) {
				modules.add(m);
			}
		}
		return self();
	}

	public @Nonnull T modules(@Nonnull Iterable<Module> pModules) {
		assertMutable();
		if (pModules != null) {
			for (Module m : pModules) {
				modules.add(m);
			}
		}
		return self();
	}

	public List<Module> getModules() {
		return modules;
	}

	public @Nonnull T constructor(Function<Map<Key,Supplier<Object>>,IComponentFactory> pConstructor) {
		assertMutable();
		constructor = pConstructor;
		return self();
	}

	public Function<Map<Key,Supplier<Object>>,IComponentFactory> getConstructor() {
		return constructor;
	}
	
	protected IComponentFactory newInstance() {
		final Map<Key,Supplier<Object>> bindings = getBindings();
		if (constructor == null) {
			final SimpleComponentFactory cf = new SimpleComponentFactory();
			cf.setBindings(bindings);
			cf.setOnTheFlyBinder(new SimpleOnTheFlyBinder());
			return cf;
		} else {
			return constructor.apply(bindings);
		}
	}

	public IComponentFactory build() {
		if (isMutable()) {
			makeImmutable();
			instance = newInstance();
		}
		return instance;
	}
	
	protected Map<Key,Supplier<Object>> getBindings() {
		final Map<Key,Supplier<Object>> bindings = new HashMap<>();
		final Binder binder = new Binder() {
			@Override
			public <O extends Object> void bind(Class<O> pType, String pName, Class<? extends O> pImplementationClass) {
				bindings.put(new Key(pType, pName), () -> {
					try {
						return pImplementationClass.newInstance();
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				});
			}

			@Override
			public <O extends Object> void bind(Class<O> pType, String pName, O pSingleton) {
				final Key key = new Key(pType, pName);
				bindings.put(key, () -> { return pSingleton; });
			}

			@Override
			public <O extends Object> void bind(Class<O> pType, String pName, Provider<? extends O> pProvider) {
				final Key key = new Key(pType, pName);
				bindings.put(key, () -> { return pProvider.get(); });
			}

			@Override
			public <O extends Object> void bind(Class<O> pType, String pName, Supplier<? extends O> pSupplier) {
				final Key key = new Key(pType, pName);
				bindings.put(key, () -> { return pSupplier.get(); });
			}
		};
		for (Module m : getModules()) {
			m.bind(binder);
		}
		return bindings;
	}
}
