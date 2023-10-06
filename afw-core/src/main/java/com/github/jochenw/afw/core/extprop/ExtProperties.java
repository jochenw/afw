/*
 * Copyright 2023 Jochen Wiedmann
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
package com.github.jochenw.afw.core.extprop;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Objects;


/** An extension of the basic {@link Properties} class, with the following enhancements:
 * <ol>
 *   <li>Extended properties can be ordered. This may come in handy, when creating property files.</li>
 *   <li>Extended properties are preserving comments. In other words, they may have documentation.</li>
 * </ol>
 * 
 */
public class ExtProperties {
	/** A single entry in a set of {@link ExtProperties}. Extended properties are immutable, and serializable.
	 */
	public static class ExtProperty {
		final @Nonnull String key, value;
		final @Nullable String[] comments;
		/** Creates a new entry with the given key, value, and comment.
		 * @param pKey The property key,
		 * @param pValue The property value.
		 * @param pComments The property comments.
		 */
		public ExtProperty(@Nonnull String pKey, @Nonnull String pValue, @Nullable String[] pComments) {
			key = Objects.requireNonNull(pKey, "Key");
			value = Objects.requireNonNull(pValue, "Value");
			comments = pComments;
		}
		/** Returns the properties key.
		 * @return The properties key. Never null.
		 */
		public @Nonnull String getKey() { return key; }
		/** Returns the properties value.
		 * @return The properties value. Never null.
		 */
		public @Nonnull String getValue() { return value; }
		/** Returns the properties comment, if any, or null.
		 * @return The properties comment, if any, or null.
		 */
		public @Nullable String[] getComments() { return comments; }
	}

	private final Map<String, ExtProperty> entries;

	/** Creates a new instance with an explicit order of keys, as specified by the given
	 * {@link Comparator}.
	 * @param pEntries A mutable map, that is destined to hold the entries of the property set.
	 */
	ExtProperties(Map<String, ExtProperty> pEntries) {
		entries = pEntries;
	}

	/** Returns an immutable map of all properties, that constitute the property set.
	 * @return An immutable map of all the entries in the property set.
	 */
	public Map<String, ExtProperty> getPropertyMap() { return Collections.unmodifiableMap(entries); } 

	/** Returns the property with the given key.
	 * @param pKey The requested properties key.
	 * @return The property with the given key, if any, or null.
	 * @see #requireProperty(String)
	 * @throws NullPointerException The parameter {@code pKey} is null.
	 */
	public @Nullable ExtProperty getProperty(@Nonnull String pKey) throws NullPointerException {
		final String key = Objects.requireNonNull(pKey, "Key");
		return entries.get(key);
	}

	/** Returns the property with the given key.
	 * @param pKey The requested properties key.
	 * @return The property with the given key. Never null. A {@link NoSuchElementException}
	 *   will be thrown, if no such key exists.
	 * @see #getProperty(String)
	 * @throws NoSuchElementException No property with the given key exists.
	 * @throws NullPointerException The parameter {@code pKey} is null.
	 */
	public @Nonnull ExtProperty requireProperty(@Nonnull String pKey) throws NoSuchElementException, NullPointerException {
		final String key = Objects.requireNonNull(pKey, "Key");
		final ExtProperty ep = entries.get(key);
		if (ep == null) {
			throw new NoSuchElementException("No property with this key exists: " + pKey);
		}
		return ep;
	}

	/** Returns the value of the property with the given key.
     * @param pKey The requested properties key.
	 * @return A non-null result indicates, that the requested property exists, and has the given value.
	 *   A null result indicates, that this property does not exist.
	 * @throws NullPointerException The parameter {@code pKey} is null.
	 */
	public @Nullable String getValue(@Nonnull String pKey) throws NullPointerException {
		final String key = Objects.requireNonNull(pKey, "Key");
		final ExtProperty ep = entries.get(key);
		if (ep == null) {
			return null;
		} else {
			return ep.getValue();
		}
	}

	/** Returns the comment of the property with the given key.
	 * @param pKey The requested properties key.
	 * @return The requested properties value, if such a property exists, or null.
	 *   <em>Note:</em> A null value does <em>not</em> indicate, that the requested
	 *   property doesn't exist, because a properties comment can be null.
	 *   If you need to distinguish between these cases, use {@link #getProperty(String)}
	 *   instead.
	 * @throws NullPointerException The parameter {@code pKey} is null.
	 */
	public @Nullable String[] getComment(@Nonnull String pKey) throws NullPointerException {
		final String key = Objects.requireNonNull(pKey, "Key");
		final ExtProperty ep = entries.get(key);
		if (ep == null) {
			return null;
		} else {
			return ep.getComments();
		}
	}

	/** Sets the given properties value, and comment.
	 * @param pKey The properties key, must not be null.
	 * @param pValue The property value, must not be null.
	 * @param pComments The property comment, may be null.
	 * @throws NullPointerException Either of the parameters {@code pKey}, or
	 *   {@code pValue} is null.
	 */
	public void setProperty(String pKey, String pValue, String[] pComments) throws NullPointerException {
		final String key = Objects.requireNonNull(pKey, "Key");
		final String value = Objects.requireNonNull(pValue, "Value");
		entries.compute(pKey, (k, ep) -> {
			return new ExtProperty(key, value, pComments);
		});
	}

	/** Sets the given properties value.
	 * If the property already exists, then the comment will be left, as it is. Otherwise,
	 * a new property will be created, without a comment.
	 * @param pKey The properties key, must not be null.
	 * @param pValue The property value, must not be null.
	 * @throws NullPointerException Either of the parameters {@code pKey}, or
	 *   {@code pValue} is null.
	 */
	public void setValue(String pKey, String pValue) throws NullPointerException {
		final String key = Objects.requireNonNull(pKey, "Key");
		final String value = Objects.requireNonNull(pValue, "Value");
		entries.compute(key, (k, ep) -> {
			if (ep == null) {
				return new ExtProperty(k, value, null);
			} else {
				if (value.equals(ep.getValue())) {
					// No need to change the value, return the existing object.
					return ep;
				} else {
					// Value is being changed, return a new object with the same comment,
					return new ExtProperty(k, value, ep.getComments());
				}
			}
		});
	}

	/** Sets the given properties value.
	 * If the property already exists, then the value will be left, as it is. Otherwise,
	 * a new property will be created, with an empty string ("") as the value.
	 * @param pKey The properties key, must not be null.
	 * @param pComments The property comment, may be null.
	 * @throws NullPointerException The parameter {@code pKey} is null.
	 */
	public void setComments(String pKey, String[] pComments) throws NullPointerException {
		final String key = Objects.requireNonNull(pKey, "Key");
		entries.compute(key, (k, ep) -> {
			if (ep == null) {
				return new ExtProperty(k, "", pComments);
			} else {
				final String[] comments = ep.getComments();
				final boolean commentsIsNull = isNull(comments);
				if ((commentsIsNull &&  isNull(pComments))
					||  (!commentsIsNull  &&  isEqual(comments, pComments))) {
					// No need to change the comment, return the existing object.
					return ep;
				} else {
					// Comment is being changed, return a new object with the same value,
					return new ExtProperty(k, ep.getValue(), pComments);
				}
			}
		});
	}

	private boolean isNull(String[] pArray) {
		return pArray == null  ||  pArray.length == 0;
	}
	private boolean isEqual(@Nonnull String[] pArr1, @Nullable String[] pArr2) {
		if (pArr2 == null) {
			return false;
		}
		final @Nonnull String[] arr2 = pArr2;
		if (pArr1.length != arr2.length) {
			return false;
		}
		for (int i = 0;  i < pArr1.length;  i++) {
			final String s1 = pArr1[i];
			final String s2 = arr2[i];
			if (s1 == null) {
				if (s2 != null) { return false; }
			} else {
				if (!s1.equals(s2)) { return false; }
			}
		}
		return true;
	}
	/** Iterates over the properties in the property sets native order.
	 * @param pConsumer A consumer, which is being invoked for every
	 * property, in the property sets native order.
	 * @throws NullPointerException The parameter {@code pConsumer} is null.
	 */
	public void forEach(Consumer<ExtProperty> pConsumer) {
		final Consumer<ExtProperty> consumer = Objects.requireNonNull(pConsumer, "Consumer");
		entries.forEach((k,ep) -> consumer.accept(ep));
	}

	/** Iterates over the properties in the order, that is given by the
	 * given comparator.
	 * @param pConsumer A consumer, which is being invoked for every
	 * property, in the order, that is given by the comparator
	 * {@code pComparator}.
	 * @param pComparator A comparator, that is being applied to the
	 *   property keys for sorting.
	 * @throws NullPointerException Either of the parameters is null.
	 */
	public void forEach(Consumer<ExtProperty> pConsumer, Comparator<String> pComparator) {
		final Consumer<ExtProperty> consumer = Objects.requireNonNull(pConsumer, "Consumer");
		final Comparator<String> comparator = Objects.requireNonNull(pComparator, "Comparator");
		final List<ExtProperty> list = new ArrayList<ExtProperty>(entries.values());
		list.sort((ep1,ep2) -> { return comparator.compare(ep1.getKey(), ep2.getKey()); });
		list.forEach(consumer);
	}

	/** Returns the size of the property set. (The number of properties in the set.)
	 * @return The size of the property set, (The number of properties.)
	 */
	public int size() {
		return entries.size();
	}

	/** Returns, whether the property set is empty. (The number of properties is 0.)
	 * @return True, if the property set is empty. Otherwise false,
	 */
	public boolean isEmpty() {
		return entries.isEmpty();
	}

	/** Returns the internal Map of extended properties. For testing only,
	 * thus package private.
	 * @return The internal Map of extended properties.
	 */
	Map<String,ExtProperty> getEntries() { return entries; }

	/** Creates a new instance of {@link IExtPropertiesWriter},
	 * with the {@link StandardCharsets#UTF_8 UTF8 character set},
	 * and the {@link System#lineSeparator() default line separator}.
	 * In other words, this is equivalent to
	 * <pre>
	 *   writer(StandardCharsets.UTF_8, System.lineSeparator())
	 * </pre>
	 * @return A new instance of {@link IExtPropertiesWriter},
	 * with the {@link StandardCharsets#UTF_8 UTF8 character set},
	 * and the {@link System#lineSeparator() default line separator}.
	 */
	public static IExtPropertiesWriter writer() {
		return writer(StandardCharsets.UTF_8,
				      System.lineSeparator());
	}

	/** Creates a new instance of {@link IExtPropertiesWriter},
	 * with the given {@link Charset character set},
	 * and the given line separator.
	 * @param pCharset The character set, which is being used
	 *   to convert characters into bytes. Typically, this
	 *   would be {@link StandardCharsets#UTF_8}.
	 * @param pLineSeparator The string, which is being used
	 *   as the line separator, typically
	 *   {@link System#lineSeparator()}, "\n" (LF), or
	 *   "\r\n" (CRLF).
	 * @return A new instance of {@link IExtPropertiesWriter},
	 * with the {@link StandardCharsets#UTF_8 UTF8 character set},
	 * and the {@link System#lineSeparator() default line separator}.
	 */
	public static IExtPropertiesWriter writer(Charset pCharset,
			                                  String pLineSeparator) {
		return new DefaultExtPropertiesWriter(pCharset, pLineSeparator);
	}

	/** Creates a new, empty property set. The property set will have
	 * explicit ordering, if the given comparator is null. Otherwise,
	 * the property set will preserve order of insertion.
	 * The created property set isn't thread-safe.
	 * @param pComparator If this parameter is null, then the created
	 *   property set will have no explicit order. Instead, order of
	 *   insertion is being maintained. Otherwise, the keys of the
	 *   property set will be ordered by the given comparator.
	 * @return The created property set.
	 */
	public static ExtProperties create(Comparator<String> pComparator) {
		final Map<String, ExtProperty> entries;
		if (pComparator == null) {
			entries = new LinkedHashMap<String, ExtProperty>();
		} else {
			entries = new TreeMap<String, ExtProperty>(pComparator);
		}
		return new ExtProperties(entries);
	}
	/** Creates a new, empty property set without explicit ordering.
	 * The created property set isn't thread-safe.
	 * This method is equivalent to
	 * <pre>
	 *   create(null);
	 * </pre>
	 * @see #create(Comparator)
	 * @return The created property set.
	 */
	public static ExtProperties create() { return create(null); }
	/** Creates a new, synchronized (thread-safe) property set with
	 * the same entries than the given, and the same order.
	 * @param pProperties The property set, that is being cloned.
	 * @return The created property set.
	 * @throws IllegalStateException The given property set could not
	 *   be cloned. This can only happen, if the property set wasn't
	 *   created by either {@link #create()}, or
	 *   {@link #create(Comparator)}.
	 */
	public static ExtProperties createSynchronized(ExtProperties pProperties) {
		final Map<String,ExtProperty> entries = pProperties.entries;
		final Map<String,ExtProperty> entries2;
		if (entries instanceof LinkedHashMap) {
			entries2 = new LinkedHashMap<String, ExtProperty>(entries);
		} else if (entries instanceof TreeMap) {
			final TreeMap<String,ExtProperty> tm = (TreeMap<String,ExtProperty>) entries;
			entries2 = new TreeMap<String,ExtProperty>(tm.comparator());
			entries2.putAll(entries);
		} else {
			throw new IllegalStateException("Unable to clone an instance of " + entries.getClass().getName());
		}
		return new ExtProperties(Collections.synchronizedMap(entries2));
	}

	/** Creates a set of properties by splitting the given list of strings into key/value/comment triplets.
	 * Example:
	 * <pre>
	 *   ExtProperties.of("foo", "bar", "The foo property with value bar",
	 *                    "answer", "42", null // The answer property with value "42", and no comment.
	 *                    "Whatever", "works", "The Whatever property with value works");
	 * </pre>
	 * The example would create a property set with the following properties:
	 * <ol>
	 *   <li>A property "foo" with the value "bar", and the comment "The foo property with the value "bar".</li>
	 *   <li>A property "answer" with the value "42", and no comment.</li>
	 *   <li>A property "Whatever" with the value "works", and the comment "The Whatever property with
	 *     value works".</li>
	 * </ol>
	 * @param pValues The string list, which provides the key/value/comment triplets. For
	 *   obvious reasons, the number of elements in the list must be a multiple of 3.
	 * @param pComparator If this parameter is null, then the created
	 *   property set will have no explicit order. Instead, order of
	 *   insertion is being maintained. Otherwise, the keys of the
	 *   property set will be ordered by the given comparator.
	 * @return The created property set.
	 * @throws NullPointerException The string list {@code pValues} is null, or either of the
	 *   keys, or values, in the list is null.
	 * @throws IllegalArgumentException The number of elements in the string list {@code pValues}
	 *   is invalid (Not a multiple of 3).
	 */
	public static ExtProperties of(Comparator<String> pComparator, String... pValues) {
		final ExtProperties ep = ExtProperties.create(pComparator);
		ep.setProperties(pValues);
		return ep;
	}

	/** Creates a set of properties by splitting the given list of strings into key/value/comment triplets.
	 * Example:
	 * <pre>
	 *   ExtProperties.of("foo", "bar", "The foo property with value bar",
	 *                    "answer", "42", null // The answer property with value "42", and no comment.
	 *                    "Whatever", "works", "The Whatever property with value works");
	 * </pre>
	 * The example would create a property set with the following properties:
	 * <ol>
	 *   <li>A property "foo" with the value "bar", and the comment "The foo property with the value "bar".</li>
	 *   <li>A property "answer" with the value "42", and no comment.</li>
	 *   <li>A property "Whatever" with the value "works", and the comment "The Whatever property with
	 *     value works".</li>
	 * </ol>
	 * @param pValues The string list, which provides the key/value/comment triplets. For
	 *   obvious reasons, the number of elements in the list must be a multiple of 3.
	 * @return The created property set.
	 * @throws NullPointerException The string list {@code pValues} is null, or either of the
	 *   keys, or values, in the list is null.
	 * @throws IllegalArgumentException The number of elements in the string list {@code pValues}
	 *   is invalid (Not a multiple of 3).
	 */
	public static ExtProperties of(String... pValues) {
		final ExtProperties ep = ExtProperties.create();
		ep.setProperties(pValues);
		return ep;
	}

	/** Splits the given string list into key / value / comment triplets, and applies
	 * them to the given property set by invoking {@link #setProperty(String, String, String[])}
	 * for each triplet.
	 * <em>Note:</em> This method is based on the assumption, that a comment is a single
	 * string, rather than multiple strings. While this may meet a lot of real-world
	 * scenarios, it may not always be correct, in which case the less convenient
	 * {@link #setProperty(String, String, String[])} must be used.
	 * @param pValues The string list, which provides the key/value/comment triplets. For
	 *   obvious reasons, the number of elements in the list must be a multiple of 3.
	 * @throws NullPointerException The string list {@code pValues} is null, or either of the
	 *   keys, or values, in the list is null.
	 * @throws IllegalArgumentException The number of elements in the string list {@code pValues}
	 *   is invalid (Not a multiple of 3).
	 */
	public void setProperties(String... pValues) {
		final String[] values = Objects.requireNonNull(pValues, "The string list (pValues) is null.");
		if (values.length % 3 != 0) {
			throw new IllegalArgumentException("The number of elements in the string list (pValues)"
					+ " is " + pValues.length + ", and not a multiple of 3.");
		}
		for (int i = 0;  i < pValues.length;  i += 3) {
			final String key = pValues[i];
			if (key == null) {
				throw new NullPointerException("The key at index " + i + " in the string list (pValues) is null");
			}
			final String value = pValues[i+1];
			if (value == null) {
				throw new NullPointerException("The value at index " + (i+1) + " in the string list (pValues) is null");
			}
			final String comment = pValues[i+2];
			setProperty(key, value, new String[] {comment});
		}
	}
}
