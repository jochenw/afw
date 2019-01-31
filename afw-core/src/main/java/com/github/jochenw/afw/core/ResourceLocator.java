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


public abstract class ResourceLocator {
	private String instanceName, applicationName;

	public String getInstanceName() {
		return instanceName;
	}
	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}
	public String getApplicationName() {
		return applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public URL getResource(String pUri) {
		return getResource(Thread.currentThread().getContextClassLoader(), pUri);
	}
	public URL requireResource(String pUri) throws NoSuchElementException {
		final URL url = getResource(pUri);
		if (url == null) {
			throw new NoSuchElementException("Unable to locate resource: " + pUri);
		}
		return url;
	}
	public abstract URL getResource(ClassLoader pClassLoader, String pUri);
	public URL requireResource(ClassLoader pClassLoader, String pUri) {
		final URL url = getResource(pUri);
		if (url == null) {
			throw new NoSuchElementException("Unable to locate resource: " + pUri
					+ " via ClassLoader " + pClassLoader.toString());
		}
		return url;
	}
}
