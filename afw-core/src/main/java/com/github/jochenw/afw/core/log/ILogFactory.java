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

import com.github.jochenw.afw.core.ResourceLocator;

public interface ILogFactory {
	void setResourceLocator(ResourceLocator pLocator);
	ResourceLocator getResourceLocator();
	void start();
	void shutdown();
    ILog getLog(Class<?> pClass);
    ILog getLog(String pId);
    IMLog getLog(Class<?> pClass, String pMethod);
    IMLog getLog(String pId, String pMethod);
}
