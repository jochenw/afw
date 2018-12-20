package com.github.jochenw.afw.core.components;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import com.github.jochenw.afw.core.util.Exceptions;

public class SimpleComponentFactory extends AbstractComponentFactory {
	public class Binding implements Supplier<Object> {
		private final Supplier<Object> supplier;
		private boolean initialized;
		private Object instance;
		Binding(Supplier<Object> pSupplier) {
			supplier = pSupplier;
		}
		@Override
		public Object get() {
			if (!initialized) {
				synchronized(this) {
					if (!initialized) {
						instance = supplier.get();
						initialize(instance);
						initialized = true;
					}
				}
			}
			return instance;
		}
	}
	public interface OnTheFlyBinder {
		Object bind(SimpleComponentFactory pComponentFactory, Class<Object> pType, String pName, String pEntityId, Class<Object> pDeclaringClass);
	}

	private OnTheFlyBinder onTheFlyBinder;

	private Map<Class<Object>,Consumer<Object>> classMetaData = new HashMap<>();
	private Map<Key,Binding> bindings = new HashMap<Key,Binding>();

	@Override
	public <O extends Object> Supplier<O> getSupplier(Class<?> pType, String pName) {
		@SuppressWarnings("unchecked")
		final Supplier<O> supplier = (Supplier<O>) bindings.get(new Key(pType, pName));
		return supplier;
	}

	@Override
	public void setBindings(Map<Key, Supplier<Object>> pBindings) {
		super.setBindings(pBindings);
		for (Map.Entry<Key,Supplier<Object>> en : pBindings.entrySet()) {
			bindings.put(en.getKey(), new Binding(en.getValue()));
		}
		final Key key = new Key(IComponentFactory.class, "");
		if (bindings.get(key) == null) {
			bindings.put(key, new Binding(() -> { return SimpleComponentFactory.this; }));
		}
	}

	public OnTheFlyBinder getOnTheFlyBinder() {
		return onTheFlyBinder;
	}

	public void setOnTheFlyBinder(OnTheFlyBinder onTheFlyBinder) {
		this.onTheFlyBinder = onTheFlyBinder;
	}

	@Override
	public void initialize(@Nonnull Object pObject) {
		Consumer<Object> consumer;
		synchronized(classMetaData) {
			consumer = classMetaData.get(pObject.getClass());
			if (consumer == null) {
				consumer = newClassMetaData(pObject.getClass());
				@SuppressWarnings("unchecked")
				Class<Object> cl = (Class<Object>) pObject.getClass();
				classMetaData.put(cl, consumer);
			}
		}
		consumer.accept(pObject);
	}

	@Nonnull protected Consumer<Object> newClassMetaData(Class<?> pType) {
		final List<Consumer<Object>> consumers = new ArrayList<>();
		findInitializers(pType, (c) -> consumers.add(c));
		return (o) -> consumers.forEach((c) -> c.accept(o));
	}

	protected void findInitializers(Class<?> pType, Consumer<Consumer<Object>> pConsumer) {
		for (Field f : pType.getDeclaredFields()) {
			if (f.isAnnotationPresent(Inject.class)) {
				@SuppressWarnings("unchecked")
				final Class<Object> cl = (Class<Object>) f.getType();
				final Named named = f.getAnnotation(Named.class);
				final String name = (named == null ? "" : named.value());
				Binding binding = getBinding(cl, name);
				if (binding == null) {
					@SuppressWarnings("unchecked")
					final Class<Object> declaringType = (Class<Object>) pType;
					Object o = (onTheFlyBinder == null ? null : onTheFlyBinder.bind(this, cl, name, f.getName(), declaringType));
					if (o == null) {
						throw new IllegalArgumentException("No binding available for field " + f.getName()
								+ " in class " + pType.getName());
					} else {
						binding = new Binding(() -> { return o; });
					}
				}
				if (binding != null) {
					final Binding b = binding;
					pConsumer.accept((o) -> {
						try {
							if (!f.isAccessible()) {
								f.setAccessible(true);
							}
							f.set(o, b.get());
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
					});
				}
			}
		}
		for (Method m : pType.getDeclaredMethods()) {
			if (m.isAnnotationPresent(Inject.class)) {
				final Class<?>[] parameterTypes = m.getParameterTypes();
				if (parameterTypes == null  ||  parameterTypes.length == 0) {
					pConsumer.accept((o) -> {
						try {
							if (!m.isAccessible()) {
								m.setAccessible(true);
							}
							m.invoke(o);
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
					});
				} else {
					final Annotation[][] annotations = m.getParameterAnnotations();
					final Binding[] bindings = new Binding[parameterTypes.length];
					for (int i = 0;  i < bindings.length;  i++) {
						Named named = null;
						final Annotation[] annos = annotations[i];
						if (annos != null) {
							for (int j = 0;  j < annos.length;  j++) {
								final Annotation ann = annos[j];
								if (ann instanceof Named) {
									named = (Named) ann;
									break;
								}
							}
						}
						final String name = (named == null ? "" : named.value());
						@SuppressWarnings("unchecked")
						final Class<Object> cl = (Class<Object>) parameterTypes[i];
						bindings[i] = getBinding(cl, name);
						if (bindings[i] == null) {
							@SuppressWarnings("unchecked")
							final Class<Object> declaringType = (Class<Object>) pType;
							Object o = (onTheFlyBinder == null ? null : onTheFlyBinder.bind(this, cl, name, m.getName() + ".p" + i, declaringType));
							if (o == null) {
								throw new IllegalArgumentException("No binding available for method " + m.getName()
										+ ", parameter " + i + " in class " + pType.getName());
							} else {
								bindings[i] = new Binding(() -> { return o; });
							}
						}
					}
					pConsumer.accept((o) -> {
						try {
							if (!m.isAccessible()) {
								m.setAccessible(true);
							}
							final Object[] args = new Object[bindings.length];
							for (int i = 0;  i < bindings.length;  i++) {
								args[i] = bindings[i].get();
							}
							m.invoke(o, args);
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
					});
				}
			}
		}
	}

	protected Binding getBinding(Class<Object> pType, String pName) {
		final Binding b = bindings.get(new Key(pType, pName));
		if (b == null) {
			
		}
		return b;
	}
}
