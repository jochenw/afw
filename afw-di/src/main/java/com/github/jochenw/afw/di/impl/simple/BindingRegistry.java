package com.github.jochenw.afw.di.impl.simple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.di.api.ComponentFactoryBuilder;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.Scope;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.impl.BindingBuilder;
import com.github.jochenw.afw.di.util.Exceptions;


/** A binding registry is basically a map of types, as the keys, and a list of bindings,
 * that provide values of these types.
 */
public class BindingRegistry implements IComponentFactoryAware {
	private final ConcurrentMap<Type,BindingSet> bindings = new ConcurrentHashMap<>();

	/** Creates a new instance, that manages bindings for the given
	 * component factory.
	 * @param pFactory The component factory, that holds this registry.
	 * @param pBindingBuilders The binding builders, that have been configured on the
	 *   component factory.
	 */
	public BindingRegistry(SimpleComponentFactory pFactory, List<BindingBuilder<Object>> pBindingBuilders) {
		initBindings(pFactory, pBindingBuilders);
	}

	/** Initializes the binding registry by applying the given set of
	 * {@code BindingBuilder bindings}. 
	 * @param pFactory The binding registries component factory.
	 * @param pBindingBuilders The bindings, that are being applied.
	 */
	protected void initBindings(IComponentFactory pFactory, List<BindingBuilder<Object>> pBindingBuilders) {
		for (BindingBuilder<Object> bb : pBindingBuilders) {
			final @NonNull Key<Object> key = bb.getKey();
			final @NonNull Type type = key.getType();
			final BindingSet bindingSet = bindings.computeIfAbsent(type, (tp) -> {
				return new BindingSet(tp);
			});
			final Binding binding = asBinding(bb);
			final Key<Object> k;
			final Annotation annotation = bb.getAnnotation();
			if (annotation == null) {
				final Class<? extends Annotation> annotationType = bb.getAnnotationType();
				if (annotationType == null) {
					k = Key.of(type);
				} else {
					k = Key.of(type, annotationType);
				}
			} else {
				k = Key.of(type, annotation);
			}
			bindingSet.register(k, binding);
		}
	}

	/** Returns the binding with the given key, if any, or null.
	 * @param pKey The requested bindings key.
	 * @return The requested binding, if any, or null.
	 */
	protected Binding getBinding(Key<Object> pKey) {
		final BindingSet bs = bindings.get(pKey.getType());
		if (bs == null) {
			return null;
		}
		return bs.find(pKey);
	}

	/** Creates a function, which may be used as a supplier for
	 * an initialized object. Ensures, that the initialization
	 * will take place only once.
	 * @param pInitializableObject An envelope object, which acts as
	 * a holder for the supplied, initialized object. Synchronization
	 * on the envelope object will be used to make the created function
	 * thread safe.
	 * @param pSupplier A supplier, which creates the raw object, that
	 * needs synchronization.
	 * @return The created function.
	 */
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
	

	/** Converts the given {@link BindingBuilder binding builder},
	 * as supplied by the {@link ComponentFactoryBuilder component
	 * factory builder} into a {@link Binding binding}.
	 * @param pBindingBuilder The binding builder, that is being
	 *   converted.
	 * @return The created binding.
	 */
	protected Binding asBinding(BindingBuilder<Object> pBindingBuilder) {
		final Function<SimpleComponentFactory,Object> baseSupplier;
		if (pBindingBuilder.getTargetClass() == null) {
			if (pBindingBuilder.getTargetConstructor() == null) {
				if (pBindingBuilder.getTargetInstance() == null) {
					@SuppressWarnings("unchecked")
					final Key<Object> key = (Key<Object>) pBindingBuilder.getTargetKey();
					if (key == null) {
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

	/** Requests a binding for the given type, with the given annotations.
	 * @param pType Type of the field, or method parameter, that is being injected.
	 * @param pAnnotations Annotations of the field, or method parameter, that is being injected.
	 * @return The requested binding, if any, or null.
	 */
	public @Nullable Binding find(Type pType, Annotation[] pAnnotations) {
		final BindingSet bindingSet = bindings.get(pType);
		if (bindingSet == null) {
			return null;
		} else {
			return bindingSet.find(pAnnotations);
		}
	}

	/** Requests a binding for the given keys type, with the given keys annotations.
	 * @param pKey A key, that provides the type, and the annotations of the field, or method
	 * parameter, that is being injected.
	 * @return The requested binding, if any, or null.
	 */
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
