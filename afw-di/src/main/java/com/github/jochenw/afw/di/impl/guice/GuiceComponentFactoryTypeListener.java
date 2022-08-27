package com.github.jochenw.afw.di.impl.guice;

import java.util.Objects;
import java.util.function.BiConsumer;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/** Implementation of a Guice {@link TypeListener}, that implements use of the
 * {@link IOnTheFlyBinder},
 */
public class GuiceComponentFactoryTypeListener implements TypeListener {
	private final GuiceComponentFactory factory;
	private final IOnTheFlyBinder onTheFlyBinder;

	/** Creates a new instance,which implements using the given {@link IOnTheFlyBinder}
	 * for the given {@link GuiceComponentFactory}.
	 * @param pFactory The component factory, that is being customized by dynamic bindings.
	 * @param pOnTheFlyBinder Thr on-the-fly binder, that provides the dynamic bindings.
	 */
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

