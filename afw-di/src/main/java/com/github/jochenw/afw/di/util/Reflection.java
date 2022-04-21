package com.github.jochenw.afw.di.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;


public class Reflection {
	private static final Lookup lookup = MethodHandles.lookup();

	public static Supplier<Object> newInstantiator(Constructor<Object> pConstructor, IntFunction<Object> pParameterSupplier) {
		try {
			final Class<?>[] parameterTypes = pConstructor.getParameterTypes();
			final MethodType mt = MethodType.methodType(void.class, parameterTypes);
			final MethodHandle mh = lookup.findConstructor(pConstructor.getDeclaringClass(), mt);
			return () -> {
				final Object[] args = new Object[parameterTypes.length];
				for (int i = 0;  i < parameterTypes.length;  i++) {
					args[i] = pParameterSupplier.apply(i);
				}
				try {
					return mh.invokeWithArguments(args);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static BiConsumer<Object,Object> newInjector(Field pField) {
		final boolean staticInjection = Modifier.isStatic(pField.getModifiers());
		try {
			final Lookup privateLookup = getPrivateLookup(pField.getDeclaringClass());
			if (privateLookup == null) {
				// Java 8
				return (pojo,value) -> {
					if (!pField.isAccessible()) {
						pField.setAccessible(true);
					}
					try {
						pField.set(pojo, value);
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				};
			} else {
				return (pojo,value) -> {
					try {
						final MethodHandle mh = privateLookup.unreflectSetter(pField);
						if (staticInjection) {
							mh.invoke(value);
						} else {
							mh.invoke(pojo, value);
						}
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				};
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static BiConsumer<Object,Object[]> newInjector(Method pMethod) {
		final boolean staticInjection = Modifier.isStatic(pMethod.getModifiers());
		try {
			final Lookup privateLookup = getPrivateLookup(pMethod.getDeclaringClass());
			if (privateLookup == null) {
				// Java 8
				return (pojo,value) -> {
					if (!pMethod.isAccessible()) {
						pMethod.setAccessible(true);
					}
					try {
						pMethod.invoke(pojo, value);
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				};
			} else {
				return (pojo,value) -> {
					try {
						final MethodHandle mh = privateLookup.unreflect(pMethod);
						if (staticInjection) {
							mh.invoke(value);
						} else {
							mh.invoke(pojo, value);
						}
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				};
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
	
	protected static Lookup getPrivateLookup(Class<?> pType) {
		try {
			final Method method = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, Lookup.class);
			return (Lookup) method.invoke(null, pType, MethodHandles.lookup());
		} catch (Throwable t) {
			return null;
		}
	}
}
