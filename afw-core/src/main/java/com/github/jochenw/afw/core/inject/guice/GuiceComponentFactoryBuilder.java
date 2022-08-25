/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.inject.guice;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder;
import com.github.jochenw.afw.core.inject.DefaultOnTheFlyBinder;
import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.Key;
import com.github.jochenw.afw.core.inject.OnTheFlyBinder;
import com.github.jochenw.afw.core.inject.Scopes;
import com.github.jochenw.afw.core.util.Exceptions;
import com.google.inject.Guice;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;


/** Builder for the {@link GuiceComponentFactory}, an implementation of
 * {@link IComponentFactory}, that is based on Google Guice.
 */
public class GuiceComponentFactoryBuilder extends ComponentFactoryBuilder<GuiceComponentFactoryBuilder> {
	private static class GcfbTypeListener implements TypeListener {
		private final IComponentFactory pComponentFactory;
		private final OnTheFlyBinder otfb;

		/**
		 * @param pComponentFactory
		 * @param otfb
		 */
		private GcfbTypeListener(IComponentFactory pComponentFactory, OnTheFlyBinder otfb) {
			this.pComponentFactory = pComponentFactory;
			this.otfb = otfb;
		}

		@Override
		public <I> void hear(TypeLiteral<I> pTypeLiteral, TypeEncounter<I> pEncounter) {
			Class<?> clazz = pTypeLiteral.getRawType();
			otfb.findConsumers(pComponentFactory, clazz, (i) -> {
				final InjectionListener<I> il = new InjectionListener<I>() {
					@Override
					public void afterInjection(I pInjectee) {
						i.accept(pInjectee);
					}
				};
				pEncounter.register(il);
			});
			while (clazz != null  &&  !Object.class.equals(clazz)) {
				for (Field field : clazz.getDeclaredFields()) {
					final Provider<Object> provider = otfb.getProvider(pComponentFactory, field);
					if (provider != null) {
						pEncounter.register(new MembersInjector<I>() {
							@Override
							public void injectMembers(I instance) {
								try {
									if (!field.isAccessible()) {
										field.setAccessible(true);
									}
									field.set(instance, provider.get());
								} catch (Throwable t) {
									throw Exceptions.show(t);
								}
							}
						});
					}
				}
				clazz = clazz.getSuperclass();
			}
		}
	}

	/** Creates a new instance.
	 */
	public GuiceComponentFactoryBuilder() {
		componentFactoryClass(GuiceComponentFactory.class);
		onTheFlyBinder(new DefaultOnTheFlyBinder());
	}

	@Override
	protected void createBindings(IComponentFactory pComponentFactory, List<BindingBuilder<?>> pBindings,
			Set<Class<?>> pStaticInjectionClasses) {
		final GuiceComponentFactory gcf = (GuiceComponentFactory) pComponentFactory;
		final com.google.inject.Module module = new com.google.inject.Module() {
			@Override
			public void configure(com.google.inject.Binder pBinder) {
				final OnTheFlyBinder otfb = getOnTheFlyBinder();
				if (otfb != null) {
					pBinder.bindListener(Matchers.any(), new GcfbTypeListener(pComponentFactory, otfb));
				}
				for (BindingBuilder<?> bb : pBindings) {
					@SuppressWarnings("unchecked")
					final BindingBuilder<Object> bbo = (BindingBuilder<Object>) bb;
					GuiceComponentFactoryBuilder.this.configure(gcf, pBinder,bbo);
				}
				for (Class<?> cl : pStaticInjectionClasses) {
					pBinder.requestStaticInjection(cl);
				}
			}
		};
		gcf.setInjector(Guice.createInjector(module));
	}

	/** Called to apply a single AFW DI binding.
	 * @param pGcf The component factory instance, that is being initializedd.
	 * @param pBinder The Guice binder, that is being configured by the initialization process.
	 * @param pBb The binding builder, that is being applied.
	 */
	protected void configure(GuiceComponentFactory pGcf, com.google.inject.Binder pBinder, BindingBuilder<Object> pBb) {
		final Key<Object> key = pBb.getKey();
		final com.google.inject.Key<Object> gKey = pGcf.asGKey(key);
		final com.google.inject.binder.ScopedBindingBuilder sbb;
		if (pBb.hasTarget()) {
			final Object instance = pBb.getTargetInstance();
			if (instance == null) {
				final Class<? extends Object> cl = pBb.getTargetClass();
				if (cl == null) {
					final Provider<? extends Object> prov = pBb.getTargetProvider();
					if (prov == null) {
						final Supplier<? extends Object> supp = pBb.getTargetSupplier();
						if (supp == null) {
							throw new IllegalStateException("No target found for binding: " + pBb.getKey().getDescription());
						} else {
							sbb = pBinder.bind(gKey).toProvider(() -> supp.get());
						}
					} else {
						sbb = pBinder.bind(gKey).toProvider(prov);
					}
				} else {
					sbb = pBinder.bind(gKey).to(cl);
				}
			} else {
				pBinder.bind(gKey).toInstance(instance);
				// Scope can be ignored, because we have a prefabricated instance.
				// Annotations are handled by the key.
				return;
			}
		} else {
			final Type type = key.getType();
			final com.google.inject.binder.AnnotatedBindingBuilder<Object> abb;
			if (type instanceof Class) {
				@SuppressWarnings("unchecked")
				final Class<Object> cl = (Class<Object>) type;
				abb = pBinder.bind(cl);
			} else {
				throw new IllegalStateException("Invalid type: " + type);
			}
			if (key.getAnnotation() == null) {
				if (key.getAnnotationClass() == null) {
					sbb = abb;
				} else {
					sbb = abb.annotatedWith(key.getAnnotationClass());
				}
			} else {
				sbb = abb.annotatedWith(key.getAnnotation());
			}
		}
		if (pBb.getScope() == Scopes.EAGER_SINGLETON) {
			sbb.asEagerSingleton();
		} else if (pBb.getScope() == Scopes.SINGLETON) {
			sbb.in(com.google.inject.Scopes.SINGLETON);
		} else {
			sbb.in(com.google.inject.Scopes.NO_SCOPE);
		}
	}
}
