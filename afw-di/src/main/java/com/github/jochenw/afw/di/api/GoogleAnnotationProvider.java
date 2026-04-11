package com.github.jochenw.afw.di.api;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;

import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/** Implementation of {@link IAnnotationProvider} for Javax
 * annotations.
 */
public class GoogleAnnotationProvider implements IAnnotationProvider {
	private static final GoogleAnnotationProvider INSTANCE = new GoogleAnnotationProvider();
	/** Returns the singleton instance.
	 * @return The singleton instance
	 */
	public static final GoogleAnnotationProvider getInstance() { return INSTANCE; }

	/** Creates a new instance.
	 */
	protected GoogleAnnotationProvider() {}

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

	/** Google Guice has no @PostConstruct annotation, so we will always return false.
	 */
	@Override
	public boolean isAnnotatedWithPostConstruct(AccessibleObject pObject) {
		return false;
	}

	/** Google Guice has no @PreDestroy annotation, so we will always return false.
	 */
	@Override
	public boolean isAnnotatedWithPreDestroy(AccessibleObject pObject) {
		return false;
	}
}
