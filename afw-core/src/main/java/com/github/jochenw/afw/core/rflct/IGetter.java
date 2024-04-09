package com.github.jochenw.afw.core.rflct;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.di.util.Exceptions;

/** An instance of this class provides access to field values,
 * or values, which are returned by get methods.
 * @param <B> The bean type.
 * @param <O> The getters output type.
 */
@FunctionalInterface
public interface IGetter<B,O> {
	/** Invokes the getter to return the value, which is currently
	 * assigned with the {@link IGetter}. Internally, the {@link IGetter
	 * getter} will read a field value, or invoke a get method.
	 * @param pObject The bean, which is supplying the requested value.
	 * @return The value, which has been retrieved from the bean
	 *   {@code pObject}.
	 */
	public O get(B pObject);

	/** Creates a new {@link IGetter}, which will work by invoking
	 * the given method.
	 * @param pMethod The getter method, which is being invoked to retrieve
	 *   a value from the bean.
	 * @param <B> The bean type. (The class declaring the method.)
	 * @param <O> The getter methods return type.
	 * @return The created {@link IGetter getter}.
	 * @throws NullPointerException The parameter {@code pMethod}
	 *   is null.
	 * @throws IllegalArgumentException The given method is not
	 *   a getter, or otherwise invalid.
	 */
	public static <B,O> IGetter<B,O> of(Method pMethod) {
		final @NonNull Method method = Objects.requireNonNull(pMethod, "Method");
		final Class<?> returnType = method.getReturnType();
		if (returnType == Void.class  ||  returnType == Void.TYPE) {
			throw new IllegalArgumentException("Not a getter method"
					+ " (Return type is void): " + method);
		}
		final Class<?>[] parameterTypes = method.getParameterTypes();
		if (parameterTypes != null  &&  parameterTypes.length > 0) {
			throw new IllegalArgumentException("Not a getter method"
					+ " (Takes parameters): " + method);
		}
		@SuppressWarnings("null")
		final @NonNull Class<?> declaringClass = method.getDeclaringClass();
		final Lookup privateLookup = Rflct.getPrivateLookup(declaringClass);
		if (privateLookup == null) {
			// Java 8
			final IGetter<B,O> getter = new IGetter<B,O>() {
				public O get(B pObject) {
					Reflection.makeAcccessible(method);
					try {
						@SuppressWarnings("unchecked")
						final O o = (O) method.invoke(pObject);
						return o;
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				}
			};
			return getter;
		} else {
			// Java 9, or later
			final MethodHandle mh;
			try {
				mh = privateLookup.unreflect(method);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			final IGetter<B,O> getter = new IGetter<B,O>() {
				public O get(B pObject) {
					try {
						final O o = (O) mh.invoke(pObject);
						return o;
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				}
			};
			return getter;
		}
	}
}
