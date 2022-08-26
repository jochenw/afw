package com.github.jochenw.afw.core.components;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Reflection;

/** Default implementation of {@link IInstantiator}.
 */
public class DefaultInstantiator implements IInstantiator {
	/** Creates a new instance of the given class, using the given {@link ClassLoader}.
	 * @param pClassLoader The {@link ClassLoader}, which is being used.
	 * @param pClassName Name of the class, that is being created.
	 * @return The created instance.
	 */
	protected Object newInstance(ClassLoader pClassLoader, String pClassName) {
		try {
			@SuppressWarnings("unchecked")
			final Class<Object> cl = (Class<Object>) pClassLoader.loadClass(pClassName);
			@SuppressWarnings("unused")
			Throwable th8 = null;
			Throwable th9 = null;
			try {
				// Are we running Java 8, or lower? If so, this should work.
				return cl.newInstance();
			} catch (Throwable t) {
				th8 = t;
			}
			try {
				// For Java 9, or later, this should work.
				final Constructor<Object> cons = cl.getDeclaredConstructor();
				if (!cons.isAccessible()) {
					cons.setAccessible(true);
				}
				return cons.newInstance();
			} catch (Throwable t) {
				th9 = t;
			}
			throw th9;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Converts the given string value into an instance of the given type.
	 * @param pType The requested type.
	 * @param pValue The string value, that is being converted.
	 * @return The converted string value.
	 */
	protected Object asValue(Class<?> pType, String pValue) {
		return Objects.convert(pValue, pType);
	}

	/** Sets the property named {@code pName} on the bean {@code pObject}
	 * to the given {@code pValue}.
	 * @param pObject The bean, that is being modified.
	 * @param pName The property, that is being updated.
	 * @param pValue The property value as a string. Internally,
	 *   {@link #asValue(Class, String)} will be invoked to convert
	 *   the string to a properly typed value.
	 */
	protected void setProperty(Object pObject, String pName, String pValue) {
		final Method setter = Reflection.getPublicSetter(pObject.getClass(), pName);
		if (setter != null) {
			final Class<?> type = setter.getParameterTypes()[0];
			final Object value = asValue(type, pValue);
			try {
				if (!setter.isAccessible()) {
					setter.setAccessible(true);
				}
				setter.invoke(pObject, value);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		} else {
			final Field field = Reflection.getField(pObject.getClass(), pName);
			if (field != null) {
				final Class<?> type = field.getType();
				final Object value = asValue(type, pValue);
				try {
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					field.set(pObject, value);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
		}
	}

	@Override
	public <O> O newInstance(ClassLoader pClassLoader, String pClassName, Map<String, String> pBeanProperties) {
		final Object object = newInstance(pClassLoader, pClassName);
		if (pBeanProperties != null) {
			for (Map.Entry<String,String> en : pBeanProperties.entrySet()) {
				final String name = en.getKey();
				final String value = en.getValue();
				setProperty(object, name, value);
			}
		}
		@SuppressWarnings("unchecked")
		final O o = (O) object;
		return o;
	}
	
}
