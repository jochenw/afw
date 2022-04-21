package com.github.jochenw.afw.di.impl.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.inject.Provider;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.Scope;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.impl.BindingBuilder;
import com.github.jochenw.afw.di.util.Exceptions;
import com.google.common.base.Function;

public class BindingRegistry implements IComponentFactoryAware {
	private final ConcurrentMap<Type,BindingSet> bindings = new ConcurrentHashMap<>();
	private final Predicate<Class<?>> staticInjectionPredicate;

	public BindingRegistry(SimpleComponentFactory pFactory, List<BindingBuilder<Object>> pBindingBuilders, Predicate<Class<?>> pStaticInjectionPredicate) {
		initBindings(pFactory, pBindingBuilders);
		staticInjectionPredicate = pStaticInjectionPredicate;
	}

	protected void initBindings(IComponentFactory pFactory, List<BindingBuilder<Object>> pBindingBuilders) {
		for (BindingBuilder<Object> bb : pBindingBuilders) {
			final Key<Object> key = bb.getKey();
			final Type type = key.getType();
			final BindingSet bindingSet = bindings.computeIfAbsent(type, (tp) -> {
				return new BindingSet(tp);
			});
			final Binding binding = asBinding(bb);
			final Key<Object> k;
			if (bb.getAnnotation() == null) {
				if (bb.getAnnotationType() == null) {
					k = new Key<Object>(type);
				} else {
					k = new Key<Object>(type, bb.getAnnotationType());
				}
			} else {
				k = new Key<Object>(type, bb.getAnnotation());
			}
			bindingSet.register(k, binding);
		}
	}

	protected Binding getBinding(Key<Object> pKey) {
		final BindingSet bs = bindings.get(pKey.getType());
		if (bs == null) {
			return null;
		}
		return bs.find(pKey);
	}

	protected Function<SimpleComponentFactory,Object> asInitializable(Object pInitializableObject, Supplier<Object> pSupplier) {
		return new Function<SimpleComponentFactory,Object>() {
			private boolean initialized;

			@Override
			public @Nullable Object apply(@Nullable SimpleComponentFactory pFactory) {
				synchronized(pInitializableObject) {
					if (!initialized) {
						pFactory.init(pInitializableObject);
						initialized = true;
					}
					return pSupplier.get();
				}
			}
			
		};
	}
	
	
	protected Binding asBinding(BindingBuilder<Object> pBindingBuilder) {
		final Function<SimpleComponentFactory,Object> baseSupplier;
		if (pBindingBuilder.getTargetClass() == null) {
			if (pBindingBuilder.getTargetConstructor() == null) {
				if (pBindingBuilder.getTargetInstance() == null) {
					@SuppressWarnings("unchecked")
					final Key<Object> key = (Key<Object>) pBindingBuilder.getTargetKey();
					if (key == null) {
						@SuppressWarnings("unchecked")
						final Provider<Object> provider = (Provider<Object>) pBindingBuilder.getTargetProvider();
						if (provider == null) {
							@SuppressWarnings("unchecked")
							final Supplier<Object> supplier = (Supplier<Object>) pBindingBuilder.getTargetSupplier();
							if (supplier == null) {
								final Type type = pBindingBuilder.getKey().getType();
								Class<?> cl = null;
								if (type instanceof Class) {
									cl = (Class<?>) type;
									if (cl.isAnnotation()  ||  cl.isEnum()  ||  cl.isArray()  ||  cl.isPrimitive()) {
										cl = (Class<?>) type;
									}
								}
								if (cl == null) {
									throw new IllegalStateException("Unable to create self-binding for the requested type " + type
											+ " for binding key=" + pBindingBuilder.getKey()
											+ ", please invoke either of the to* methods on the binding builder.");
								} else {
									final Class<?> clazz = cl;
									baseSupplier = (scf) -> scf.newInstance(clazz);
								}
							} else {
								baseSupplier = asInitializable(supplier, supplier);
							}
						} else {
							baseSupplier = asInitializable(provider, () -> provider.get());
						}
					} else {
						final Binding binding = getBinding(key);
						if (binding == null) {
							throw new NullPointerException("Binding not registered for key: " + pBindingBuilder.getTargetKey()); 
						}
						baseSupplier = (scf) -> {
							return binding.apply(scf);
						};
					}
				} else {
					baseSupplier = (scf) -> {
						return pBindingBuilder.getTargetInstance();
					};
				}
			} else {
				baseSupplier = (scf) -> scf.newInstance(pBindingBuilder.getTargetConstructor());
			}
		} else {
			baseSupplier = (scf) -> scf.newInstance(pBindingBuilder.getTargetClass());
		}
		final Scope scope = pBindingBuilder.getScope();
		if (scope == null  ||  scope == Scopes.NO_SCOPE) {
			return new NoScopeBinding(baseSupplier);
		} else if (scope == Scopes.SINGLETON) {
			return new SingletonBinding(baseSupplier);
		} else if (scope == Scopes.EAGER_SINGLETON) {
			return new EagerSingletonBinding(baseSupplier);
		} else {
			throw new IllegalStateException("Invalid scope: " + scope);
		}
	}

	public Binding find(Type pType, Annotation[] pAnnotations) {
		final BindingSet bindingSet = bindings.get(pType);
		if (bindingSet == null) {
			return null;
		} else {
			return bindingSet.find(pAnnotations);
		}
	}

	public Binding find(Key<Object> pKey) {
		final Type type = pKey.getType();
		final BindingSet bindingSet = bindings.get(type);
		if (bindingSet == null) {
			return null;
		} else {
			return bindingSet.find(pKey);
		}
	}

	@Override
	public void init(IComponentFactory pFactory) throws Exception {
		bindings.forEach((tp, bs) -> {
			bs.forEach((b) -> {
				if (b instanceof IComponentFactoryAware) {
					try {
						((IComponentFactoryAware) b).init(pFactory);
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
				}
			});
		});
	}
}
