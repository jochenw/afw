package com.github.jochenw.afw.core.inject;

import java.io.Serializable;
import java.lang.annotation.Annotation;

import javax.annotation.Nonnull;

public class Names {
	private static class NamedImpl implements javax.inject.Named, Serializable {
		private static final long serialVersionUID = -3588508855233450088L;
		private final @Nonnull String value;

		NamedImpl(@Nonnull String pValue) {
			value = pValue;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return javax.inject.Named.class;
		}

		@Override
		public @Nonnull String value() {
			return value;
		}	
	}

	public static javax.inject.Named named(@Nonnull String pValue) {
		return new NamedImpl(pValue);
	}
}
