package com.github.jochenw.afw.core.inject;

import java.lang.reflect.ParameterizedType;

import javax.annotation.Nonnull;

public class Types {
	public static class Type<T extends Object> {
		private @Nonnull final java.lang.reflect.Type rawType;

		public Type() {
			final java.lang.reflect.Type t = getClass().getGenericSuperclass();
			if (t instanceof Class) {
				rawType = t;
			} else if (t instanceof ParameterizedType) {
				final ParameterizedType ptype = (ParameterizedType) t;
				final java.lang.reflect.Type[] typeArgs = ptype.getActualTypeArguments();
				if (typeArgs != null  &&  typeArgs.length > 0) {
					rawType = typeArgs[0];
				} else {
					throw new IllegalStateException("Unsupported type: " + t);
				}
			} else {
				throw new IllegalStateException("Unsupported type: " + t);
			}
		}

		public @Nonnull java.lang.reflect.Type getRawType() {
			return rawType;
		}
	}
}
