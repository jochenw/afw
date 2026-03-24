package com.github.jochenw.afw.di.api;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;

/** Interface of an object, that recognizes inject-related annotations of
 * a given type.
 */
public interface IAnnotationProvider {
	/** Returns true, if the given method, constructor, or
	 * field is annotated with an @Inject annotation.
	 * @param pObject The method, constructor, or field,
	 * which is being tested.
	 * @return True, if the given object has an
	 *   @Inject annotation.
	 */
	public boolean isInjectable(AccessibleObject pObject);

	/** If the given method, method parameter, constructor,
	 * or field, is annotated with an @Named annotation:
	 * Returns the value of the annotation.
	 * Otherwise, returns null.
	 * @param pObject The method, constructor, or field,
	 * which is being tested.
	 * @return The value of the given objects @Named
	 *   annotation, if any, or null.
	 */
	public String getNamedValue(AnnotatedElement pObject);

	/** If the given type is a valid provider type, create a valid provider
	 * instance by using the given {@link ISupplier} to obtain the
	 * providers result. If not, return null.
	 * @param pProviderType Type of the created provider.
	 * @param pSupplier The supplier, which is being used
	 *   internally by the created provider.
	 * @return A supplier, which returns the created provider.
	 */
	public ISupplier<Object> getProvider(Type pProviderType, ISupplier<Object> pSupplier);

	/** Returns true, if the given method, or field, is
	 * annotated with @PostConstruct.
	 * @param pObject The method, or field, which is being tested.
	 * @return True, if a @PostConstruct annotation is present on the
	 *   given object, otherwise false.
	 */
	public boolean isAnnotatedWithPostConstruct(AccessibleObject pObject);

	/** Returns true, if the given method, or field, is
	 * annotated with @PreDestroy.
	 * @param pObject The method, or field, which is being tested.
	 * @return True, if a @PostConstruct annotation is present on the
	 *   given object, otherwise false.
	 */
	public boolean isAnnotatedWithPreDestroy(AccessibleObject pObject);
}
