package com.github.jochenw.afw.core.props;

import java.net.MalformedURLException;
import java.net.URL;

import org.jspecify.annotations.NonNull;


/** Implementation of {@link IURLProperty}.
 */
public class UrlProperty extends AbstractProperty<URL> implements IURLProperty {
	UrlProperty(@NonNull String pKey, URL pDefaultValue) {
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
