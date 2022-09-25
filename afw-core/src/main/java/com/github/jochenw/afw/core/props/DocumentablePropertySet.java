package com.github.jochenw.afw.core.props;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Objects;


/** A replacement for {@link Properties}, that supports comments on properties, as a kind of
 * documentation.
 */
public class DocumentablePropertySet {
	/** A {@link DocumentableProperty} is basically a standard property, that can have a comment as
	 * documentation.
	 */
	public static class DocumentableProperty {
		private final @Nullable String comment;
		private final @Nonnull String key, value;

		
		/** Creates a new instance.
		 * @param pComment The comment, that acts as the properties documentation, or null.
		 * @param pKey The property key. Never null.
		 * @param pValue The property value. Never null.
		 */
		public DocumentableProperty(@Nullable String pComment, @Nonnull String pKey, @Nonnull String pValue) {
			comment = pComment;
			key = Objects.requireNonNull(pKey, "Key");
			value = Objects.requireNonNull(pValue, "Value");
		}

		/** Returns the comment, that acts as the properties documentation.
		 * @return The comment, that acts as the properties documentation, or null.
		 */
		public @Nullable String getComment() { return comment; }
		/** Returns the property key.
		 * @return The property key. Never null.
		 */
		public @Nonnull String getKey() { return key; }
		/** Returns the property value.
		 * @return The property value. Never null.
		 */
		public @Nonnull String getValue() { return value; }
	}

	private final Map<String,DocumentableProperty> properties = new LinkedHashMap<>();

	/** Returns the property with the given key.
	 * @param pKey The property key, that is being queried.
	 * @return the property with the given key, possibly null.
	 */
	public @Nullable DocumentableProperty getProperty(String pKey) {
		return properties.get(pKey);
	}

	/** Returns the comment of the property with the given key.
	 * @param pKey The property key, that is being queried.
	 * @return The comment of the with the given key, possibly null.
	 */
	public @Nullable String getComment(String pKey) {
		final DocumentableProperty dp = properties.get(pKey);
		if (dp == null) {
			return null;
		} else {
			return dp.getComment();
		}
	}

	/** Returns the value of the property with the given key.
	 * @param pKey The property key, that is being queried.
	 * @return The value of the with the given key, possibly null.
	 */
	public @Nullable String getValue(String pKey) {
		final DocumentableProperty dp = properties.get(pKey);
		if (dp == null) {
			return null;
		} else {
			return dp.getValue();
		}
	}

	/** Adds a new property with the given key, value, and comment.
	 * @param pKey The property key.
	 * @param pValue The property value.
	 * @param pComment The property comment, possibly null.
	 * @throws NullPointerException The property key, or value, are null.
	 * @return The property, that has been registered with the given key before, or null.
	 */
	public @Nullable DocumentableProperty put(@Nonnull String pKey, @Nonnull String pValue, @Nullable String pComment) {
		final @Nonnull String key = Objects.requireNonNull(pKey, "Key");
		final @Nonnull String value = Objects.requireNonNull(pValue, "Value");
		final DocumentableProperty dp = new DocumentableProperty(pComment, key, value);
		return properties.put(key, dp);
	}

	/**
	 * Returns, whether the property set is empty.
	 * @return True, if the property set is empty, otherwise false.
	 */
	public boolean isEmpty() {
		return properties.isEmpty();
	}

	/**
	 * Returns the size of the property set, which is defined as the number of unique keys.
	 * @return The number of properties in the set.
	 */
	public int size() {
		return properties.size();
	}

	/** Iterates over the properties in the set.
	 * @param pConsumer The consumer, which will be invoked with the properties in the set.
	 */
	public void forEach(FailableConsumer<DocumentableProperty,?> pConsumer) {
		properties.values().forEach((dp) -> {
			try {
				pConsumer.accept(dp);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		});
	}
	
	/** Creates a new instance by parsing the given {@link IReadable}.
	 * @param pReadable The document, that contains the stored property set.
	 * @param pCharset The character set of the property file. Defaults to
	 *    {@link StandardCharsets#UTF_8}.
	 * @return The property set, that has has been parsed from the document.
	 * @throws IOException Parsing the property set has failed.
	 * @throws NullPointerException The parameter {@code pReadable} is null.
	 */
	public static DocumentablePropertySet of(IReadable pReadable, Charset pCharset) {
		try {
			return new PropertySetParser().read(pReadable, pCharset);
		} catch (Throwable e) {
			throw Exceptions.show(e);
		}
	}

}
