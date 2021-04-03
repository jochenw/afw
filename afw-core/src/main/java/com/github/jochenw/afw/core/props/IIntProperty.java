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


/** Interface of an integer {@link IProperty}.
 */
public interface IIntProperty extends IProperty<Integer> {
	/**
	 * Returns the value as a primitive integer.
	 * @return The value as a primitive integer.
	 */
    int getIntValue();
	/**
	 * Returns the default value as a primitive integer.
	 * @return The default value as a primitive integer.
	 */
    int getIntDefaultValue();
}
