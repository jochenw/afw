package com.github.jochenw.afw.core.data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.data.Data.Accessor.PathCriterion;
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

		/** Interface of a path criterion, as used by
		 * {@link Data.Accessor#requirePath(Object, String, String, PathCriterion...)}.
		 */
		@FunctionalInterface
		public interface PathCriterion {
			/** Called to test, whether the given Path {@code pPath} meets the criterion.
			 * If so, the criterion must return null. Otherwise, the criterion should
			 * return an error message string, making use of the given description
			 * (a parameter name).
			 * @param pPath The path, that is being tested by the criterion.
			 * @param pDescription Name of the parameter, that supplied the path.
			 * @return Error message, if the path failed to meet the criterion, or null.
			 */
			public String test(Path pPath, String pDescription);
		}

		/** Extracts a path from the given data store, ensuring that the path
		 * meets the given critera.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Short description of the expected value, for use in error messages.
		 * @param pCriteria The criteria, that the path must meet.
		 * @return The string, which has been retrieved.
		 * @throws NullPointerException The value, which has been extracted from the data store, is null.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is empty, or not a string.
		 * @throws IllegalStateException The value, which has been extracted from the data store,
		 *   doesn't meet the given criteria.
		 * @see Data#FILE_EXISTS
		 * @see Data#DIR_EXISTS
		 * @see Data#NOT_EXISTS
		 */
		public @Nonnull Path requirePath(@Nonnull O pData, @Nonnull String pKey,
				                         @Nonnull String pDescription, PathCriterion... pCriteria) {
			final Path path = requirePath(pData, pKey, pDescription);
			if (pCriteria != null) {
				for (PathCriterion pc : pCriteria) {
					final String error = pc.test(path, pDescription);
					if (error != null) {
						throw new IllegalStateException(error);
					}
				}
			}
			return path;
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

	/** A {@link PathCriterion}, that tests, whether the path is an existing file.
	 */
	public static final PathCriterion FILE_EXISTS = (p,d) -> {
		if (Files.isRegularFile(p)) {
			return null;
		} else {
			return "Invalid value for parameter " + d + ": Expected existing file, got " + p;
		}
	};

	/** A {@link PathCriterion}, that tests, whether the path is an existing directory.
	 */
	public static final PathCriterion DIR_EXISTS = (p,d) -> {
		if (Files.isDirectory(p)) {
			return null;
		} else {
			return "Invalid value for parameter " + d + ": Expected existing directory, got " + p;
		}
	};

	/** A {@link PathCriterion}, that tests, whether the path is a non-existing item.
	 */
	public static final PathCriterion NOT_EXISTS = (p,d) -> {
		if (!Files.exists(p)) {
			return null;
		} else {
			return "Invalid value for parameter " + d + ": Expected a non-existing item, got " + p;
		}
	};

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

	/** Converts the given object array into a map by using every even element as a key, and the
	 * following odd element as a value.
	 * @param pValues The elements of the map as a list of key/value pairs: Every even element
	 *   (a string) is a key, and the successor (odd element) is a value. 
	 * @return
	 */
	public static Map<String,Object> asMap(Object... pValues) {
		final Map<String,Object> map = new HashMap<>();
		if (pValues != null) {
			for (int i = 0;  i < pValues.length;  i += 2) {
				map.put((String) pValues[i], pValues[i+1]);
			}
		}
		return map;
	}
}
