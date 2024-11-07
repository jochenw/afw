package com.github.jochenw.afw.core.util;

import java.io.Serializable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/** A mutable object providing a string value, faking a mutable string.
 */
public class MutableString implements Serializable, Consumer<String>, Supplier<String> {
	private static final long serialVersionUID = 81563172113169338L;
	/** The actual string value.
	 */
	private String value;

	private MutableString(String pValue) { value = pValue; }

	/** Creates a new instance with the given value
	 * @param pValue The created instances value.
	 * @return The created instance.
	 */
	public static MutableString of(@Nullable String pValue) { return new MutableString(pValue); }

	/** Creates a new instance with the value null.
	 * @return The created instance.
	 */
	public static MutableString of() { return new MutableString(null); }

	/** Returns the value string.
	 * @return The value string.
	 */
	@Override
	public @Nullable String get() { return value; }

	/** Returns true, if the value string is non-null, and empty.
	 * @return True, if the value string is non-null, and empty.
	 */
	public boolean isEmpty() { return value != null  &&  value.isEmpty(); }

	/** Returns true, if the value string is non-null, and contains only
	 * white-space characters.
	 * @return True, if the value string is non-null, and contains
	 * only white-space characters.
	 */
	public boolean isBlank() { return value != null  &&  value.trim().isEmpty(); }

	/** Returns true, if the value string is null, or empty.
	 * @return True, if the value string is null, or empty.
	 */
	public boolean isNullOrEmpty() { return value == null  ||  value.isEmpty(); }

	/** Sets the string value, discarding the previous value.
	 * @param pValue The new string value,
	 */
	public void setValue(@Nullable String pValue) { value = pValue; }
	
	/** Sets the string value, discarding the previous value. Synonym
	 * for {@link #setValue(String)}.
	 * @param pValue The new string value,
	 */
	@Override public void accept(String pValue) { value = pValue; }
}
