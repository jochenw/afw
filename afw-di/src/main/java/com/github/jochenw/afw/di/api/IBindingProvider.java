package com.github.jochenw.afw.di.api;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import com.github.jochenw.afw.di.impl.DiUtils;


/** The binding provider is a source for bindings, possibly
 * dynamically generated. It is mainly used to support
 * additional annotations, like {@link LogInject},
 * {@link PropInject}, or @PostConstruct.
 */
public interface IBindingProvider {
	/** Returns true, if the binding provider can create an injector for the
	 * given field. If so, the component factory will invoke
	 * {@link #createInjector(IComponentFactory, Field)}.
	 * @param pField The field, for which an injector is requested.
	 * @return True, if an injector can be created for the given field.
	 */
	public boolean isInjectable(Field pField);

	/** Called to create an injector for the given field. The
	 * component factory may invoke this <em>only</em>, if
	 * {@link #isInjectable(Field)} has been invoked before,
	 * and the result was true.
	 * @param pComponentFactory The component factory, as a
	 *   source for bindings.
	 * @param pField The field, for which an injector is being
	 *   generated.
	 * @return The generated injector; never null.
	 */
	public BiConsumer<IComponentFactory, Object> createInjector(IComponentFactory pComponentFactory, Field pField);

	/** Returns true, if the binding provider can create an injector for the
	 * given method. If so, the component factory will invoke
	 * {@link #createInjector(IComponentFactory, Field)}.
	 * @param pMethod The field, for which an injector is requested.
	 * @return True, if an injector can be created for the given field.
	 */
	public boolean isInjectable(Method pMethod);

	/** Called to create an injector for the given field. The
	 * component factory may invoke this <em>only</em>, if
	 * {@link #isInjectable(Field)} has been invoked before,
	 * and the result was true.
	 * @param pComponentFactory The component factory, as a
	 *   source for bindings.
	 * @param pMethod The method, for which an injector is being
	 *   generated.
	 * @return The generated injector; never null.
	 */
	public BiConsumer<IComponentFactory, Object> createInjector(IComponentFactory pComponentFactory, Method pMethod);

	/** Called to ensure, that {@link AccessibleObject#isAccessible()}
	 * returns true for the given field, or method.
	 * @param pObject The field, or method, which should be made
	 * accessible.
	 */
	@SuppressWarnings({ "deprecation", "javadoc" })
	static void assertAccessible(AccessibleObject pObject) {
		if (!pObject.isAccessible()) {
			pObject.setAccessible(true);
		}
	}

	/** Called to invoke the given method, passing the given values.
	 * @param pMethod The method, which is being invoked.
	 * @param pInstance The instance, which is holding the field.
	 * @param pValues The method invocation parameters.
	 */
	static void invoke(Method pMethod, Object pInstance, Object... pValues) {
		IBindingProvider.assertAccessible(pMethod);
		try {
			pMethod.invoke(pInstance, pValues);
		} catch (Exception e) {
			throw DiUtils.show(e);
		}
	}

	/** Called to modify the given field by setting it's value to
	 * the given.
	 * @param pField The field, which is being updated.
	 * @param pInstance The instance, which is holding the field.
	 * @param pValue The new field value.
	 */
	static void set(Field pField, Object pInstance, Object pValue) {
		IBindingProvider.assertAccessible(pField);
		try {
			pField.set(pInstance, pValue);
		} catch (Exception e) {
			throw DiUtils.show(e);
		}
	}

}
