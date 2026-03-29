package com.github.jochenw.afw.di.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.jochenw.afw.di.api.ILifecycleController.TerminableListener;
import com.github.jochenw.afw.di.impl.AbstractBindingProvider;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory.Configuration;
import com.github.jochenw.afw.di.impl.DiUtils;
import com.github.jochenw.afw.di.impl.LogInjectBindingProvider;
import com.github.jochenw.afw.di.impl.PropInjectBindingProvider;


/**| (propInjectBindingProvider != null  &&  propInjectBindingProvider.isInjectable(p
 * This binding provider implements support for @PostConstruct, @PreDestroy,
 * {@link LogInject}, and {@link PropInject}.
 * @param <L> Type of the logger.
 * @param <P> Type of the property instance.
 */
public class DefaultBindingProvider<L,P> extends AbstractBindingProvider {
	private LogInjectBindingProvider<L> logInjectBindingProvider;
	private PropInjectBindingProvider<P> propInjectBindingProvider;
    private ILifecycleController lifeCycleController;
    private IAnnotationProvider annotationProvider;

	@Override
	public boolean isInjectable(Field pField) {
		return (logInjectBindingProvider != null  &&  logInjectBindingProvider.isInjectable(pField))
			|| (propInjectBindingProvider != null  &&  propInjectBindingProvider.isInjectable(pField));
	}

	@Override
	public BiConsumer<IComponentFactory, Object> createInjector(IComponentFactory pComponentFactory, Field pField) {
		if (logInjectBindingProvider != null  &&  logInjectBindingProvider.isInjectable(pField)) {
			return logInjectBindingProvider.createInjector(pComponentFactory, pField);
		}
		if (propInjectBindingProvider != null  &&  propInjectBindingProvider.isInjectable(pField)) {
			return propInjectBindingProvider.createInjector(pComponentFactory, pField);
		}
		throw new IllegalStateException("Cannot create an injector for a logger. or a property, for the field "
				+ pField.getName() + " in class " + pField.getDeclaringClass().getName());
	}

	@Override
	public boolean isInjectable(Method pMethod) {
		return (logInjectBindingProvider != null  &&  logInjectBindingProvider.isInjectable(pMethod))
				|| (propInjectBindingProvider != null  &&  propInjectBindingProvider.isInjectable(pMethod))
				|| (lifeCycleController != null  &&
					(annotationProvider.isAnnotatedWithPostConstruct(pMethod)  ||  annotationProvider.isAnnotatedWithPreDestroy(pMethod)));
	}

	@Override
	public BiConsumer<IComponentFactory, Object> createInjector(IComponentFactory pComponentFactory, Method pMethod) {
		if (logInjectBindingProvider != null  &&  logInjectBindingProvider.isInjectable(pMethod)) {
			return logInjectBindingProvider.createInjector(pComponentFactory, pMethod);
		}
		if (propInjectBindingProvider != null  &&  propInjectBindingProvider.isInjectable(pMethod)) {
			return propInjectBindingProvider.createInjector(pComponentFactory, pMethod);
		}
		if (lifeCycleController != null) {
			final Consumer<Object> startListener;
			if (annotationProvider.isAnnotatedWithPostConstruct(pMethod)) {
				startListener = newRunnable(pMethod, "@PostConstruct");
			} else {
				startListener = null;
			}
			final Consumer<Object> shutdownListener;
			if (annotationProvider.isAnnotatedWithPreDestroy(pMethod)) {
				shutdownListener = newRunnable(pMethod, "@PreDestroy");
			} else {
				shutdownListener = null;
			}
			if (startListener == null &&  shutdownListener == null) {
				throw new IllegalStateException("Cannot create a startup, or shutdown listener, for the method "
						+ pMethod.getName() + " in class " + pMethod.getDeclaringClass().getName());
			}
			final BiConsumer<IComponentFactory, Object> injector = (cf,o) -> {
				lifeCycleController.addListener(new TerminableListener() {
					@Override
					public void start() {
						if (startListener != null) {
							startListener.accept(o);
						}
					}

					@Override
					public void shutdown() {
						if (shutdownListener != null) {
							shutdownListener.accept(o);
						}
					}
				});
			};
			return injector;
		}
		throw new IllegalStateException("Unable to create an injector for method " + pMethod.getName()
			+ " in class " + pMethod.getDeclaringClass().getName());
	}

	protected Consumer<Object> newRunnable(Method pMethod, String pAnnotation) {
		if (pMethod.getParameterCount() != 0) {
			throw new IllegalStateException("The method " + pMethod
					+ " in class " + pMethod.getDeclaringClass().getName()
					+ " is annotated with " + pAnnotation
					+ ", but requires parameters.");
		}
		return (o) -> {
			DiUtils.assertAccessible(pMethod);
			try {
				pMethod.invoke(o);
			} catch (Exception e) {
				throw DiUtils.show(e);
			}
		};
	}
		
	@Override
	public void init(IComponentFactory pComponentFactory, Configuration pConfiguration) {
		annotationProvider = pConfiguration.getAnnotationProvider();
		logInjectBindingProvider = pComponentFactory.getInstance(LogInjectBindingProvider.class);
		propInjectBindingProvider = pComponentFactory.getInstance(PropInjectBindingProvider.class);
		lifeCycleController = pComponentFactory.getInstance(ILifecycleController.class);
	}
}
