package com.github.jochenw.afw.di.api;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

/** Implementation of {@link IAnnotationProvider} for Javax
 * annotations.
 */
public class JavaxAnnotationProvider implements IAnnotationProvider {
	private static final JavaxAnnotationProvider INSTANCE = new JavaxAnnotationProvider();
	/** Returns the singleton instance.
	 * @return The singleton instance
	 */
	public static final JavaxAnnotationProvider getInstance() { return INSTANCE; }

	/** Creates a new instance.
	 */
	protected JavaxAnnotationProvider() {}

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

	@Override
	public boolean isAnnotatedWithPreDestroy(AccessibleObject pObject) {
		return pObject.isAnnotationPresent(PreDestroy.class);
	}
}
