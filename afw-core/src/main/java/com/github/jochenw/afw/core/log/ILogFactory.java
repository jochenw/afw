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
package com.github.jochenw.afw.core.log;

import com.github.jochenw.afw.core.ILifecycleController;
import com.github.jochenw.afw.core.ResourceLocator;


/** Interface of a logger factory.
 */
public interface ILogFactory extends ILifecycleController.TerminableListener {
	/**
	 * Sets the {@link ResourceLocator}, as an aid for locating config files.
	 * @param pLocator The {@link ResourceLocator}, an aid for locating config files.
	 */
	void setResourceLocator(ResourceLocator pLocator);
	/**
	 * Returns the {@link ResourceLocator}, as an aid for locating config files.
	 * @return The {@link ResourceLocator}, an aid for locating config files.
	 */
	ResourceLocator getResourceLocator();
	/**
	 * Returns a logger, which uses the given classes fully qualified name as
	 * the logger id.
	 * @param pClass This classes fully qualified name is being used as the
	 *   logger id.
	 * @return A logger, which uses the given classes fully qualified name as
	 * the logger id.
	 */
    ILog getLog(Class<?> pClass);
	/**
	 * Returns a logger, which uses the given logger id.
	 * @param pId The logger id.
	 * @return A logger, which uses the given classes fully qualified name as
	 * the logger id.
	 */
    ILog getLog(String pId);
	/**
	 * Returns a method logger, which uses the given classes fully qualified name as
	 * the logger id, and the given method name.
	 * @param pClass This classes fully qualified name is being used as the
	 *   logger id.
	 * @param pMethod The created loggers method name.
	 * @return A logger, which uses the given classes fully qualified name as
	 * the logger id.
	 */
    IMLog getLog(Class<?> pClass, String pMethod);
	/**
	 * Returns a method logger, which uses the given logger id, and the given
	 * method name.
	 * @param pId The logger id.
	 * @param pMethod The created loggers method name.
	 * @return A logger, which uses the given classes fully qualified name as
	 * the logger id.
	 */
    IMLog getLog(String pId, String pMethod);
}
