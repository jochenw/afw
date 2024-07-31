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

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;


/**
 * This is a helper class, that should aim in the creation of bindings for
 * fields, and methods, that are using Generics. Using this works as follows:
 * Suggest, that you want to create a binding for the following field:
 * <pre>
 *     private @Inject List&lt;String&gt; stringList;
 * </pre>
 * The problem is, that you can't simply create a binding for a list, like
 * <pre>
 *     b.bind(List.class).toInstance(new ArrayList&lt;String&gt;());
 * </pre>
 * because this binding would only apply to raw lists, and not to lists with
 * the generic type &lt;String&gt;.
 * However, you can do this:
 * <pre>
 *     final Type&lt;List&lt;String&gt;&gt; stringListType = new Types.Type&lt;ListString&gt;&gt;(){};
 *     b.bind(stringListType).toInstance(new ArrayList&lt;String&gt;());
 * </pre>
 * Not exactly obvious, not really comfortable, but it works!
 */
public class Types {
	/** This constructor s only present to omit certain
	 * Javadoc warnings.
	 */
	public Types() {
	}

	/**
	 * A generic class, that supports access to its parameter type at runtime.
	 * @param <T> The parameter type.
	 */
	public static class Type<T extends Object> {
		private final java.lang.reflect.@NonNull Type rawType;

		/**
		 * Creates a new instance.
		 */
		public Type() {
			this(null);
		}

		/**
		 * Creates a new instance with the given raw type.
		 * @param pRawType The raw type.
		 */
		public Type(java.lang.reflect.Type pRawType) {
			final java.lang.reflect.Type rt = pRawType == null ? getClass().getGenericSuperclass() : pRawType;
			if (rt instanceof ParameterizedType) {
				final ParameterizedType ptype = (ParameterizedType) rt;
				final java.lang.reflect.Type[] typeArgs = ptype.getActualTypeArguments();
				@SuppressWarnings("null")
				final java.lang.reflect.@NonNull Type rType = typeArgs[0];
				rawType = rType;
			} else {
				throw new IllegalStateException("Unsupported type: " + rt);
			}
		}

		/** Returns the parameter type.
		 * @return The parameter type.
		 */
		public java.lang.reflect.@NonNull Type getRawType() {
			return rawType;
		}
	}

	public static final Type TYPE_LIST_OBJECT = new Types.Type<List<Object>>() {};
	public static final Type TYPE_LIST_STRING = new Types.Type<List<String>>() {};
	public static final Type TYPE_MAP_STRING_OBJECT = new Types.Type<Map<String,Object>>() {};
	public static final Type TYPE_MAP_STRING_STRING = new Types.Type<Map<String,String>>() {};
}
