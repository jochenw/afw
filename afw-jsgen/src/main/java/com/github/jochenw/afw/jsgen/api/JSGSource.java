package com.github.jochenw.afw.jsgen.api;

import java.util.List;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.impl.Quoter;

public interface JSGSource extends IAnnotatable, IProtectable {
	@Nonnull JSGQName getType();

	@Nonnull public static String q(@Nonnull String pValue) {
		return Quoter.valueOf(pValue);
	}

	@Nonnull List<Object> getContent();
}
