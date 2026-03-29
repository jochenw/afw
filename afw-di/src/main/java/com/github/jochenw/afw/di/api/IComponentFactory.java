package com.github.jochenw.afw.di.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.Scopes.Scope;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.simple.SimpleComponentFactory;

import org.jspecify.annotations.NonNull;


/**
 * Interface of a dependency injection controller.
 */
public interface IComponentFactory {
	/** Exception, which is thrown by {@link IComponentFactory#requireInstance(Key)},
	 * if no suitable binding has been registered.
	 */
	public static class NoSuchBindingException extends RuntimeException {
		private static final long serialVersionUID = -4852441857005934044L;
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
		 * {@link IBinding#apply(IComponentFactory)} method.
		 * @param pScope The created bindings scope.
		 * @return The created binding.
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
	/** Returns the binding with the given key, if any, or null.
	 * @param pKey The key, under which the binding has been registered.
	 * @return The requested binding, if available, or null.
	 * @throws NullPointerException The key parameter is null.
	 */
	public <T> IBinding<T> getBinding(Key<T> pKey);

	/** If a binding with the given key is registered: Invokes the bindings
	 * supplier, and returns the value. Otherwise, returns null.
	 * @param pKey The key, under which the binding has been registered.
	 * @return The requested binding, if available, or null.
	 * @throws NullPointerException The key parameter is null.
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
	 * The created instance will be a {@link SimpleComponentFactory}.
	 * @return The created builder.
	 */
	public static ComponentFactoryBuilder<SimpleComponentFactory> builder() {
		return builder(SimpleComponentFactory.class);
	}

	/** Creates a new builder, which creates an instance of the given
	 * class.
	 * @param pType Type of the created builder.
	 * @return The created builder.
	 */
	public static <T extends AbstractComponentFactory> ComponentFactoryBuilder<T> builder(Class<T> pType) {
		return new ComponentFactoryBuilder<T>(pType);
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
