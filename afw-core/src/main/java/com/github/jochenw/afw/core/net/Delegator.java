package com.github.jochenw.afw.core.net;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.jochenw.afw.core.util.Exceptions;

public class Delegator {
	private final Object source;
	private final Object delegate;
	private final Class<?> superclass;

	public Delegator(Object pSource, Class<?> pSuperclass, Object pDelegate) {
		source = pSource;
		superclass = pSuperclass;
		delegate = pDelegate;
	}

	public Delegator(Object pSource, Class<?> pSuperclass, String pDelegateClassName) {
		try {
			source = pSource;
			superclass = pSuperclass;
			Class<?> implClass = Class.forName(pDelegateClassName);
			Constructor<?> delegateConstructor = implClass.getDeclaredConstructor();
			delegateConstructor.setAccessible(true);
			delegate = delegateConstructor.newInstance();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
	}

	public final <T> T invoke(String pMethodName, Object... args) {
		try {
			final Method method = findMethod(pMethodName, args);
			@SuppressWarnings("unchecked")
			final T t = (T) invoke0(method, args);
			return t;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	private Object invoke0(Method pMethod, Object[] pArgs) {
		try {
			writeFields(superclass, source, delegate);
			pMethod.setAccessible(true);
			Object result = pMethod.invoke(delegate, pArgs);
			writeFields(superclass, delegate, source);
			return result;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	private void writeFields(Class<?> clazz, Object from, Object to) throws Exception {
		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			field.set(to, field.get(from));
		}
	}

	private Method findMethod(String pMethodName, Object[] pArgs) throws NoSuchMethodException {
		Class<?> clazz = superclass;
		if (pArgs.length == 0) {
			return clazz.getDeclaredMethod(pMethodName);
		}
		Method match = null;
		next:
			for (Method method : clazz.getDeclaredMethods()) {
				if (method.getName().equals(pMethodName)) {
					Class<?>[] classes = method.getParameterTypes();
					if (classes.length == pArgs.length) {
						for (int i = 0; i < classes.length; i++) {
							Class<?> argType = classes[i];
							argType = convertPrimitiveClass(argType);
							if (!argType.isInstance(pArgs[i])) continue next;
						}
						if (match == null) {
							match = method;
						} else {
							throw new IllegalStateException("Duplicate match for method " + pMethodName);
						}
					}
				}
			}
		if (match != null) {
			return match;
		}
		throw new IllegalStateException("Method not found: " + pMethodName);
	}

	private Class<?> convertPrimitiveClass(Class<?> pPrimitive) {
		if (pPrimitive.isPrimitive()) {
			if (pPrimitive == int.class) {
				return Integer.class;
			}
			if (pPrimitive == boolean.class) {
				return Boolean.class;
			}
			if (pPrimitive == float.class) {
				return Float.class;
			}
			if (pPrimitive == long.class) {
				return Long.class;
			}
			if (pPrimitive == double.class) {
				return Double.class;
			}
			if (pPrimitive == short.class) {
				return Short.class;
			}
			if (pPrimitive == byte.class) {
				return Byte.class;
			}
			if (pPrimitive == char.class) {
				return Character.class;
			}
		}
		return pPrimitive;
	}

	public DelegatorMethodFinder delegateTo(String methodName,
			Class<?>... parameters) {
		return new DelegatorMethodFinder(methodName, parameters);
	}

	public class DelegatorMethodFinder {
		private final Method method;

		public DelegatorMethodFinder(String methodName,
				Class<?>... parameterTypes) {
			try {
				method = superclass.getDeclaredMethod(
						methodName, parameterTypes
						);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		public <T> T invoke(Object... parameters) {
			@SuppressWarnings("unchecked")
			T t = (T) Delegator.this.invoke0(method, parameters);
			return t;
		}
	}
}
