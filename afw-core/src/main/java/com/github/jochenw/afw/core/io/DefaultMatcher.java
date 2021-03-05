/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.io;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;



/** Default implementation of {@link IMatcher}, which supports:
 * <ol>
 *   <li>regular expressions</li>
 *   <li>case sensitive/case insensitive matching</li>
 * </ol>
 * To create an instance of this class, use {@link #asMatchers(String[], boolean)}.
 */
public class DefaultMatcher implements IMatcher {
	private final String patternStr, regex;
	private final Pattern pattern;


	/** Creates a new instance, which matches the given regular expression,
	 * with case sensitive matching.
	 * @param pPattern The regular expression, that is being matched.
	 */
	public DefaultMatcher(String pPattern) {
		this(pPattern, true);
	}

	/** Creates a new instance, which matches the given regular expression,
	 * and the given value for case sensitive matching.
	 * @param pPattern The regular expression, that is being matched.
	 * @param pCaseSensitive True, if the regular expression is being
	 *   case sensitive. False for case insensitive matching.
	 */
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
		if (pPatternStr.startsWith("**/")) {
			sb.replace(0, "^.*\\/".length(), "^(.*\\/|)");
		}
		if (pPatternStr.endsWith("**/*")) {
			sb.replace(sb.length()-".*\\/[^/]*$".length(), sb.length(), ".*$");
		}
		return sb.toString();
	}

	@Override
	public boolean test(String pUri) {
		return pattern.matcher(pUri).matches();
	}

	/**
	 * Creates a new instance of {@link IMatcher}, which accepts a file name,
	 * if it matches either of the include strings, but none of the exclude strings.
	 * @param pIncludes The include strings, that the created matcher should accept.
	 * @param pExcludes The exclude strings, that the created matcher should rejept.
	 * @param pCaseSensitive True, if the created matcher should be case sensitive.
	 *   False, if it should be case insensitive.
	 * @return A matcher, which meets the above criteria.
	 */
	public static IMatcher newMatcher(String[] pIncludes, String[] pExcludes, boolean pCaseSensitive) {
		final IMatcher[] includes = asMatchers(pIncludes, pCaseSensitive);
		final IMatcher[] excludes = asMatchers(pExcludes, pCaseSensitive);
		return newMatcher(includes, excludes);
	}
	/**
	 * Creates a new instance of {@link IMatcher}, which accepts a file name,
	 * if it matches either of the include matchers, but none of the exclude matchers.
	 * @param pIncludes The include matchers, that the created matcher should accept.
	 * @param pExcludes The exclude matchers, that the created matcher should rejept.
	 * @return A matcher, which meets the above criteria.
	 */
	public static IMatcher newMatcher(IMatcher[] pIncludes, IMatcher[] pExcludes) {
		if (isTrivial(pIncludes)) {
			if (isTrivial(pExcludes)) {
				return new IMatcher() {
					@Override
					public boolean isMatchingAll() {
						return true;
					}

					@Override
					public boolean test(String pUri) {
						return true;
					}
				};
			} else {
				final Predicate<String> predicate = newPredicate(pExcludes);
				return new IMatcher() {
					@Override
					public boolean test(String pUri) {
						return !predicate.test(pUri);
					}
				};
			}
		} else {
			if (isTrivial(pExcludes)) {
				final Predicate<String> predicate = newPredicate(pIncludes);
				return new IMatcher() {
					@Override
					public boolean test(String pUri) {
						return predicate.test(pUri);
					}
				};
			} else {
				final Predicate<String> i = newPredicate(pIncludes);
				final Predicate<String> e = newPredicate(pExcludes);
				return new IMatcher() {
					@Override
					public boolean test(String pUri) {
						return i.test(pUri)  &&  !e.test(pUri);
					}
				};
			}
		}
	}

	private static IMatcher[] asMatchers(String[] pPatterns, boolean pCaseSensitive) {
		if (pPatterns == null  ||  pPatterns.length == 0) {
			return null;
		}
		final IMatcher[] matchers = new IMatcher[pPatterns.length];
		for (int i = 0;  i < matchers.length;  i++) {
			matchers[i] = new DefaultMatcher(pPatterns[i], pCaseSensitive);
		}
		return matchers;
	}
	private static boolean isTrivial(IMatcher[] pMatchers) {
		if (pMatchers == null  ||  pMatchers.length == 0) {
			return true;
		}
		for (IMatcher m : pMatchers) {
			if (!m.isMatchingAll()) {
				return false;
			}
		}
		return true;
	}

	private static Predicate<String> newPredicate(IMatcher[] pMatchers) {
		return (s) -> {
			for (IMatcher m : pMatchers) {
				if (m.test(s)) {
					return true;
				}
			}
			return false;
		};
	}
}
