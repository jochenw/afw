package com.github.jochenw.afw.core.util;

import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Utility class for working with {@link Locale Locales}.
 */
public class Locales {
	/**
	 * Returns the {@link Locale} with the given language tag.
	 * @param pLanguageTag The language tag, for which to query a locale.
	 *   For example: "en-US", "en_us", or "de".
	 * @return The matching Locale, if any, or null.
	 * @throws NullPointerException The input parameter is null.
	 */
	public static @Nullable Locale getLocale(@NonNull String pLanguageTag) throws NullPointerException {
		final String languageTag = Objects.requireNonNull(pLanguageTag, "Language Tag");
		final Locale loc = Locale.forLanguageTag(languageTag.replace('_', '-'));
		if (loc == null  ||  "und".equals(loc.toLanguageTag())) {
			return null;
		}
		return loc;
	}

	/**
	 * Returns the {@link Locale} with the given language tag.
	 * @param pLanguageTag The language tag, for which to query a locale.
	 *   For example: "en-US", "en_us", or "de".
	 * @return The matching Locale. Never null.
	 * @throws NullPointerException The input parameter is null.
	 * @throws IllegalArgumentException No matching Locale was found.
	 */
	public static @NonNull Locale requireLocale(@NonNull String pLanguageTag) throws IllegalArgumentException {
		final Locale loc = getLocale(pLanguageTag);
		if (loc == null) {
			throw new IllegalArgumentException("Invalid language tag: " + pLanguageTag);
		}
		return loc;
	}
}
