package com.github.jochenw.afw.core.util;

import javax.annotation.Nullable;

public class Generics {
	public static @Nullable <O extends Object> O cast(@Nullable Object pObject) {
		@SuppressWarnings("unchecked")
		final @Nullable O o = (O) pObject;
		return o;
	}
}
