package com.github.jochenw.afw.core.io;

import java.util.Objects;
import java.util.regex.Pattern;

public class DefaultMatcher implements IMatcher {
	private final String patternStr, regex;
	private final Pattern pattern;

	public DefaultMatcher(String pPattern) {
		this(pPattern, true);
	}

	public DefaultMatcher(String pPattern, boolean pCaseSensitive) {
		patternStr = Objects.requireNonNull(pPattern, "Pattern");
		regex = asRegex(patternStr);
		if (pCaseSensitive) {
			pattern = Pattern.compile(regex);
		} else {
			pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		}
	}

	private String asRegex(String pPatternStr) {
		final StringBuilder sb = new StringBuilder();
		sb.append('^');
		int len = pPatternStr.length();
		int offset = 0;
		while (len > 0) {
			if (len >= 1) {
				final int c0 = pPatternStr.charAt(offset);
				if (len >= 2) {
					final int c1 = pPatternStr.charAt(offset+1);
					if (c0 == '*'  &&  c1 == '*') {
						sb.append(".*");
						len -= 2;
						offset += 2;
						continue;
					}
				}
				if (c0 == '*') {
					sb.append("[^/]*");
				} else if (Character.isLetterOrDigit(c0)) {
					sb.append((char) c0);
				} else {
					sb.append("\\");
					sb.append((char) c0);
				}
				len -= 1;
				offset += 1;
			}
		}
		sb.append('$');
		return sb.toString();
	}

	@Override
	public boolean matches(String pUri) {
		return pattern.matcher(pUri).matches();
	}

}
