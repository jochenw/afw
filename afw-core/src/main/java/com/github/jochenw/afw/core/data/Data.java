package com.github.jochenw.afw.core.data;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Objects;

/** Utility class for working with data objects.
 */
public class Data {
	/**
	 * @param <O> Type of the data store object.
	 */
	public static class Accessor<O extends Object> {
		private final BiFunction<O,String,Object> function;

		protected Accessor(BiFunction<O,String,Object> pFunction) {
			function = pFunction;
		}

		/** Extracts a value from the given data store.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The value, which has been retrieved.
		 */
		public @Nullable Object getValue(@Nonnull O pData, @Nonnull String pKey) {
			final O object = Objects.requireNonNull(pData, "Object");
			final String key = Objects.requireNonNull(pKey, "Key");
			return function.apply(object, key);
		}
		/** Extracts a string from the given data store.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Short description of the expected value, for use in error messages.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 */
		public @Nullable String getString(@Nonnull O pData, @Nonnull String pKey, @Nonnull String pDescription) {
			Object value = getValue(pData, pKey);
			if (value == null) {
				return null;
			} else {
				if (value instanceof String) {
					return (String) value;
				} else {
					final String description = Objects.notNull(pDescription, pKey);
					throw new IllegalArgumentException("Invalid value for parameter " + description
							                        + ": Expected string, got " + value.getClass().getName());
				}
			}
		}
		/** Extracts a string from the given data store.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 */
		public @Nullable String getString(@Nonnull O pData, @Nonnull String pKey) {
			return getString(pData, pKey, pKey);
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
		public @Nonnull String requireString(@Nonnull O pData, @Nonnull String pKey,
				                             @Nonnull String pDescription) {
			Object value = getValue(pData, pKey);
			if (value == null) {
				throw new NullPointerException("Missing value for parameter " + pDescription);
			} else {
				if (value instanceof String) {
					final String s = (String) value;
					if (s.length() == 0) {
						throw new IllegalArgumentException("Empty value for parameter " + pDescription);
					}
					return s;
				} else {
					throw new IllegalArgumentException("Invalid value for parameter " + pDescription
							                        + ": Expected string, got " + value.getClass().getName());
				}
			}
		}
		/** Extracts a non-empty string from the given data store.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The string, which has been retrieved.
		 * @throws NullPointerException The value, which has been extracted from the data store, is null.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is empty, or not a string.
		 */
		public @Nonnull String requireString(@Nonnull O pData, @Nonnull String pKey) {
			return requireString(pData, pKey, pKey);
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
		public @Nonnull Path requirePath(@Nonnull O pData, @Nonnull String pKey,
				                         @Nonnull String pDescription) {
			Object value = getValue(pData, pKey);
			if (value == null) {
				throw new NullPointerException("Missing value for parameter " + pDescription);
			} else {
				if (value instanceof String) {
					final String s = (String) value;
					if (s.length() == 0) {
						throw new IllegalArgumentException("Empty value for parameter " + pDescription);
					}
					return Paths.get(s);
				} else if (value instanceof Path) {
					return (Path) value;
				} else if (value instanceof File) {
					return ((File) value).toPath();
				} else {
					throw new IllegalArgumentException("Invalid value for parameter " + pDescription
							                        + ": Expected path, got " + value.getClass().getName());
				}
			}
		}
		/** Extracts a path value from the given map.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The path, which has been retrieved.
		 * @throws NullPointerException The value, which has been extracted from the data store, is null.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is empty, or not a string.
		 */
		public @Nonnull Path requirePath(@Nonnull O pData, @Nonnull String pKey) {
			return requirePath(pData, pKey, pKey);
		}

		/** Extracts a boolean value from the given map.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Short description of the expected value, for use in error messages.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 */
		public @Nullable Boolean getBoolean(@Nonnull O pData, @Nonnull String pKey, @Nonnull String pDescription) {
			final Object value = getValue(pData, pKey);
			if (value == null) {
				return null;
			} else {
				if (value instanceof Boolean) {
					return (Boolean) value;
				} else if (value instanceof String) {
					return Boolean.valueOf((String) value);
				} else {
					throw new IllegalArgumentException("Invalid value for parameter " + pDescription
							+ ": Expected string, or boolean, got " + value.getClass().getName());
				}
			}
		}

		/** Extracts a boolean value from the given map.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 */
		public @Nullable Boolean getBoolean(@Nonnull O pData, @Nonnull String pKey) {
			return getBoolean(pData, pKey, "Map key " + pKey);
		}
	}

	/** {@link Accessor} object for instances of {@link Map Map<String,Object>}.
	 */
	public static final Accessor<Map<String,Object>> MAP_ACCESSOR = new Accessor<Map<String,Object>>((map,key) -> map.get(key));
	/** {@link Accessor} object for instances of {@link Properties}.
	 */
	public static final Accessor<Properties> PROPS_ACCESSOR = new Accessor<Properties>((props,key) -> props.get(key));

	/** Extracts a string from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@Nonnull Map<String,Object> pMap, @Nonnull String pKey, @Nonnull String pDescription) {
		return MAP_ACCESSOR.getString(pMap, pKey, pDescription);
	}

	/** Extracts a string from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@Nonnull Map<String,Object> pMap, @Nonnull String pKey) {
		return MAP_ACCESSOR.getString(pMap, pKey);
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
		return PROPS_ACCESSOR.getString(pProperties, pKey, pDescription);
	}

	/** Extracts a string from the given map.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@Nonnull Properties pProperties, @Nonnull String pKey) {
		return PROPS_ACCESSOR.getString(pProperties, pKey, pKey);
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
		return MAP_ACCESSOR.requireString(pMap, pKey, pDescription);
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
		return MAP_ACCESSOR.requireString(pMap, pKey, pKey);
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
		return PROPS_ACCESSOR.requireString(pProperties, pKey, pDescription);
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
		return PROPS_ACCESSOR.requireString(pProperties, pKey, pKey);
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
		return MAP_ACCESSOR.requirePath(pMap, pKey, pDescription);
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
		return MAP_ACCESSOR.requirePath(pMap, pKey, "Map key " + pKey);
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
		return PROPS_ACCESSOR.requirePath(pProperties, pKey, pDescription);
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
		return PROPS_ACCESSOR.requirePath(pProperties, pKey, pKey);
	}

	/** Extracts a boolean value from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public @Nullable Boolean getBoolean(@Nonnull Map<String,Object> pMap, @Nonnull String pKey, @Nonnull String pDescription) {
		return MAP_ACCESSOR.getBoolean(pMap, pKey, pDescription);
	}

	/** Extracts a boolean value from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable Boolean getBoolean(@Nonnull Map<String,Object> pMap, @Nonnull String pKey) {
		return MAP_ACCESSOR.getBoolean(pMap, pKey, pKey);
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
		return PROPS_ACCESSOR.getBoolean(pProperties, pKey, pDescription);
	}

	/** Extracts a boolean value from the given property set.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable Boolean getBoolean(@Nonnull Properties pProperties, @Nonnull String pKey) {
		return PROPS_ACCESSOR.getBoolean(pProperties, pKey, pKey);
	}
}
