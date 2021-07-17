/**
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
package com.github.jochenw.afw.core;

import java.net.URL;

import javax.annotation.Nullable;



/**
 * Default implementation of the {@link ResourceLocator}.
 */
public class DefaultResourceLoader extends ResourceLocator {
	String resourcePrefix;

	/**
	 * Creates a new instance with application name = null, and instance name = null,
	 * and resource prefix = null.
	 */
	public DefaultResourceLoader() {
		this(null, null);
	}

	/**
	 * Creates a new instance with the given application name, and the given
	 * instance name, and the resource prefix null.
	 * @param pApplicationName The application name
	 * @param pInstanceName The instance name
	 */
	public DefaultResourceLoader(@Nullable String pApplicationName, @Nullable String pInstanceName) {
		this(pApplicationName, pInstanceName, null);
	}

	/**
	 * Creates a new instance with the given application name, and the given
	 * instance name, and the given resource prefix.
	 * @param pApplicationName The application name
	 * @param pInstanceName The instance name
	 * @param pResourcePrefix The resource prefix
	 */
	public DefaultResourceLoader(@Nullable String pApplicationName, @Nullable String pInstanceName,
			             @Nullable String pResourcePrefix) {
		setApplicationName(pApplicationName);
		setInstanceName(pInstanceName);
		resourcePrefix = pResourcePrefix;
	}

	/**
	 * Returns the resource prefix.
	 * @return The resource prefix.
	 */
	public @Nullable String getResourcePrefix() {
		return resourcePrefix;
	}

	/**
	 * Sets the resource prefix.
	 * @param pResourcePrefix The resource prefix.
	 */
	public void setResourcePrefix(@Nullable String pResourcePrefix) {
		resourcePrefix = pResourcePrefix;
	}
	
	@Override
	public URL getResource(ClassLoader pClassLoader, String pUri) {
		final String instanceName = getInstanceName();
		if (instanceName != null  &&  instanceName.length() > 0) {
			final String uri = instanceName + "/" + pUri;
			final URL url = findResource(uri, pClassLoader);
			if (url != null) {
				return url;
			}
		}
		return findResource(pUri, pClassLoader);
	}

        /** Called internally from {@link #getResource(ClassLoader,String)}
         * to implement the possible application of the {@link #getResourcePrefix()}.
         * @param pUri The URI, which is being searched.
         * @param pCl The ClassLoader, which is being used for searching.
         * @return An URL, which matches the specified URI, and can be
         *   loaded through the given ClassLoader, or null, if no such resource
         *   has been found.
         */
	protected URL findResource(String pUri, ClassLoader pCl) {
		final String rp = getResourcePrefix();
		if (rp == null  ||  rp.length() == 0) {
			return pCl.getResource(pUri);
		} else {
			if (rp.endsWith("/")) {
				return pCl.getResource(rp + pUri);
			} else {
				return pCl.getResource(rp + "/" + pUri);
			}
		}
	}
}
