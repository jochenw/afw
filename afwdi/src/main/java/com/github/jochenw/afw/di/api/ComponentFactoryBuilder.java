package com.github.jochenw.afw.di.api;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory.Configuration;
import com.github.jochenw.afw.di.impl.BinderImpl;
import com.github.jochenw.afw.di.impl.DefaultAnnotationProvider;
import com.github.jochenw.afw.di.impl.DiUtils;

/** A builder for instances of {@link IComponentFactory}.
 * @param <T> Type of the created component factory.
 */
public class ComponentFactoryBuilder<T extends AbstractComponentFactory> {
	private final Class<T> type;
	private IAnnotationProvider annotationProvider;
	private boolean immutable;
	private List<IModule> modules;
	private Scopes.Scope defaultScope;
	private IComponentFactory parent;

	/** Called by methods, which change the builders state, to ensure,
	 * that changing the state is still valid.
	 */
	protected void assertMutable() {
		if (immutable) {
			throw new IllegalStateException("This builder is no longer mutable.");
		}
	}

	/** Called, if the builder may no longer change its state.
	 */
	protected void makeImmutable() {
		assertMutable();
		immutable = true;
	}

	/** Creates a new instance with the given type.
	 * @param pType The type of component factory, that this builder creates.
	 */
	public ComponentFactoryBuilder(Class<T> pType) {
		type = pType;
	}


	/** Returns the builders {@link IAnnotationProvider}, which will
	 * be used by the created component factory.
	 * @return The component factories [@link {@link IAnnotationProvider};
	 *   never null, as there is a {@link DefaultAnnotationProvider#getInstance()
	 *   default value}.
	 */
	public IAnnotationProvider getAnnotationProvider() {
		if (annotationProvider == null) {
			return DefaultAnnotationProvider.getInstance();
		} else {
			return annotationProvider;
		}
	}

	/** Returns the default scope. Never null, because
	 * {@link Scopes#SINGLETON} acts as the default value.
	 * @return The default scope.
	 */
	public Scopes.Scope getDefaultScope() {
		if (defaultScope == null) {
			return Scopes.SINGLETON;
		} else {
			return defaultScope;
		}
	}

	/** Returns the list of {@link IModule modules}, which are
	 * being used to configure the component factory bindings.
	 * @return The list of {@link IModule modules}, which are
	 *   being used to configure the component factory bindings.
	 */
	public List<IModule> getModules() {
		if (modules == null) {
			return Collections.emptyList();
		} else {
			return modules;
		}
	}

	/** Returns the component factories parent, or null. If a
	 * parent is present, then the created component factory
	 * inherits all of the parents bindings, but the created
	 * factories bindings will take precedence.
	 * @return The component factories parent, or null.
	 */
	public IComponentFactory getParent() { return parent; }
	
	/** Sets the builders {@link IAnnotationProvider}, which will
	 * be used by the created component factory.
	 * @param pAnnotationProvider The annotation provider, which
	 *   is being used; may be nukk, in which case the
	 *   {@link DefaultAnnotationProvider#getInstance() default
	 *   value} will be used.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder<T> annotationProvider(IAnnotationProvider pAnnotationProvider) {
		assertMutable();
		annotationProvider = pAnnotationProvider;
		return this;
	}

	/** Sets the default scope.
	 * @param pScope The new default scope. May be null, in which
	 *   case {@link Scopes#SINGLETON} will be used.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder<T> defaultScope(Scopes.Scope pScope) {
		assertMutable();
		defaultScope = pScope;
		return this;
	}
	/** Adds the given {@link IModule} to the list of modules, which are being
	 * used to configure the bindings.
	 * @param pModule The module, which is being added.
	 * @return This builder.
	 * @throws NullPointerException The parameter {@code pModule} is null.
	 */
	public ComponentFactoryBuilder<T> module(IModule pModule) {
		final IModule module = Objects.requireNonNull(pModule, "Module");
		assertMutable();
		if (modules == null) {
			modules = new ArrayList<>();
		}
		modules.add(module);
		return this;
	}

	/** Adds the given {@link IModule modules} to the list of modules, which are being
	 * used to configure the bindings.
	 * @param pModules The modules, which are being added.
	 * @return This builder.
	 * @throws NullPointerException The parameter {@code pModule} is null.
	 */
	public ComponentFactoryBuilder<T> modules(IModule... pModules) {
		final IModule[] modules = Objects.requireNonNull(pModules, "Modules");
		assertMutable();
		for (IModule module : modules) {
			module(module);
		}
		return this;
	}

	/** Adds the given {@link IModule modules} to the list of modules, which are being
	 * used to configure the bindings.
	 * @param pModules The modules, which are being added.
	 * @return This builder.
	 * @throws NullPointerException The parameter {@code pModule} is null.
	 */
	public ComponentFactoryBuilder<T> modules(Iterable<IModule> pModules) {
		final Iterable<IModule> modules = Objects.requireNonNull(pModules, "Modules");
		assertMutable();
		for (IModule module : modules) {
			module(module);
		}
		return this;
	}

	/** Sets the component factories parent, or null. If a
	 * parent is present, then the created component factory
	 * inherits all of the parents bindings, but the created
	 * factories bindings will take precedence.
	 * @param pParent The component factories parent, or null.
	 * @return This builder.
	 */
	public ComponentFactoryBuilder<T> getParent(IComponentFactory pParent) {
		if (pParent != null  &&
			!type.isAssignableFrom(pParent.getClass())) {
			throw new IllegalArgumentException("The parents type is "
					+ pParent.getClass().getName()
					+ ", but the expected type is "
					+ type.getName());
		}
		assertMutable();
		parent = pParent;
		return this;
	}
		

	/** Creates the requested {@link IComponentFactory}, applying the
	 * bindings, as configured by the modules.
	 * @return The created component factory.
	 */
	public T build() {
		makeImmutable();
		final BinderImpl binder = new BinderImpl(getDefaultScope());
		for (IModule module : getModules()) {
			module.configure(binder);
		}
		/** Finally, as a non-overwritable binding, create a binding
		 * for the component factory itself.
		 */
		binder.bind(IComponentFactory.class).to((cf) -> cf).asSingleton();
		binder.validate();
		final Configuration configuration =
				new Configuration(
						binder.getBindings(),
						getAnnotationProvider(),
						getDefaultScope(),
						getParent());
		final T acf;
		try {
			final Class<? extends AbstractComponentFactory> cfType = (Class<? extends AbstractComponentFactory>) type;
			final Constructor<? extends AbstractComponentFactory> cons = cfType.getConstructor(Configuration.class);
			@SuppressWarnings("unchecked")
			final T t = (T) cons.newInstance(configuration);
			acf = t;
		} catch (Throwable t) {
			throw DiUtils.show(t);
		}
		for (Consumer<IComponentFactory> finalizer : binder.getFinalizers()) {
			finalizer.accept(acf);
		}
		return acf;
	}
}
