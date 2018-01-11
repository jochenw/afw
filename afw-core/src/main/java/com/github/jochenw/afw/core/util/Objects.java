package com.github.jochenw.afw.core.util;

public class Objects {
	public static <T> T notNull(T pValue, T pDefault) {
		if (pValue == null) {
			return pDefault;
		} else {
			return pValue;
		}
	}

	public static <T> T requireNonNull(T pValue, String pMessage) {
        if (pValue == null)
            throw new NullPointerException(pMessage);
        return pValue;
	}
}
