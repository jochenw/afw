package com.github.jochenw.afw.core.inject;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import com.google.inject.Provider;


public interface OnTheFlyBinder {
	public <O> Provider<O> getProvider(IComponentFactory pCf, Field pField);
	public <O> void findConsumers(IComponentFactory pCf, Class<?> pType, Consumer<Consumer<O>> pConsumerSink);
}
