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

import javax.annotation.Nullable;


/** Utility class for working with Generics.
 */
public class Generics {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	private Generics() {}

	/** Tricks the compiler into believing a cast.
	 * @param <O> The casts result type.
	 * @param pObject The object, which is being casted.
	 * @return The input object, after casting.
	 */
	public static @Nullable <O extends Object> O cast(@Nullable Object pObject) {
		@SuppressWarnings("unchecked")
		final @Nullable O o = (O) pObject;
		return o;
	}
}
