package com.github.jochenw.afw.di.impl;

import java.lang.annotation.Annotation;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.impl.simple.Binding;
import com.github.jochenw.afw.di.impl.simple.SimpleComponentFactory;

import jakarta.inject.Provider;

/** An annotation povider, which supports the {@link javax.inject.Inject}
 * annotation.
 */
public class JakartaAnnotationProvider implements IAnnotationProvider {
	@Override
	public @NonNull Class<? extends Annotation> getInjectClass() {
		return jakarta.inject.Inject.class;
	}

	@Override
	public @NonNull Class<? extends Annotation> getNamedClass() {
		return jakarta.inject.Named.class;
	}

	@Override
	public @NonNull Class<?> getProviderClass() {
		return Provider.class;
	}

	@Override
	public @NonNull Binding getProvider(Binding pBinding) {
		return new Binding() {
			@Override
			public Object apply(SimpleComponentFactory pCf) {
				return new Provider<Object>() {
					@Override
					public Object get() {
						return pBinding.apply(pCf);
					}
				};
			}
		};
	}

	@Override
    public String getNamedValue(@NonNull Annotation pAnnotation) {
	    if (pAnnotation instanceof jakarta.inject.Named) {
		    return ((jakarta.inject.Named) pAnnotation).value();
	    }
	    return null;
    }

	@Override
	public @NonNull String getId() {
		return "jakarta.inject";
	}
}
