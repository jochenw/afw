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

import java.util.Properties;


/** A property filter is an object, that updates a given
 * set of properties, returning an updated set.
 * Examples of a property filter could be:
 * <ul>
 *   <li>An interpolator, that resolves property references.</li>
 *   <li>An enhancer, that adds more properties.</li>
 *   <li>An purger, that removes properties.</li>
 * </ul>
 */
public interface PropertyFilter {
	/**
	 * Called to filter the given set of properties.
	 * It is implementation specific, whether a new
	 * object is returned, or the same object
	 * (after in-place modifications).
	 * @param pProperties The properties, that are
	 *   being filtered.
	 * @return The filtered set of properties.
	 */
	public Properties filter(Properties pProperties);
}
