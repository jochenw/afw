package com.github.jochenw.afw.jsgen.api;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.impl.Quoter;

public interface JSGSource extends JSGClass {
	@Nonnull public static String q(@Nonnull String pValue) {
		return Quoter.valueOf(pValue);
	}
}
