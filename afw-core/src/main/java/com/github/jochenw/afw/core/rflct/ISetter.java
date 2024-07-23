package com.github.jochenw.afw.core.rflct;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.di.util.Exceptions;

/** Instance of this class provide the ability to change
 * field values, either by direct modification of the
 * field, or by invoking a setter method.
 * @param <B> The bean type.
 * @param <I> The setters parameter type.
 * @see IGetter 
 */
@FunctionalInterface
public interface ISetter<B,I> {
	/** Invokes the setter, updating the underlying field value.
	 * @param pObject The bean, which is being updated.
	 * @param pValue The updated value.
	 */
	public void set(@NonNull B pObject, I pValue);

	/** Creates a new instance, which operates by invoking the given
	 * setter method.
	 * @param pMethod The method, which is being invoked to update
	 * the bean.
     * @param <B> The bean type.
     * @param <I> The setters parameter type.
	 * @return The created instance.
	 * @throws NullPointerException The parameter {@code pMethod} is null.
	 */
	public static <B,I> ISetter<B,I> of(Method pMethod) {
		final @NonNull Method method = Objects.requireNonNull(pMethod, "Method");
		@SuppressWarnings("null")
		final @NonNull Class<?> methodClass = pMethod.getDeclaringClass();
		final Class<?>[] parameterTypes = pMethod.getParameterTypes();
		if (parameterTypes.length == 0) {
			throw new IllegalArgumentException("The method " + pMethod + " cannot be used as a setter, because it takes no arguments.");
		}
		if (parameterTypes.length > 1) {
			throw new IllegalArgumentException("The method " + pMethod + " cannot be used as a setter, because it takes multiple arguments.");
		}
		final Lookup lookup = Rflct.getPrivateLookup(methodClass);
		if (lookup == null) {
			// Java 8, use classic reflection.
			return (b,i) -> {
				Reflection.makeAcccessible(method);
				try {
					method.invoke(b, i);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};
		} else {
			// Java 9+, use method handles.
			final MethodHandle mh;
			try {
				mh = lookup.unreflect(method);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			return (b,i) -> {
				try {
					mh.invoke(b, i);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};
		}
	}
	/** Creates a new instance, which operates by updating the given field
	 * directly.
	 * @param pField The field, which is being updated.
     * @param <B> The bean type.
     * @param <I> The setters parameter type.
	 * @return The created instance.
	 * @throws NullPointerException The parameter {@code pField} is null.
	 */
	public static <B,I> ISetter<B,I> of(Field pField) {
		final @NonNull Field field = Objects.requireNonNull(pField, "Field");
		@SuppressWarnings("null")
		final @NonNull Class<?> fieldClass = field.getDeclaringClass();
		@SuppressWarnings("null")
		final @NonNull Class<?> fieldType = field.getType();
		final Lookup lookup = Rflct.getPrivateLookup(fieldClass);
		if (lookup == null) {
			// Java 8, use classic reflection.
			return (b,i) -> {
				try {
					Reflection.makeAcccessible(field);
					field.set(b, i);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};
		} else {
			// Java 9+, use method handles.
			final MethodHandle mh;
			try {
				mh = lookup.findSetter(fieldClass, field.getName(), fieldType);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			return (b,i) -> {
				try {
					mh.invokeWithArguments(b, i);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};
		}
	}

	/** Creates a new instance, which operates by updating the field
	 * with the given name directly
	 * @param pDeclaringType Type of the bean class, which declares the
	 * field.
	 * @param pProperty Name of the property, which is being updated.
     * @param <B> The bean type.
     * @param <I> The setters parameter type.
	 * @return The created instance.
	 * @throws NullPointerException Either of the parameters is null.
	 * @throws IllegalArgumentException No field with the name
	 *   {@code pProperty} was found in the class {@code pDeclaringType}.
	 */
	public static <B,I> ISetter<B,I> of(Class<B> pDeclaringType, @NonNull String pProperty) {
		final @NonNull Class<B> declaringType = Objects.requireNonNull(pDeclaringType, "Type");
		final @NonNull String property = Objects.requireNonNull(pProperty, "Property");
		Class<?> cl = declaringType;
		while (cl != null  &&  cl != Object.class) {
			Field field;
			try {
				field = declaringType.getDeclaredField(property);
			} catch (NoSuchFieldException e) {
				field = null;
			}
			if (field != null) {
				return of(field);
			}
			cl = cl.getSuperclass();
		}
		if (property.length() > 0) {
			final String setterMethodName = "set" + Character.toUpperCase(property.charAt(0)) + property.substring(1);
			final String builderMethodName = property;
			cl = declaringType;
			while (cl != null  &&  cl != Object.class) {
				for (Method m : cl.getDeclaredMethods()) {
					final int modifiers = m.getModifiers();
					if (!Modifier.isStatic(modifiers)  &&  !Modifier.isAbstract(modifiers)) {
						final Class<?> returnType = m.getReturnType();
						final Class<?>[] parameterTypes = m.getParameterTypes();
						if (m.getName().equals(setterMethodName)  &&
								(returnType == Void.TYPE  ||  returnType == Void.class)  &&
								parameterTypes.length == 1) {
							// This is a setter method. Convert it to an ISetter, and
							// return that.
							return of(m);
						}
						if (m.getName().equals(builderMethodName)  &&
								(returnType != Void.TYPE  &&  returnType != Void.class)  &&
								parameterTypes.length == 1) {
							// This is a builder method. Convert it to an ISetter, and
							// return that.
							return of(m);
						}
					}
				}
			}
			throw new IllegalArgumentException("No field named " + property +
					                           ", no setter method named " + setterMethodName +
					                           ", and no builder method named " + builderMethodName +
					                           " found in class " + declaringType.getName() +
					                           ", or any anchestor class.");
 		}
		throw new IllegalArgumentException("No field named " + property + " found in class " + declaringType.getName());
	}
}
