package com.github.jochenw.afw.core.data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

		/** Creates a new instance with the given access function.
		 * @param pFunction The access function, that reads a value for
		 *   a parameter from an instance of {@code O}.
		 */
		protected Accessor(BiFunction<O,String,Object> pFunction) {
			function = pFunction;
		}

		/** Extracts a value from the given data store.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The value, which has been retrieved.
		 */
		public @Nullable Object getValue(@NonNull O pData, @NonNull String pKey) {
			final O object = Objects.requireNonNull(pData, "Data");
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
		public @Nullable String getString(@NonNull O pData, @NonNull String pKey, @NonNull String pDescription) {
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
		public @Nullable String getString(@NonNull O pData, @NonNull String pKey) {
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
		public @NonNull String requireString(@NonNull O pData, @NonNull String pKey,
				                             @NonNull String pDescription) {
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
		public @NonNull String requireString(@NonNull O pData, @NonNull String pKey) {
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
		public @NonNull Path requirePath(@NonNull O pData, @NonNull String pKey,
				                         @NonNull String pDescription) {
			Object value = getValue(pData, pKey);
			if (value == null) {
				throw new NullPointerException("Missing value for parameter " + pDescription);
			} else {
				if (value instanceof String) {
					final String s = (String) value;
					if (s.length() == 0) {
						throw new IllegalArgumentException("Empty value for parameter " + pDescription);
					}
					@SuppressWarnings("null")
					final @NonNull Path path = Paths.get(s);
					return path;
				} else if (value instanceof Path) {
					return (Path) value;
				} else if (value instanceof File) {
					@SuppressWarnings("null")
					final @NonNull Path path = ((File) value).toPath();
					return path;
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

		/** Extracts a path from the given data store.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Short description of the expected value, for use in error messages.
		 * @return The string, which has been retrieved.
		 * @throws NullPointerException The value, which has been extracted from the data store, is null.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is empty, or not a string.
		 * @throws IllegalStateException The value, which has been extracted from the data store,
		 *   doesn't meet the given criteria.
		 */
		public @Nullable Path getPath(@NonNull O pData, @NonNull String pKey,
				                      @NonNull String pDescription) {
			final Object pathObject = getValue(pData, pKey);
			if (pathObject == null) {
				return null;
			}
			if (pathObject instanceof Path) {
				return (Path) pathObject;
			} else if (pathObject instanceof File) {
				return ((File) pathObject).toPath();
			} else if (pathObject instanceof String) {
				final String str = (String) pathObject;
				if (str.length() == 0) {
					throw new IllegalArgumentException("Empty value for parameter " + pDescription);
				}
				return Paths.get((String) pathObject);
			} else {
				throw new IllegalArgumentException("Invalid value for parameter " + pDescription
                        + ": Expected path, got " + pathObject.getClass().getName());
			}
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
		public @NonNull Path requirePath(@NonNull O pData, @NonNull String pKey,
				                         @NonNull String pDescription, PathCriterion... pCriteria) {
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
		public @NonNull Path requirePath(@NonNull O pData, @NonNull String pKey) {
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
		public @Nullable Boolean getBoolean(@NonNull O pData, @NonNull String pKey, @NonNull String pDescription) {
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

		/** Extracts a non-null boolean value from the given map.
		 * @param pData The data store, from which a value is being extracted.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Short description of the expected value, for use in error messages.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 * @throws NullPointerException The extracted value is null.
		 */
		public @NonNull Boolean requireBoolean(@NonNull O pData, @NonNull String pKey, @NonNull String pDescription) {
			final Object value = getValue(pData, pKey);
			if (value == null) {
				throw new NullPointerException("Missing value for parameter " + pDescription);
			} else {
				if (value instanceof Boolean) {
					return (Boolean) value;
				} else if (value instanceof String) {
					@SuppressWarnings("null")
					final @NonNull Boolean valueOf = Boolean.valueOf((String) value);
					return valueOf;
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
		public @Nullable Boolean getBoolean(@NonNull O pData, @NonNull String pKey) {
			return getBoolean(pData, pKey, "Map key " + pKey);
		}

		/** Extracts an enumeration value from the given data store.
		 * @param pData The data store, from which a value is being extracted.
		 * @param <E> The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pType The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Description of the requested parameter.
		 *   May be null, in which case the {@code pKey} is being used.
		 * @return The extracted value, if available, or null.
		 * @throws IllegalArgumentException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid string value.
		 * @throws InvalidDataTypeException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid data type.
		 * @throws NullPointerException Either of the required parameters
		 *   ({@code pData}, {@code pType}, or {@code pKey}) is null
		 */
		public @Nullable <E extends Enum<E>> E getEnum(@NonNull O pData,
				                                       @NonNull Class<E> pType,
				                                       @NonNull String pKey,
				                                       @Nullable String pDescription) {
			Objects.requireNonNull(pType, "Type");
			final Object value = getValue(pData, pKey);
			try {
				@SuppressWarnings("null")
				final E v = (E) Data.convert(pType, value, Objects.notNull(pDescription, pKey));
				return v;
			} catch (InvalidDataTypeException e) {
				throw e;
			} catch (IllegalArgumentException e) {
				final String enumValues = Objects.enumNamesAsString(pType, "|");
				throw new IllegalArgumentException("Invalid value for parameter " + pDescription
						+ ": Expected " + enumValues + ", got " + value);
			}
		}
	
		/** Extracts an enumeration value from the given data store.
		 * This is equivalent to calling
		 * <pre>
		 *   getEnum(pData, pType, pKey, pKey);
		 * </pre>
		 * @param pData The data store, from which a value is being extracted.
		 * @param <E> The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pType The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The extracted value, if available, or null.
		 * @throws IllegalArgumentException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid string value.
		 * @throws InvalidDataTypeException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid data type.
		 * @throws NullPointerException Either of the required parameters
		 *   ({@code pData}, {@code pType}, or {@code pKey}) is null
		 */
		public @Nullable <E extends Enum<E>> E getEnum(@NonNull O pData,
				                                       @NonNull Class<E> pType,
				                                       @NonNull String pKey) {
			return getEnum(pData, pType, pKey, pKey);
		}

		/** Extracts a non-null enumeration value from the given data store.
		 * @param pData The data store, from which a value is being extracted.
		 * @param <E> The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pType The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Description of the requested parameter.
		 *   May be null, in which case the {@code pKey} is being used.
		 * @return The extracted value, if available. Never null.
		 * @throws IllegalArgumentException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid string value.
		 * @throws InvalidDataTypeException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid data type.
		 * @throws NoSuchElementException The requested value is null,
		 *   or missing.
		 * @throws NullPointerException Either of the required parameters
		 *   ({@code pData}, {@code pType}, or {@code pKey}) is null
		 */
		public @NonNull <E extends Enum<E>> E requireEnum(@NonNull O pData,
				                                          @NonNull Class<E> pType,
				                                          @NonNull String pKey,
				                                          @Nullable String pDescription)
				throws NoSuchElementException, IllegalArgumentException,
		               NullPointerException {
			final @Nullable E e = getEnum(pData, pType, pKey, pDescription);
			if (e == null) {
				throw new NoSuchElementException("Missing value for parameter " + pDescription);
			}
			final @NonNull E result = e;
			return result;
		}
	
		/** Extracts a non-null enumeration value from the given data store.
		 * This is equivalent to
		 * <pre>
		 *   requireEnum(pData, pType, pKey, pKey)
		 * </pre>
		 * @param pData The data store, from which a value is being extracted.
		 * @param <E> The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pType The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The extracted value, if available. Never null.
		 * @throws IllegalArgumentException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid string value.
		 * @throws InvalidDataTypeException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid data type.
		 * @throws NoSuchElementException The requested value is null,
		 *   or missing.
		 * @throws NullPointerException Either of the required parameters
		 *   ({@code pData}, {@code pType}, or {@code pKey}) is null
		 */
		public @NonNull <E extends Enum<E>> E requireEnum(@NonNull O pData,
				                                          @NonNull Class<E> pType,
				                                          @NonNull String pKey)
				throws NoSuchElementException, IllegalArgumentException,
		               NullPointerException {
			return requireEnum(pData, pType, pKey, pKey);
		}
	}

	/** Exception class, which is thrown by the data methods for requesting
	 * an enumeration instance. These include, in particular:
	 * <ul>
	 *   <li>{@link Data.Accessible#getEnum(Class, String)}</li>
	 *   <li>{@link Data.Accessible#getEnum(Class, String, String)}</li>
	 *   <li>{@link Data.Accessible#requireEnum(Class, String)}</li>
	 *   <li>{@link Data.Accessible#requireEnum(Class, String, String)}</li>
	 *   <li>{@link Data.Accessor#getEnum(Object, Class, String)}</li>
	 *   <li>{@link Data.Accessor#getEnum(Object, Class, String, String)}</li>
	 *   <li>{@link Data.Accessor#requireEnum(Object, Class, String)}</li>
	 *   <li>{@link Data.Accessor#requireEnum(Object, Class, String, String)}</li>
	 * </ul>
	 */
	public static class InvalidDataTypeException extends IllegalArgumentException {
		private static final long serialVersionUID = -504168013115790559L;

		/** Creates a new instance without error message, and cause.
		 */
		public InvalidDataTypeException() {
			super();
		}

		/** Creates a new instance with the given error message,
		 * and cause.
		 * @param pMessage The error message.
		 * @param pCause The exceptions cause.
		 */
		public InvalidDataTypeException(String pMessage, Throwable pCause) {
			super(pMessage, pCause);
		}

		/** Creates a new instance with the given error message,
		 * but without cause.
		 * @param pMessage The error message.
		 */
		public InvalidDataTypeException(String pMessage) {
			super(pMessage);
		}

		/** Creates a new instance with the given cause.
		 * @param pCause The exceptions cause.
		 */
		public InvalidDataTypeException(Throwable pCause) {
			super(pCause);
		}
	}
	
	private static <E extends Enum<E>> @Nullable E convert(@NonNull Class<E> pType,
			                                     Object pValue, @NonNull String pDescription) {
		if (pValue == null) {
			return null;
		} else if (pValue instanceof String) {
			final String s = (String) pValue;
			try {
				return Enum.valueOf(pType, s);
			} catch (IllegalArgumentException e) {
				return Objects.valueOf(pType, s);
			}
		} else if (pType.isAssignableFrom(pValue.getClass())) {
			return pType.cast(pValue);
		} else {
			throw new InvalidDataTypeException("Invalid value for parameter "
					+ pDescription + ": Expected " + pType.getName() + ", got "
					+ pValue.getClass().getName() + ", " + pValue);
		}
	}

	/**
	 * Same as {@link Data.Accessor}, except that the data store
	 * is embedded, and not supplied as a parameter. In other
	 * words, the {@link Data.Accessible} is more convenient to
	 * use.
	 */
	public static class Accessible {
		private final Function<String,Object> function;

		/** Creates a new instance with the given access function.
		 * @param pFunction The access function, that reads a value for
		 *   a parameter from an instance of {@code O}.
		 */
		protected Accessible(Function<String,Object> pFunction) {
			function = pFunction;
		}

		/** Extracts a value from the given data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The value, which has been retrieved.
		 */
		public @Nullable Object getValue(@NonNull String pKey) {
			final String key = Objects.requireNonNull(pKey, "Key");
			return function.apply(key);
		}
		/** Extracts a string from the given data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Short description of the expected value, for use in error messages.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 */
		public @Nullable String getString(@NonNull String pKey, @NonNull String pDescription) {
			Object value = getValue(pKey);
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
		 * @param pKey The key, which is being queried in the data store.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 */
		public @Nullable String getString(@NonNull String pKey) {
			return getString(pKey, pKey);
		}
		/** Extracts a non-empty string from the given data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Short description of the expected value, for use in error messages.
		 * @return The string, which has been retrieved.
		 * @throws NullPointerException The value, which has been extracted from the data store, is null.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is empty, or not a string.
		 */
		public @NonNull String requireString(@NonNull String pKey,
				                             @NonNull String pDescription) {
			Object value = getValue(pKey);
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
		/** Extracts a non-empty string from the data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The string, which has been retrieved.
		 * @throws NullPointerException The value, which has been extracted from the data store, is null.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is empty, or not a string.
		 */
		public @NonNull String requireString(@NonNull String pKey) {
			return requireString(pKey, pKey);
		}

		/** Extracts a path from the given data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Short description of the expected value, for use in error messages.
		 * @return The string, which has been retrieved.
		 * @throws NullPointerException The value, which has been extracted from the data store, is null.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is empty, or not a string.
		 */
		@SuppressWarnings("null")
		public @NonNull Path requirePath(@NonNull String pKey,
				                         @NonNull String pDescription) {
			Object value = getValue(pKey);
			if (value == null) {
				throw new NullPointerException("Missing value for parameter " + pDescription);
			} else {
				if (value instanceof String) {
					final String s = (String) value;
					if (s.length() == 0) {
						throw new IllegalArgumentException("Empty value for parameter " + pDescription);
					}
					final @NonNull Path path = Paths.get(s);
					return path;
				} else if (value instanceof Path) {
					return (Path) value;
				} else if (value instanceof File) {
					final @NonNull Path path = ((File) value).toPath();
					return path;
				} else {
					throw new IllegalArgumentException("Invalid value for parameter " + pDescription
							                        + ": Expected path, got " + value.getClass().getName());
				}
			}
		}

		/** Extracts a path from the data store, ensuring that the path
		 * meets the given criteria.
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
		public @NonNull Path requirePath(@NonNull String pKey,
				                         @NonNull String pDescription, PathCriterion... pCriteria) {
			final Path path = requirePath(pKey, pDescription);
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

		/** Extracts a path value from the data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The path, which has been retrieved.
		 * @throws NullPointerException The value, which has been extracted from the data store, is null.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is empty, or not a string.
		 */
		public @NonNull Path requirePath(@NonNull String pKey) {
			return requirePath(pKey, pKey);
		}

		/** Extracts a boolean value from the data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Short description of the expected value, for use in error messages.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 */
		public @Nullable Boolean getBoolean(@NonNull String pKey, @NonNull String pDescription) {
			final Object value = getValue(pKey);
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

		/** Extracts a boolean value from the data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDefaultValue The default value, which is being used,
		 *   if the extracted value is null, or otherwise invalid.
		 * @return The boolean value, which has been retrieved.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public boolean requireBoolean(@NonNull String pKey, @NonNull String pDefaultValue) {
			final Object value = getValue(pKey);
			if (value == null) {
				return Boolean.valueOf(pDefaultValue).booleanValue();
			} else {
				if (value instanceof Boolean) {
					return ((Boolean) value).booleanValue();
				} else if (value instanceof String) {
					return Boolean.valueOf((String) value).booleanValue();
				} else {
					return Boolean.valueOf(pDefaultValue).booleanValue();
				}
			}
		}

		/** Extracts a boolean value from the data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 */
		public @Nullable Boolean getBoolean(@NonNull String pKey) {
			return getBoolean(pKey, pKey);
		}

		/** Extracts a non-null boolean value from the data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The string, which has been retrieved.
		 * @throws IllegalArgumentException The value, which has been extracted from the data store,
		 *   is not a string.
		 * @throws NullPointerException The extracted string value is null.
		 */
		public @NonNull Boolean requireBoolean(@NonNull String pKey) {
			return requireBoolean(pKey, pKey);
		}

		/** Creates a new {@link Accessible}, which accesses the given map.
		 * This is equivalent to {@code new Data.Accessable((k) -> pMap.get(k)}.
		 * @param pMap The map, that is being accessed.
		 * @return The created {@link Accessible}
		 * @throws NullPointerException The parameter {@code pMap} is null.
		 */
		public static Accessible of(Map<String,Object> pMap) {
			final Map<String,Object> map = Objects.requireNonNull(pMap, "Map");
			return new Accessible(map::get);
		}

		/** Creates a new {@link Accessible}, which accesses the given property set.
		 * This is equivalent to {@code new Data.Accessable((k) -> pProperties.get(k)}.
		 * @param pProperties The property set, that is being accessed.
		 * @return The created {@link Accessible}
		 * @throws NullPointerException The parameter {@code pProperties} is null.
		 */
		public static Accessible of(Properties pProperties) {
			final Properties props = Objects.requireNonNull(pProperties, "Properties");
			return new Accessible(props::get);
		}

		/** Creates a new {@link Accessible}, which uses the given accessor function.
		 * This is equivalent to {@code new Data.Accessable(pAccessor)}.
		 * @param pAccessor The accessor function, that is being used.
		 * @return The created {@link Accessible}
		 * @throws NullPointerException The parameter {@code pAccessor} is null.
		 */
		public static Accessible of(Function<String,Object> pAccessor) {
			final Function<String,Object> acc = Objects.requireNonNull(pAccessor, "Accessor");
			return new Accessible(acc);
		}

		/** Extracts a non-null value from the data store.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The value, which has been retrieved.
		 * @param <O> Type of the retrieved value.
		 */
		public <O> @NonNull O requireValue(@NonNull String pKey) {
			@SuppressWarnings("unchecked")
			final @Nullable O o = (O) getValue(pKey);
			if (o == null) {
				throw new NullPointerException(pKey);
			}
			final @NonNull O o2 = o;
			return o2;
		}
	
		/** Extracts an enumeration value from the data store.
		 * @param <E> The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pType The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Description of the requested parameter.
		 *   May be null, in which case the {@code pKey} is being used.
		 * @return The extracted value, if available, or null.
		 * @throws IllegalArgumentException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid string value.
		 * @throws InvalidDataTypeException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid data type.
		 * @throws NullPointerException Either of the required parameters
		 *   ({@code pType}, or {@code pKey}) is null
		 */
		public @Nullable <E extends Enum<E>> E getEnum(@NonNull Class<E> pType,
				                                       @NonNull String pKey,
				                                       @Nullable String pDescription) {
			Objects.requireNonNull(pType, "Type");
			final Object value = getValue(pKey);
			try {
				@SuppressWarnings("null")
				final E v = (E) Data.convert(pType, value, Objects.notNull(pDescription, pKey));
				return v;
			} catch (InvalidDataTypeException e) {
				throw e;
			} catch (IllegalArgumentException e) {
				final String enumValues = Objects.enumNamesAsString(pType, "|");
				throw new IllegalArgumentException("Invalid value for parameter " + pDescription
						+ ": Expected " + enumValues + ", got " + value);
			}
		}
	
		/** Extracts an enumeration value from the data store.
		 * This is equivalent to calling
		 * <pre>
		 *   getEnum(pType, pKey, pKey);
		 * </pre>
		 * @param <E> The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pType The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The extracted value, if available, or null.
		 * @throws IllegalArgumentException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid string value.
		 * @throws InvalidDataTypeException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid data type.
		 * @throws NullPointerException Either of the required parameters
		 *   ({@code pType}, or {@code pKey}) is null
		 */
		public @Nullable <E extends Enum<E>> E getEnum(@NonNull Class<E> pType,
				                                       @NonNull String pKey) {
			return getEnum(pType, pKey, pKey);
		}

		/** Extracts a non-null enumeration value from the data store.
		 * @param <E> The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pType The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pKey The key, which is being queried in the data store.
		 * @param pDescription Description of the requested parameter.
		 *   May be null, in which case the {@code pKey} is being used.
		 * @return The extracted value, if available, or null.
		 * @throws IllegalArgumentException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid string value.
		 * @throws InvalidDataTypeException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid data type.
		 * @throws NullPointerException Either of the required parameters
		 * ({@code pType}, or {@code pKey}) is null.
		 * @throws NoSuchElementException The requested value is
		 *    missing, or null.
		 * @throws NullPointerException Either of the required parameters
		 *    ({@code pType}, or {@code pKey}) is null
		 */
		public <E extends Enum<E>> @NonNull E requireEnum(@NonNull Class<E> pType,
				                                          @NonNull String pKey,
				                                          @Nullable String pDescription)
		        throws IllegalArgumentException, NullPointerException,
		               NoSuchElementException {
			final @Nullable E e = getEnum(pType, pKey, pDescription);
			if (e == null) {
				throw new NoSuchElementException("Missing value for parameter " + pDescription);
			} else {
				return e;
			}
		}

		/** Extracts a non-null enumeration value from the data store.
		 * This is equivalent to
		 * <pre>
		 *   requireEnum(pType, pKey, pKey)
		 * </pre>
		 * @param <E> The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pType The enumeration type, to which the extracted value must
		 *   be converted.
		 * @param pKey The key, which is being queried in the data store.
		 * @return The extracted value, if available. Never null.
		 * @throws IllegalArgumentException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid string value.
		 * @throws InvalidDataTypeException The extracted value cannot be converted
		 *   into the enumeration type, because it has an invalid data type.
		 * @throws NoSuchElementException The requested value is null,
		 *   or missing.
		 * @throws NullPointerException Either of the required parameters
		 *   ({@code pType}, or {@code pKey}) is null
		 */
		public <E extends Enum<E>> @NonNull E requireEnum(@NonNull Class<E> pType,
				                                          @NonNull String pKey)
				throws NoSuchElementException, IllegalArgumentException,
		               NullPointerException {
			return requireEnum(pType, pKey, pKey);
		}
	}


	/** {@link Data.Accessor} object for instances of {@link Map Map&lt;String,Object&gt;}.
	 */
	public static final Accessor<Map<String,Object>> MAP_ACCESSOR = new Accessor<Map<String,Object>>((map,key) -> map.get(key));
	/** {@link Data.Accessor} object for instances of {@link Properties}.
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
	public static @Nullable String getString(@NonNull Map<String,Object> pMap, @NonNull String pKey, @NonNull String pDescription) {
		return MAP_ACCESSOR.getString(pMap, pKey, pDescription);
	}

	/** Extracts a string from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@NonNull Map<String,Object> pMap, @NonNull String pKey) {
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
	public static @Nullable String getString(@NonNull Properties pProperties, @NonNull String pKey, @NonNull String pDescription) {
		return PROPS_ACCESSOR.getString(pProperties, pKey, pDescription);
	}

	/** Extracts a string from the given map.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable String getString(@NonNull Properties pProperties, @NonNull String pKey) {
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
	public static @NonNull String requireString(@NonNull Map<String,Object> pMap, @NonNull String pKey,
			                                    @NonNull String pDescription) {
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
	public static @NonNull String requireString(@NonNull Map<String,Object> pMap, @NonNull String pKey) {
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
	public static @NonNull Object requireString(@NonNull Properties pProperties, @NonNull String pKey,
			                             @NonNull String pDescription) {
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
	public static @NonNull String requireString(@NonNull Properties pProperties, @NonNull String pKey) {
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
	public static @NonNull Path requirePath(@NonNull Map<String,Object> pMap, @NonNull String pKey,
			                         @NonNull String pDescription) {
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
	public static @NonNull Path requirePath(@NonNull Map<String,Object> pMap, @NonNull String pKey) {
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
	public static @NonNull Path requirePath(@NonNull Properties pProperties, @NonNull String pKey,
			                                @NonNull String pDescription) {
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
	public static @NonNull Path requirePath(@NonNull Properties pProperties, @NonNull String pKey) {
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
	public static @Nullable Boolean getBoolean(@NonNull Map<String,Object> pMap, @NonNull String pKey, @NonNull String pDescription) {
		return MAP_ACCESSOR.getBoolean(pMap, pKey, pDescription);
	}

	/** Extracts a non-null boolean value from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @param pDescription Short description of the expected value, for use in error messages.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 * @throws NullPointerException The extracted value is null.
	 */
	public static @NonNull Boolean requireBoolean(@NonNull Map<String,Object> pMap, @NonNull String pKey, @NonNull String pDescription) {
		return MAP_ACCESSOR.requireBoolean(pMap, pKey, pDescription);
	}

	/** Extracts a boolean value from the given map.
	 * @param pMap The map, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable Boolean getBoolean(@NonNull Map<String,Object> pMap, @NonNull String pKey) {
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
	public static @Nullable Boolean getBoolean(@NonNull Properties pProperties, @NonNull String pKey, @NonNull String pDescription) {
		return PROPS_ACCESSOR.getBoolean(pProperties, pKey, pDescription);
	}

	/** Extracts a boolean value from the given property set.
	 * @param pProperties The property set, from which a value is being extracted.
	 * @param pKey The key, which is being queried in the data store.
	 * @return The string, which has been retrieved.
	 * @throws IllegalArgumentException The value, which has been extracted from the data store,
	 *   is not a string.
	 */
	public static @Nullable Boolean getBoolean(@NonNull Properties pProperties, @NonNull String pKey) {
		return PROPS_ACCESSOR.getBoolean(pProperties, pKey, pKey);
	}

	/** Converts the given object array into a map by using every even element as a key, and the
	 * following odd element as a value.
	 * @param pValues The elements of the map as a list of key/value pairs: Every even element
	 *   (a string) is a key, and the successor (odd element) is a value. 
	 * @return The created map.
	 */
	public static @NonNull Map<String,Object> asMap(Object... pValues) {
		final Map<String,Object> map = new HashMap<>();
		if (pValues != null) {
			for (int i = 0;  i < pValues.length;  i += 2) {
				map.put((String) pValues[i], pValues[i+1]);
			}
		}
		return map;
	}
}
