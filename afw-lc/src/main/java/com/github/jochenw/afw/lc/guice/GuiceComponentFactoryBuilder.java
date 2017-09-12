package com.github.jochenw.afw.lc.guice;

import javax.inject.Provider;

import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.ComponentFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;

public class GuiceComponentFactoryBuilder extends ComponentFactoryBuilder {
	private static class AfwBinder extends Binder {
		private final com.google.inject.Binder binder;

		AfwBinder(com.google.inject.Binder pBinder) {
			binder = pBinder;
		}

		@Override
		public <T> void bind(Class<T> pType) {
			binder.bind(pType).in(Scopes.SINGLETON);
		}
		@Override
		public <T> void bind(Class<T> pType, String pName, T pInstance) {
			if (pInstance == null) {
				bind(pType, pInstance);
			} else {
				if (pName == null) {
					binder.bind(pType).toInstance(pInstance);
				} else {
					binder.bind(pType).annotatedWith(Names.named(pName)).toInstance(pInstance);
				}
			}
		}

		@Override
		public <T> void bind(Class<T> pType, T pInstance) {
			if (pInstance == null) {
				final Provider<T> prov = new Provider<T>() {
					@Override
					public T get() {
						return pInstance;
					}
				};
				binder.bind(pType).toProvider(prov).in(Scopes.SINGLETON);;
			} else {
				binder.bind(pType).toInstance(pInstance);
			}
		}

		@Override
		public <T> void bind(Class<T> pType, String pName, Class<? extends T> pImplClass) {
			if (pName == null) {
				bind(pType, pImplClass);
			} else {
				binder.bind(pType).annotatedWith(Names.named(pName)).to(pImplClass).in(Scopes.SINGLETON);
			}
		}

		@Override
		public <T> void bind(Class<T> pType, Class<? extends T> pImplClass) {
			binder.bind(pType).to(pImplClass).in(Scopes.SINGLETON);
		}
	}

	@Override
	protected ComponentFactory newComponentFactory() {
		return new GuiceComponentFactory();
	}

	@Override
	protected void configure(final ComponentFactory pComponentFactory, final Module pModule) {
		final com.google.inject.Module module = new com.google.inject.Module() {
			@Override
			public void configure(com.google.inject.Binder pBinder) {
				pBinder.bindListener(Matchers.any(), new GuiceInjLogListener(getLogFactory(), getPropertyFactory()));
				pModule.configure(new AfwBinder(pBinder));
			}
		};
		final Injector injector = Guice.createInjector(module);
		((GuiceComponentFactory) injector.getInstance(ComponentFactory.class)).setInjector(injector);
	}
}
