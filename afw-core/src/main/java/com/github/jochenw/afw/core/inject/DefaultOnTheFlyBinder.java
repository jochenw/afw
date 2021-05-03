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
package com.github.jochenw.afw.core.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.github.jochenw.afw.core.ILifecycleController;
import com.github.jochenw.afw.core.ILifecycleController.TerminableListener;
import com.github.jochenw.afw.core.inject.simple.SimpleComponentFactory;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.IMLog;
import com.github.jochenw.afw.core.props.BooleanProperty;
import com.github.jochenw.afw.core.props.IBooleanProperty;
import com.github.jochenw.afw.core.props.IIntProperty;
import com.github.jochenw.afw.core.props.ILongProperty;
import com.github.jochenw.afw.core.props.IProperty;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.props.IntProperty;
import com.github.jochenw.afw.core.props.LongProperty;
import com.github.jochenw.afw.core.props.StringProperty;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions;
import com.github.jochenw.afw.core.util.Strings;


/** Default implementation of {@link OnTheFlyBinder}, for use
 * with the {@link SimpleComponentFactory}.
 */
public class DefaultOnTheFlyBinder implements OnTheFlyBinder {
	@Override
	public <O> void findConsumers(IComponentFactory pCf, Class<?> pType, Consumer<Consumer<O>> pConsumerSink) {
		final List<Method> initMethods = new ArrayList<>();
		final List<Method> shutdownMethods = new ArrayList<>();
		for (Method m : pType.getDeclaredMethods()) {
			if (Modifier.isPublic(m.getModifiers())) {
				if (!Modifier.isStatic(m.getModifiers())  &&  !Modifier.isAbstract(m.getModifiers())) {
					if (m.getParameterCount() == 0) {
						if (m.isAnnotationPresent(PostConstruct.class)) {
							initMethods.add(m);
						}
						if (m.isAnnotationPresent(PreDestroy.class)) {
							shutdownMethods.add(m);
						}
					}
				}
			}
		}
		if (!initMethods.isEmpty()  ||  !shutdownMethods.isEmpty()) {
			final Consumer<O>  consumer = (o) -> {
				final ILifecycleController lcController = pCf.requireInstance(ILifecycleController.class);
				final TerminableListener tl = new TerminableListener() {
					@Override
					public void start() {
						for (Method m : initMethods) {
							Functions.run(() -> m.invoke(o));
						}
					}

					@Override
					public void shutdown() {
						Throwable th = null;
						for (Method m : shutdownMethods) {
							try {
								m.invoke(o);
							} catch (Throwable t) {
								if (th == null) {
									th = t;
								}
							}
						}
						if (th != null) {
							throw Exceptions.show(th);
						}
					}
				};
				lcController.addListener(tl);
			};
			pConsumerSink.accept(consumer);
		}
	}

	@Override
	public <O> Provider<O> getProvider(IComponentFactory pCf, Field pField) {
		for (Annotation a : pField.getAnnotations()) {
			if (a instanceof LogInject) {
				return getLogInjectProvider(pCf, pField, (LogInject) a);
			} else if (a instanceof PropInject) {
				return getPropInjectProvider(pCf, pField, (PropInject) a);
			}
		}
		return null;
	}

	protected <O> Provider<O> getPropInjectProvider(IComponentFactory pCf, Field pField, PropInject pPropInject) {
		final String id;
		if (Strings.isEmpty(pPropInject.id())) {
			id = pField.getDeclaringClass().getName() + "." + pField.getName();
		} else {
			id = pPropInject.id();
		}
		final String defaultValueStr;
		if (pPropInject.defaultValue() == PropInject.NO_DEFAULT) {
			defaultValueStr = null;
		} else {
			defaultValueStr = pPropInject.defaultValue();
		}
		final Class<?> type = pField.getType();
		if (String.class.equals(type)) {
			return () -> {
				final IPropertyFactory propFactory = pCf.requireInstance(IPropertyFactory.class);
				@SuppressWarnings("unchecked")
				final O o = (O) propFactory.getPropertyValue(id);
				return o;
			};
		} else if (Boolean.class.equals(type)  ||  Boolean.TYPE.equals(type)) {
			final boolean defaultBool = getDefaultBooleanValue(pField, defaultValueStr);
			return () -> {
				final IPropertyFactory propFactory = pCf.requireInstance(IPropertyFactory.class);
				final String s = propFactory.getPropertyValue(id);
				Boolean b;
				try {
					b = Boolean.valueOf(s);
				} catch (Throwable t) {
					b = Boolean.valueOf(defaultBool);
				}
				@SuppressWarnings("unchecked")
				final O o = (O) b;
				return o;
			};
		} else if (Integer.class.equals(type)  ||  Integer.TYPE.equals(type)) {
			final Integer defaultInt = getDefaultIntValue(pField, defaultValueStr);
			return () -> {
				final IPropertyFactory propFactory = pCf.requireInstance(IPropertyFactory.class);
				final String s = propFactory.getPropertyValue(id);
				Integer i;
				try {
					i = Integer.valueOf(s);
				} catch (Throwable t) {
					i = defaultInt;
				}
				@SuppressWarnings("unchecked")
				final O o = (O) i;
				return o;
			};
		} else if (Long.class.equals(type)  ||  Long.TYPE.equals(type)) {
			final Long defaultLong = getDefaultLongValue(pField, defaultValueStr);
			return () -> {
				final IPropertyFactory propFactory = pCf.requireInstance(IPropertyFactory.class);
				final String s = propFactory.getPropertyValue(id);
				Long l;
				try {
					l = Long.valueOf(s);
				} catch (Throwable t) {
					l = defaultLong;
				}
				@SuppressWarnings("unchecked")
				final O o = (O) l;
				return o;
			};
		} else if (IntProperty.class.equals(type)  ||  IIntProperty.class.equals(type)) {
			final Integer defaultInt = getDefaultIntValue(pField, defaultValueStr);
			return () -> {
				final IPropertyFactory propFactory = pCf.requireInstance(IPropertyFactory.class);
				@SuppressWarnings("unchecked")
				final O o = (O) propFactory.getIntProperty(id, defaultInt.intValue());
				return o;
			};
		} else if (LongProperty.class.equals(type)  ||  ILongProperty.class.equals(type)) {
			final Long defaultLong = getDefaultLongValue(pField, defaultValueStr);
			return () -> {
				final IPropertyFactory propFactory = pCf.requireInstance(IPropertyFactory.class);
				@SuppressWarnings("unchecked")
				final O o = (O) propFactory.getLongProperty(id, defaultLong.longValue());
				return o;
			};
		} else if (BooleanProperty.class.equals(type)  ||  IBooleanProperty.class.equals(type)) {
			final Boolean defaultBool = getDefaultBooleanValue(pField, defaultValueStr);
			return () -> {
				final IPropertyFactory propFactory = pCf.requireInstance(IPropertyFactory.class);
				@SuppressWarnings("unchecked")
				final O o = (O) propFactory.getBooleanProperty(id, defaultBool.booleanValue());
				return o;
			};
		} else if (IProperty.class.equals(type)  ||  StringProperty.class.equals(type)) {
			return () -> {
				final IPropertyFactory propFactory = pCf.requireInstance(IPropertyFactory.class);
				@SuppressWarnings("unchecked")
				final O o = (O) propFactory.getProperty(id, defaultValueStr);
				return o;
			};
		} else {
			throw new IllegalStateException("Invalid field type for " + pField + ", annotated with @PropInject"
					+ ". Expected String|Integer|int|Long|long|Boolean|boolean|IIntProperty|ILongProperty|IBooleanProperty|IProperty, got " + type.getName());
		}
	}

	protected <O> Provider<O> getLogInjectProvider(IComponentFactory pCf, AnnotatedElement pAnnotatable, LogInject pAnnotation) {
		final String id;
		final Class<?> type;
		if (pAnnotatable instanceof Field) {
			final Field field = (Field) pAnnotatable;
			type = field.getType();
			id = Strings.notEmpty(pAnnotation.id(), field.getDeclaringClass().getName());
		} else if (pAnnotatable instanceof Method) {
			final Method method = (Method) pAnnotatable;
			type = method.getParameterTypes()[0];
			id = Strings.notEmpty(pAnnotation.id(), method.getDeclaringClass().getName());
		} else {
			throw new IllegalStateException("Invalid type for annotatable: " + pAnnotatable.getClass().getName());
		}
		if (type == ILog.class) {
			// Nothing to do here.
		} else if (type == IMLog.class) {
			if (Strings.isEmpty(pAnnotation.mName())) {
				throw new IllegalStateException("Missing, or empty mName attribute on @LogInject of " + pAnnotatable);
			}
		} else {
			throw new IllegalStateException("Invalid type " + type.getName() + " for @LogInject (Must be either ILog, or IMLog.)");
		}
		return () -> {
			final ILogFactory logFactory = pCf.requireInstance(ILogFactory.class);
			if (type == ILog.class) {
				@SuppressWarnings("unchecked")
				final O o = (O) logFactory.getLog(id);
				return o;
			} else if (type == IMLog.class) {
				@SuppressWarnings("unchecked")
				final O o = (O) logFactory.getLog(id, pAnnotation.mName());
				return o;
			} else {
				throw new IllegalStateException("Invalid type " + type.getName() + " for @LogInject (Must be either ILog, or IMLog.)");
			}
		};
	}

	protected Long getDefaultLongValue(AnnotatedElement pAnnotatable, final String pDefaultValueStr) {
		if (pDefaultValueStr == null) {
			throw new IllegalStateException("@PropInject on " + pAnnotatable + " requires a defaultValue().");
		}
		final Long defaultLong;
		try {
			defaultLong = Long.valueOf(pDefaultValueStr);
		} catch (Throwable t) {
			throw new IllegalStateException("@PropInject on " + pAnnotatable + " has an invalid default value: " + pDefaultValueStr);
		}
		return defaultLong;
	}

	protected Integer getDefaultIntValue(AnnotatedElement pAnnotatable, final String pDefaultValueStr) {
		if (pDefaultValueStr == null) {
			throw new IllegalStateException("@PropInject on " + pAnnotatable + " requires a defaultValue().");
		}
		final Integer defaultInt;
		try {
			defaultInt = Integer.valueOf(pDefaultValueStr);
		} catch (Throwable t) {
			throw new IllegalStateException("@PropInject on " + pAnnotatable + " has an invalid default value: " + pDefaultValueStr);
		}
		return defaultInt;
	}

	protected Boolean getDefaultBooleanValue(AnnotatedElement pAnnotatable, final String pDefaultValueStr) {
		if (pDefaultValueStr == null) {
			throw new IllegalStateException("@PropInject on " + pAnnotatable + " requires a defaultValue().");
		}
		final Boolean defaultBoolean;
		try {
			defaultBoolean = Boolean.valueOf(pDefaultValueStr);
		} catch (Throwable t) {
			throw new IllegalStateException("@PropInject on " + pAnnotatable + " has an invalid default value: " + pDefaultValueStr);
		}
		return defaultBoolean;
	}

	@Override
	public <O> void findBindings(IComponentFactory pCf, Class<?> pType,
			BiConsumer<Key<O>, ScopedProvider<O>> pBindingSink) {
		final Class<?> cl = (Class<?>) pType;
		if (cl.isAnnotationPresent(Singleton.class)) {
			final ScopedProvider<O> scopedProvider = new ScopedProvider<O>() {
				@Override
				public O get() {
					@SuppressWarnings("unchecked")
					final O o = (O) pCf.newInstance(cl);
					return o;
				}

				@Override
				public Scope getScope() {
					return Scopes.SINGLETON;
				}

			};
			pBindingSink.accept(new Key<O>(pType), scopedProvider);
		}
	}
}
