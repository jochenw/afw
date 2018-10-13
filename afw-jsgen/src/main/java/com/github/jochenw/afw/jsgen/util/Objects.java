package com.github.jochenw.afw.jsgen.util;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Objects {
	@Nonnull public static <O> O requireNonNull(@Nullable O pObject, @Nullable String pMessage) {
		if (pObject == null) {
			throw new NullPointerException(pMessage == null ? "Object is null" : pMessage);
		} else {
			return pObject;
		}
	}

	@Nonnull public static <O> O[] requireAllNonNull(O[] pObjects, String pMessage) {
		@Nonnull final O[] objects = requireNonNull(pObjects, pMessage);
		for (int i = 0;  i < pObjects.length;  i++) {
			if (objects[i] == null) {
				throw new NullPointerException(pMessage + "[" + i + "]");
			}
		}
		return objects;
	}

	@Nonnull public static <O extends Iterable<?>> O requireAllNonNull(O pObjects, String pMessage) {
		@Nonnull final O objects = requireNonNull(pObjects, pMessage);
		final Iterator<?> iter = objects.iterator();
		int i = 0;
		while (iter.hasNext()) {
			Object o = iter.next();
			if (o == null) {
				throw new NullPointerException(pMessage + "[" + i + "]");
			}
			++i;
		}
		return objects;
	}

}
