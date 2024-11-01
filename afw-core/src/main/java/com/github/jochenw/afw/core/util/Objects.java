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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.function.Functions.FailableRunnable;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;


/** Utility class for working with objects. (Working with objects is
 * something, that you will always do. So, this class will always be
 * useful.)
 */
public class Objects {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	public Objects() {}

	/**
	 * Checks, if the given value is null. If so, returns the default value.
	 * Otherwise, returns the non-null input value.
	 * @param <T> Type of the input (and output) value.
	 * @param pValue The input value.
	 * @param pDefault The default value to return, if the input value is null.
	 * @return The input value, if non-null. Otherwise the default value.
	 */
	public static @NonNull <T> T notNull(@Nullable T pValue, @NonNull T pDefault) {
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
	 * @throws NullPointerException The parameter {@code pSupplier} is null,
	 *   or the supplier has returned a null value.
	 */
	public static @NonNull <T> T notNull(@Nullable T pValue, @NonNull FailableSupplier<T,?> pSupplier) {
		if (pValue == null) {
			final @NonNull FailableSupplier<T, ?> supplier = requireNonNull(pSupplier, "The parameter pSupplier must not be null.");
			final T value;
			try {
				value = supplier.get();
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
	public static @NonNull <T> T requireNonNull(@Nullable T pValue, String pMessage) {
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
	public static @NonNull <T> T requireNonNull(T pValue) {
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
	public static <T> T @NonNull [] requireAllNonNull(T[] pValues, String pDescription) {
		if (pValues == null) {
			throw new NullPointerException(pDescription + "s");
		} else {
			final T @NonNull [] array = pValues;
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
	public static @NonNull <T> Iterable<T> requireAllNonNull(Iterable<T> pValues, String pDescription) {
		if (pValues == null) {
			throw new NullPointerException(pDescription + "s");
		} else {
			final @NonNull Iterable<T> iterable = pValues;
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
	public static @NonNull <O> O[] arrayOf(@NonNull O... pValues) {
		return pValues;
	}

	/** Creates an array of the given type with the given number of
	 * null elements.
	 * @param pType The array element type.
	 * @param <O> The arrays element type.
	 * @param pNumberOfElements The created arrays length.
	 * @return The created array
	 */
	public static @NonNull <O> O[] arrayOf(@NonNull Class<O> pType, int pNumberOfElements) {
		@SuppressWarnings("null")
		final @NonNull Object[] objectArray = (Object[]) Array.newInstance(pType, pNumberOfElements);
		@SuppressWarnings({ "unchecked", "null" })
		final @NonNull O[] array = (O[]) objectArray;
		return array;
	}

	/** This class is being used by the {@link CachedObjectManager} for
	 * serialization, and deserialization. The purpose is the ability
	 * to use different mechanism for serialization, and deserialization,
	 * if necessary.
	 */
	public static class CachedObjectSerializer {
		/** Creates a new instance.
		 */
		public CachedObjectSerializer() {}
	
		/** Serializes the given object to the given output stream.
		 * (Called to write the cached object to the cache file.).
		 * @param pObject The object, that is being cached.
		 * @param pOut The output stream, to which the
		 *   cached object is being written.
		 * @throws Exception Reading the cache file has failed.
		 */
		public void write(@NonNull Object pObject, @NonNull OutputStream pOut) throws Exception {
			final Object object = Objects.requireNonNull(pObject, "Object");
			final OutputStream os = Objects.requireNonNull(pOut, "OutputStream"); 
			try {
				final ObjectOutputStream oos = new ObjectOutputStream(os);
				oos.writeObject(object);
				oos.flush();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		/** Deserializes the given object from the given input stream.
		 * (Called to read the cached object from the cache file.).
		 * @param pIn The input stream, from which the
		 *   cached object is being read.
		 * @return The cached object, that has been read from the input
		 *   stream.
		 * @throws Exception Reading the cache file has failed.
		 */
		public @NonNull Object read(@NonNull InputStream pIn) throws Exception {
			final InputStream in = Objects.requireNonNull(pIn, "InputStream");
			try {
				final ObjectInputStream ois = new ObjectInputStream(in);
				@SuppressWarnings("null")
				final @NonNull Object result = ois.readObject();
				return result;
			} catch (ClassNotFoundException e) {
				throw new UndeclaredThrowableException(e);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	/** A builder-like object, which is being
	 * used by the {@link Objects#getCacheableObject(Path, Functions.FailableSupplier)} method.
	 * @param <O> Type of the cached object.
	 */
	public static class CachedObjectManager<O> {
		private final Path cacheFile;
		private final FailableSupplier<O,?> supplier;
		private CachedObjectSerializer serializer;

		/** Creates a new instance with the given cache file, and
		 * cached object supplier.
		 * @param pCacheFile The cache file. If a cached object has been created, it
		 *   will be stored here.
		 * @param pSupplier The cached object supplier. It will be invoked,
		 *   if the cache file doesn't exist.
		 */
	    protected CachedObjectManager(@NonNull Path pCacheFile, @NonNull FailableSupplier<O,?> pSupplier) {
			cacheFile = Objects.requireNonNull(pCacheFile, "Cache file path");
			supplier = Objects.requireNonNull(pSupplier, "Object supplier");
		}

	    /** Returns the cache file.
		 * @return The cache file.
	     */
		public Path getCacheFile() {
			return cacheFile;
		}
		/** Returns the serializer/deserializer.
		 * @return The serializer/deserializer.
		 */
		public CachedObjectSerializer getSerializer() {
			if (serializer == null) {
				serializer = new CachedObjectSerializer();
			}
			return serializer;
		}
		/** Sets the serializer/deserializer, overriding the
		 * default implementation.
		 * @param pSerializer The serializer/deserializer to use,
		 *   or null (restore the default behavior).
		 * @return This cached object manager.
		 */
		public CachedObjectManager<O> serializer(CachedObjectSerializer pSerializer) {
			serializer = pSerializer;
			return this;
		}
		/** Creates a new instance with the given cache file, and
		 * cached object supplier.
		 * @param pCacheFile The cache file. If a cached object has been created, it
		 *   will be stored here.
		 * @param pSupplier The cached object supplier. It will be invoked,
		 *   if the cache file doesn't exist.
		 * @param <O> Type of the cached object.
		 * @return The created instance.
		 */
		public static <O> CachedObjectManager<O> of(@NonNull Path pCacheFile, @NonNull FailableSupplier<O,?> pSupplier) {
			return new CachedObjectManager<>(pCacheFile, pSupplier);
		}
		/** Returns the cached object, if available.
		 * Otherwise, invokes the cached object supplier, caches the
		 * created object, and returns it.
		 * @return The cached, or created object.
		 */
		public O get() {
			final Path cf = getCacheFile();
			if (Files.isRegularFile(cf)) {
				try (@SuppressWarnings("null")
				     @NonNull InputStream in = Files.newInputStream(cf)) {
					@SuppressWarnings("unchecked")
					final O o = (O) getSerializer().read(in);
					return o;
				} catch (Exception e) {
					throw Exceptions.show(e);
				}
			} else {
				try {
					final @NonNull O o = Objects.requireNonNull(supplier.get(),
							  "The supplier returned a null object.");
					FileUtils.createDirectoryFor(cf);
					try (@SuppressWarnings("null")
					     @NonNull OutputStream out = Files.newOutputStream(cf)) {
						getSerializer().write(o, out);
					}
					return o;
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
		}
	}

	/** Returns a cached object, if available. Otherwise, invokes the cached object supplier,
	 * stores the created object in the cache file, and returns it. Subsequent invocations of
	 * the same method with the same parameters will no longer invoke the cached object
	 * supplier, but read the object from the cache file.
	 * @param pCacheFile The cache file. If that file exists, it is assumed to contain a valid
	 *   cached object.
	 * @param pSupplier The cached object supplier, which will be invoked to create the
	 *   cached object afresh, if the cache file isn't found.
	 * @param <O> Type of the cached object.
	 * @return The cached, or created object.
	 */
	public static <O> O getCacheableObject(@NonNull Path pCacheFile, @NonNull FailableSupplier<O,? > pSupplier) {
		return CachedObjectManager.of(pCacheFile, pSupplier).get();
	}

	/** Returns a cached object, if available. Otherwise, invokes the cached object supplier,
	 * stores the created object in the cache file, and returns it. Subsequent invocations of
	 * the same method with the same parameters will no longer invoke the cached object
	 * supplier, but read the object from the cache file.
	 * @param pCacheFile The cache file. If that file exists, it is assumed to contain a valid
	 *   cached object.
	 * @param pSerializer The serializer, which is being used,
	 *   when writing, or reading the cache file.
	 * @param pSupplier The cached object supplier, which will be invoked to create the
	 *   cached object afresh, if the cache file isn't found.
	 * @param <O> Type of the cached object.
	 * @return The cached, or created object.
	 */
	public static <O> O getCacheableObject(@NonNull Path pCacheFile,
			@NonNull CachedObjectSerializer pSerializer,
			@NonNull FailableSupplier<O,? > pSupplier) {
		return CachedObjectManager.of(pCacheFile, pSupplier).serializer(pSerializer).get();
	}

	/** For testing only: Returns a value, which is assumed to be non-null,
	 * but is actually null. (In practice, this is used to test the correct behavior
	 * for methods, that are supposed to throw an {@link NullPointerException}
	 * in case of invalid null values.
	 * @param <O> The expected object type.
	 * @return The faked non-null value.
	 */
	@SuppressWarnings("null")
	public static <O> @NonNull O fakeNonNull() {
		final O nullO = (O) null;
		final @NonNull O o = (@NonNull O) nullO;
		return o;
	}

	/** Returns, whether the given object reference is null.
	 * @param pObject The object reference, which is being tested.
	 * @return True, if the object reference is null, otherwise false.
	 */
	public static boolean isNull(Object pObject) {
		return pObject == null;
	}

	/** Returns an array of all the values in the given Enumeration class.
	 * Basically equivalent to invoking the static method {@code values()}
	 * in the enumeration class.
	 * @param <E> The Enumeration class, which is being queried for instances.
	 * @param pType The Enumeration class, which is being queried for instances.
	 * @return An array of all instances in the Enumeration class.
	 */
	public static <E extends Enum<E>> @NonNull E[] enumValues(@NonNull Class<E> pType) {
		final Class<E> type = Objects.requireNonNull(pType, "Type");
		final Lookup lookup = MethodHandles.lookup();
		final Class<?> enumArrayType = Array.newInstance(type, 0).getClass();
		MethodType valuesMethodType = MethodType.methodType(enumArrayType, new Class<?>[0]);
		try {
			final MethodHandle valuesMethodHandle = lookup.findStatic(type, "values", valuesMethodType);
			final @NonNull E[] values = (@NonNull E[]) valuesMethodHandle.invoke();
			return values;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Returns a string with all the names in the given Enumeration class.
	 * Basically equivalent to invoking the static method {@code values()}
	 * in the enumeration class, and then joining the names 
	 * @param <E> The Enumeration class, which is being queried for instances.
	 * @param pType The Enumeration class, which is being queried for instances.
	 * @param pSeparator An optional separator, that is used two separate
	 *   consecutive names.
	 * @return An array of all instances in the Enumeration class.
	 */
	public static <E extends Enum<E>> @NonNull String enumNamesAsString(@NonNull Class<E> pType, String pSeparator) {
		final E[] values = enumValues(pType);
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < values.length;  i++) {
			if (i > 0  &&  pSeparator != null) {
				sb.append(pSeparator);
			}
			sb.append(values[i].name());
		}
		@SuppressWarnings("null")
		final @NonNull String result = sb.toString();
		return result;
	}

	/** Searches for an instance of the given enumeration type with the given name.
	 * Same as {@link Enum#valueOf(Class, String)}, except that this one is case
	 * insensitive.
	 * @param <E> The enumeration type.
	 * @param pType The enumeration type
	 * @param pName Name of the instance, that is being returned.
	 * @return The requested instance. Never null, an {@link IllegalArgumentException}
	 * is thrown in case of an invalid name.
	 * @throws NullPointerException Either of the arguments is null.
	 * @throws IllegalArgumentException The {@code pName} is invalid,
	 *   and no matching instance was found.
	 */
	public static <E extends Enum<E>> E valueOf(@NonNull Class<E> pType, @NonNull String pName) {
		final @NonNull Class<E> type = requireNonNull(pType, "Enum Type");
		final @NonNull String name = requireNonNull(pName, "Enum Name");
		try {
			return Enum.valueOf(type, name);
		} catch (IllegalArgumentException e) {
			for (E instance : enumValues(pType)) {
				if (pName.equalsIgnoreCase(instance.name())) {
					return instance;
				}
			}
			throw new IllegalArgumentException("Invalid name for an instance of "
					+ type.getName() + ": Expected "
					+ enumNamesAsString(type, "|") + ", got " + name);
		}
	}

	/** Performs a cast to the expected object type.
	 * @param pObject The object, which is being casted.
	 * @param <O> The expected object type.
	 * @return The input object, which is now declared to have the expected
	 * object type.
	 */
	public static <O> O cast(Object pObject) {
		@SuppressWarnings("unchecked")
		final O o = (O) pObject;
		return o;
	}

	/** Invokes the given consumer, if the argument {@code pValue} is null.
	 *  @param pValue A (possibly null) value. If it is non-null, then the
	 *    given consumer will be invoked, passing the non-null value.
	 *  @param pFunction The function, which will be invoked to process the
	 *    non-null value, and produce the output value.
	 *  @param <O> Type of the value, which is tested for null. Also
	 *   input type of the function.
	 *  @param <Out> Output type of the function. Also output type
	 *   of this method.
	 * @param <E> Exception, which may be thrown by the function.
	 *  @return The output object, that has been produced by the
	 *    function (if the value is non-null), or null.
	 * @throws E An exception, which has been thrown by the consumer, and
	 *   will be passed to the caller.
	 */
	public static <O,Out,E extends Throwable> @Nullable Out ifNotNull(@Nullable O pValue, FailableFunction<@NonNull O,Out,E> pFunction) throws E {
		if (pValue != null) {
			final @NonNull O value = Objects.requireNonNull(pValue);
			return pFunction.apply(value);
		} else {
			return null;
		}
	}
	/** Invokes the given consumer, if the argument {@code pValue} is null.
	 *  @param pValue A (possibly null) value. If it is non-null, then the
	 *    given consumer will be invoked, passing the non-null value.
	 *  @param pConsumer The consumer, which will be invoked to process the
	 *    non-null value.
	 * @param <O> Type of the value, which is tested for null. Also
	 *   input type of the consumer.
	 * @param <E> Exception, which may be thrown by the consumer.
	 * @throws E An exception, which has been thrown by the consumer, and
	 *   will be passed to the caller.
	 */
	public static <O,E extends Throwable> void ifNotNull(@Nullable O pValue, FailableConsumer<@NonNull O,E> pConsumer) throws E {
		if (pValue != null) {
			final @NonNull O value = Objects.requireNonNull(pValue);
			pConsumer.accept(value);
		}
	}

	/** Invokes the given consumer, if the argument {@code pValue} is null.
	 * Otherwise, invokes the given {@code pRunnable}.
	 * @param pValue A (possibly null) value. If it is non-null, then the
	 *   given consumer will be invoked, passing the non-null value.
	 * @param pConsumer The consumer, which will be invoked to process the
	 *   non-null value.
	 * @param pRunnable The runnable, which will be invoked, if the value
	 * is non-null.
	 * @param <O> Type of the value, which is tested for null. Also
	 *   input type of the consumer.
	 * @param <E1> Exception, which may be thrown by the consumer.
	 * @param <E2> Exception, which may be thrown by the runnable.
	 * @throws E1 An exception, which has been thrown by the consumer, and
	 *   will be passed to the caller.
	 * @throws E2 An exception, which has been thrown by the runnable, and
	 *   will be passed to the caller.
	 */
	public static <O,E1 extends Throwable,E2 extends Throwable> void ifNotNullElse(@Nullable O pValue,
			FailableConsumer<@NonNull O,E1> pConsumer, FailableRunnable<E2> pRunnable) throws E1, E2 {
		if (pValue != null) {
			final @NonNull O value = Objects.requireNonNull(pValue);
			pConsumer.accept(value);
		} else {
			pRunnable.run();
		}
	}
}
