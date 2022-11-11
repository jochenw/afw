package com.github.jochenw.afw.core.props;

import java.nio.file.Path;
import java.nio.file.Paths;


public class PathProperty extends AbstractProperty<Path> implements IPathProperty {
	PathProperty(String pKey, Path pDefaultValue) {
		super(pKey, pDefaultValue);
	}

	@Override
	protected Path convert(String pStrValue) {
		if (pStrValue == null  ||  pStrValue.length() == 0) {
			return getDefaultValue();
		}
		return Paths.get(pStrValue);
	}
}
