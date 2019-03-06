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
package com.github.jochenw.afw.core.inject;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Objects;

import javax.annotation.Nonnull;

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
		public boolean equals(Object pOther) {
			if (pOther == null) {
				return false;
			}
			if (pOther instanceof NamedImpl) {
				final NamedImpl other = (NamedImpl) pOther;
				if (value == null) {
					return other.value == null;
				} else {
					return value.equals(other.value);
				}
			} else {
				return false;
			}
		}
	}

	public static javax.inject.Named named(@Nonnull String pValue) {
		return new NamedImpl(pValue);
	}

	public static String upperCased(String pPrefix, String pSuffix) {
		Objects.requireNonNull(pPrefix, "Prefix");
		Objects.requireNonNull(pSuffix, "Suffix");
		return pPrefix + Character.toUpperCase(pSuffix.charAt(0)) + pSuffix.substring(1);
	}
}
