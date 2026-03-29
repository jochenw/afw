package com.github.jochenw.afw.di.impl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;


/** Implementation of {@link IAnnotationProvider} for Jakarta
 * annotations.
 */
public class JakartaAnnotationProvider implements IAnnotationProvider {
	private static final JakartaAnnotationProvider INSTANCE = new JakartaAnnotationProvider();
	/** Returns the singleton instance.
	 * @return The singleton instance
	 */
	public static final JakartaAnnotationProvider getInstance() { return INSTANCE; }

	@Override
	public boolean isInjectable(AccessibleObject pObject) {
		return pObject.isAnnotationPresent(Inject.class);
	}

	@Override
	public String getNamedValue(AnnotatedElement pObject) {
		final Named named = pObject.getAnnotation(Named.class);
		if (named == null) {
			return null;
		} else {
			return named.value();
		}
	}

	@Override
	public ISupplier<Object> getProvider(Type pProviderType, ISupplier<Object> pSupplier) {
		if (pProviderType == Provider.class  ||  pProviderType.equals(Provider.class)) {
			return (cf) -> {
				final Provider<Object> provider = () -> pSupplier.apply(cf);
				return provider;
			};
		}
		return null;
	}


	@Override
	public boolean isAnnotatedWithPostConstruct(AccessibleObject pObject) {
		return pObject.isAnnotationPresent(PostConstruct.class);
	}

	/** Google Guice has no @PreDestroy annotation, so we will always return false.
	 */
	@Override
	public boolean isAnnotatedWithPreDestroy(AccessibleObject pObject) {
		return pObject.isAnnotationPresent(PreDestroy.class);
	}
}
