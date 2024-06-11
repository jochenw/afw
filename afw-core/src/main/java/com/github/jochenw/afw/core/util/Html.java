package com.github.jochenw.afw.core.util;

import java.util.function.Supplier;

/** Utility class for working with Html, or Jsp files.
 */
public class Html {
	/**
	 * Escape the given string into a format, that can be embedded into an HTML element,
	 * @param pValue The string, which is being escaped.
	 * @return The unmodified input parameter {@code pValue}, if no escaping is necessary.
	 *   Otherwise, the input parameter, with escaping applied.
	 */
	public static String escapeHtml(String pValue) {
		return escape(pValue, Html::escapeHtmlChar);
	}

	/**
	 * If the given string is non-empty: Escape it into a format, that can be embedded into an
	 * HTML element. Otherwise, return the string "&nbsp;".
	 * @param pValue The string, which is being escaped.
	 * @return The unmodified input parameter {@code pValue}, if it is non-empty,
	 *   and no escaping is necessary. If the input parameter is non-empty, and
	 *   escaping is necessary, returns it, with escaping applied. Otherwise
	 *   (the input parameter is null, or empty) the string "&nbsp;".
	 */
	public static String escapeOrNbsp(String pValue) {
		if (pValue == null  ||  pValue.length() == 0) {
			return "&nbsp;";
		} else {
			return escapeHtml(pValue);
		}
	}

	/**
	 * If the given boolean value is true: Invoke the given supplier, and convert the
	 * returned string into a format, that can be embedded into an
	 * HTML element. Otherwise, return the string "&nbsp;".
	 * @param pEmpty True, if invoking the string supplier is supposed to
	 *   return an empty value. Otherwise false.
	 * @param pValueSupplier The supplier, which returns the string value, that
	 *   is being escaped.
	 * @return The converted HTML string, or the token "&nbsp;".
	 */
	public static String escapeOrNbspIf(boolean pEmpty, Supplier<String> pValueSupplier) {
		if (pEmpty) {
			final String value = pValueSupplier.get();
			if (value == null  ||  value.length() == 0) {
				return "&nbsp;";
			} else {
				return escapeHtml(value);
			}
		}
		return "&nbsp;";
	}

	/**
	 * Escape the given string into a format, that can be embedded into an HTML attribute.
	 * Basically, this is the same than {@link #escapeHtml(String)}, except that it
	 * also escapes the {@code "}, and {@code '} characters.
	 * @param pValue The string, which is being escaped.
	 * @return The unmodified input parameter {@code pValue}, if no escaping is necessary.
	 *   Otherwise, the input parameter, with escaping applied.
	 */
	public static String escapeHtmlAttr(String pValue) {
		return escape(pValue, Html::escapeHtmlAttributeChar);
	}

	private static String escapeHtmlAttributeChar(final char pChar) {
		switch(pChar) {
		case '<':
			return "&lt;";
		case '>':
			return "&gt;";
		case '&':
			return "&amp;";
		case '"':
			return "&quot;";
		case '\'':
			return "&#39;";
		default:
			return null;
		}
	}

	private static String escapeHtmlChar(final char pChar) {
		switch(pChar) {
		case '<':
			return "&lt;";
		case '>':
			return "&gt;";
		case '&':
			return "&amp;";
		default:
			return null;
		}
	}

	/** Interface of a function, that maps characters to escaped strings.
	 */
	public interface Escaper {
		/** Called to escape a single character.
		 * @param pChar The character, that is being escaped.
		 * @return The string, that replaces the character, or null, if
		 * escaping isn't necessary for this character.
		 */
		String escape(char pChar);
	}

	/** Called to escape the given value, using the given
	 * character mapper. Internally, this method implements
	 * {@link #escapeHtml(String)}, and {@link #escapeHtmlAttr(String)}.
	 * @param pValue The string, which is being escaped.
	 * @param pEscaper The character mapper.
	 * @return The unmodified input parameter {@code pValue}, if no escaping is necessary.
	 *   Otherwise, the input parameter, with escaping applied.
	 */
	public static String escape(String pValue, Escaper pEscaper) {
		StringBuilder sb = null;
		for (int i = 0;  i < pValue.length();  i++) {
			final char c = pValue.charAt(i);
			final String cEscapedStr = pEscaper.escape(c);
			if (cEscapedStr == null) {
				if (sb != null) {
					sb.append(c);
				}
			} else {
				if (sb == null) {
					sb = new StringBuilder();
					sb.append(pValue, 0, i);
				}
				sb.append(cEscapedStr);
			}
		}
		if (sb == null) {
			return pValue;
		} else {
			return sb.toString();
		}
	}
}
