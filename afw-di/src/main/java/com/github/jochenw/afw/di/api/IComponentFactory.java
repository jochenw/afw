package com.github.jochenw.afw.di.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.IComponentFactory.IBinding;
import com.github.jochenw.afw.di.api.Scopes.Scope;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.SimpleComponentFactory;

import org.jspecify.annotations.NonNull;


/**
 * Interface of a dependency injection controller.
 */
public interface IComponentFactory {
	/** Configuration of the {@link IComponentFactory}, as created by the
	 * {@link ComponentFactoryBuilder}.
	 */
	public static interface IConfiguration {
		/** Returns the map of bindings.
		 * @return The map of bindings.
		 */
		public Map<Key<Object>, IBinding<Object>> getBindings();
		/** Returns the annotation provider.
		 * @return The annotation provider.
		 */
		public IAnnotationProvider getAnnotationProvider();
		/** Returns the default scope.
		 * @return The default scope.
		 */
		public Scopes.Scope getDefaultScope();
		/** Returns the component factories parent, if any, or null.
		 * @return The component factories parent, if any, or null.
		 */
		public IComponentFactory getParent();
		/** Returns the set of classes, that require injection of static
		 * methods, or fields.
		 * @return The set of classes, that require injection of static
		 * methods, or fields.
		 */
		public Set<Class<?>> getStaticInjectionClasses();

		/** Returns the list of additional binding providers. These
		 * are not yet initialized, and it is the component factories
		 * responsibility to do that.
		 * @return The list of additional binding providers.
		 */
		public List<IBindingProvider> getBindingProviders();
	}

	/** Exception, which is thrown by {@link IComponentFactory#requireInstance(Key)},
	 * if no suitable binding has been registered.
	 */
	public static class NoSuchBindingException extends RuntimeException {
		private static final long serialVersionUID = -4852441857005934044L;
		/** The key, that was requested, but not found.
		 */
		private final Key<?> key;

		/** Creates a new instance with the key, that has been missing,
		 * and the given error message.
		 * @param pKey The key, for which a binding has been requested, but not found.
		 * @param pMessage The error message.
		 */
		public NoSuchBindingException(Key<?> pKey, String pMessage) {
			super(pMessage);
			key = pKey;
		}

		/** Returns the key, for which a binding has been requested, but not found.
		 * @return The key, for which a binding has been requested, but not found.
		 */
		public Key<?> getKey() { return key; }
	}
	/** Interface of the function, which is internally held by the binding,
	 * that provides the requested instance.
	 * @param <T> Type of the provided instance.
	 */
	@FunctionalInterface
	public interface ISupplier<T> extends Function<IComponentFactory,T> {}
	/** Interface of the registered binding.
	 * @param <T> Type of the instance, which is provided by the binding.
	 */
	public interface IBinding<T> extends ISupplier<T> {
		/** Returns the bindings key.
		 * @return The bindings key.
		 */
		public Key<T> getKey();
		/** Returns the bindings scope, either of {@link Scopes#SINGLETON},
		 * {@link Scopes#EAGER_SINGLETON}, or {@link Scopes#NO_SCOPE}.
		 * @return The bindings scope, either of {@link Scopes#SINGLETON},
		 * {@link Scopes#EAGER_SINGLETON}, or {@link Scopes#NO_SCOPE}.
		 */
		public Scopes.Scope getScope();

		/** Creates a new binding with the given, key, supplier, and scope.
		 * @param pKey The created bindings key.
		 * @param pSupplier Implementation of the created bindings
		 * {@link IBinding#apply(Object)} method.
		 * @param pScope The created bindings scope.
		 * @return The created binding.
		 * @param <T> The bindings type.
		 */
		public static <T> IBinding<T> of(Key<T> pKey, ISupplier<T> pSupplier,
				                         Scopes.Scope pScope) {
			return new IBinding<T>() {
				@Override public T apply(IComponentFactory pCf) { return pSupplier.apply(pCf); }
				@Override public Key<T> getKey() { return pKey; }
				@Override public Scope getScope() { return pScope; }
			};
		}
	}

	/** Initializes the {@link IComponentFactory} by passing the configuration.
	 * The caller is supposed to invoke this method exactly once, before
	 * actually using the {@link IComponentFactory}.
	 * @param pConfiguration The configuration.
	 */
	public void init(IConfiguration pConfiguration);

	/** Returns the binding with the given key, if any, or null.
	 * @param pKey The key, under which the binding has been registered.
	 * @return The requested binding, if available, or null.
	 * @throws NullPointerException The key parameter is null.
	 * @param <T> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 */
	public <T> IBinding<T> getBinding(Key<T> pKey);

	/** If a binding with the given key is registered: Invokes the bindings
	 * supplier, and returns the value. Otherwise, returns null.
	 * @param pKey The key, under which the binding has been registered.
	 * @return The requested binding, if available, or null.
	 * @throws NullPointerException The key parameter is null.
	 * @param <T> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 */
	public default <T> T getInstance(Key<T> pKey) {
		final Key<T> key = Objects.requireNonNull(pKey);
		final IBinding<T> binding = getBinding(key);
		if (binding == null) {
			return null;
		} else {
			return binding.apply(this);
		}
	}

	/** If a binding with the given type, and the given name, as a key
	 * is registered: Invokes the bindings supplier, and returns the
	 * value. Otherwise, returns null.
	 * @param pType The bindings type.
	 * @param pName The bindings name.
	 * @return The requested bindings value, if available, or null.
	 * @param <T> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 */
	public default <T> T getInstance(Type pType, String pName) {
		final Key<T> key = Key.of(pType, pName);
		return getInstance(key);
	}

	/** If a binding with the given type, and the name "", as a key
	 * is registered: Invokes the bindings supplier, and returns the
	 * value. Otherwise, returns null.
	 * @param pType The bindings type.
	 * @return The requested bindings value, if available, or null.
	 * @param <T> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 */
	public default <T> T getInstance(Type pType) {
		return getInstance(pType, "");
	}

	/** If a binding with the given class, and the given name, as a key
	 * is registered: Invokes the bindings supplier, and returns the
	 * value. Otherwise, returns null.
	 * @param pType The bindings type.
	 * @param pName The bindings name.
	 * @return The requested bindings value, if available, or null.
	 * @param <S> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 * @param <T> Type of the binding.
	 */
	public default <T,S extends T> S getInstance(Class<T> pType, String pName) {
		@SuppressWarnings("unchecked")
		final S s = (S) getInstance((Type) pType, pName);
		return s;
	}

	/** If a binding with the given class, and the name "", as a key
	 * is registered: Invokes the bindings supplier, and returns the
	 * value. Otherwise, returns null.
	 * @param pType The bindings type.
	 * @return The requested bindings value, if available, or null.
	 * @param <S> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 * @param <T> Type of the binding.
	 */
	public default <T,S extends T> S getInstance(Class<T> pType) {
		return getInstance(pType, "");
	}

	/** If a binding with the given key is registered: Invokes the bindings
	 * supplier, and returns the value. Otherwise, throws a
	 * {@link NoSuchBindingException}.
	 * @param pKey The key, under which the binding has been registered.
	 * @return The requested binding, if available.
	 * @throws NoSuchBindingException No binding with the given key has
	 *   been registered.
	 * @throws NullPointerException The key parameter is null.
	 * @param <T> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 */
	public default <T> T requireInstance(Key<T> pKey) {
		final Key<T> key = Objects.requireNonNull(pKey);
		final IBinding<T> binding = getBinding(key);
		if (binding == null) {
			throw new NoSuchBindingException(key, "No such binding has been registered: " + key);
		} else {
			return binding.apply(this);
		}
	}

	/** If a binding with the given type, and the given name, as a key
	 * is registered: Retrieves the bindings, invokes the supplier,
	 * and returns the value. Otherwise, throws a
	 * {@link NoSuchBindingException}.
	 * @param pType The bindings type.
	 * @param pName The bindings name.
	 * @return The requested binding, if available.
	 * @throws NoSuchBindingException No binding with the given key has
	 *   been registered.
	 * @throws NullPointerException The key parameter is null.
	 * @param <T> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 */
	public default <T> T requireInstance(Type pType, String pName) {
		final Key<T> key = Key.of(pType, pName);
		return requireInstance(key);
	}

	/** If a binding with the given type, and the name "", as a key
	 * is registered: Retrieves the binding, invokes the supplier,
	 * and returns the value. Otherwise, throws a
	 * {@link NoSuchBindingException}.
	 * @param pType The bindings type.
	 * @return The requested binding, if available.
	 * @throws NoSuchBindingException No binding with the given key has
	 *   been registered.
	 * @throws NullPointerException The key parameter is null.
	 * @param <T> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 */
	public default <T> T requireInstance(Type pType) {
		final Key<T> key = Key.of(pType, "");
		return requireInstance(key);
	}

	/** If a binding with the given type, and the given name, as a key
	 * is registered: Retrieves the binding, invokes the supplier,
	 * and returns the value. Otherwise, throws a
	 * {@link NoSuchBindingException}.
	 * @param pType The bindings type.
	 * @param pName The bindings name.
	 * @return The requested binding, if available.
	 * @throws NoSuchBindingException No binding with the given key has
	 *   been registered.
	 * @throws NullPointerException The key parameter is null.
	 * @param <S> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 * @param <T> Type of the binding.
	 */
	public default <T,S extends T> S requireInstance(Class<T> pType, String pName) {
		@SuppressWarnings("unchecked")
		final Key<S> key = (Key<S>) Key.of(pType, pName);
		return requireInstance(key);
	}

	/** If a binding with the given type, and the name "", as a key
	 * is registered: Retrieves the binding, invokes the supplier,
	 * and returns the value. Otherwise, throws a
	 * {@link NoSuchBindingException}.
	 * @param pType The bindings type.
	 * @return The requested binding, if available.
	 * @throws NoSuchBindingException No binding with the given key has
	 *   been registered.
	 * @throws NullPointerException The key parameter is null.
	 * @param <S> Type of the objects, which the binding
	 * {@link IBinding#apply(Object)} supplies.
	 * @param <T> Type of the binding.
	 */
	public default <T,S extends T> @NonNull S requireInstance(Class<T> pType) {
		return requireInstance(pType, "");
	}
	
	/** Initializes the given object by injecting values into all
	 * the fields, and methods, that request it.
	 * @param pObject The object, which is being initialized.
	 */
	public void init(Object pObject);

	/** Creates a new builder, which creates an instance of {@link IComponentFactory}.
	 * The created instance will be a {@code SimpleComponentFactory}.
	 * @return The created builder.
	 */
	public static ComponentFactoryBuilder<?> builder() {
		return builder(() -> new SimpleComponentFactory());
	}

	/** Creates a new builder.
	 * @param pSupplier Supplier for the instance, which is being created by this builder.
	 * @param <T> Type of the created builder.
	 * @return The created builder.
	 */
	public static <T extends IComponentFactory> ComponentFactoryBuilder<T> builder(Supplier<T> pSupplier) {
		return new ComponentFactoryBuilder<T>(pSupplier);
	}

	/** Creates a new instantiator for the given type. Internally,
	 * the instantiator works by creating an instance of the
	 * given implementation type.
	 * @param <T> Type of the instance, that the instantiator
	 *   returns.
	 * @param pImplType Type, that is being instantiated.
	 * @return The created instantiator.
	 */
	public <T> Supplier<T> getInstantiator(Class<? extends T> pImplType);

	/** Creates a new instantiator for the given type. Internally,
	 * the instantiator works by invoking the given constructor.
	 * @param <T> Type of the instance, that the instantiator
	 *   returns.
	 * @param pConstructor Constructor, that is being invoked.
	 * @return The created instantiator.
	 */
	public <T> Supplier<T> getInstantiator(Constructor<? extends T> pConstructor);

	/** Returns an immutable map with the component factories bindings.
	 * This is mainly for test purposes, and should not be used
	 * without very good reasons.
	 * @return The component factories bindings.
	 */
	public Map<Key<Object>, IBinding<Object>> getBindings();
}
