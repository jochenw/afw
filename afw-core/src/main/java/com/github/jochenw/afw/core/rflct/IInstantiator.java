package com.github.jochenw.afw.core.rflct;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Reflection;

/** An instantiator is an object, that can create instances
 * of a given class by invoking a suitable constructor in a
 * typesafe manner.
 * To obtain an instantiator, use either of the methods
 * {@link #of(Constructor)},
 * {@link #of(Class, Class[])},
 * {@link #of(String, Class[])},
 * or {@link #of(ClassLoader, String, Class[])}.
 * @param <C> The class, which is being instantiated.
 */
public interface IInstantiator<C> {
	/** Creates a new instance of the class {@code &lt;C&gt; }.
	 * @param pArgs The constructor arguments, if any.
	 * @return The created instance.
	 */
	public @NonNull C newInstance(Object... pArgs);

	/** Creates a new instantiator.
	 * @param pConstructor The constructor, which is being
	 *   used internally by the instantiator.
	 * @param <C> Type of the instances, that are being
	 *   created by the instantiator.
	 * @return The created instantiator.
	 */
	public static <C> IInstantiator<C> of(Constructor<C> pConstructor) {
		final Constructor<C> constructor = Objects.requireNonNull(pConstructor, "Constructor");
		@SuppressWarnings("null")
		final @NonNull Class<C> declaringClass = constructor.getDeclaringClass();
		final Lookup lookup = Rflct.getPrivateLookup(declaringClass);
		if (lookup == null) {
			// Java 8, use traditional reflection.
			return new IInstantiator<C>() {
				@Override
				public @NonNull C newInstance(Object... pArgs) {
					Reflection.makeAcccessible(constructor);
					@NonNull
					C instance;
					try {
						@SuppressWarnings("null")
						final @NonNull C c = constructor.newInstance(pArgs);
						instance = c;
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						throw new UndeclaredThrowableException(e);
					}
					return instance;
				}
			};
		} else {
			final Class<?>[] parameterTypes = constructor.getParameterTypes();
			// Java 9+, use the private lookup.
			final MethodType mt = Rflct.getMethodType(declaringClass,
					                                  parameterTypes);
			MethodHandle mh;
			try {
				mh = lookup.findConstructor(declaringClass, mt);
			} catch (NoSuchMethodException | IllegalAccessException e) {
				throw new UndeclaredThrowableException(e);
			}
			return new IInstantiator<C>() {
				@Override
				public @NonNull C newInstance(Object... pArgs) {
					final int numberOfArgs;
					if (pArgs == null) {
						numberOfArgs = 0;
					} else {
						numberOfArgs = pArgs.length;
					}
					if (parameterTypes.length != numberOfArgs) {
						throw new IllegalArgumentException("Expected "
								+ parameterTypes.length + " arguments, got "
								+ numberOfArgs);
					}
					final Object[] args = new Object[parameterTypes.length];
					args[0] = declaringClass;
					if (pArgs != null) {
						for (int i = 0;  i < pArgs.length;  i++) {
							args[i] = pArgs[i];
						}
					}
					@NonNull
					C instance;
					try {
						@SuppressWarnings({ "null", "unchecked" })
						final @NonNull C c = (C) mh.invokeWithArguments(args);
						instance = c;
					} catch (RuntimeException e) {
						throw e;
					} catch (Error e) {
						throw e;
					} catch (Throwable e) {
						throw new UndeclaredThrowableException(e);
					}
					return instance;
				}
			};
		}
	}

	/** Creates a new instantiator.
	 * @param pType Type of the instances, that are being created
	 *   by the instantiator.
	 * @param pParameterTypes Types of parameter types, that are
	 *   being supplied when invoking the instantiator. (Used
	 *   to select a constructor.)
	 * @param <C> Type of the instances, that are being
	 *   created by the instantiator.
	 * @return The created instantiator.
	 * @throws NullPointerException The parameter type is null.
	 */
	public static <C> IInstantiator<C> of(Class<C> pType,
			                              Class<?>... pParameterTypes) {
		final @NonNull Class<C> type = Objects.requireNonNull(pType, "Type");
		final Class<?>[] parameterTypes;
		if (pParameterTypes == null) {
			parameterTypes = (Class<?>[]) Array.newInstance(Class.class, 0);
		} else {
			parameterTypes = pParameterTypes;
		}
		@NonNull Constructor<C> constructor;
		try {
			@SuppressWarnings("null")
			final @NonNull Constructor<C> cons = type.getConstructor(parameterTypes);
			constructor = cons;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new UndeclaredThrowableException(e);
		}
		return of(constructor);
	}

	/** Creates a new instantiator.
	 * @param pClassLoader The class loader, which is being used
	 *   to load the type class.
	 * @param pType Type of the instances, that are being created
	 *   by the instantiator.
	 * @param pParameterTypes Types of parameter types, that are
	 *   being supplied when invoking the instantiator. (Used
	 *   to select a constructor.)
	 * @param <C> Type of the instances, that are being
	 *   created by the instantiator.
	 * @return The created instantiator.
	 * @throws NullPointerException The parameter type is null.
	 */
	public static <C> IInstantiator<C> of(ClassLoader pClassLoader,
			                              String pType,
			                              Class<?>... pParameterTypes) {
		@SuppressWarnings("null")
		final @NonNull ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader cl = Objects.notNull(pClassLoader, currentClassLoader);
		final @NonNull String type = Objects.requireNonNull(pType, "Type");
		@NonNull Class<C> clazz;
		try {
			@SuppressWarnings({"unchecked", "null"})
			final @NonNull Class<C> clss = (Class<C>) cl.loadClass(type); 
			clazz = clss;
		} catch (ClassNotFoundException e) {
			throw new UndeclaredThrowableException(e);
		}
		return of(clazz, pParameterTypes);
	}

	/** Creates a new instantiator. Equivalent to
	 * <pre>
	 *   of(null, pType, pParameterTypes);
	 * </pre>
	 * In other words, this will use the current threads
	 * context class loader.
	 * @param pType Type of the instances, that are being created
	 *   by the instantiator.
	 * @param pParameterTypes Types of parameter types, that are
	 *   being supplied when invoking the instantiator. (Used
	 *   to select a constructor.)
	 * @param <C> Type of the instances, that are being
	 *   created by the instantiator.
	 * @return The created instantiator.
	 * @throws NullPointerException The parameter type is null.
	 */
	public static <C> IInstantiator<C> of(String pType,
			                              Class<?>... pParameterTypes) {
		return of(Thread.currentThread().getContextClassLoader(), pType, pParameterTypes);
	}
}
