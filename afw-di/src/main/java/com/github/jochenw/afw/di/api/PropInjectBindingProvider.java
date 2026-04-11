package com.github.jochenw.afw.di.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Properties;
import java.util.function.BiConsumer;

import com.github.jochenw.afw.di.api.IComponentFactory.IConfiguration;
import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;

/**
 * A binding provider, which supports the @PropInject annotation.
 * @param <P> Type of the property, which is being injected.
 */
public class PropInjectBindingProvider<P> extends AbstractBindingProvider {
	/** Interface of the logger factory, which actually creates the
	 * loggers, that are being injected.
	 */
	public interface PropertyFactory {
		/** Called to return the injected property with the given type, the given
		 * property id, and default value, and the given nullable attribute.
		 * @param pComponentFactory The component factory to use.
		 * @param pPropertyType Type of the property. May also be
		 *   {@link String}, if the injected value is the property value,
		 *   rather than a property object, or {@link Properties}, if the
		 *   requested value is the whole property set.
		 * @param pPropertyId The property id.
		 * @param pDefaultValue The properties default value, if any, or null.
		 * @param pNullable	True, if the property is null.
		 * @return The created property instance.
		 */
		public Object apply(IComponentFactory pComponentFactory, Type pPropertyType,
				       String pPropertyId, String pDefaultValue, boolean pNullable);
	}
	private final Class<?> propertyObjectType;
	private final PropertyFactory propertyFactory;

	/** Creates a new instance with the given property object type, and the given property factory.
	 * @param pPropertyObjectType Type of the property objects, which are being injected.
	 * @param pPropertyFactory The logger factory, which creates 
	 */
	public PropInjectBindingProvider(Class<P> pPropertyObjectType, PropertyFactory pPropertyFactory) {
		propertyObjectType = pPropertyObjectType;
		propertyFactory = pPropertyFactory;
	}

	@Override
	public boolean isInjectable(Field pField) {
		if (!pField.isAnnotationPresent(PropInject.class)) {
			/* The field is not annotated with @PropInject, so we cannot inject a property value.
			 */
			return false;
		}
		final Class<?> fieldType = pField.getType();
		if (!fieldType.isAssignableFrom(String.class)  &&  !fieldType.isAssignableFrom(Properties.class)
				&&  (propertyObjectType != null  &&  !fieldType.isAssignableFrom(propertyObjectType))) {
			return false;
		}
		/* Field type is okay, and the field is annotated with @PropInject, so we can
		 * inject a value.
		 */
		return true;
 	}

	@Override
	public BiConsumer<IComponentFactory,Object> createInjector(IComponentFactory pComponentFactory, Field pField) {
		final PropInject propInject = pField.getAnnotation(PropInject.class);
		if (propInject == null) {
			throw new IllegalStateException("Expected a PropInject annotation on field "
					+ pField.getName() + " in class " + pField.getDeclaringClass().getName());
		}
		String propertyId = propInject.id();
		if (propertyId.length() == 0) {
			propertyId = pField.getDeclaringClass().getName() + "." + pField.getName();
		}
		final String propId = propertyId;
		final String defaultValue = propInject.defaultValue();
		final boolean nullable = propInject.nullable();
		final ISupplier<Object> supplier = (cf) -> {
			Object propertyValue = propertyFactory.apply(cf, pField.getType(), propId, defaultValue, nullable);
			if (propertyValue == null) {
				if (defaultValue == PropInject.NO_DEFAULT) {
					if (nullable) {
						throw new NullPointerException("The logger factory returned null for property id " + propId
								+ ", and field name " + pField.getName());
					} else {
						propertyValue = null;
					}
				}
			}
			return propertyValue;
		};
		return (cf,o) -> {
			final Object value = supplier.apply(cf);
			IBindingProvider.set(pField, o, value);
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
		if (!pMethod.isAnnotationPresent(PropInject.class)) {
			/** The method doesn't have a {@code @LogInject} annotation,
			 * so we can't inject a logger.
			 */
			return false;
		}
		final Class<?> parameterType = pMethod.getParameterTypes()[0];
		if (!parameterType.isAssignableFrom(String.class)  &&  !parameterType.isAssignableFrom(Properties.class)
				&&  (propertyObjectType != null  &&  !parameterType.isAssignableFrom(propertyObjectType))) {
			return false;
		}
		/** Everything's fine, and we can inject a logger.
		 */
		return true;
	}

	@Override
	public BiConsumer<IComponentFactory,Object> createInjector(IComponentFactory pComponentFactory, Method pMethod) {
		final PropInject propInject = pMethod.getAnnotation(PropInject.class);
		if (propInject == null) {
			throw new IllegalStateException("Expected a PropInject annotation on method "
					+ pMethod.getName() + " in class " + pMethod.getDeclaringClass().getName());
		}
		String propertyId = propInject.id();
		if (propertyId.length() == 0) {
			final String methodName = pMethod.getName();
			String attributeName = methodName;
			for (String prefix : Arrays.asList("get", "has", "is")) {
				if (methodName.length() > prefix.length()
					&&  methodName.startsWith(prefix)
					&&  Character.isUpperCase(methodName.charAt(prefix.length()))) {
					final String suffix = methodName.substring(prefix.length());
					attributeName = Character.toLowerCase(suffix.charAt(0)) + suffix.substring(1);
					break;
				}
			}
			propertyId = pMethod.getDeclaringClass().getName() + "." + attributeName;
		}
		final String propId = propertyId;
		final String defaultValue = propInject.defaultValue();
		final boolean nullable = propInject.nullable();
		final Class<?> parameterType = pMethod.getParameterTypes()[0];
		final ISupplier<Object> supplier = (cf) -> {
			Object propertyValue = propertyFactory.apply(cf, parameterType, propId, defaultValue, nullable);
			if (propertyValue == null) {
				if (defaultValue == PropInject.NO_DEFAULT) {
					if (nullable) {
						throw new NullPointerException("The logger factory returned null for property id " + propId
								+ ", and method name " + pMethod.getName());
					} else {
						propertyValue = null;
					}
				}
			}
			return propertyValue;
		};
		return (cf,o) -> {
			final Object value = supplier.apply(cf);
			IBindingProvider.invoke(pMethod, o, value);
		};
	}


	@Override
	public void init(IComponentFactory pComponentFactory, IConfiguration pConfiguration) {
		// Does nothing.
	}

	
}
