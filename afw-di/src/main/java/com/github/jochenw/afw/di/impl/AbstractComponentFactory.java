package com.github.jochenw.afw.di.impl;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.github.jochenw.afw.di.api.Key;
import com.google.inject.name.Names;


public abstract class AbstractComponentFactory implements IComponentFactory {
	private String id;
	private Key<IComponentFactory> key;

	public abstract void configure(IOnTheFlyBinder pOnTheFlyBinder,
			                       List<BindingBuilder<Object>> pBuilders,
			                       Set<Class<?>> pStaticInjectionClasses);

	public void setId(String pId) {
		id = Objects.requireNonNull(pId, "Id");
		key = new Key<IComponentFactory>(IComponentFactory.class, Names.named(id));
	}

	public String getId() {
		return id;
	}

	protected Key<IComponentFactory> getKey() {
		return key;
	}
}
