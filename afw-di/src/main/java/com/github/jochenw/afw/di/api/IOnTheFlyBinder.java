package com.github.jochenw.afw.di.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;


public interface IOnTheFlyBinder {
	boolean isInjectable(Class<?> pClazz);
	BiConsumer<IComponentFactory,Object> getInjector(Class<?> pClass);
	boolean isInstantiable(Type pType, Annotation[] pAnnotations);
	Function<IComponentFactory, Object> getInstance(Type pType, Annotation[] pAnnotations);
}
