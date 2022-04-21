package com.github.jochenw.afw.di.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;

public abstract class AbstractOnTheFlyBinder implements IOnTheFlyBinder {
	private static class IsInjectableException extends RuntimeException {
		private static final long serialVersionUID = 2820548098780229944L;
	}
	protected abstract boolean isInjectable(Field pField);
	protected abstract boolean isInjectable(Method pMethod);
	protected abstract BiConsumer<IComponentFactory,Object> getInjector(Field pField);
	protected abstract BiConsumer<IComponentFactory,Object> getInjector(Method pMethod);

	@Override
	public boolean isInjectable(Class<?> pClazz) {
		try {
			walk(pClazz, (cl) -> {
				for (Field field : cl.getDeclaredFields()) {
					if (isInjectable(field)) {
						throw new IsInjectableException();
					}
				}
				for (Method method : cl.getDeclaredMethods()) {
					if (isInjectable(method)) {
						throw new IsInjectableException();
					}
				}
			});
		} catch (IsInjectableException e) {
			return true;
		}
		return false;
	}

	private void walk(Class<?> pClass, Consumer<Class<?>> pConsumer) {
		Class<?> clazz = pClass;
		while (clazz != null  &&  clazz != Object.class) {
			pConsumer.accept(clazz);
			clazz = clazz.getSuperclass();
		}
	}

	@Override
	public BiConsumer<IComponentFactory, Object> getInjector(Class<?> pClass) {
		final List<BiConsumer<IComponentFactory,Object>> injectors = new ArrayList<>();
		walk(pClass, (cl) -> {
			for (Field field : cl.getDeclaredFields()) {
				if (isInjectable(field)) {
					final BiConsumer<IComponentFactory, Object> injector = getInjector(field);
					if (injector == null) {
						throw new NullPointerException("isInjectable(Field) is true, bug getInjector(Field) returned null for " + field);
					}
					injectors.add(injector);
				}
			}
			for (Method method : cl.getDeclaredMethods()) {
				if (isInjectable(method)) {
					final BiConsumer<IComponentFactory, Object> injector = getInjector(method);
					if (injector == null) {
						throw new NullPointerException("isInjectable(Method) is true, bug getInjector(Method) returned null for " + method);
					}
					injectors.add(injector);
				}
			}
		});
		return (cf,inst) -> injectors.forEach((bc) -> bc.accept(cf, inst));
	}

}
