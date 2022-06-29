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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** This annotation can be used to request the injection of loggers.
 * The request is satisfied by using an {@link IOnTheFlyBinder}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface PropInject {
	/**
	 * A string constant, that indicates, that there is no default value
	 * for a property.
	 */
	public static String NO_DEFAULT = "";

	/** Returns the property key. Defaults to "fully qualified class
	 * name of the declaring class" + "." + "field name".
	 * @return The property key.
	 */
	String id() default "";
	/** Returns the properties default value, or {@link #NO_DEFAULT}, if
	 * there is no such value.
	 * @return The properties default value.
	 */
	String defaultValue() default NO_DEFAULT;
}
