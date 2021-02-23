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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/** A class for reading, and writing property files, including comments.
 */
public class Properties {
	public static class Entry {
		private final @Nonnull String key, value;
		private final @Nullable String comment;
		public Entry(@Nonnull String pKey, @Nonnull String pValue, @Nullable String pComment) {
			key = pKey;
			value = pValue;
			comment = pComment;
		}
		public @Nonnull String getKey() { return key; }
		public @Nonnull String getValue() { return value; }
		public @Nullable String getComment() { return comment; }
	}

	private final Map<String,Entry> map = new LinkedHashMap<>();

	public Entry getEntry(String pKey) {
		return map.get(pKey);
	}
	public String getValue(String pKey) {
		final Entry entry = getEntry(pKey);
		return entry == null ? null : entry.getValue();
	}
	public String getComment(String pKey) {
		final Entry entry = getEntry(pKey);
		return entry == null ? null : entry.getComment();
	}
	public void forEach(@Nonnull Consumer<Entry> pConsumer) {
		map.values().forEach(pConsumer);
	}
	public void forEach(@Nonnull BiConsumer<String,String> pConsumer) {
		forEach((e) -> pConsumer.accept(e.getKey(), e.getValue()));
	}
	public void put(@Nonnull String pKey, @Nonnull String pValue) {
		put(pKey, pValue, null);
	}
	public void put(@Nonnull String pKey, @Nonnull String pValue, @Nullable String pComment) {
		map.put(pKey, new Entry(pKey, pValue, pComment));
	}
}
