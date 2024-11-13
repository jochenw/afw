package com.github.jochenw.afw.di.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.api.PropInject;

/** Abstract base class for implementing an {@link IOnTheFlyBinder}.
 */
public abstract class AbstractOnTheFlyBinder implements IOnTheFlyBinder {
	/** Creates a new instance. Private, to avoid accidental instantiation.
	 * This constructor might be removed, it is mainly present to avoid a Javadoc
	 * warning with JDK 21.
	 */
	protected AbstractOnTheFlyBinder() {}

	private static class IsInjectableException extends RuntimeException {
		private static final long serialVersionUID = 2820548098780229944L;
	}

	/** Returns, whether a value for the given field can be injected.
	 * @param pField The field, which is being tested.
	 * @return True, if a value can be injected. This is typically the
	 *   case, if the field is annotated with a suitable annotation,
	 *   like {@link LogInject}, or {@link PropInject}, and has a
	 *   suitable data type. Otherwise, returns false.
	 *   @see #getInjector(Field)
	 */
	protected abstract boolean isInjectable(Field pField);
	/** Returns, whether a value can be injected by invoking the#
	 *    given method.
	 * @param pMethod The method, which is being tested.
	 * @return True, if a value can be injected. This is typically the
	 *   case, if the method is annotated with a suitable annotation,
	 *   like {@link LogInject}, or {@link PropInject}, and has a
	 *   suitable parameter type. Otherwise, returns false.
	 */
	protected abstract boolean isInjectable(Method pMethod);
	/** If {@link #isInjectable(Field)} returns true, then
	 * this method will be invoked, to create an actual injector.
	 * @param pField The field, on which the created injector
	 *   must operate.
	 * @return An injector, which injects a value into the given
	 * field.
	 */
	protected abstract BiConsumer<@NonNull IComponentFactory,Object> getInjector(Field pField);
	/** If {@link #isInjectable(Method)} returns true, then
	 * this method will be invoked, to create an actual injector.
	 * @param pMethod The method, on which the created injector
	 *   must operate.
	 * @return An injector, which injects a value by invoking
	 *   the given method.
	 */
	protected abstract BiConsumer<IComponentFactory,Object> getInjector(Method pMethod);

	@Override
	public boolean isInjectable(Class<?> pClazz) {
		try {
			walk(pClazz, (cl) -> {
				for (Field field : cl.getDeclaredFields()) {
					if (isInjectable(field)) {
						throw new IsInjectableException();
					}
				}
				for (Method method : cl.getDeclaredMethods()) {
					if (isInjectable(method)) {
						throw new IsInjectableException();
					}
				}
			});
		} catch (IsInjectableException e) {
			return true;
		}
		return false;
	}

	private void walk(Class<?> pClass, Consumer<Class<?>> pConsumer) {
		Class<?> clazz = pClass;
		while (clazz != null  &&  clazz != Object.class) {
			pConsumer.accept(clazz);
			clazz = clazz.getSuperclass();
		}
	}

	@Override
	public BiConsumer<IComponentFactory, Object> getInjector(Class<?> pClass) {
		final List<BiConsumer<IComponentFactory,Object>> injectors = new ArrayList<>();
		walk(pClass, (cl) -> {
			for (Field field : cl.getDeclaredFields()) {
				if (isInjectable(field)) {
					final BiConsumer<@NonNull IComponentFactory, Object> injector = getInjector(field);
					if (injector == null) {
						throw new NullPointerException("isInjectable(Field) is true, bug getInjector(Field) returned null for " + field);
					}
					injectors.add(injector);
				}
			}
			for (Method method : cl.getDeclaredMethods()) {
				if (isInjectable(method)) {
					final BiConsumer<IComponentFactory, Object> injector = getInjector(method);
					if (injector == null) {
						throw new NullPointerException("isInjectable(Method) is true, but getInjector(Method) returned null for " + method);
					}
					injectors.add(injector);
				}
			}
		});
		return (cf,inst) -> injectors.forEach((bc) -> bc.accept(cf, inst));
	}

}
