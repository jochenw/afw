package com.github.jochenw.afw.core.props;

import java.net.MalformedURLException;
import java.net.URL;


public class UrlProperty extends AbstractProperty<URL> implements IURLProperty {
	UrlProperty(String pKey, URL pDefaultValue) {
		super(pKey, pDefaultValue);
	}

	@Override
	protected URL convert(String pStrValue) {
		if (pStrValue == null) {
			return getDefaultValue();
		}
		try {
			return new URL(pStrValue);
		} catch (MalformedURLException e) {
			return getDefaultValue();
		}
	}
}