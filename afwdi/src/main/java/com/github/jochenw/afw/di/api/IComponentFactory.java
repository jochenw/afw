package com.github.jochenw.afw.di.api;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.simple.SimpleComponentFactory;

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
	public interface IBinding<T> {
		/** Returns the bindings key.
		 * @return The bindings key.
		 */
		public Key<T> getKey();
		/** Returns the bindings supplier.
		 * @return The bindings supplier.
		 */
		public ISupplier<T> getSupplier();
		/** Returns the bindings scope, either of {@link Scopes#SINGLETON},
		 * {@link Scopes#EAGER_SINGLETON}, or {@link Scopes#NO_SCOPE}.
		 * @return The bindings scope, either of {@link Scopes#SINGLETON},
		 * {@link Scopes#EAGER_SINGLETON}, or {@link Scopes#NO_SCOPE}.
		 */
		public Scopes.Scope getScope();
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
			return binding.getSupplier().apply(this);
		}
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
			return binding.getSupplier().apply(this);
		}
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
}
