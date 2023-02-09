package com.github.jochenw.afw.di.api;

import java.lang.reflect.Constructor;
import java.util.function.Supplier;




/** A binding builder, which may be linked to a particular supplier.
 * The supplier is being used to acquire the object, which is being injected by
 * the binding.
 * @param <T> Type of the binding, and of the injected object.
 */
public interface LinkableBindingBuilder<T> extends ScopableBindingBuilder {
	/** Requests, that the injected object is being created by instantiating
	 * the given class.
	 * @param pImplementation The class, that is being instantiated. This class
	 * must not be abstract, and it must have a public default constructor, or
	 * a constructor, that is annotated with \@Inject.
	 * @return A binding builder without scope, but with supplier.
	 * @see #toClass(Class)
	 */
	ScopableBindingBuilder to(Class<? extends T> pImplementation);
	/** Requests, that the injected object is being created by instantiating
	 * the given class.
	 * @param pImplementation The class, that is being instantiated. This class
	 * must not be abstract, and it must have a public default constructor, or
	 * a constructor, that is annotated with \@Inject.
	 * @return A binding builder without scope, but with supplier.
	 * @see #to(Class)
	 */
	ScopableBindingBuilder toClass(Class<? extends T> pImplementation);
	/** Requests, that the injected object is being created by
	 * referencing the given {@link Key key}.
	 * @param pKey The key, which is being used to acquire the injected object.
	 * @return A binding builder without scope, but with supplier.
	 */
	ScopableBindingBuilder to(Key<? extends T> pKey);
	/** Requests, that the injected object should be the given instance.
	 * The bindings scope is implicitly specified as {@link Scopes#SINGLETON}.
	 * @param pInstance The instance, which is being injected by this
	 * binding.
	 */
	void toInstance(T pInstance);
	/** Requests, that the injected object is given by an invocation of the
	 * given {@link Supplier}.
	 * @param pSupplier The provider, which is supplying the injected object.
	 * @return A binding builder without scope, but with supplier.
	 */
	ScopableBindingBuilder toSupplier(Supplier<? extends T> pSupplier);
	/** Requests, that the injected object is being created by invoking the
	 * given constructor.
	 * @param pConstructor The constructor, that is being invoked to create
	 *   the injected object.
	 * @param <S> Type, that is created by the constructor.
	 * @return A binding builder without scope, but with supplier.
	 */
    <S extends T> ScopableBindingBuilder toConstructor(Constructor<S> pConstructor);
}
