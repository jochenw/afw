package com.github.jochenw.afw.core.props;

import java.net.MalformedURLException;
import java.net.URL;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Strings;


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
			final @NonNull String urlStr = pStrValue;
			return Strings.asUrl(urlStr);
		} catch (MalformedURLException e) {
			return getDefaultValue();
		}
	}
}
