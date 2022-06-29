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
package com.github.jochenw.afw.di.api;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Named;


/** This class supports working with instances of {@link Named}.
 */
public class Names {
	private static class NamedImpl implements javax.inject.Named, Serializable {
		private static final long serialVersionUID = -3588508855233450088L;
		private final @Nonnull String value;

		NamedImpl(@Nonnull String pValue) {
			value = pValue;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return javax.inject.Named.class;
		}

		@Override
		public @Nonnull String value() {
			return value;
		}

		
		
		@Override
		public int hashCode() {
			return java.util.Objects.hash(value);
		}

		@Override
		public boolean equals(Object pOther) {
			if (pOther == null) {
				return false;
			}
			if (pOther instanceof Named) {
				final Named other = (Named) pOther;
				return value.equals(other.value());
			} else {
				return false;
			}
		}
	}

	/** Creates an instance of {@link Named} with the given value.
	 * @param pValue The created annotations value.
	 * @return A newly created instance of {@link Named} with the given value.
	 */
	public static javax.inject.Named named(@Nonnull String pValue) {
		return new NamedImpl(pValue);
	}

	/** Creates a camel cased method property name. Example: prefix=get,
	 * suffix=foo yields getFoo.
	 * @param pPrefix The method names prefix, for example "get", or "is".
	 * @param pSuffix The property name.
	 * @return The prefix, appended by the upercased first character of the
	 *   suffix, and the remainder of the suffix.
	 */
	public static String upperCased(String pPrefix, String pSuffix) {
		Objects.requireNonNull(pPrefix, "Prefix");
		Objects.requireNonNull(pSuffix, "Suffix");
		return pPrefix + Character.toUpperCase(pSuffix.charAt(0)) + pSuffix.substring(1);
	}
}
