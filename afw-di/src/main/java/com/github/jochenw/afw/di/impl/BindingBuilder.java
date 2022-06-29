package com.github.jochenw.afw.di.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.function.Supplier;

import javax.inject.Provider;

import com.github.jochenw.afw.di.api.AnnotatableBindingBuilder;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.LinkableBindingBuilder;
import com.github.jochenw.afw.di.api.Names;
import com.github.jochenw.afw.di.api.ScopableBindingBuilder;
import com.github.jochenw.afw.di.api.Scope;
import com.github.jochenw.afw.di.api.Scopes;


public class BindingBuilder<T> implements LinkableBindingBuilder<T>, AnnotatableBindingBuilder<T> {
	private final Key<T> key;
	private Scope scope = Scopes.NO_SCOPE;
	private Class<? extends Annotation> annotationType;
	private Annotation annotation;
	private Class<?> targetClass;
	private Key<?> targetKey;
	private Supplier<?> targetSupplier;
	private Provider<?> targetProvider;
	private Constructor<?> targetConstructor;
	private Object targetInstance;

	public BindingBuilder(Key<T> pKey) {
		key = Objects.requireNonNull(pKey, "Key");
	}

	public Scope getScope() {
		return scope;
	}
	
	public Class<?> getTargetClass() {
		return targetClass;
	}

	public Key<?> getTargetKey() {
		return targetKey;
	}

	public Supplier<?> getTargetSupplier() {
		return targetSupplier;
	}

	public Provider<?> getTargetProvider() {
		return targetProvider;
	}

	public Constructor<?> getTargetConstructor() {
		return targetConstructor;
	}

	public Object getTargetInstance() {
		return targetInstance;
	}

	public Class<? extends Annotation> getAnnotationType() {
		return annotationType;
	}

	public Annotation getAnnotation() {
		return annotation;
	}

	public Key<T> getKey() {
		return key;
	}

	@Override
	public void in(Scope pScope) {
		scope = Objects.requireNonNull(pScope, "Scope");
	}

	@Override
	public void asEagerSingleton() {
		scope = Objects.requireNonNull(Scopes.EAGER_SINGLETON);
	}

	@Override
	public LinkableBindingBuilder<T> annotatedWith(Class<? extends Annotation> pAnnotationType) {
		annotationType = Objects.requireNonNull(pAnnotationType, "AnnotationType");
		return this;
	}

	@Override
	public LinkableBindingBuilder<T> annotatedWith(Annotation pAnnotation) {
		annotation = Objects.requireNonNull(pAnnotation, "Annotation");
		return this;
	}

	@Override
	public LinkableBindingBuilder<T> named(String pName) {
		return annotatedWith(Names.named(pName));
	}

	@Override
	public ScopableBindingBuilder to(Class<? extends T> pImplementation) {
		return toClass(pImplementation);
	}

	@Override
	public ScopableBindingBuilder toClass(Class<? extends T> pImplementation) {
		final Class<?> implementation = Objects.requireNonNull(pImplementation, "Implementation");
		targetClass = implementation;
		return this;
	}

	@Override
	public ScopableBindingBuilder to(Key<? extends T> pKey) {
		final Key<? extends T> key = Objects.requireNonNull(pKey, "Key");
		targetKey = key;
		return this;
	}

	@Override
	public void toInstance(T pInstance) {
		final T t = Objects.requireNonNull(pInstance, "Instance");
		targetInstance = t;
		scope = Scopes.SINGLETON;
	}

	@Override
	public ScopableBindingBuilder toProvider(Provider<? extends T> pProvider) {
		final Provider<? extends T> provider = Objects.requireNonNull(pProvider, "Provider");
		targetProvider = provider;
		return this;
	}

	@Override
	public ScopableBindingBuilder toSupplier(Supplier<? extends T> pSupplier) {
		final Supplier<? extends T> supplier = Objects.requireNonNull(pSupplier, "Supplier");
		targetSupplier = supplier;
		return this;
	}

	@Override
	public <S extends T> ScopableBindingBuilder toConstructor(Constructor<S> pConstructor) {
		final Constructor<S> constructor = Objects.requireNonNull(pConstructor, "Constructor");
		targetConstructor = constructor;
		return this;
	}
}