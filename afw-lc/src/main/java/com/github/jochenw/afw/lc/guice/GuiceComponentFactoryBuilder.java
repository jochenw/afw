package com.github.jochenw.afw.lc.guice;

import javax.inject.Provider;

import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.ComponentFactoryBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

public class GuiceComponentFactoryBuilder extends ComponentFactoryBuilder {

	@Override
	protected ComponentFactory newComponentFactory() {
		return new GuiceComponentFactory();
	}

	@Override
	protected void configure(ComponentFactory pComponentFactory, Module pModule) {
		final com.google.inject.Module module = newModule(pModule);
		final Injector injector = Guice.createInjector(module);
		((GuiceComponentFactory) pComponentFactory).setInjector(injector);
	}

	private com.google.inject.Module newModule(Module pModule) {
		return new com.google.inject.Module() {
			@Override
			public void configure(final com.google.inject.Binder pGuiceBinder) {
				final Binder binder = new Binder() {
					@Override
					public <T> void bind(Class<T> pType, String pName, T pInstance) {
						pGuiceBinder.bind(pType).annotatedWith(Names.named(pName)).toInstance(pInstance);
					}

					@Override
					public <T> void bind(Class<T> pType, T pInstance) {
						pGuiceBinder.bind(pType).toInstance(pInstance);
					}

					@Override
					public <T> void bindProvider(Class<T> pType, String pName, Provider<T> pProvider,
							boolean pSingleton) {
						pGuiceBinder.bind(pType).annotatedWith(Names.named(pName)).toProvider(pProvider).in(asScope(pSingleton));
					}

					private Scope asScope(boolean pSingleton) {
						if (pSingleton) {
							return Scopes.SINGLETON;
						} else {
							return Scopes.NO_SCOPE;
						}
					}
					
					@Override
					public <T> void bindProvider(Class<T> pType, Provider<T> pProvider, boolean pSingleton) {
						pGuiceBinder.bind(pType).toProvider(pProvider).in(asScope(pSingleton));
					}

					@Override
					public <T> void bindClass(Class<T> pType, String pName, Class<? extends T> pImplClass,
							boolean pSingleton) {
						pGuiceBinder.bind(pType).annotatedWith(Names.named(pName)).to(pImplClass).in(asScope(pSingleton));
					}

					@Override
					public <T> void bindClass(Class<T> pType, Class<? extends T> pImplClass, boolean pSingleton) {
						if (pType == pImplClass) {
							pGuiceBinder.bind(pType).in(asScope(pSingleton));
						} else {
							pGuiceBinder.bind(pType).to(pImplClass).in(asScope(pSingleton));
						}
					}
				};
				pModule.configure(binder);
			}
		};
	}
}
