package com.github.jochenw.afw.di.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.jochenw.afw.di.api.IComponentFactory.IBinding;

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

}
