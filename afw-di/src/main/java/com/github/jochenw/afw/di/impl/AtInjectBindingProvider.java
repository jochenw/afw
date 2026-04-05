package com.github.jochenw.afw.di.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactory.IBinding;
import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory.Configuration;


/**
 * The default implementation of a binding provider, which handles the
 * {@code * @Inject}, and {@code @Named} annotations.
 */
public class AtInjectBindingProvider extends AbstractBindingProvider {
	private ConcurrentMap<Key<Object>,IBinding<Object>> bindings;
	private IAnnotationProvider annotationProvider;
	private final Set<Class<? extends Annotation>> annotationClasses = new HashSet<>();

	/** Creates a new instance.
	 */
	public AtInjectBindingProvider() {}

	/** Non-functional; the component factory is supposed to invoke
	 * {@link #init(Configuration, ConcurrentMap)} instead.
	 */
	public void init(IComponentFactory pComponentFactory, Configuration pConfiguration) {
		throw new IllegalStateException("Not implemented, use init(Configuration, ConcurrentMap) instead.");
	}

	/** Initializes this binding provider with the component
	 * factories configuration, and the set of bindings.
	 * @param pConfiguration The component factories configuration.
	 * @param pBindings The component factories bindings.
	 */
	public void init(Configuration pConfiguration, ConcurrentMap<Key<Object>,IBinding<Object>> pBindings) {
		bindings = pBindings;
		bindings.keySet().forEach((k) -> {
			final Class<? extends Annotation> annotationType = k.getAnnotationType();
			if (annotationType != null) {
				annotationClasses.add(annotationType);
			}
			final Annotation annotation = k.getAnnotation();
			if (annotation != null) {
				annotationClasses.add(annotation.getClass());
			}
		});
		annotationProvider = pConfiguration.getAnnotationProvider();
	}

	@Override
	public boolean isInjectable(Field pField) {
		return annotationProvider.isInjectable(pField);
	}

	@Override
	public BiConsumer<IComponentFactory,Object> createInjector(IComponentFactory pComponentFactory, Field pField) {
		final Supplier<String> description = () -> "field " + pField.getName() + " in class " + pField.getDeclaringClass().getName();
		final IBinding<Object> binding = requireBinding(pComponentFactory, pField.getGenericType(), pField, description);
		return (cf,o) -> {
			final Object value = binding.apply(cf);
			DiUtils.set(pField, o, value);
		};
	}

	@Override
	public boolean isInjectable(Method pMethod) {
		return annotationProvider.isInjectable(pMethod);
	}

	@Override
	public BiConsumer<IComponentFactory,Object> createInjector(IComponentFactory pComponentFactory, Method pMethod) {
		final Type[] parameterTypes = pMethod.getGenericParameterTypes();
		final AnnotatedType[] parameterAnnotations = pMethod.getAnnotatedParameterTypes();
		@SuppressWarnings("unchecked")
		final IBinding<Object>[] parameterBindings = (IBinding<Object>[]) Array.newInstance(IBinding.class, parameterTypes.length);
		for (int i = 0;  i < parameterBindings.length;  i++) {
			final int index = i;
			final Supplier<String> description = () -> "parameter " + index + " of method " + pMethod.getName()
			        + " in class " + pMethod.getDeclaringClass().getName();
			parameterBindings[i] = requireBinding(pComponentFactory, parameterTypes[i], parameterAnnotations[i], description);
		}
		return (cf,o) -> {
			final Object[] values = new Object[parameterBindings.length];
			for (int i = 0;  i < parameterBindings.length;  i++) {
				values[i] = parameterBindings[i].apply(cf);
			}
			DiUtils.invoke(pMethod, o, values);
		};
	}

	/** Called to find a binding for a value, that is being injected.
	 * @param pComponentFactory The component factory, which acts as the the source for
	 * configured binding.
	 * @param pType Type of the value, that is being injected.
	 * @param pAnnotations Annotations, that configure the injection point.
	 * @param pDescriptor Human readable description of the injection point,
	 *   for use in error messages.
	 * @return The requested bin
	 */
	protected IBinding<Object> requireBinding(IComponentFactory pComponentFactory, Type pType, AnnotatedElement pAnnotations,
			Supplier<String> pDescriptor) {
		String name = annotationProvider.getNamedValue(pAnnotations);
		if (name == null) {
			name = "";
		}
		IBinding<Object> firstBindingMatchingType = null;
		/** Try to find an annotated binding.
		 */
		for (Annotation annotation : pAnnotations.getAnnotations()) {
			if (annotationClasses.contains(annotation.annotationType())) {
				final Key<Object> annotationKey = Key.of(pType, name, null, annotation);
				final IBinding<Object> annotationBinding = getBinding(pComponentFactory, annotationKey);
				if (annotationBinding != null) {
					return annotationBinding;
				}
				final Class<? extends Annotation> annotationType = annotation.annotationType();
				if (annotationType != null  &&  firstBindingMatchingType == null) {
					final Key<Object> annotationTypeKey = Key.of(pType, name, annotationType, null);
					final IBinding<Object> annotationTypeBinding = getBinding(pComponentFactory, annotationTypeKey);
					if (annotationTypeBinding != null) {
						firstBindingMatchingType = annotationTypeBinding;
					}
				}
			}
		}
		if (firstBindingMatchingType != null) {
			return firstBindingMatchingType;
		}
		/* No annotated binding found, try a binding without annotation.
		 */
		final Key<Object> key = Key.of(pType, name);
		final IBinding<Object> binding = getBinding(pComponentFactory, key);
		if (binding == null) {
			if (pType instanceof ParameterizedType pt) {
				final Type providerType = pt.getRawType();
				final Type[] parameterTypes = pt.getActualTypeArguments();
				final Type providedType = parameterTypes == null  ||  parameterTypes.length != 1 ? null : parameterTypes[0]; 
				if (providedType != null) {
					final Key<Object> providerKey = Key.of(providedType, name);
					final IBinding<Object> providerBinding = getBinding(pComponentFactory, providerKey);
					if (providerBinding != null) {
						final ISupplier<Object> supplier = annotationProvider.getProvider(providerType, providerBinding);
						if (supplier != null) {
							return IBinding.of(key, supplier, providerBinding.getScope());
						}
					}
				}
			}
		} else {
			return binding;
		}
		throw new IllegalStateException("No suitable binding found for " + pDescriptor.get());
	}

	/** Called to find an existing binding with the given key in the given component factory.
	 * @param pComponentFactory The component factory, which may be queried for the binding.
	 * @param pKey The requested bindings key.
	 * @return The 
	 */
	protected IBinding<Object> getBinding(IComponentFactory pComponentFactory, Key<Object> pKey) {
		final IBinding<Object> binding = bindings.get(pKey);
		if (binding == null) {
			return binding;
		} else {
			return pComponentFactory.getBinding(pKey);
			
		}
	}
}
