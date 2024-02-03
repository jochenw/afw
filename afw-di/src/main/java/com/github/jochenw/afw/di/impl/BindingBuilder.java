package com.github.jochenw.afw.di.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.di.api.AnnotatableBindingBuilder;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.LinkableBindingBuilder;
import com.github.jochenw.afw.di.api.ScopableBindingBuilder;
import com.github.jochenw.afw.di.api.Scope;
import com.github.jochenw.afw.di.api.Scopes;


/** A builder for component factory bindings.
 * @param <T> Type of the object, that is being injected by the binding.
 */
public abstract class BindingBuilder<T> implements LinkableBindingBuilder<T>, AnnotatableBindingBuilder<T> {
	private final @NonNull Key<T> key;
	private Key<T> annotatedKey;
	private Scope scope = Scopes.NO_SCOPE;
	private Class<? extends Annotation> annotationType;
	private Annotation annotation;
	private Class<?> targetClass;
	private Key<?> targetKey;
	private Supplier<?> targetSupplier;
	private Constructor<?> targetConstructor;
	private Object targetInstance;

	/** Creates a new instance with the given key.
	 * @param pKey The bindings key, which provides the information, where, and when, the
	 *   binding is applicable.
	 */
	public BindingBuilder(Key<T> pKey) {
		key = Objects.requireNonNull(pKey, "Key");
		annotatedKey = key;
	}

	/**
	 * Returns the bindings scope.
	 * @return The bindings scope.
	 * @see #in(Scope)
	 * @see #asEagerSingleton()
	 */
	public Scope getScope() {
		return scope;
	}

	/** If {@link #to(Class)} has been invoked: Returns the bindings
	 * implementation class. Otherwise, returns null.
	 * @return The bindings implementation class, if any, or null.
	 * @see #to(Class)
	 */
	public Class<?> getTargetClass() {
		return targetClass;
	}

	/** If {@link #to(Key)} has been invoked: Returns the bindings
	 * implementation key. Otherwise, returns null.
	 * @return The bindings implementation key, if any, or null.
	 * @see #to(Key)
	 */
	public Key<?> getTargetKey() {
		return targetKey;
	}

	/** If {@link #toSupplier(Supplier)} has been invoked: Returns the bindings
	 * implementation supplier. Otherwise, returns null.
	 * @return The bindings implementation supplier, if any, or null.
	 * @see #toSupplier(Supplier)
	 */
	public Supplier<?> getTargetSupplier() {
		return targetSupplier;
	}

	/** If {@link #toConstructor(Constructor)} has been invoked: Returns the bindings
	 * implementation constructor. Otherwise, returns null.
	 * @return The bindings implementation constructor, if any, or null.
	 * @see #toConstructor(Constructor)
	 */
	public Constructor<?> getTargetConstructor() {
		return targetConstructor;
	}

	/** If {@link #toInstance(Object)} has been invoked: Returns the bindings
	 * implementation instance. Otherwise, returns null.
	 * @return The bindings implementation instance, if any, or null.
	 * @see #toInstance(Object)
	 */
	public Object getTargetInstance() {
		return targetInstance;
	}

	/** If {@link #annotatedWith(Class)} has been invoked: Returns the bindings
	 * annotaion class. Otherwise, returns null.
	 * @return The bindings annotation class, if any, or null.
	 * @see #annotatedWith(Class)
	 */
	public Class<? extends Annotation> getAnnotationType() {
		return annotationType;
	}

	/** If {@link #annotatedWith(Annotation)} has been invoked: Returns the bindings
	 * annotation instance. Otherwise, returns null.
	 * @return The bindings annotation instance, if any, or null.
	 * @see #annotatedWith(Class)
	 */
	public Annotation getAnnotation() {
		return annotation;
	}

	/** Returns the bindings key, excluding optional annotations.
	 * @return The bindings key, excluding optional annotations.
	 * @see #getAnnotatedKey()
	 */
	public @NonNull Key<T> getKey() {
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
		annotatedKey = Key.of(key.getType(), pAnnotationType);
		return this;
	}

	@Override
	public LinkableBindingBuilder<T> annotatedWith(Annotation pAnnotation) {
		annotation = Objects.requireNonNull(pAnnotation, "Annotation");
		annotatedKey = Key.of(key.getType(), pAnnotation);
		return this;
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

	/** Returns the bindings key, including optional annotations.
	 * @return The bindings key, including optional annotations.
	 * @see #getKey()
	 */
	public Key<T> getAnnotatedKey() {
		return annotatedKey;
	}
}