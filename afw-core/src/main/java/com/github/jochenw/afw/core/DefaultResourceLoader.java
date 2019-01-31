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

import org.apache.logging.log4j.util.Strings;

public class DefaultResourceLoader extends ResourceLocator {
	String resourcePrefix;

	public DefaultResourceLoader() {
		this(null, null);
	}

	public DefaultResourceLoader(String pApplicationName, String pInstanceName) {
		this(pApplicationName, pInstanceName, null);
	}

	public DefaultResourceLoader(String pApplicationName, String pInstanceName, String pResourcePrefix) {
		setApplicationName(pApplicationName);
		setInstanceName(pInstanceName);
		resourcePrefix = pResourcePrefix;
	}

	public String getResourcePrefix() {
		return resourcePrefix;
	}

	public void setResourcePrefix(String pResourcePrefix) {
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

	protected URL findResource(String pUri, ClassLoader pCl) {
		final String rp = getResourcePrefix();
		if (Strings.isEmpty(rp)) {
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
