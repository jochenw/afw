package com.github.jochenw.afw.di.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactory.IBinding;
import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;
import com.github.jochenw.afw.di.api.IModule.AnnotatableBindingBuilder;
import com.github.jochenw.afw.di.api.IModule.IBinder;
import com.github.jochenw.afw.di.api.IModule.LinkableBindingBuilder;
import com.github.jochenw.afw.di.api.IModule.ScopableBindingBuilder;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.api.Scopes.Scope;


/** Implementation of {@link IBinder}.
 */
public class BinderImpl implements IBinder {
	/** Implementation of {@link AnnotatableBindingBuilder},
	 * {@link LinkableBindingBuilder}, and
	 * {@link ScopableBindingBuilder}.
	 */
	public static class BindingBuilder implements AnnotatableBindingBuilder<Object> {
		private Key<Object> key;
		private ISupplier<Object> supplier;
		private Scopes.Scope scope;
		private boolean applied;
		private Class<Object> selfBindingClass;

		/** Creates a new instance with the given key.
		 * @param pKey The builders temporary key.
		 */
		public BindingBuilder(Key<Object> pKey) {
			key = pKey;
		}

		@Override
		public ScopableBindingBuilder<Object> to(ISupplier<? extends Object> pSupplier) {
			@SuppressWarnings("unchecked")
			final ISupplier<Object> supplier = (ISupplier<Object>) Objects.requireNonNull(pSupplier, "Supplier");
			this.supplier = supplier;
			return this;
		}

		@Override
		public ScopableBindingBuilder<Object> toSupplier(Supplier<? extends Object> pSupplier) {
			@SuppressWarnings("unchecked")
			final Supplier<Object> supplier = (Supplier<Object>) Objects.requireNonNull(pSupplier, "Supplier");
			return to((ISupplier<Object>) (cf) -> supplier.get());
		}

		@Override
		public ScopableBindingBuilder<Object> toClass(Class<? extends Object> pImplType) {
			@SuppressWarnings("unchecked")
			final Class<Object> cl = (Class<Object>) Objects.requireNonNull(pImplType);
			final Function<IComponentFactory,Object> function =
					DiUtils.deferredSupplier((cf) -> () -> cf.getInstantiator(cl).get());
			return to((cf) -> function.apply(cf));
		}

		@Override
		public ScopableBindingBuilder<Object> toConstructor(Constructor<? extends Object> pConstructor) {
			@SuppressWarnings("unchecked")
			final Constructor<Object> cons = (Constructor<Object>) Objects.requireNonNull(pConstructor);
			final Function<IComponentFactory,Object> function =
					DiUtils.deferredSupplier((cf) -> () -> cf.getInstantiator(cons));
			return to((cf) -> function.apply(cf));
		}

		@Override
		public void in(Scope pScope) {
			scope = pScope;
		}

		@Override
		public LinkableBindingBuilder<Object> annotatedWith(Class<? extends Annotation> pAnnotationType) {
			final Class<? extends Annotation> annotationType = Objects.requireNonNull(pAnnotationType, "AnnotationType");
			if (key.getAnnotation() != null  ||  key.getAnnotationType() != null) {
				throw new IllegalStateException("The binding builder is already annotated.");
			}
			key = Key.of(key.getType(), key.getName(), annotationType, null);
			return this;
		}

		@Override
		public LinkableBindingBuilder<Object> annotatedWith(Annotation pAnnotation) {
			final Annotation annotation = Objects.requireNonNull(pAnnotation, "Annotation");
			if (key.getAnnotation() != null  ||  key.getAnnotationType() != null) {
				throw new IllegalStateException("The binding builder is already annotated.");
			}
			key = Key.of(key.getType(), key.getName(), null, annotation);
			return this;
		}

		@Override
		public LinkableBindingBuilder<Object> named(String pName) {
			final String name = Objects.requireNonNull(pName, "Name");
			key = Key.of(key.getType(), name, key.getAnnotationType(), key.getAnnotation());
			return this;
		}
		
	}

	private final Scope defaultScope;
	private final Map<Key<Object>,IBinding<Object>> bindings = new HashMap<>();
	private final List<BindingBuilder> builderList = new ArrayList<>();
	private final List<Consumer<IComponentFactory>> finalizers = new ArrayList<>();
	private Set<Class<?>> staticInjectionClasses;

	/** Creates a new instance with the given default scope.
	 * @param pDefaultScope The default scope.
	 */
	public BinderImpl(Scope pDefaultScope) {
		defaultScope = Objects.requireNonNull(pDefaultScope, "DefaultScope");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> LinkableBindingBuilder<T> bind(Key<T> pKey) {
		final Key<Object> key = (Key<Object>) Objects.requireNonNull(pKey, "Key");
		final BindingBuilder builder = new BindingBuilder(key);
		builderList.add(builder);
		return (LinkableBindingBuilder<T>) builder;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> AnnotatableBindingBuilder<T> bind(Type pType, String pName) {
		final Type type = Objects.requireNonNull(pType, "Type");
		final String name = Objects.requireNonNull(pName, "Name");
		final Key<Object> key = Key.of(type, name);
		final AnnotatableBindingBuilder<T> builder = (AnnotatableBindingBuilder<T>) bind(key);
		return builder;
		
	}

	@Override
	public <T> AnnotatableBindingBuilder<T> bind(Type pType) {
		return bind(pType, "");
	}

	@Override
	public <T> AnnotatableBindingBuilder<T> bind(Class<? extends T> pType, String pName) {
		return bind((Type) pType, pName);
	}

	@Override
	public <T> AnnotatableBindingBuilder<T> bind(Class<? extends T> pType) {
		@SuppressWarnings("unchecked")
		final Class<Object> type = (Class<Object>) Objects.requireNonNull(pType, "Type");
		final AnnotatableBindingBuilder<T> abb = bind((Type) pType);
		if (!pType.isAnnotation()
			&&  !pType.isArray()
			&&  !pType.isEnum()
			&&  !pType.isInterface()) {
			((BindingBuilder) abb).selfBindingClass = type;
		}
		return abb;
	}

	@Override
	public void addFinalizer(Consumer<IComponentFactory> pFinalizer) {
		finalizers.add(Objects.requireNonNull(pFinalizer, "Finalizer"));
	}

	@Override
	public void staticInjection(Class<?>... pClasses) {
		if (staticInjectionClasses == null) {
			staticInjectionClasses = new HashSet<>();
		}
		staticInjectionClasses.addAll(Arrays.asList(pClasses));
	}

	/** Called to validate, and apply all of the binders binding builders-
	 * Afterwards, the methods {@link #getBindings()}, and
	 * {@link #getFinalizers()} may be invoked.
	 */
	public void validate() {
		for (BindingBuilder builder : builderList) {
			validate(builder);
		}
	}

	/** Called to validate the given binding builder.
	 * If the builder is found to be valid, it will also
	 * be added to the map of bindings.
	 * @param pBuilder The binding builder, which is being applied.
	 */
	protected void validate(BindingBuilder pBuilder) {
		if (!pBuilder.applied) {
			final Key<Object> key = Objects.requireNonNull(pBuilder.key, "Key");
			final ISupplier<Object> supplier;
			if (pBuilder.supplier == null) {
				if (pBuilder.selfBindingClass == null) {
					throw new IllegalStateException("The binding for key " + key
							+ " is not linked to any target, and no self-binding is possible."
							+ " (Neither of the methods to(ISupplier), toClass(Class),"
							+ " toConstructor(Constructor). toInstance(Object), or toSupplier(Supplier)"
							+ " was invoked on the binding builder.");
				} else {
					pBuilder.toClass(pBuilder.selfBindingClass);
					supplier = Objects.requireNonNull(pBuilder.supplier);
				}
			} else {
				supplier = pBuilder.supplier;
			}
			final Scope scope = pBuilder.scope == null ? defaultScope : pBuilder.scope;
			final ISupplier<Object> scopedSupplier;
			if (scope == Scopes.SINGLETON  ||  scope == Scopes.EAGER_SINGLETON) {
				scopedSupplier = new SingletonSupplier<Object>(supplier);
			} else if (scope == Scopes.NO_SCOPE) {
				scopedSupplier = new NoScopeSupplier<Object>(supplier);
			} else {
				throw new IllegalStateException("Invalid scope: Expected SINGLETON, EAGER_SINGLETON, or NO_SCOPE, got " + scope);
			}
			final IBinding<Object> binding = IBinding.of(key, scopedSupplier, scope); 
			bindings.put(key, binding);
			if (scope == Scopes.EAGER_SINGLETON) {
				addFinalizer((cf) -> cf.getInstance(key));
			}
			pBuilder.applied = true;
		}
	}

	/** Implementation of {@link ISupplier} for {@link Scopes#NO_SCOPE}.
	 * @param <T> Type of the supplied instance.
	 */
	public static class NoScopeSupplier<T> implements ISupplier<T> {
		private final ISupplier<T> actualSupplier;

		/** Creates a new instance with the given actual supplier.
		 * @param pActualSupplier The actual supplier.
		 */
		public NoScopeSupplier(ISupplier<T> pActualSupplier) {
			actualSupplier = pActualSupplier;
		}
		@Override
		public T apply(IComponentFactory pCf) {
			final T t = actualSupplier.apply(pCf);
			pCf.init(t);
			return t;
		}
	}
	/** Implementation of {@link ISupplier} for {@link Scopes#SINGLETON},
	 * and {@link Scopes#EAGER_SINGLETON}.
	 * @param <T> Type of the supplied instance.
	 */
	public static class SingletonSupplier<T> implements ISupplier<T> {
		private final ISupplier<T> actualSupplier;
		private boolean instanceValid;
		private T instance;

		/** Creates a new instance with the given actual supplier.
		 * @param pActualSupplier The actual supplier.
		 */
		public SingletonSupplier(ISupplier<T> pActualSupplier) {
			actualSupplier = pActualSupplier;
		}

		@Override
		public T apply(IComponentFactory pCf) {
			final T t;
			synchronized(this) {
				if (!instanceValid) {
					final T t2 = actualSupplier.apply(pCf);
					pCf.init(t2);
					t = instance = t2;
					instanceValid = true;
				} else {
					t = instance;
				}
			}
			return t;
		}
	}
	
	/** Returns the map of bindings, that have been configured.
	 * @return The map of bindings, that have been configured.
	 */
	public Map<Key<Object>,IBinding<Object>> getBindings() { return bindings; }

	/** Returns the list of finalizers.
	 * @return The list of finalizers.
	 */
	public List<Consumer<IComponentFactory>> getFinalizers() { return finalizers; }

	/** Returns the set of classes, for which static injection
	 * has been enabled by invocations of {@link #staticInjection(Class...)}.
	 * @return The set of classes, for which static injection
	 *   has been enabled.
	 */
	public Set<Class<?>> getStaticInjectionClasses() {
		if (staticInjectionClasses == null) {
			return Collections.emptySet();
		}
		return staticInjectionClasses;
	}
}
