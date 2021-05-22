package com.github.jochenw.afw.core.data;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Functions;
import com.github.jochenw.afw.core.util.Functions.FailableFunction;

/** Utility class for working with data objects.
 */
public class Data {
	/** Extracts a value from the given data store.
	 * @param pData The data store, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The value, which has been retrieved.
	 */
	public static @Nullable Object getValue(@Nonnull FailableFunction<String,Object,?> pData, @Nonnull String pKey) {
		return Functions.apply(pData, pKey);
	}
	/** Extracts a string from the given data store.
	 * @param pData The data store, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@Nonnull FailableFunction<String,Object,?> pData, @Nonnull String pKey, @Nonnull String pDescription) {
		Object value = getValue(pData, pKey);
		if (value == null) {
			return null;
		} else {
			if (value instanceof String) {
				return (String) value;
			} else {
				throw new IllegalArgumentException("Invalid value for " + pDescription
						                        + ": Expected string, got " + value.getClass().getName());
			}
		}
	}
	/** Extracts a non-empty string from the given data store.
	 * @param pData The data store, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull String requireString(@Nonnull FailableFunction<String,Object,?> pData, @Nonnull String pKey,
			                                    @Nonnull String pDescription) {
		Object value = getValue(pData, pKey);
		if (value == null) {
			throw new NullPointerException("Missing value for " + pDescription);
		} else {
			if (value instanceof String) {
				final String s = (String) value;
				if (s.length() == 0) {
					throw new IllegalArgumentException("Empty value for " + pDescription);
				}
				return s;
			} else {
				throw new IllegalArgumentException("Invalid value for " + pDescription
						                        + ": Expected string, got " + value.getClass().getName());
			}
		}
	}
	/** Extracts a path from the given data store.
	 * @param pData The data store, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull Path requirePath(@Nonnull FailableFunction<String,Object,?> pData, @Nonnull String pKey,
			                         @Nonnull String pDescription) {
		Object value = getValue(pData, pKey);
		if (value == null) {
			throw new NullPointerException("Missing value for " + pDescription);
		} else {
			if (value instanceof String) {
				final String s = (String) value;
				if (s.length() == 0) {
					throw new IllegalArgumentException("Empty value for " + pDescription);
				}
				return Paths.get(s);
			} else if (value instanceof Path) {
				return (Path) value;
			} else if (value instanceof File) {
				return ((File) value).toPath();
			} else {
				throw new IllegalArgumentException("Invalid value for " + pDescription
						                        + ": Expected path, got " + value.getClass().getName());
			}
		}
	}
	/** Extracts a string from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@Nonnull Map<String,Object> pMap, @Nonnull String pKey, @Nonnull String pDescription) {
		return getString(pMap::get, pKey, pDescription);
	}
	/** Extracts a string from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@Nonnull Map<String,Object> pMap, @Nonnull String pKey) {
		return requireString(pMap::get, pKey, "Map key " + pKey);
	}
	/** Extracts a string from the given properties.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@Nonnull Properties pProperties, @Nonnull String pKey, @Nonnull String pDescription) {
		return getString(pProperties::get, pKey, pDescription);
	}
	/** Extracts a string from the given map.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@Nonnull Properties pProperties, @Nonnull String pKey) {
		return getString(pProperties::get, pKey, "property " + pKey);
	}
	/** Extracts a non-empty string from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull String requireString(@Nonnull Map<String,Object> pMap, @Nonnull String pKey,
			                                    @Nonnull String pDescription) {
		return requireString(pMap::get, pKey, pDescription);
	}
	/** Extracts a non-empty string from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull String requireString(@Nonnull Map<String,Object> pMap, @Nonnull String pKey) {
		return requireString(pMap::get, pKey, "Map key " + pKey);
	}
	/** Extracts a non-empty string from the given property set.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull Object requireString(@Nonnull Properties pProperties, @Nonnull String pKey,
			                             @Nonnull String pDescription) {
		return requireString(pProperties::get, pKey, pDescription);
	}
	/** Extracts a non-empty string from the given property set.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull String requireString(@Nonnull Properties pProperties, @Nonnull String pKey) {
		return requireString(pProperties::get, pKey, "Property " + pKey);
	}
	/** Extracts a path value from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The path, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull Path requirePath(@Nonnull Map<String,Object> pMap, @Nonnull String pKey,
			                         @Nonnull String pDescription) {
		return requirePath(pMap::get, pKey, pDescription);
	}
	/** Extracts a path value from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The path, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull Path requirePath(@Nonnull Map<String,Object> pMap, @Nonnull String pKey) {
		return requirePath(pMap::get, pKey, "Map key " + pKey);
	}
	/** Extracts a path value from the given property set.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The path, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull Path requirePath(@Nonnull Properties pProperties, @Nonnull String pKey,
			                         @Nonnull String pDescription) {
		return requirePath(pProperties::get, pKey, pDescription);
	}
	/** Extracts a path value from the given property set.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The path, which has been retrieved.
	 * @throws NullPointerException The value, which has been extracted from the data store, is null.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is empty, or not a string.
	 */
	public static @Nonnull Path requirePath(@Nonnull Properties pProperties, @Nonnull String pKey) {
		return requirePath(pProperties::get, pKey, "Property " + pKey);
	}
	/** Extracts a boolean value from the given data store.
	 * @param pData The data store, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable Boolean getBoolean(@Nonnull FailableFunction<String,Object,?> pData, @Nonnull String pKey, @Nonnull String pDescription) {
		Object value = getValue(pData, pKey);
		if (value == null) {
			return null;
		} else {
			if (value instanceof String) {
				return Boolean.valueOf((String) value);
			} else if (value instanceof Boolean) {
				return (Boolean) value;
			} else {
				throw new IllegalArgumentException("Invalid value for " + pDescription
						                        + ": Expected boolean, or string, got " + value.getClass().getName());
			}
		}
	}
	/** Extracts a boolean value from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable Boolean getBoolean(@Nonnull Map<String,Object> pMap, @Nonnull String pKey, @Nonnull String pDescription) {
		return getBoolean(pMap::get, pKey, pDescription);
	}
	/** Extracts a boolean value from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable Boolean getBoolean(@Nonnull Map<String,Object> pMap, @Nonnull String pKey) {
		return getBoolean(pMap::get, pKey, "Map key " + pKey);
	}
	/** Extracts a boolean value from the given property set.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable Boolean getBoolean(@Nonnull Properties pProperties, @Nonnull String pKey, @Nonnull String pDescription) {
		return getBoolean(pProperties::get, pKey, pDescription);
	}
	/** Extracts a boolean value from the given property set.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable Boolean getBoolean(@Nonnull Properties pProperties, @Nonnull String pKey) {
		return getBoolean(pProperties::get, pKey, "Property " + pKey);
	}
}
