package com.github.jochenw.afw.core.rflct;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
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
		Field field;
		try {
			field = declaringType.getDeclaredField(property);
		} catch (NoSuchFieldException e) {
			field = null;
		}
		if (field != null) {
			return of(field);
		}
		throw new IllegalArgumentException("No field named " + property + " found in class " + declaringType.getName());
	}
}
