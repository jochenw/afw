/*
 * Copyright 2022 Jochen Wiedmann
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
package com.github.jochenw.afw.core.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import org.jspecify.annotations.NonNull;


/** A ClassLoader, which works like an URLClassLoader, except that it
 * implements a parent-last lookup.
 */
public class ParentLastClassLoader extends ClassLoader {
	private final URL[] urls;
	private final URLClassLoader delegateWithParent;
	private final URLClassLoader delegateWithoutParent;
	
	/** Creates a new instance, which loads classes from the given Jar files.
	 * @param pParent The parent ClassLoader, to which loading is being
	 *   delegated. 
	 * @param pUrls URL's of jar files, from which classes are being loaded.
	 */
	public ParentLastClassLoader(ClassLoader pParent, URL... pUrls) {
		super(null);
		@SuppressWarnings("null")
		final @NonNull ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader parent = Objects.notNull(pParent, contextClassLoader);
		urls = Objects.requireNonNull(pUrls, "Urls");
		delegateWithParent = new URLClassLoader(urls, parent);
		delegateWithoutParent = new URLClassLoader(urls, null);
	}

	/** Creates a new instance, which loads classes from the given Jar files.
	 * @param pUrls URL's of jar files, from which classes are being loaded.
	 * @return The created instance.
	 */
	public static ParentLastClassLoader of(URL... pUrls) {
		return new ParentLastClassLoader(Thread.currentThread().getContextClassLoader(), pUrls);
	}

	/** Creates a new instance, which loads classes from the given Jar files.
	 * @param pUrls URL's of jar files, from which classes are being loaded.
	 * @return The created instance.
	 */
	public static ParentLastClassLoader of(Collection<URL> pUrls) {
		return of(Thread.currentThread().getContextClassLoader(), pUrls);
	}

	/** Creates a new instance, which loads classes from the given Jar files.
	 * @param pParent The parent ClassLoader, to which loading is being
	 *   delegated. 
	 * @param pUrls URL's of jar files, from which classes are being loaded.
	 * @return The created instance.
	 */
	public static ParentLastClassLoader of(ClassLoader pParent, Collection<URL> pUrls) {
		return new ParentLastClassLoader(pParent, pUrls.toArray(new URL[pUrls.size()]));
	}
	
	@Override
	protected Class<?> findClass(String pName) throws ClassNotFoundException {
		try {
			return delegateWithoutParent.loadClass(pName);
		} catch (ClassNotFoundException e) {
			return delegateWithParent.loadClass(pName);
		}
	}
}
