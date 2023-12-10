/*
 * Copyright 2023 Jochen Wiedmann
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
package com.github.jochenw.afw.di.impl.limited;

import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;


/** A super-simple, and limited variant of the
 * {@link com.github.jochenw.afw.di.api.IComponentFactory}:
 * <ol>
 *   <li>No support for {@code Inject} annotations, but
 *   initialization by implementing {@link IComponent}.</li>
 *   <li>Scope is always singleton.</li>
 * </ol>
 * The purpose is to have a minimal solution available, if
 * you are in a situation, where you cannot import jar files into
 * a project, but importing simple source files works.
 * (The author has been in exactly that situation in 2023.)
 */
public class LimitedComponentFactory {
	/** Interface of a component, that can initialize itself.
	 */
	public static interface IComponent {
		/** Initializes the component by providing access to
		 * the component factory.
		 * @param pComponentFactory The component factory,
		 *   that created this component.
		 * @throws Throwable Initializing the component has failed.
		 */
		public void init(LimitedComponentFactory pComponentFactory) throws Throwable;
	}
	/** Interface of a module, that configures
	 * the component factory by providing bindings.
	 */
	public static interface IModule {
		/** Called to configure the component factory by providing
		 * bindings.
		 * @param pBinder The binder, which provides methods for
		 *   creating a binding.
		 */
		public void configure(IBinder pBinder);
		/** Creates a new module, which applies first the current
		 * modules bindings, and then the given modules.
		 * @param pModule The extending module. In case
		 *   of conflicting bindings, the extending module
		 *   takes precedence: It can overwrite bindings,
		 *   that are created by the current module.
		 * @return An extension of the current module, which
		 *   adds the given modules bindings to it's own.
		 */
		public default IModule extend(IModule pModule) {
			if (pModule == null) {
				return this;
			} else {
				final IModule mod = this;

				return (b) -> {
					mod.configure(b);
					pModule.configure(b);
				};
			}
		}
	}
	/** Interface of a binding creator.
	 */
	public static interface IBinder {
		/** Creates a binding, which binds the given supplier to the given
		 * type, and name.
		 * @param pType Type of the created binding.
		 * @param pName Name of the created binding.
		 * @param pSupplier A supplier for the singleton instance. It is
		 *   guaranteed to be invoked only once (When the binding is being
		 *   requested for the first time.
		 * @param <O> Type of the created binding.
		 */
		public <O> void bindToSupplier(Class<O> pType, String pName, Supplier<? extends O> pSupplier);
		/** Creates a binding, which binds the given supplier to the given
		 * type, and default name.
		 * @param pType Type of the created binding.
		 * @param pSupplier A supplier for the singleton instance. It is
		 *   guaranteed to be invoked only once (When the binding is being
		 *   requested for the first time.
		 * @param <O> Type of the created binding.
		 */
		public default <O> void bindToSupplier(Class<O> pType, Supplier<? extends O> pSupplier) {
			bindToSupplier(pType, "", pSupplier);
		}
		/** Creates a binding, which binds the given object to the given type,
		 * and name.
		 * @param pType Type of the created binding.
		 * @param pName Name of the created binding.
		 * @param pInstance The singleton instance.
		 * @param <O> Type of the created binding.
		 */
		public default <O> void bind(Class<O> pType, String pName, O pInstance) {
			final O instance = Objects.requireNonNull(pInstance, "Instance");
			bindToSupplier(pType, pName, (Supplier<O>) () -> instance);
		}
		/** Creates a binding, which binds the given object to the given type,
		 * and default name.
		 * @param pType Type of the created binding.
		 * @param pInstance The singleton instance.
		 * @param <O> Type of the created binding.
		 */
		public default <O> void bind(Class<O> pType, O pInstance) {
			bind(pType, "", pInstance);
		}
		/** Creates a binding, which binds the given implementation type
		 * to the given type, and name.
		 * @param pType Type of the created binding.
		 * @param pName Name of the created binding.
		 * @param pImplType The implementation type; the singleton instance
		 *   will be created by using the implementation types public
		 *   non-args (default) constructor.
		 * @param <O> Type of the created binding.
		 */
		public default <O> void bind(Class<O> pType, String pName, Class<? extends O> pImplType) {
			final Class<? extends O> implType = Objects.requireNonNull(pImplType, "Implementation Type");
			final Constructor<? extends O> constructor;
			try {
				constructor = implType.getConstructor();
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("The implementation class "
						+ pImplType.getName() + " doesn't have a public no-args constructor.");
			}
			final Supplier<O> supplier = () -> {
				try {
					final O o = (O) constructor.newInstance();
					return o;
				} catch (RuntimeException|Error e) {
					throw e;
				} catch (Throwable t) {
					throw new UndeclaredThrowableException(t);
				}
			};
			bindToSupplier(pType, pName, supplier);
		}
		/** Creates a binding, which binds the given implementation type
		 * to the given type, and default name.
		 * @param pType Type of the created binding.
		 * @param pImplType The implementation type; the singleton instance
		 *   will be created by using the implementation types public
		 *   non-args (default) constructor.
		 * @param <O> Type of the created binding.
		 */
		public default <O> void bind(Class<O> pType, Class<? extends O> pImplType) {
			bind(pType, "", pImplType);
		}
		/** Creates a binding, which binds the given type to itself
		 * as the implementation type, with the given name.
		 * @param pType Type of the created binding.
		 * @param pName Name of the created binding.
		 * @param <O> Type of the created binding.
		 */
		public default <O> void bind(Class<O> pType, String pName) {
			bind(pType, pName, pType);
		}
		/** Creates a binding, which binds the given type to itself
		 * as the implementation type, with the default name.
		 * @param pType Type of the created binding.
		 * @param <O> Type of the created binding.
		 */
		public default <O> void bind(Class<O> pType) {
			bind(pType, "");
		}
	}
	/** A bindings key, comprising of the bindings type, and name.
	 */
	public static class Key {
		private final Class<?> type;
		private final String name;
		/** Creates a new instance with the given type, and name.
		 * @param pType The bindings type.
		 * @param pName The bindings name.
		 */
		public Key(Class<?> pType, String pName) {
			type = Objects.requireNonNull(pType, "Type");
			name = Objects.requireNonNull(pName, "Name");
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, name);
		}

		@Override
		public boolean equals(Object pOther) {
			if (pOther != null  &&  pOther instanceof Key  &&  getClass() == pOther.getClass()) {
				final Key other = (Key) pOther;
				return type.equals(other.type)  &&  name.equals(other.name);
			}
			return false;
		}

		
		/** Returns the bindings type.
		 * @return The bindings type.
		 */
		public Class<?> getType() { return type; }
		/** Returns the bindings name.
		 * @return The bindings name.
		 */
		public String getName() { return name; }
	}
	/** A binding is a supplier of an initialized singleton object,
	 * which has been registered under a particular {@link Key key}.
	 */
	public static class Binding {
		private Object instance;
		private final Key key;
		private final Supplier<Object> supplier;
		/** Creates a new instance with the given key, and supplier.
		 * @param pKey The bindings key.
		 * @param pSupplier Supplier of the (not yet initialized) singleton
		 *   object.
		 */
		public Binding(Key pKey, Supplier<Object> pSupplier) {
			key = pKey;
			supplier = pSupplier;
		}
		/** Returns the initialized singleton object.
		 * @param pComponentFactory The component factory, which can be
		 *   used to initialize the singleton object, if necessary.
		 * @return The initialized singleton object.
		 */
		public Object get(LimitedComponentFactory pComponentFactory) {
			synchronized(this) {
				if (instance == null) {
					instance = Objects.requireNonNull(supplier.get(),
							() -> "Binding returned a null value: "
								+ "type=" + key.getType().getName()
								+ ", name=" + key.getName());
					if (instance instanceof IComponent) {
						try {
							((IComponent) instance).init(pComponentFactory);
						} catch (RuntimeException|Error e) {
							throw e;
						} catch (Throwable t) {
							throw new UndeclaredThrowableException(t);
						}
					}
				}
				return instance;
			}
		}
	}

	/** Creates a new instance with the given module.
	 * @param pModule The component factories configurator.
	 * @return The created component factory, ready for use.
	 */
	public static LimitedComponentFactory of(IModule pModule) {
		final Map<Key,Binding> bindings = new HashMap<>();
		final IBinder binder = new IBinder() {
			@Override
			public <O> void bindToSupplier(Class<O> pType, String pName, Supplier<? extends O> pSupplier) {
				final Key key = new Key(pType, pName);
				bindings.computeIfAbsent(key, (k) -> {
					@SuppressWarnings("unchecked")
					final Supplier<Object> supplier = (Supplier<Object>) pSupplier;
					return new Binding(key, supplier);
				});
			}
		};
		pModule.configure(binder);
		final Key myKey = new Key(LimitedComponentFactory.class, "");
		bindings.put(myKey, new Binding(myKey, () -> null) {
			@Override
			public Object get(LimitedComponentFactory pComponentFactory) {
				return pComponentFactory;
			}
		});
		return new LimitedComponentFactory(bindings);
	}

	/** Creates a new instance with the given modules.
	 * @param pModules The component factories configurators.
	 * @return The created component factory, ready for use.
	 */
	public static LimitedComponentFactory of(IModule... pModules) {
		return of((b) -> {
			for (IModule module : pModules) {
				module.configure(b);
			}
		});
	}

	/** Creates a new instance with the given bindings.
	 * @param pBindings The bindings, that have been registered as thr
	 *   component factories configuration.
	 */
	protected LimitedComponentFactory(Map<Key,Binding> pBindings) {
		bindings = pBindings;
	}
	private final Map<Key,Binding> bindings;

	/** Returns an instance of the given type, with the given name,
	 * if available, or null.
	 * @param pType Type of the requested instance.
	 * @param pName Name of the requested instance.
	 * @param <O> Type of the requested instance.
	 * @return The requested instance, if a suitable binding is
	 *   available, or null.
	 */
	public <O> O getInstance(Class<?> pType, String pName) {
		final Key key = new Key(pType, pName);
		final Binding binding = bindings.get(key);
		if (binding == null) {
			return null;
		} else {
			@SuppressWarnings("unchecked")
			final O o = (O) binding.get(this);
			return o;
		}
	}

	/** Returns an instance of the given type, with the default name,
	 * if available, or null.
	/** Returns an instance of the given type, with the given name,
	 * if available, or null.
	 * @param pType Type of the requested instance.
	 * @param <O> Type of the requested instance.
	 * @return The requested instance, if a suitable binding is
	 *   available, or null.
	 */
	public <O> O getInstance(Class<?> pType) {
		return getInstance(pType, "");
	}

	/** Returns an instance of the given type, with the given name,
	 * if available. Throws a {@link NoSuchElementException}, if
	 * no such instance is available.
	 * @param pType Type of the requested instance.
	 * @param pName Name of the requested instance.
	 * @param <O> Type of the requested instance.
	 * @return The requested instance, if a suitable binding is
	 *   available, or null.
	 * @throws NoSuchElementException No binding with the
	 * given type, and name has been registered.
	 */
	public <O> O requireInstance(Class<O> pType, String pName) {
		final Key key = new Key(pType, pName);
		final Binding binding = bindings.get(key);
		if (binding == null) {
			throw new NoSuchElementException("No binding has been registered"
					+ " with type=" + pType.getName() + ", and name=" + pName);
		} else {
			@SuppressWarnings("unchecked")
			final O o = (O) binding.get(this);
			return o;
		}
	}

	/** Returns an instance of the given type, with the default name,
	 * if available. Throws a {@link NoSuchElementException}, if
	 * no such instance is available.
	 * @param pType Type of the requested instance.
	 * @param <O> Type of the requested instance.
	 * @return The requested instance, if a suitable binding is
	 *   available, or null.
	 * @throws NoSuchElementException No binding with the
	 * given type, and name has been registered.
	 */
	public <O> O requireInstance(Class<O> pType) {
		return requireInstance(pType, "");
	}
}
