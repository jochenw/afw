package com.github.jochenw.afw.core.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.impl.DefaultOnTheFlyBinder;

public class AfwCoreOnTheFlyBinder extends DefaultOnTheFlyBinder {
	@Override
	protected boolean isInjectable(Field pField) {
		return pField.isAnnotationPresent(LogInject.class)
				||  pField.isAnnotationPresent(PropInject.class)
				||  super.isInjectable(pField);
	}

	@Override
	protected boolean isInjectable(Method pMethod) {
		return pMethod.isAnnotationPresent(PostConstruct.class)  ||  pMethod.isAnnotationPresent(PreDestroy.class)
				||  pMethod.isAnnotationPresent(LogInject.class)
				||  pMethod.isAnnotationPresent(PropInject.class)
				||  super.isInjectable(pMethod);
	}

	@Override
	protected BiConsumer<IComponentFactory, Object> getInjector(Field pField) {
		final LogInject
	}

	@Override
	protected BiConsumer<IComponentFactory, Object> getInjector(Method pMethod) {
		// TODO Auto-generated method stub
		return super.getInjector(pMethod);
	}
}
