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

import java.util.Objects;


/** This class supports working with instances of {@code Named}.
 */
public class Names {
	/** Creates a new instance. Package private, to avoid accidental instantiaton.
	 * This default constructor might be removed, it is mainly present to
	 * avoid a Javadoc warning with JDK 21.
	 */
	Names() {}

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
