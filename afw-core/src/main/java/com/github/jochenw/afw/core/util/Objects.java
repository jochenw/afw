/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Functions.FailableSupplier;


/** Utility class for working with objects. (Working with objects is
 * something, that you will always do. So, this class will always be
 * useful.)
 */
public class Objects {
	/**
	 * Checks, if the given value is null. If so, returns the default value.
	 * Otherwise, returns the non-null input value.
	 * @param <T> Type of the input (and output) value.
	 * @param pValue The input value.
	 * @param pDefault The default value to return, if the input value is null.
	 * @return The input value, if non-null. Otherwise the default value.
	 */
	public static @Nonnull <T> T notNull(@Nullable T pValue, @Nonnull T pDefault) {
		if (pValue == null) {
			return pDefault;
		} else {
			return pValue;
		}
	}

	/**
	 * Checks, if the given value is null. If so, invokes the supplier, and returns
	 * the suppliers value. Otherwise, returns the non-null input value.
	 * @param <T> Type of the input (and output) value.
	 * @param pValue The input value.
	 * @param pSupplier A supplier, which returns the default value. The supplier is
	 *   only invoked, if necessary. (If {@code pValue} is null.
	 * @return The input value, if non-null. Otherwise the default value.
	 * @throws NullPointerException The default value supplier
	 */
	public static @Nonnull <T> T notNull(@Nullable T pValue, @Nonnull FailableSupplier<T,?> pSupplier) {
		if (pValue == null) {
			final T value;
			try {
				value = pSupplier.get();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			return requireNonNull(value, "The supplier returned null.");
		} else {
			return pValue;
		}
	}

	/**
	 * Checks, if the given value is null. If so, throws a {@link NullPointerException}.
	 * Otherwise, returns the non-null input value.
	 * @param <T> Type of the input (and output) value.
	 * @param pValue The input value, if non-null.
	 * @param pMessage The exceptions message, if the input value is null.
	 * @return The input value, if non-null.
	 */
	public static @Nonnull <T> T requireNonNull(@Nullable T pValue, String pMessage) {
        if (pValue == null) {
        	if (pMessage == null) {
        		throw new NullPointerException();
        	} else {
        		throw new NullPointerException(pMessage);
        	}
	    }
        return pValue;
	}

	/**
	 * Checks, if the given value is null. If so, throws a {@link NullPointerException}.
	 * Otherwise, returns the non-null input value.
	 * @param <T> Type of the input (and output) value.
	 * @param pValue The input value, if non-null.
	 * @return The input value, if non-null.
	 */
	public static @Nonnull <T> T requireNonNull(T pValue) {
        if (pValue == null)
            throw new NullPointerException();
        return pValue;
	}

	/**
	 * Checks, whether the given array, and all its elements, are non-null.
	 * @param <T> The element type.
	 * @param pValues The object to check. Also the return value in case of success.
	 * @param pDescription Used to construct an error message, if necessary.
	 * @return The unmodified input array.
	 */
	public static @Nonnull <T> T[] requireAllNonNull(T[] pValues, String pDescription) {
		if (pValues == null) {
			throw new NullPointerException(pDescription + "s");
		} else {
			final @Nonnull T[] array = pValues;
			for (int i = 0;  i < array.length;  i++) {
				if (array[i] == null) {
					throw new NullPointerException(pDescription + ", element " + i);
				}
			}
			return array;
		}
	}

	/**
	 * Checks, whether the given {@link Iterable iterable}, and all its elements, are non-null.
	 * @param <T> The element type.
	 * @param pValues The object to check. Also the return value in case of success.
	 * @param pDescription Used to construct an error message, if necessary.
	 * @return The unmodified input {@link Iterable iterable}.
	 */
	public static @Nonnull <T> Iterable<T> requireAllNonNull(Iterable<T> pValues, String pDescription) {
		if (pValues == null) {
			throw new NullPointerException(pDescription + "s");
		} else {
			final @Nonnull Iterable<T> iterable = pValues;
			int i = 0;
			for (T t : iterable) {
				if (t == null) {
					throw new NullPointerException(pDescription + ", element " + i);
				}
				++i;
			}
			return iterable;
		}
	}

	/** Converts the given string into an object of the given type.
	 * @param pValue The string value to convert.
	 * @param pType The required objects type.
	 * @return The converted object.
	 */
	public static Object convert(String pValue, Class<?> pType) {
		if (pValue == null) {
			return null;
		} else if (String.class == pType) {
			return pValue;
		} else if (Boolean.TYPE == pType  ||  Boolean.class == pType) {
			final boolean b = Boolean.parseBoolean(pValue);
			return Boolean.valueOf(b);
		} else if (Long.TYPE == pType  ||  Long.class == pType) {
			final long l;
			try {
				l = Long.parseLong(pValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid long value: " + pValue, e);
			}
			return Long.valueOf(l);
		} else if (Integer.TYPE == pType  ||  Integer.class == pType) {
			final int i;
			try {
				i = Integer.parseInt(pValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid integer value: " + pValue, e);
			}
			return Integer.valueOf(i);
		} else if (Short.TYPE == pType  ||  Short.class == pType) {
			final short s;
			try {
				s = Short.parseShort(pValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid short value: " + pValue, e);
			}
			return Short.valueOf(s);
		} else if (Byte.TYPE == pType  ||  Byte.class == pType) {
			final byte b;
			try {
				b = Byte.parseByte(pValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid byte value: " + pValue, e);
			}
			return Byte.valueOf(b);
		} else if (Double.TYPE == pType  ||  Double.class == pType) {
			final double d;
			try {
				d = Double.parseDouble(pValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid double value: " + pValue, e);
			}
			return Double.valueOf(d);
		} else if (Float.TYPE == pType  ||  Float.class == pType) {
			final float f;
			try {
				f = Float.parseFloat(pValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid float value: " + pValue, e);
			}
			return Float.valueOf(f);
		} else if (BigDecimal.class == pType) {
			final BigDecimal bd;
			try {
				bd = new BigDecimal(pValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid BigDecimal value: " + pValue, e);
			}
			return bd;
		} else if (BigInteger.class == pType) {
			final BigInteger bi;
			try {
				bi = new BigInteger(pValue);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid BigInteger value: " + pValue, e);
			}
			return bi;
		} else if (URL.class == pType) {
			final URL url;
			try {
				url = new URL(pValue);
			} catch (MalformedURLException e) {
				throw new IllegalArgumentException("Invalid URL value: " + pValue, e);
			}
			return url;
		} else if (Path.class == pType) {
			final Path p;
			try {
				p = Paths.get(pValue);
			} catch (Throwable t) {
				throw new IllegalArgumentException("Invalid path value: " + pValue, t);
			}
			return p;
		} else if (File.class == pType) {
			final File f;
			try {
				f = new File(pValue);
			} catch (Throwable t) {
				throw new IllegalArgumentException("Invalid file value: " + pValue, t);
			}
			return f;
		} else {
			throw new IllegalStateException("Invalid type: " + pType.getName());
		}
	}

	/** Creates an array, that contains the given elements.
	 * @param pValues The created arrays elements.
	 * @param <O> The arrays element type.
	 * @return The created array
	 */
	@SafeVarargs
	public static <O> @Nonnull O[] arrayOf(@Nonnull O... pValues) {
		return pValues;
	}
}
