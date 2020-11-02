package com.github.jochenw.afw.core.util;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Utility class for working with {@link Locale Locales}.
 */
public class Locales {
	public static @Nullable Locale getLocale(@Nonnull String pLanguageTag) throws NullPointerException {
		final String languageTag = Objects.requireNonNull(pLanguageTag, "Language Tag");
		final Locale loc = Locale.forLanguageTag(languageTag.replace('_', '-'));
		if (loc == null  ||  "und".equals(loc.toLanguageTag())) {
			return null;
		}
		return loc;
	}

	public static @Nonnull Locale requireLocale(@Nonnull String pLanguageTag) throws IllegalArgumentException {
		final Locale loc = getLocale(pLanguageTag);
		if (loc == null) {
			throw new IllegalArgumentException("Invalid language tag: " + pLanguageTag);
		}
		return loc;
	}
}
