package com.github.jochenw.afw.core.rflct;

import java.util.Objects;

import org.jspecify.annotations.NonNull;


/** Utility class for wowking with names in Java reflection.
 */
public class Names {
	/** Creates a new instance. Protected, because this class contains only
	 * static methods.
	 */
	protected Names() {}

	/** Creates a camel cased method property name. Example: prefix=get,
	 * suffix=foo yields getFoo.
	 * @param pPrefix The method names prefix, for example "get", or "is".
	 * @param pSuffix The property name.
	 * @return The prefix, appended by the upercased first character of the
	 *   suffix, and the remainder of the suffix.
	 */
	public static @NonNull String upperCased(@NonNull String pPrefix, @NonNull String pSuffix) {
		Objects.requireNonNull(pPrefix, "Prefix");
		Objects.requireNonNull(pSuffix, "Suffix");
		return pPrefix + Character.toUpperCase(pSuffix.charAt(0)) + pSuffix.substring(1);
	}
}
