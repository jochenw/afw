package com.github.jochenw.afw.di.impl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.api.IComponentFactory.ISupplier;


/** Default implementation of {@link IAnnotationProvider}.
 * Supports jakarta, javax, and google annotations.
 */
public class DefaultAnnotationProvider implements IAnnotationProvider {
	private static final List<IAnnotationProvider> ANNOTATION_PROVIDERS = newAnnotationProviders();

	/** Creates the list of actual annotation providers, that are available.
	 * @return The created list of actual annotation providers, that are available.
	 */
	private static List<IAnnotationProvider> newAnnotationProviders() {
		final List<IAnnotationProvider> lst = new ArrayList<>();
		final IAnnotationProvider jakap = getAnnotationProvider("jakarta");
		if (jakap != null) {
			lst.add(jakap);
		}
		final IAnnotationProvider jxap = getAnnotationProvider("javax");
		if (jxap != null) {
			lst.add(jxap);
		}
		final IAnnotationProvider gap = getAnnotationProvider("google");
		if (gap != null) {
			lst.add(gap);
		}
		return lst;
	}

	private static final DefaultAnnotationProvider INSTANCE = new DefaultAnnotationProvider(ANNOTATION_PROVIDERS);

	/** Returns the singleton instance.
	 * @return The singleton instance
	 */
	public static final DefaultAnnotationProvider getInstance() { return INSTANCE; }

	private final List<IAnnotationProvider> annotationProviders;
	/** Creates a new instance as a wrapper for the given list of annotation providers.
	 * @param pAnnotationProviders The list of wrapped annotation providers.
	 */
	public DefaultAnnotationProvider(List<IAnnotationProvider> pAnnotationProviders) {
		annotationProviders = pAnnotationProviders;
	}

	/** Returns the list of annotation providers.
	 * @return The list of annotation providers.
	 */
	public List<IAnnotationProvider> getAnnotationProviders() {
		return annotationProviders;
	}

	/** Returns the annotation provider with the given id, if available, or null.
	 * @param pId The annotation providers id, either of "jakarta", "javax",
	 *   "google", or "default".
	 * @return The requested annotation provider, if available, or null.
	 * @throws IllegalArgumentException The value of the parameter
	 *   {@code pId} is invalid.
	 */
	public static IAnnotationProvider getAnnotationProvider(String pId) {
		final String className;
		switch(Objects.requireNonNull(pId, "Id").toLowerCase()) {
		case "jakarta":
	        className = "com.github.jochenw.afw.di.impl.JakartaAnnotationProvider";
	        break;
		case "javax":
	    	className = "com.github.jochenw.afw.di.impl.JavaxAnnotationProvider";
	    	break;
		case "google":
	    	className = "com.github.jochenw.afw.di.impl.GoogleAnnotationProvider";
	    	break;
		case "default":
	    	className = "com.github.jochenw.afw.di.impl.DefaultAnnotationProvider";
	    	break;
	    default:
	    	throw new IllegalArgumentException("Invalid annotation provider id:"
	    			+ " Expected jakarta,javax,google, or default, got " + pId);
		}
		try {
			final Class<?> apClass = Class.forName(className);
			final Method getInstanceMethod = apClass.getMethod("getInstance");
			final IAnnotationProvider ap = (IAnnotationProvider) getInstanceMethod.invoke(null);
			return ap;
		} catch (Throwable t) {
			return null;
		}
	}
	
	@Override
	public boolean isInjectable(AccessibleObject pObject) {
		for (IAnnotationProvider ap : getAnnotationProviders()) {
			final boolean injectable = ap.isInjectable(pObject);
			if (injectable) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getNamedValue(AnnotatedElement pObject) {
		for (IAnnotationProvider ap : getAnnotationProviders()) {
			final String value = ap.getNamedValue(pObject);
			if (value != null) {
				return value;
			}
		}
		return null;
	}

	@Override
	public ISupplier<Object> getProvider(Type pProviderType, ISupplier<Object> pSupplier) {
		for (IAnnotationProvider ap : getAnnotationProviders()) {
			final ISupplier<Object> supplier = ap.getProvider(pProviderType, pSupplier);
			if (supplier != null) {
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean isAnnotatedWithPostConstruct(AccessibleObject pObject) {
		for (IAnnotationProvider ap : getAnnotationProviders()) {
			if (ap.isAnnotatedWithPostConstruct(pObject)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isAnnotatedWithPreDestroy(AccessibleObject pObject) {
		for (IAnnotationProvider ap : getAnnotationProviders()) {
			if (ap.isAnnotatedWithPreDestroy(pObject)) {
				return true;
			}
		}
		return false;
	}
}
