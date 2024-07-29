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

/** This class specifies the possible instances of {@link Scope}.
 */
public class Scopes {
	/** Creates a new instance. Private, to avoid accidental instantiation.
	 * This constructor might be removed, it is mainly present to avoid a Javadoc
	 * warning with JDK 21.
	 */
	private Scopes() {}

	/** The binding creates a singleton with lazy instantiation.
	 */
	public static final Scope SINGLETON = new Scope();
	/** The binding creates an eager singleton (Instantiated
	 * immediately, rather than lazy.
	 */
	public static final Scope EAGER_SINGLETON = new Scope();
	/** The binding creates a new instance with any request.
	 */
	public static final Scope NO_SCOPE = new Scope();
}
