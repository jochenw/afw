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
import java.util.NoSuchElementException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/**
 * The {@link ResourceLocator} maintains the application name, and the instance name.
 * An application may exist in multiple instances. (For example, a web application,
 * which is deployed several times.) If so, the application name is assumed to be
 * identical for all instances, whereas the instance name is assumed to be unique. 
 */
public abstract class ResourceLocator {
	private @Nullable String instanceName, applicationName;


	/**
	 * Returns the instance name.
	 * @return The instance name.
	 */
	public @Nullable String getInstanceName() {
		return instanceName;
	}
	/**
	 * Sets the instance name.
	 * @param pInstanceName The instance name.
	 */
	public void setInstanceName(@Nullable String pInstanceName) {
		instanceName = pInstanceName;
	}
	/**
	 * Returns the application name.
	 * @return The application name.
	 */
	public @Nullable String getApplicationName() {
		return applicationName;
	}
	/**
	 * Sets the application name.
	 * @param pApplicationName The application name.
	 */
	public void setApplicationName(@Nullable String pApplicationName) {
		applicationName = pApplicationName;
	}

	/**
	 * Returns the resource with the given URI, searching in the
	 * current threads context class loader.
	 * @param pUri The URI, which is being searched.
	 * @return An URL, which may be opened to read the resource,
	 *   or null, if the resource wasn't found.
	 * @see #requireResource(String)
	 */
	public @Nullable URL getResource(@NonNull String pUri) {
		return getResource(Thread.currentThread().getContextClassLoader(), pUri);
	}
	/**
	 * Returns the resource with the given URI, searching in the
	 * current threads context class loader.
	 * @param pUri The URI, which is being searched.
	 * @return An URL, which may be opened to read the resource.
	 *   Never null, an Exception is thrown, if the resource
	 *   wasn't found.
	 * @throws NoSuchElementException The resource wasn't found.
	 * @see #requireResource(String)
	 */
	public URL requireResource(String pUri) throws NoSuchElementException {
		final URL url = getResource(pUri);
		if (url == null) {
			throw new NoSuchElementException("Unable to locate resource: " + pUri);
		}
		return url;
	}
	/**
	 * Returns the resource with the given URI, searching in the
	 * given class loader.
	 * @param pClassLoader The class loader, which is being used to perform
	 *   the search.
	 * @param pUri The URI, which is being searched.
	 * @return An URL, which may be opened to read the resource,
	 *   or null, if the resource wasn't found.
	 * @see #requireResource(ClassLoader, String)
	 * @see #getResource(String)
	 */
	public abstract URL getResource(ClassLoader pClassLoader, String pUri);
	/**
	 * Returns the resource with the given URI, searching in the
	 * given class loader.
	 * @param pClassLoader The class loader, which is being used to perform
	 *   the search.
	 * @param pUri The URI, which is being searched.
	 * @return An URL, which may be opened to read the resource.
	 *   Never null. (An Exception is thrown, if the resource wasn't found.)
	 * @throws NoSuchElementException The resource wasn't found.
	 * @see #getResource(ClassLoader, String)
	 * @see #getResource(String)
	 */
	public URL requireResource(ClassLoader pClassLoader, String pUri) {
		final URL url = getResource(pUri);
		if (url == null) {
			throw new NoSuchElementException("Unable to locate resource: " + pUri
					+ " via ClassLoader " + pClassLoader.toString());
		}
		return url;
	}
}
