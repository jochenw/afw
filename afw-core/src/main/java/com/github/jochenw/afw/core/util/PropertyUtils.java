/*
 * Copyright 2021 Jochen Wiedmann
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

import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;


/** A class for reading, and writing property files, including comments.
 */
public class PropertyUtils {
	/** A class, which representy an entry in the property set.
	 */
	public static class Entry {
		private final @NonNull String key, value;
		private final @Nullable String comment;
		/** Creates a new instance with the given key, value, and comment.
		 * @param pKey The property key.
		 * @param pValue The property value.
		 * @param pComment The property comment.
		 */
		public Entry(@NonNull String pKey, @NonNull String pValue, @Nullable String pComment) {
			key = pKey;
			value = pValue;
			comment = pComment;
		}
		/** Returns the property key.
		 * @return The property key.
		 */
		public @NonNull String getKey() { return key; }
		/** Returns the property value.
		 * @return The property value.
		 */
		public @NonNull String getValue() { return value; }
		/** Returns the property comment.
		 * @return The property comment.
		 */
		public @Nullable String getComment() { return comment; }
	}

	private final Map<String,Entry> map = new LinkedHashMap<>();

	/** Returns the property entry with the given key.
	 * @param pKey The requested property key.
	 * @return A property entry with the given key, if any, or null.
	 */
	public Entry getEntry(String pKey) {
		return map.get(pKey);
	}
	/** Returns the property value with the given key.
	 * @param pKey The requested property key.
	 * @return A property value with the given key, if any, or null.
	 */
	public String getValue(String pKey) {
		final Entry entry = getEntry(pKey);
		return entry == null ? null : entry.getValue();
	}
	/** Returns the comment with the given key.
	 * @param pKey The requested property key.
	 * @return A property comment with the given key, if any, or null.
	 */
	public String getComment(String pKey) {
		final Entry entry = getEntry(pKey);
		return entry == null ? null : entry.getComment();
	}
	/**
	 * Iterates over all the entries in the property set,
	 * invoking the given consumer.
	 * @param pConsumer  The consumer, which ought to process the property entries.
	 */
	public void forEach(@NonNull FailableConsumer<Entry,?> pConsumer) {
		map.values().forEach((e) -> Functions.accept(pConsumer, e));
	}
	/**
	 * Iterates over all the entries in the property set,
	 * invoking the given consumer.
	 * @param pConsumer The consumer, which ought to process the properties.
	 */
	public void forEach(@NonNull FailableBiConsumer<String,String,?> pConsumer) {
		forEach((e) -> Functions.accept(pConsumer, e.getKey(), e.getValue()));
	}
	/**
	 * Adds a new entry to the property set. If an entry with the given
	 * property key already exists, then it is being replaced by the new entry.
	 * @param pKey The property key.
	 * @param pValue The property value.
	 */
	public void put(@NonNull String pKey, @NonNull String pValue) {
		put(pKey, pValue, null);
	}
	/**
	 * Adds a new entry to the property set. If an entry with the given
	 * property key already exists, then it is being replaced by the new entry.
	 * @param pKey The property key.
	 * @param pValue The property value.
	 * @param pComment The property comment.
	 */
	public void put(@NonNull String pKey, @NonNull String pValue, @Nullable String pComment) {
		map.put(pKey, new Entry(pKey, pValue, pComment));
	}
}
