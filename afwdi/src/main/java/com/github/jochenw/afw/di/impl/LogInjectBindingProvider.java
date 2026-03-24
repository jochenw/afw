package com.github.jochenw.afw.di.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactory.IBinding;
import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory.Configuration;

/**
 * A binding provider, which supports the @LogInject annotation.
 * @param <L> Type of the logger, which is being injected.
 */
public class LogInjectBindingProvider<L> extends AbstractBindingProvider {
	/** Interface of the logger factory, which actually creates the
	 * loggers, that are being injected.
     * @param <T> Type of the logger, which is being injected.
	 */
	public interface LoggerFactory<T> {
		/** Called to create a logger with the given logger id, and method name,
		 * using the given component factory.
		 * @param pComponentFactory The component factory to use.
		 * @param pLoggerId The logger id.
		 * @param pMName The method name, if any, or the empty string.
		 * @return The created logger.
		 */
		public T apply(IComponentFactory pComponentFactory, String pLoggerId, String pMName);
	}
	private final Class<L> loggerType;
	private final LoggerFactory<L> loggerFactory;

	/** Creates a new instance with the given logger type, and the given logger factory.
	 * @param pLoggerType Type of the loggers, which are being injected.
	 * @param pLoggerFactory The logger factory, which creates 
	 */
	public LogInjectBindingProvider(Class<L> pLoggerType, LoggerFactory<L> pLoggerFactory) {
		loggerType = pLoggerType;
		loggerFactory = pLoggerFactory;
	}

	@Override
	public boolean isInjectable(Field pField) {
		return pField.isAnnotationPresent(LogInject.class)  &&  pField.getType().isAssignableFrom(loggerType);
	}

	@Override
	public BiConsumer<IComponentFactory,Object> createInjector(IComponentFactory pComponentFactory, Field pField) {
		final LogInject logInject = pField.getAnnotation(LogInject.class);
		if (logInject == null) {
			throw new IllegalStateException("Expected a LogInject annotation on field "
					+ pField.getName() + " in class " + pField.getDeclaringClass().getName());
		}
		String loggerId = logInject.id();
		if (loggerId.length() == 0) {
			loggerId = pField.getDeclaringClass().getCanonicalName();
		}
		final String logId = loggerId;
		final String mName = logInject.mName();
		final ISupplier<Object> supplier = (cf) -> {
			final Object logger = loggerFactory.apply(cf, logId, mName);
			if (logger == null) {
				throw new NullPointerException("The logger factory returned null for logger id " + logId
						+ ", and method name " + mName);
			}
			return logger;
		};
		return (cf,o) -> {
			final Object value = supplier.apply(cf);
			DiUtils.set(pField, o, value);
		};
	}

	@Override
	public boolean isInjectable(Method pMethod) {
		final Class<?> returnType = pMethod.getReturnType();
		if (returnType != null &&  returnType != Void.TYPE  &&  returnType != Void.class) {
			/* Return type isn't void, so the method is not a setter,
			 *  and we can't inject a logger.
			 */
			return false;
		}
		if (pMethod.getParameterCount() != 1) {
			/* A setter has exactly one parameter, so the method is not a
			 *setter, and we can't inject a logger.
			 */
			return false;
		}
		if (!pMethod.isAnnotationPresent(LogInject.class)) {
			/** The method doesn't have a {@code @LogInject} annotation,
			 * so we can't inject a logger.
			 */
			return false;
		}
		final Class<?> parameterType = pMethod.getParameterTypes()[0];
		if (!parameterType.isAssignableFrom(loggerType)) {
			/* The parameter type isn't suitable for a logger, so we cannot
			 * inject a logger.
			 */
			return false;
		}
		/** Everything's fine, and we can inject a logger.
		 */
		return false;
	}

	@Override
	public BiConsumer<IComponentFactory,Object> createInjector(IComponentFactory pComponentFactory, Method pMethod) {
		final LogInject logInject = pMethod.getAnnotation(LogInject.class);
		if (logInject == null) {
			throw new IllegalStateException("Expected a LogInject annotation on method "
					+ pMethod.getName() + " in class " + pMethod.getDeclaringClass().getName());
		}
		String loggerId = logInject.id();
		if (loggerId.length() == 0) {
			loggerId = pMethod.getDeclaringClass().getName();
		}
		final String logId = loggerId;
		final String mName = logInject.mName();
		final ISupplier<Object> supplier = (cf) -> {
			final Object logger = loggerFactory.apply(cf, logId, mName);
			if (logger == null) {
				throw new NullPointerException("The logger factory returned null for logger id " + logId
						+ ", and method name " + mName);
			}
			return logger;
		};
		return (cf,o) -> {
			final Object value = supplier.apply(cf);
			DiUtils.invoke(pMethod, o, value);
		};
	}

	@Override
	public void init(IComponentFactory pComponentFactory, Configuration pConfiguration) {
		// Does nothing.
	}
}
