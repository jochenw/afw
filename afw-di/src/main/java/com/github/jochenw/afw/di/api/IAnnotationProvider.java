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
package com.github.jochenw.afw.di.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import com.github.jochenw.afw.di.impl.simple.Binding;

/** This class is used to adjust the application to the respective
 * annotation framework. In theory, AFW DI should work just fine with
 * {@link javax.inject.Inject javax.inject}, {@link jakarta.inject.Inject},
 * or {@link com.google.inject.Inject}.
 */
public interface IAnnotationProvider {
	/** Returns the frameworks Inject annotation class.
	 * @return The frameworks Inject annotation class.
	 */
	@Nonnull Class<? extends Annotation> getInjectClass();
	/** Returns the frameworks Named annotation class.
	 * @return The frameworks Named annotation class.
	 */
	@Nonnull Class<? extends Annotation> getNamedClass();
	/** Returns the frameworks Provider class.
	 * @return The frameworks Provider class.
	 */
	@Nonnull Class<?> getProviderClass();
	/** Creates a Provider for the given binding.
	 * @param pBinding The binding, for which a provider is being created.
	 * @return The created provider.
	 */
	@Nonnull Binding getProvider(Binding pBinding);
	/** If this object is an instance of the frameworks Named annotation class:
	 * Returns the Named annotations value. Otherwise, returns null.
	 * @param pAnnotation The annotation object, which is considered to
	 *   be an instance of the frameworks Named annotation class.
	 * @return The Named annotations value, if applicable, or null.
	 */
	@Nullable String getNamedValue(@Nonnull Annotation pAnnotation);
	/** Returns the annotation providers id. As of this writing, valid id's
	 * are "javax.inject", "jakarta.inject", and "com.google.inject".
	 * @return The annotation providers id.
	 */
	@Nonnull String getId();
	/** Creates a new Named annotation with the given value.
	 * @param pValue The created annotations value.
	 * @return The created Named annotation.
	 */
	default @Nonnull Annotation newNamed(@Nonnull String pValue) {
		final Class<?>[] interfaces = (Class<?>[]) Array.newInstance(Class.class, 1);
		interfaces[0] = getNamedClass();
		return (Annotation) Proxy.newProxyInstance(Annotations.class.getClassLoader(), interfaces, new InvocationHandler() {
			@Override
			public Object invoke(Object pProxy, Method pMethod, Object[] pArgs) throws Throwable {
				if ("equals".equals(pMethod.getName())) {
					final Object pOther = pArgs[0];
					if (pOther == null) {
						return false;
					}
					if (pOther instanceof Annotation) {
						final String value = getNamedValue((Annotation) pOther);
						if (value == null) {
							return false;
						} else {
							return value.equals(pValue);
						}
					} else {
						return false;
					}
				}  else if ("value".equals(pMethod.getName())) {
					return pValue;
				} else if ("hashCode".equals(pMethod.getName())) {
					return Integer.valueOf(Objects.hash(pValue));
				} else if ("annotationType".equals(pMethod.getName())) {
					return javax.inject.Named.class;
				}
				throw new IllegalStateException("Not implemented: " + pMethod);
			}
		});
	}
}
