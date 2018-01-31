package com.github.jochenw.afw.rm.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import com.github.jochenw.afw.rm.api.ComponentFactory;
import com.github.jochenw.afw.rm.util.Exceptions;

public class SimpleComponentFactory extends ComponentFactory {
	protected static class Key {
		private final Class<Object> type;
		private final String name;
		public Key(Class<Object> pType, String pName) {
			type = pType;
			name = pName;
		}
		@Override
		public int hashCode() {
			return 31 * ((name == null) ? 0 : name.hashCode()) + ((type == null) ? 0 : type.hashCode());
		}
		@Override
		public boolean equals(Object pOther) {
			if (this == pOther)
				return true;
			if (pOther == null)
				return false;
			if (getClass() != pOther.getClass())
				return false;
			Key other = (Key) pOther;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return type.equals(other.type);
		}
	}
	protected class Wrapper {
		private Object instance;
		private boolean initialized;
		private boolean providing;

		public Object getInstance() {
			synchronized(this) {
				if (!initialized) {
					if (providing) {
						if (instance instanceof Class<?>) {
							try {
								@SuppressWarnings("unchecked")
								final Class<Object> cl = (Class<Object>) instance;
								instance = cl.newInstance();
							} catch (Throwable t) {
								throw Exceptions.show(t);
							}
						} else if (instance instanceof Provider<?>) {
							@SuppressWarnings("unchecked")
							final Provider<Object> prov = (Provider<Object>) instance;
							instance = prov.get();
						} else {
							throw new IllegalStateException("Unable to provide from an instance of " + instance.getClass().getName());
						}
						providing = false;
					}
					if (instance instanceof Initializable) {
						((Initializable) instance).init(SimpleComponentFactory.this);
					}
					initialized = true;
				}
			}
			return instance;
		}
	}

	private final Map<Key,Wrapper> bindings = new HashMap<Key, Wrapper>();

	@Override
	public <O> O getInstance(Class<O> pType) {
		return getInstance(pType, null);
	}

	@Override
	public <O> O getInstance(Class<O> pType, String pName) {
		@SuppressWarnings("unchecked")
		final Class<Object> cl = (Class<Object>) pType;
		final Key key = new Key(cl, pName);
		final Wrapper wrapper = bindings.get(key);
		if (wrapper == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final O o = (O) wrapper.getInstance();
		return o;
	}

	public <O> void setInstance(Class<O> pType, O pInstance) {
		setInstance(pType, null, pInstance);
	}

	public <O> void setInstance(Class<O> pType, String pName, O pInstance) {
		@SuppressWarnings("unchecked")
		final Class<Object> cl = (Class<Object>) pType;
		final Key key = new Key(cl, pName);
		final Wrapper wrapper = new Wrapper();
		wrapper.instance = pInstance;
		bindings.put(key, wrapper);
	}

	public <O> void setInstanceClass(Class<O> pType, Class<? extends O> pImplementation) {
		setInstanceClass(pType, null, pImplementation);
	}

	public <O> void setInstanceClass(Class<O> pType, String pName, Class<? extends O> pImplementation) {
		@SuppressWarnings("unchecked")
		final Class<Object> cl = (Class<Object>) pType;
		final Key key = new Key(cl, pName);
		final Wrapper wrapper = new Wrapper();
		wrapper.instance = pImplementation;
		wrapper.providing = true;
		bindings.put(key, wrapper);
	}

	public <O> void setInstanceProvider(Class<O> pType, Provider<? extends O> pProvider) {
		setInstanceProvider(pType, null, pProvider);
	}

	public <O> void setInstanceProvider(Class<O> pType, String pName, Provider<? extends O> pProvider) {
		@SuppressWarnings("unchecked")
		final Class<Object> cl = (Class<Object>) pType;
		final Key key = new Key(cl, pName);
		final Wrapper wrapper = new Wrapper();
		wrapper.instance = pProvider;
		wrapper.providing = true;
		bindings.put(key, wrapper);
	}
}
