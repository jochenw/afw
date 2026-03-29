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
package com.github.jochenw.afw.core.props;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * Interface of a property filter, which updates property values by interpolation.
 */
public interface Interpolator extends PropertyFilter {
	/** Interface of a set of properties, that can be interpolated.
	 */
	public interface StringSet {
		/**
		 * Returns an iterator over the full property set.
		 * @return An iterator over the full property set.
		 */
		public Iterator<Map.Entry<@NonNull String, @Nullable String>> getValues();
		/**
		 * Returns the value of the given property.
		 * @param pKey Key of the property, that is being queried.
		 * @return Value of the property, that is being queried.
		 */
		public @Nullable String getValue(@NonNull String pKey);
		/**
		 * Sets the value of the given property.
		 * @param pKey Key of the property, that is being updated.
		 * @param pValue Value of the property, that is being updated.
		 */
		public void setValue(@NonNull String pKey, @Nullable String pValue);
	}
	/**
	 * Returns, whether the given property value can be interpolated.
	 * (Whether it returns a property reference, like "abc${foo}def".)
	 * @param pValue The property value, that is being checked.
	 * @return True, if the property value can be interpolated.
	 *   False otherwise.
	 */
	public boolean isInterpolatable(String pValue);
	/** Called to interpolate the given property value, assuming that
	 * {@link #isInterpolatable(String)} has returned true on the same
	 * value.
	 * @param pValue The property value, that should be interpolated.
	 * @return The resulting property value, after interpolation.
	 */
	public @Nullable String interpolate(@NonNull String pValue);
	/** Called to interpolate the given property set in-place: The
	 * original values are lost, if any modifications are done.
	 * @param pValues The property set, that is being updated.
	 */
	public void interpolate(StringSet pValues);
	/** Called to interpolate the given property map in-place: The
	 * original values are lost, if any modifications are done.
	 * @param pProperties The property set, that is being updated.
	 */
	public default void interpolate(final Map<Object,Object> pProperties) {
		final StringSet stringSet = new StringSet() {
			@Override
			public Iterator<Entry<@NonNull String, @Nullable String>> getValues() {
				final Iterator<Entry<Object,Object>> iter = pProperties.entrySet().iterator();
				return new Iterator<Entry<@NonNull String, @Nullable String>>(){
					@Override
					public boolean hasNext() {
						return iter.hasNext();
					}

					@Override
					public Entry<@NonNull String, @Nullable String> next() {
						final Entry<Object,Object> entry = iter.next();
						return new Entry<@NonNull String, @Nullable String>(){
							@Override
							public @NonNull String getKey() {
								@SuppressWarnings("null")
								final @NonNull String key = (@NonNull String) entry.getKey();
								return key;
							}

							@Override
							public @Nullable String getValue() {
								return (@Nullable String) entry.getValue();
							}

							@Override
							public String setValue(@Nullable String pValue) {
								final String value = getValue();
								entry.setValue(pValue);
								return value;
							}
						};
					}
				};
			}

			@Override
			public @Nullable String getValue(@NonNull String pKey) {
				return (@Nullable String) pProperties.get(pKey);
			}

			@Override
			public void setValue(@NonNull String pKey, @Nullable String pValue) {
				pProperties.put(pKey, pValue);
			}
		};
		interpolate(stringSet);
	}

	@Override
	public default Properties filter(final Properties pProperties) {
		final Properties interpolatedProperties = new Properties();
		interpolatedProperties.putAll(pProperties);
		final Map<Object,Object> map = interpolatedProperties;
		interpolate(map);
		return interpolatedProperties;
	}
}
