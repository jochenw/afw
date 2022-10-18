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


/** Utility class for working with Java reflection.
 */
public class Reflection {
	/** Returns a supplier, which creates an instance by invoking the given constructor.
	 * The constructors parameters are obtained by invoking the given parameter function.
	 * @param pConstructor The constructor, which is being invoked to create the instance.
	 * @param pParameterSupplier The parameter supplier, which is being invoked to
	 * obtain the constructor arguments. For example, if the constructor requires three
	 *   arguments, then the parameter supplier will be invoked three times, with the
	 *   arguments 0, 1, and 2, in that order.
	 * @return The created supplier. Invoking this supplier will trigger invocations of
	 *   the parameter supplier (if necessary), followed by an invocation of the
	 *   constructor. The constructors result will be returned by the supplier.
	 */
	public static Supplier<Object> newInstantiator(Constructor<Object> pConstructor, IntFunction<Object> pParameterSupplier) {
		try {
			final Lookup privateLookup = getPrivateLookup(pConstructor.getDeclaringClass());
			final Class<?>[] parameterTypes = pConstructor.getParameterTypes();
			if (privateLookup == null) {
				// Java 8
				return () -> {
					final Object[] args = new Object[parameterTypes.length];
					for (int i = 0;  i < parameterTypes.length;  i++) {
						args[i] = pParameterSupplier.apply(i);
					}
					if (!pConstructor.isAccessible()) {
						pConstructor.setAccessible(true);
					}
					try {
						return pConstructor.newInstance(args);
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				};
			} else {
				final MethodType mt = MethodType.methodType(void.class, parameterTypes);
				final MethodHandle mh = privateLookup.findConstructor(pConstructor.getDeclaringClass(), mt);
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
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Returns a {@link BiConsumer}, which may be used to set the given fields value
	 * on an instance.
	 * @param pField The field, to which the injector will write the value.
	 * @return The created {@link BiConsumer biconsumer}. Invoking this consumer
	 *  will trigger a modification of {@code pField} on an instance. The {@link
	 *  BiConsumer biconsumer's} arguments are the instance, that is being modified,
	 *  and the new field value.
	 */
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

	/** Returns a {@link BiConsumer}, which may be used to invoke a setter method
	 * on an instance.
	 * @param pMethod The setter method, that will be invoked by the injector.
	 * @return The created {@link BiConsumer biconsumer}. Invoking this consumer
	 *  will trigger an invocation of {@code pMethod} on an instance. The {@link
	 *  BiConsumer biconsumer's} arguments are the instance, that is being modified,
	 *  and the setter methods argument.
	 */
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

	/** Creates a consumer, which invokes the given no-args method on an instance.
	 * @param <O> Type of the instance, on which a method is being invoked.
	 * @param pMethod The method, that is being invoked. This method must not take
	 *   any arguments.
	 * @return A consumer, which invokes the given no-args method on an instance.
	 */
	public static <O> Consumer<O> newInvoker(Method pMethod) {
		final Lookup privateLookup = getPrivateLookup(pMethod.getDeclaringClass());
		final Class<?>[] parameterTypes = pMethod.getParameterTypes();
		if (parameterTypes != null  &&  parameterTypes.length > 0) {
			throw new IllegalArgumentException("Unable to create an invoker for method "
					+ pMethod + ", because it takes parameters.");
		}
		if (privateLookup == null) {
			// Java 8: Use Reflection
			return (inst) -> {
				try {
					pMethod.invoke(inst);
				} catch (Exception e) {
					throw Exceptions.show(e);
				}
			};
		} else {
			MethodHandle mh;
			try {
				mh = privateLookup.unreflect(pMethod);
			} catch (Exception e) {
				throw Exceptions.show(e);
			}
			return (inst) -> {
				try {
					mh.invoke(inst);
				} catch (Throwable e) {
					throw Exceptions.show(e);
				}
			};
		}
	}
}
