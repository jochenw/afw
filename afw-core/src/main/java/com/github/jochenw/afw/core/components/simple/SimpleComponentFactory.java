package com.github.jochenw.afw.core.components.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Provider;

import com.github.jochenw.afw.core.components.ComponentFactory;
import com.github.jochenw.afw.core.components.ComponentFactoryBuilder.Binder;
import com.github.jochenw.afw.core.components.Initializable;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Strings;

public class SimpleComponentFactory extends ComponentFactory {
	private static class Key {
		private String type, id;

		Key(Class<?> pType, String pId) {
			type = pType.getName();
			id = Strings.notNull(pId);
		}
		Key(Class<?> pType) {
			this(pType, "");
		}

		String getType() { return type; }
		String getId() { return id; }

		@Override
		public String toString() {
			if (id.length() == 0) {
				return "Key: type=" + type;
			} else {
				return "Key: type=" + type + ", id=" + id;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(type, id);
		}

		@Override
		public boolean equals(Object pOther) {
			if (pOther == null  ||  pOther.getClass() != getClass()) {
				return false;
			}
			Key other = (Key) pOther;
			return type.equals(other.type)  &&  id.equals(other.id);
		}
	}

	private static class Wrapper {
		private Object object;
		private boolean providing;
		private boolean initialized;
		private boolean singleton;
		Wrapper(Object pObject) {
			object = pObject;
		}
	}

	private Map<Key,Wrapper> components = new HashMap<>();

	@Override
	public <T> T getInstance(Class<T> pType) {
		final Key key = new Key(pType);
		final Wrapper wrapper = components.get(key);
		return unwrap(wrapper);
	}

	@Override
	public <T> T getInstance(Class<T> pType, String pId) {
		final Key key = new Key(pType, pId);
		@SuppressWarnings("unchecked")
		final Wrapper wrapper = components.get(key);
		return unwrap(wrapper);
	}

	private <T> T unwrap(Wrapper pWrapper) {
		if (pWrapper == null) {
			return null;
		}
		final Object object;
		final boolean initialized;
		synchronized (pWrapper) {
			if (pWrapper.providing) {
				@SuppressWarnings("unchecked")
				final Provider<Object> provider = (Provider<Object>) pWrapper.object;
				try {
					object = provider.get();
					initialized = false;
					if (pWrapper.singleton) {
						pWrapper.object = object;
						pWrapper.initialized = true;
						pWrapper.providing = false;
					}
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			} else {
				if (pWrapper.singleton) {
					object = pWrapper.object;
					initialized = pWrapper.initialized;
				} else {
					try {
						final Class<?> cl = (Class<?>) pWrapper.object;
						object = cl.newInstance();
						initialized = false;
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				}
			}
			if (!initialized) {
				initialize(object);
				if (pWrapper.singleton) {
					pWrapper.initialized = true;
				}
			}
		}
		@SuppressWarnings("unchecked")
		final T t = (T) object;
		return t;
	}
	
	@Override
	public void initialize(Object pObject) {
		if (pObject instanceof Initializable) {
			((Initializable) pObject).init(this);
		}
	}

	private final Binder binder = new Binder() {
		@Override
		public <T> void bindClass(Class<T> pType) {
			bindClass(pType, "", pType);
		}

		@Override
		public <T> void bind(Class<T> pType, String pName, T pInstance) {
			final Key key = new Key(pType, pName);
			final Wrapper wrapper = new Wrapper(pInstance);
			wrapper.singleton = true;
			components.put(key, wrapper);
		}

		@Override
		public <T> void bind(Class<T> pType, T pInstance) {
			bind(pType, "", pInstance);
		}

		@Override
		public <T> void bindProvider(Class<T> pType, String pName, Provider<T> pProvider) {
			bindProvider(pType, pName, pProvider, true);
		}

		@Override
		public <T> void bindProvider(Class<T> pType, String pName, Provider<T> pProvider, boolean pSingleton) {
			final Key key = new Key(pType, pName);
			final Wrapper wrapper = new Wrapper(pProvider);
			wrapper.providing = true;
			wrapper.singleton = pSingleton;
			components.put(key, wrapper);
		}

		@Override
		public <T> void bindClass(Class<T> pType, String pName, Class<? extends T> pImplClass) {
			bindClass(pType, pName, pImplClass, false);
		}

		@Override
		public <T> void bindClass(Class<T> pType, String pName, Class<? extends T> pImplClass, boolean pSingleton) {
			final Provider<T> provider = new Provider<T>() {
				@Override
				public T get() {
					try {
						return pImplClass.newInstance();
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				}
			};
			bindProvider(pType, pName, provider, pSingleton);
		}

		@Override
		public <T> void bindClass(Class<T> pType, Class<? extends T> pImplClass) {
			bindClass(pType, "", pImplClass);
		}

		@Override
		public <T> void bindClass(Class<T> pType, Class<? extends T> pImplClass, boolean pSingleton) {
			bindClass(pType, "", pImplClass, pSingleton);
		}
	};

	Binder getBinder() {
		return binder;
	}
}
