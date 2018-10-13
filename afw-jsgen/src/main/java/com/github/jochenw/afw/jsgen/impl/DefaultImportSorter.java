package com.github.jochenw.afw.jsgen.impl;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.api.JSGImportSorter;
import com.github.jochenw.afw.jsgen.api.JSGQName;

public class DefaultImportSorter implements JSGImportSorter {
	@Override
	public int compare(@Nonnull JSGQName o1, @Nonnull JSGQName o2) {
		return o1.getQName().compareToIgnoreCase(o2.getQName());
	}

	@Override
	public int getCategory(JSGQName pName) {
		if (pName.getPackageName().startsWith("java.")) {
			return 0;
		} else if (pName.getPackageName().startsWith("javax.")) {
			return 1;
		} else if (pName.getPackageName().startsWith("com.")) {
			return 2;
		} else if (pName.getPackageName().startsWith("org.")) {
			return 3;
		} else {
			return 4;
		}
	}
}
