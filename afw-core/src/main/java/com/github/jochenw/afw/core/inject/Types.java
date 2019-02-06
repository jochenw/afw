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

import java.lang.reflect.ParameterizedType;

import javax.annotation.Nonnull;

public class Types {
	public static class Type<T extends Object> {
		private @Nonnull final java.lang.reflect.Type rawType;

		public Type() {
			final java.lang.reflect.Type t = getClass().getGenericSuperclass();
			if (t instanceof Class) {
				rawType = t;
			} else if (t instanceof ParameterizedType) {
				final ParameterizedType ptype = (ParameterizedType) t;
				final java.lang.reflect.Type[] typeArgs = ptype.getActualTypeArguments();
				if (typeArgs != null  &&  typeArgs.length > 0) {
					rawType = typeArgs[0];
				} else {
					throw new IllegalStateException("Unsupported type: " + t);
				}
			} else {
				throw new IllegalStateException("Unsupported type: " + t);
			}
		}

		public @Nonnull java.lang.reflect.Type getRawType() {
			return rawType;
		}
	}
}
