package com.github.jochenw.afw.di.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.github.jochenw.afw.di.util.Exceptions;


/** Default implementation of {@link IOnTheFlyBinder}.
 * Supports {@code PostConstruct}, and {@code PreDestroy}.
 */
public class DefaultOnTheFlyBinder extends AbstractOnTheFlyBinder {
	@Override
	protected boolean isInjectable(Field pField) {
		return false;
	}

	@Override
	protected boolean isInjectable(Method pMethod) {
		final Class<?> cl = pMethod.getDeclaringClass();
		final Class<?> returnType = pMethod.getReturnType();
		final Class<?>[] parameterTypes = pMethod.getParameterTypes();
		return (IComponentFactoryAware.class.isAssignableFrom(cl)  &&
				"init".equals(pMethod.getName())  &&
				(returnType == null  ||  returnType == Void.TYPE)  &&
				parameterTypes.length == 0  &&  IComponentFactory.class == parameterTypes[0]);
	}

	@Override
	protected BiConsumer<IComponentFactory, Object> getInjector(Field pField) {
		return null;
	}

	@Override
	protected BiConsumer<IComponentFactory, Object> getInjector(Method pMethod) {
		final Class<?> cl = pMethod.getDeclaringClass();
		final Class<?> returnType = pMethod.getReturnType();
		final Class<?>[] parameterTypes = pMethod.getParameterTypes();
		if (IComponentFactoryAware.class.isAssignableFrom(cl)  &&
				"init".equals(pMethod.getName())  &&
				(returnType == null  ||  returnType == Void.TYPE)  &&
				parameterTypes.length == 0  &&  IComponentFactory.class == parameterTypes[0]) {
			return (cf,inst) -> {
				try {
					pMethod.invoke(inst, cf);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};
		} else {
			return null;
		}
	}

	@Override
	public boolean isInstantiable(Type pType, Annotation[] pAnnotations) {
		return false;
	}

	@Override
	public Function<IComponentFactory, Object> getInstantiator(Type pType, Annotation[] pAnnotations) {
		return null;
	}

}
