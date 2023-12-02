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
package com.github.jochenw.afw.core.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.github.jochenw.afw.core.util.Strings;


/** An abstract base class for deriving servlet context initializers, that maintain a data object
 * as a context attribute.
 * @param <Data> The data objects type.
 */
public abstract class AfwCoreContextInitializer<Data extends Object> implements ServletContextListener {
	private static final String KEY = AfwCoreContextInitializer.class.getName() + ".KEY";

	@Override
	public void contextInitialized(ServletContextEvent pSce) {
		final ServletContext sc = pSce.getServletContext();
		final String instanceName = getInstanceName(sc.getContextPath());
		final Data data = newData(instanceName);
		sc.setAttribute(KEY, data);
	}

	@Override
	public void contextDestroyed(ServletContextEvent pSce) {
		final ServletContext sc = pSce.getServletContext();
		final Data data = getData(sc);
		sc.setAttribute(KEY, data);
		shutdown(data);
	}

	/** Converts the current web applications context path into the
	 * application instance name.
	 * @param pContextName The web applications context path.
	 * @return The applications instance name, as derived from the context path.
	 */
	protected String getInstanceName(String pContextName) {
		if (Strings.isEmpty(pContextName)) {
			return "ROOT";
		} else if (pContextName.startsWith("/")) {
			return pContextName.substring(1);
		} else {
			return pContextName;
		}
	}

	/**
	 * Returns the data object.
	 * @param <O> The data objects type.
	 * @param pContext The servlet context, that has been initialized, and contains the data
	 *   object.
	 * @return The created d
	 */
	public static <O> O getData(ServletContext pContext) {
		@SuppressWarnings("unchecked")
		final O data = (O) pContext.getAttribute(KEY);
		if (data == null) {
			throw new IllegalStateException("Data not available in context");
		}
		return data;
	}

	/** Called to destroy the given data object.
	 * @param pData The data object, that is being destroyed.
	 */
	protected void shutdown(Data pData) {}

	/** Called to create the new data object, which is being stored as a servlet context attribute.
	 * @param pInstanceName The instance name, as derived from the servlet context path.
	 * @return The created Data object.
	 */
    protected abstract Data newData(String pInstanceName);
}
