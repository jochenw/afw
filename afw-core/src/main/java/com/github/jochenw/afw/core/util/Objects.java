package com.github.jochenw.afw.core.util;

import javax.annotation.Nonnull;

public class Objects {
	public static <T> T notNull(T pValue, T pDefault) {
		if (pValue == null) {
			return pDefault;
		} else {
			return pValue;
		}
	}

	public static @Nonnull <T> T requireNonNull(T pValue, String pMessage) {
        if (pValue == null)
            throw new NullPointerException(pMessage);
        return pValue;
	}

	public static @Nonnull <T> T requireNonNull(T pValue) {
        if (pValue == null)
            throw new NullPointerException();
        return pValue;
	}

	public static @Nonnull <T> T[] requireAllNonNull(T[] pValues, String pDescription) {
		if (pValues == null) {
			throw new NullPointerException(pDescription + "s");
		} else {
			final @Nonnull T[] array = pValues;
			for (int i = 0;  i < array.length;  i++) {
				if (array[i] == null) {
					throw new NullPointerException(pDescription + ", element " + i);
				}
			}
			return array;
		}
	}

	public static @Nonnull <T> Iterable<T> requireAllNonNull(Iterable<T> pValues, String pDescription) {
		if (pValues == null) {
			throw new NullPointerException(pDescription + "s");
		} else {
			final @Nonnull Iterable<T> iterable = pValues;
			int i = 0;
			for (T t : iterable) {
				if (t == null) {
					throw new NullPointerException(pDescription + ", element " + i);
				}
				++i;
			}
			return iterable;
		}
	}
}
