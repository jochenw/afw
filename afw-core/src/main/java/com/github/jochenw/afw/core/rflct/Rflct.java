package com.github.jochenw.afw.core.rflct;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Objects;

import org.jspecify.annotations.NonNull;

/** Utility class, which is shared by all the interfaces
 * in this package.
 */
public class Rflct {
	/** This method returns a private {@link Lookup}, if available.
	 * Otherwise, it returns null. This is also used to distinguish
	 * between the case, that we are using Java 8 (aka traditional
	 * Java reflection), or Java 9+ ({@link MethodHandle method
	 * handles}).
	 * @param pType Type, on which the requested {@link Lookup}
	 *   operates. Must not be null.
	 * @return The requested {@link Lookup}, if available, otherwise
	 *   null. The latter case indicates, that we are running on
	 *   Java 8, or lower.
	 * @throws NullPointerException The parameter is null.
	 */
	public static Lookup getPrivateLookup(@NonNull Class<?> pType) {
		final Class<?> type = Objects.requireNonNull(pType, "Type");
        try {
            // This is supposed to work fine on Java 9+
            final Method method = MethodHandles.class.getDeclaredMethod("privateLookupIn", Class.class, Lookup.class);
            final MethodHandle mh = MethodHandles.publicLookup().unreflect(method);
            return (Lookup) mh.invoke(type, MethodHandles.lookup());
        } catch (Throwable t) {
            // We are running on Java 8.
            return null;
        }
	}

	/** Creates a {@link MethodType method type}.
	 * @param pResultType The result type
	 * @param pParameterTypes The parameter types, if any.
	 * @param <C> The result type
	 * @return The created {@link MethodType method type}.
	 */
	public static <C> MethodType getMethodType(Class<C> pResultType, Class<?>[] pParameterTypes) {
		return MethodType.methodType(pResultType, pParameterTypes);
	}
}
