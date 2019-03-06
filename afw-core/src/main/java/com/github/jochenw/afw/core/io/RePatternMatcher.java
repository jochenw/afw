package com.github.jochenw.afw.core.io;

import java.util.function.Predicate;
import java.util.regex.Pattern;

public class RePatternMatcher implements Predicate<String> {
	private final String re;
	private final Pattern pattern;

	public RePatternMatcher(String pPattern) {
		final boolean caseSensitive;
		final String pat;
		if (pPattern.endsWith("/i")) {
			pat = pPattern.substring(0, pPattern.length()-2);
			caseSensitive = false;
		} else if (pPattern.endsWith("/s")) {
			pat = pPattern.substring(0, pPattern.length()-2);
			caseSensitive = true;
		} else { 
			pat = pPattern;
			caseSensitive = true;
		}
		re = asRe(pat);
		pattern = Pattern.compile(re, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
	}

	private String asRe(String pPattern) {
		final StringBuilder sb = new StringBuilder();
		sb.append('^');
		int offset = 0;
		while (offset < pPattern.length()) {
			final char c = pPattern.charAt(offset++);
			if (c == '*') {
				sb.append(".*");
			} else if (Character.isLetterOrDigit(c)) {
				sb.append(c);
			} else {
				sb.append('\\');
				sb.append(c);
			}
		}
		sb.append('$');
		return sb.toString();
	}

	@Override
	public boolean test(String pValue) {
		return pattern.matcher(pValue).matches();
	}

}
