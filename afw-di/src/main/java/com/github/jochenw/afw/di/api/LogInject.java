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
public @interface LogInject {
	/** Returns the loggers id. Defaults to the fully qualified name of the
	 * class, which declares the loggers field.
	 * @return The loggers id. Defaults to the fully qualified name of the
	 * class, which declares the loggers field.
	 */
	String id() default "";
	/** If a method logger is being injected: Declares the method name.
	 * Otherwise, this is being ignored.
	 * @return The method name, if a method logger is being injected.
	 *   Otherwise, this is being ignored.
	 */
	String mName() default "";
}
