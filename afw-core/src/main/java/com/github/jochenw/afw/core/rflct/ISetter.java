package com.github.jochenw.afw.core.rflct;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.di.api.Names;
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
		if (Modifier.isAbstract(pMethod.getModifiers())) {
			throw new IllegalArgumentException("The method " + pMethod + " cannot be used as a setter, because it is abstract.");
		}
		if (Modifier.isStatic(pMethod.getModifiers())) {
			throw new IllegalArgumentException("The method " + pMethod + " cannot be used as a setter, because it is static.");
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
	public static <B,I> ISetter<B,I> of(@NonNull Class<B> pDeclaringType, @NonNull String pProperty) {
		Objects.requireNonNull(pDeclaringType, "Type");
		final @NonNull String property = Objects.requireNonNull(pProperty, "Property");
		@Nullable Class<?> cl = pDeclaringType;
		final String setterName = Names.upperCased("set", property);
		while (cl != null  &&  cl != Object.class) {
			// Try finding a get method.
			final Method[] methods = cl.getDeclaredMethods();
			for (Method m : methods) {
				final Class<?> methodType = m.getReturnType();
				final boolean voidMethod = (methodType == Void.class  ||  methodType == Void.TYPE  ||  methodType == null);
				final Class<?>[] parameterTypes = m.getParameterTypes();
				final boolean exactlyOneParameter = parameterTypes != null  &&  parameterTypes.length == 1;
				final int mod = m.getModifiers();
				final boolean abstractOrStatic = Modifier.isAbstract(mod)  ||  Modifier.isStatic(mod);
				if (setterName.equals(m.getName())) {
					// Is the method a traditional setter:
					//  - Void type
					//  - Exactly one parameter
					//  - Neither abstract, nor static
					if (voidMethod  &&  exactlyOneParameter  &&  !abstractOrStatic) {
						return of(m);
					}
				} else if (property.equals(m.getName())) {
					// Is this a builder method:
					//  - Non-void type
					//  - Exactly one parameter
					//  - Neither abstract, nor static
					if (!voidMethod  &&  exactlyOneParameter  &&  !abstractOrStatic) {
						return of(m);
					}
				}
			}
			// Try finding a matching field.
			try {
				final Field field = cl.getDeclaredField(property);
				return of(field);
			} catch (NoSuchFieldException e) {
				// Do nothing.
			}
			cl = cl.getSuperclass();
		}
		throw new IllegalArgumentException("Neither a matching setter method " + setterName
				+ ", nor a matching builder method " + property
				+ ", nor a matching field " + property
				+ " found in class " + pDeclaringType.getName()
				+ ", or any superclass");
	}
}
