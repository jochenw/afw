package com.github.jochenw.afw.di.impl.guice;

import java.util.Objects;
import java.util.function.BiConsumer;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class GuiceComponentFactoryTypeListener implements TypeListener {
	private final GuiceComponentFactory factory;
	private final IOnTheFlyBinder onTheFlyBinder;

	public GuiceComponentFactoryTypeListener(GuiceComponentFactory pFactory, IOnTheFlyBinder pOnTheFlyBinder) {
		factory = Objects.requireNonNull(pFactory, "Factory");
		onTheFlyBinder = Objects.requireNonNull(pOnTheFlyBinder, "Binder");
	}

	@Override
	public <I> void hear(TypeLiteral<I> pType, TypeEncounter<I> pEncounter) {
		Class<?> clazz = pType.getRawType();
		if (onTheFlyBinder.isInjectable(clazz)) {
			final BiConsumer<IComponentFactory,Object> injector = onTheFlyBinder.getInjector(clazz);
			pEncounter.register(new InjectionListener<Object>() {
				@Override
				public void afterInjection(Object pInjectee) {
					injector.accept(factory, pInjectee);
				}
			});
		}
	}
}

