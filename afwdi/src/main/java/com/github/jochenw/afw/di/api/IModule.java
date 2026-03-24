package com.github.jochenw.afw.di.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;

/** A module is created by the component factories user. The purpose of the
 * module is the registration of bindings in the component factory. The
 * module does so by invoking either of the bind methods in the provided
 * {@link IBinder}.
 */
@FunctionalInterface
public interface IModule {
	/** The binder is being provided by the {@link ComponentFactoryBuilder}
	 * when invoking the modules {@link IModule#configure(IBinder)}
	 * method. The purpose of the binder is the creation of bindings, that
	 * are being registered with the created {@link IComponentFactory}.
	 */
	public interface IBinder {
		/** Creates a binding builder for creation, and registration,
		 * of a new binding. The binding will be registered under
		 * the given key.
		 * @param <T> The created bindings type.
		 * @param pKey The registered bindings key.
		 * @return The created binding builder, for further configuration of the
		 * binding.
		 */
		public <T> LinkableBindingBuilder<T> bind(Key<T> pKey);
		/** Creates a binding builder for creation, and registration,
		 * of a new binding. The binding will be registered under
		 * the given key.
		 * @param <T> The created bindings type.
		 * @param pType The created bindings type.
		 * @param pName The created bindings name.
		 * @return The created binding builder, for further configuration of the
		 * binding.
		 */
		public <T> AnnotatableBindingBuilder<T> bind(Type pType, String pName);
		/** Creates a binding builder for creation, and registration,
		 * of a new binding. The binding will be registered under
		 * the given key.
		 * @param <T> The created bindings type.
		 * @param pType The created bindings type.
		 * @return The created binding builder, for further configuration of the
		 * binding.
		 */
		public <T> AnnotatableBindingBuilder<T> bind(Type pType);
		/** Creates a binding builder for creation, and registration,
		 * of a new binding. The binding will be registered under
		 * the given key.
		 * @param <T> The created bindings type.
		 * @param pType The created bindings type.
		 * @param pName The created bindings name.
		 * @return The created binding builder, for further configuration of the
		 * binding.
		 */
		public default <T> AnnotatableBindingBuilder<T> bind(Types.Type<? extends T> pType, String pName) {
			final Types.Type<? extends T> type = Objects.requireNonNull(pType, "Type");
			return bind(type.getType(), pName);
		}
		/** Creates a binding builder for creation, and registration,
		 * of a new binding. The binding will be registered under
		 * the given key.
		 * @param <T> The created bindings type.
		 * @param pType The created bindings type.
		 * @return The created binding builder, for further configuration of the
		 * binding.
		 */
		public default <T> AnnotatableBindingBuilder<T> bind(Types.Type<? extends T> pType) {
			final Types.Type<? extends T> type = Objects.requireNonNull(pType, "Type");
			return bind(type.getType());
		}
		/** Creates a binding builder for creation, and registration,
		 * of a new binding. The binding will be registered under
		 * the given key.
		 * @param <T> The created bindings type.
		 * @param pType The created bindings type.
		 * @param pName The created bindings name.
		 * @return The created binding builder, for further configuration of the
		 * binding.
		 */
		public <T> AnnotatableBindingBuilder<T> bind(Class<? extends T> pType, String pName);
		/** Creates a binding builder for creation, and registration,
		 * of a new binding. The binding will be registered under
		 * the given key.
		 * @param <T> The created bindings type.
		 * @param pType The created bindings type.
		 * @return The created binding builder, for further configuration of the
		 * binding.
		 */
		public <T> AnnotatableBindingBuilder<T> bind(Class<? extends T> pType);
		/** Adds a finalizer. The finalizer will be invoked, when the the
		 * {@link IComponentFactory} is fully configured. For example,
		 * this will be used to perform eager instantiation on bindings
		 * with scope {@link Scopes#EAGER_SINGLETON}.
		 * @param pFinalizer The finalizer, which is being invoked later.
		 */
		public void addFinalizer(Consumer<IComponentFactory> pFinalizer);
		/** Request that static fields, and methods should be injected in
		 * the given classes.
		 * @param pClasses The set of classes, for which static injection
		 *   should be enabled.
		 */
		void staticInjection(Class<?>... pClasses);
	}
	/** Interface of a binding builder, for which the key is only temporary,
	 * and may be adjusted by providing an annotation, or an annotation name.
	 * @param <T> Type of the binding, which is being registered.
	 */
	public interface AnnotatableBindingBuilder<T> extends LinkableBindingBuilder<T> {
		/** Alters the created bindings temporary key by adding an annotation type.
		 * The altered key will have the same type, and name, the given annotation
		 * type, and no annotation.
		 * @param pAnnotationType The annotation type.
		 * @return The altered key.
		 */
		public LinkableBindingBuilder<T> annotatedWith(Class<? extends Annotation> pAnnotationType);
		/** Alters the created bindings temporary key by adding an annotation.
		 * The altered key will have the same type, and name, no annotation
		 * type, and the given annotation.
		 * @param pAnnotation The altered keys annotation.
		 * @return The altered key.
		 */
		public LinkableBindingBuilder<T> annotatedWith(Annotation pAnnotation);
		/** Alters the created bindings temporary key by adding a name.
		 * The altered key will have the same type, the given name,
		 * and no annotation, or annotation type.
		 * @param pName The altered keys name.
		 * @return The altered key.
		 */
		public LinkableBindingBuilder<T> named(String pName);
	}
	/** Interface of a binding builder, which needs a supplier.
	 * @param <T> Type of the binding, which is being registered.
	 */
	public interface LinkableBindingBuilder<T> extends ScopableBindingBuilder<T> {
		/** Sets the supplier of the created binding.
		 * @param pSupplier The supplier, which is being used.
		 * @return A binding builder for further configuration of the scope.
		 */
		public ScopableBindingBuilder<T> to(ISupplier<? extends T> pSupplier);
		/** Sets the supplier of the created binding.
		 * @param pSupplier The supplier, which is being used.
		 * @return A binding builder for further configuration of the scope.
		 */
		public ScopableBindingBuilder<T> toSupplier(Supplier<? extends T> pSupplier);
		/** Creates a supplier for the binding, which is being registered.
		 * The created supplier will instantiate the given implementation class.
		 * @param pImplType The implementation class, which is being instantiated.
		 * @return A binding builder for further configuration of the scope.
		 */
		public ScopableBindingBuilder<T> toClass(Class<? extends T> pImplType);
		/** Creates a supplier for the binding, which is being registered.
		 * The created supplier will invoke the given constructor to
		 * create the instance, that the binding returns.
		 * @param pConstructor The constructor, which is being invoked.
		 * @return A binding builder for further configuration of the scope.
		 */
		public ScopableBindingBuilder<T> toConstructor(Constructor<? extends T> pConstructor);
		/** Creates a supplier for the binding, which is being registered.
		 * The created supplier will simply return the given instance. As a
		 * side effect, this also specifies the bindings scope to be
		 *   {@link Scopes#SINGLETON}.
		 * @param pInstance The instance, which will be returned by the created
		 *   supplier.
		 */
		public default void toInstance(T pInstance) {
			toSupplier(() -> pInstance).in(Scopes.SINGLETON);
		}
		/** Creates a supplier for the binding, which is being registered.
		 * The created supplier will invoke the given function, and
		 * return the result.
		 * @param pFunction The function, which will be invoked by the
		 *   created supplier.
		 */
		public default void toFunction(Function<IComponentFactory,? extends T> pFunction) {
			to((ISupplier<? extends T>) (cf) -> pFunction.apply(cf));
		}
	}
	/** Interface of a binding builder, which needs a scope. (The
	 * default scope can be applied, if no scoping
	 * method is being invoked.
	 * @param <T> Type of the binding, which is being registered.
	 */
	public interface ScopableBindingBuilder<T> {
		/** Called to set the scope of the registered binding to the given.
		 * @param pScope The scope of the binding, which is being created.
		 */
		public void in(Scopes.Scope pScope);
		/** Called to set the scope of the registered binding to 
		 * {@link Scopes#SINGLETON}.
		 */
		public default void asSingleton() { in(Scopes.SINGLETON); }
		/** Called to set the scope of the registered binding to 
		 * {@link Scopes#EAGER_SINGLETON}.
		 */
		public default void asEagerSingleton() { in(Scopes.EAGER_SINGLETON); }
		/** Called to set the scope of the registered binding to 
		 * {@link Scopes#NO_SCOPE}.
		 */
		public default void asUnscoped() { in(Scopes.NO_SCOPE); }
	}

	/** Called to perform the registration of bindings by invoking the
	 * given binders bind methods.
	 * @param pBinder The binder, which is being invoked by the module
	 *   to register bindings.
	 */
	public void configure(IBinder pBinder);

	/** Returns a module, which internally invokes this modules
	 * {@link #configure(IBinder)} method first, and then the
	 * given modules. By doing so, the given module is
	 * effectively overriding this module.
	 * @param pModule The extending module, which is overriding this
	 *   one. May be null, in which case the returned module is
	 *   simply the current.
	 * @return The created module.
	 */
	public default IModule extend(IModule pModule) {
		if (pModule == null) {
			return this;
		} else {
			final IModule current = this;
			return (b) -> {
				current.configure(b);
				pModule.configure(b);
			};
		}
	}

	/** Returns a module, which invokes this modules
	 * {@link #configure(IBinder)} method first, and then the
	 * all of given modules configure methods. By doing so,
	 * the given module is effectively overriding this module.
	 * @param pModules The extending modules, with increasing
	 * order (later modules may be overriding previous ones).
	 * @return The created module.
	 */
	public default IModule extend(IModule... pModules) {
		final IModule[] modules = Objects.requireNonNull(pModules, "Modules");
		if (modules.length == 0) {
			return this;
		} else {
			return (b) -> {
				IModule.this.configure(b);
				for (IModule module : modules) {
					if (module != null) {
						module.configure(b);
					}
				}
			};
		}
	}

	/** Returns a module, which invokes this modules
	 * {@link #configure(IBinder)} method first, and then the
	 * all of given modules configure methods. By doing so,
	 * the given module is effectively overriding this module.
	 * @param pModules The extending modules, with increasing
	 * order (later modules may be overriding previous ones).
	 * @return The created module.
	 */
	public default IModule extend(Iterable<IModule> pModules) {
		final Iterable<IModule> modules = Objects.requireNonNull(pModules, "Modules");
		if (modules.iterator().hasNext()) {
			return (b) -> {
				IModule.this.configure(b);
				for (IModule module : modules) {
					if (module != null) {
						module.configure(b);
					}
				}
			};
		} else {
			return this;
		}
	}
}
