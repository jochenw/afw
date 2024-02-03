package com.github.jochenw.afw.di.impl;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.impl.simple.Binding;
import com.github.jochenw.afw.di.impl.simple.SimpleComponentFactory;


/** An annotation provider, which supports the {@link javax.inject.Inject}
 * annotation.
 */
public class JavaxAnnotationProvider implements IAnnotationProvider {
	@Override
	public @NonNull Class<? extends Annotation> getInjectClass() {
		return javax.inject.Inject.class;
	}

	@Override
	public @NonNull Class<? extends Annotation> getNamedClass() {
		return javax.inject.Named.class;
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
		if (pAnnotation instanceof javax.inject.Named) {
			return ((javax.inject.Named) pAnnotation).value();
		}
		return null;
	}

	@Override
	public @NonNull String getId() {
		return "javax.inject";
	}
}
