package com.github.jochenw.afw.di.api;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.Function;


/**
 * An on-the-fly binder customizes the {@link IComponentFactory} by adding bindings
 * dynamically. Typically, this is being used by code, that handles non-standard
 * annotations, like {@link PropInject}, or {@link LogInject}. Within AFW DI, this is also used
 * to handle standard annotations, like {@code PostConstruct}, or {@code PreDestroy}.
 */
public interface IOnTheFlyBinder {
	/** Returns, whether the {@link IOnTheFlyBinder} wishes to inject values into objects
	 * of the given type. If so, the caller is supposed to invoke {@link #getInjector(Class)}
	 * afterwards.
	 * @param pClazz The instance class, that is being tested for custom injections. Typically,
	 *   the on-the-fly binder will check, whether the class contains annotated fields, or
	 *   methods.
	 * @return True, if the {@link IOnTheFlyBinder} can inject values into objects of the
	 *   given type.
	 */
	boolean isInjectable(Class<?> pClazz);
	/** Called to create an object (the {@code injector}, that injects values into
	 * instances of the given type. This will only be invoked, if {@link #isInjectable(Class)}
	 * has returned true for the same type before.
	 * 
	 * Typically, the on-the-fly binder will check, whether the class contains annotated fields, or
	 * methods, and generate code, that sets these fiels, or invokes these methods.
	 * @param pClass The instance class, for which an injector is requested.
	 * @return The created injector.
	 */
	BiConsumer<IComponentFactory,Object> getInjector(Class<?> pClass);
	/** Returns, whether the on-the-fly binder wishes to create instances of the given type,
	 * which has the given annotations.
	 * If that is the case, the method {@link #getInstantiator(Type, Annotation[])}
	 * will be invoked afterwards. Otherwise, the instance will be created by finding a
	 * suitable constructor.
	 * @param pType Type of the object, that is being created.
	 * @param pAnnotations Annotations of the field, or method parameter, for which a
	 *   value is being created.
	 * @return True, if the on-the-fly binder can create a matching instantiator,
	 *   otherwise false.
	 */
	boolean isInstantiable(Type pType, Annotation[] pAnnotations);
	/** Called to create an object (the {@code instantiator}, that creates
	 * instances of the given type. This will only be invoked, if {@link #isInjectable(Class)}
	 * has returned true for the same type before.
	 * 
	 * Typically, the on-the-fly binder will check, whether the class contains annotated constructors,
	 * and generate code, that sets invokes these constructors using suitable values.
	 * @param pType Type of the instance, that is being created.
	 * @param pAnnotations Annotations of the field, or method parameter, for which a
	 *   value is being created.
	 * @return The created instantiator.
	 */
	Function<IComponentFactory, Object> getInstantiator(Type pType, Annotation[] pAnnotations);
}
