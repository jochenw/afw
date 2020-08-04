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
package com.github.jochenw.afw.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/** Utility class for working with strings. As you are always working with strings,
 * this class ought to be useful always.
 */
public class Strings {
	/** Returns the given value, if it is non-null, or the empty string ("").
	 * @param pValue The value to check for null.
	 * @return A non-null string: Either the given value, if that is non-null,
	 *   or the empty string ("").
	 */
	public static @Nonnull String notNull(@Nullable String pValue) {
		return notNull(pValue, "");
	}
	/** Returns the given value, if it is non-null, or the default value.
	 * @param pValue The value to check for null.
	 * @param pDefault The non-null default value.
	 * @return A non-null string: Either the given value, if that is non-null,
	 *   or the default value.
	 */
	public static @Nonnull String notNull(@Nullable String pValue, @Nonnull String pDefault) {
		return Objects.notNull(pValue, pDefault);
	}
	/** Returns the given value, if it is non-null, and non-empty, or
	 * the default value.
	 * @param pValue The value to check for null, or empty.
	 * @param pDefault The non-null, and non-empty default value.
	 * @return A non-null, and non-empty string: Either the given value,
	 * if that is non-null, and non-empty, or the default value.
	 */
	public static String notEmpty(String pValue, String pDefault) {
		if (pValue == null  ||  pValue.length() == 0) {
			return pDefault;
		} else {
			return pValue;
		}
	}

	/** Asserts, that the given value is non-null. An {@link NullPointerException}
	 * is thrown otherwise.
	 * @param pValue The value being checked for non-null.
	 * @param pMessage If the message is non-null, and the check fails, then this
	 *   will be used as the thrown exceptions message. Otherwise, a default
	 *   message will be used.
	 * @return The input value, if it is non-null, indeed.
	 * @throws NullPointerException The check failed, because the input value
	 *   is null.
	 */
	public static @Nonnull String requireNonNull(@Nullable String pValue, @Nonnull String pMessage) {
		return Objects.requireNonNull(pValue, pMessage);
	}

	/** Converts the given string into a byte array, using the UTF-8
	 * character set.
	 * @param pContents The string veing converted.
	 * @return The converted string.
	 * @throws UncheckedIOException The conversion failed.
	 */
	public static byte[] getBytes(String pContents) {
        return getBytes(pContents, "UTF-8");
    }

	/** Converts the given string into a byte array, using the given
	 * character set.
	 * @param pContents The string veing converted.
	 * @param pCharset The character set being used.
	 * @return The converted string.
	 * @throws UncheckedIOException The conversion failed.
	 */
    public static byte[] getBytes(String pContents, String pCharset) {
        try {
            return pContents.getBytes(pCharset);
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
    }

    /** Converts the given object to a string, using the
     * {@link Object#toString()} method.
     * @param pArg The object to convert into a string.
     * @return The converted object.
     * @see #append(Appendable, Object)
     */
    public static String toString(Object pArg) {
        if (pArg == null) {
            return "null";
        } else if (pArg.getClass().isArray()
                   ||  pArg instanceof Collection
                   ||  pArg instanceof Map) {
            final StringBuilder sb = new StringBuilder();
            append(sb, pArg);
            return sb.toString();
        } else {
            return String.valueOf(pArg);
        }
    }

    /** Writes the given objects as a string to the given {@link StringBuilder}.
     * @param pSb The target stream.
     * @param pArgs The objects being written.
	 * @throws UncheckedIOException The writing failed.
     */
    public static void append(StringBuilder pSb, Object... pArgs) {
        try {
            append((Appendable) pSb, pArgs);
        } catch (IOException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
    }

    /** Writes the given objects as a string to the given {@link Appendable}.
     * @param pAppendable The target stream.
     * @param pArgs The objects being written.
	 * @throws IOException The writing failed.
     */
    public static void append(Appendable pAppendable, Object... pArgs) throws IOException {
        if (pArgs == null) {
            pAppendable.append("null");
        } else {
            for (int i = 0;  i < pArgs.length;  i++) {
                if (i > 0) {
                    pAppendable.append(", ");
                }
                append(pAppendable, pArgs[i]);
            }
        }
    }

    /** Writes the given object as a string to the given {@link StringBuilder}.
     * @param pSb The target stream.
     * @param pArg The object being written.
	 * @throws UncheckedIOException The writing failed.
     */
    public static void append(StringBuilder pSb, Object pArg) {
        try {
            append((Appendable) pSb, pArg);
        } catch (IOException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
    }

    /** Writes the given object as a string to the given {@link Appendable}.
     * @param pAppendable The target stream.
     * @param pArg The objects being written.
	 * @throws IOException The writing failed.
     */
    public static void append(Appendable pAppendable, Object pArg) throws IOException {
        if (pArg == null) {
            pAppendable.append("null");
        } else {
            if (pArg.getClass().isArray()) {
                pAppendable.append('[');
                for (int i = 0;  i < Array.getLength(pArg);  i++) {
                    if (i > 0) {
                        pAppendable.append(", ");
                    }
                    append(pAppendable, Array.get(pArg, i));
                }
                pAppendable.append(']');
            } else if (pArg instanceof List) {
                final List<?> list = (List<?>) pArg;
                pAppendable.append('<');
                for (int i = 0;  i < list.size();  i++) {
                    if (i > 0) {
                        pAppendable.append(", ");
                    }
                    append(pAppendable, list.get(i));
                }
                pAppendable.append('>');
            } else if (pArg instanceof Map) {
                final Map<?,?> map = (Map<?,?>) pArg;
                pAppendable.append('{');
                boolean first = true;
                for (Map.Entry<?,?> en : map.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        pAppendable.append(", ");
                    }
                    append(pAppendable, en.getKey());
                    pAppendable.append(" => ");
                    append(pAppendable, en.getValue());
                }
                pAppendable.append('}');
            } else if (pArg instanceof Collection) {
                final Collection<?> collection = (Collection<?>) pArg;
                pAppendable.append('(');
                boolean first = true;
                for (Iterator<?> iter = collection.iterator();  iter.hasNext();  ) {
                    if (first) {
                        first = false;
                    } else {
                        pAppendable.append(", ");
                    }
                    append(pAppendable, iter.next());
                }
                pAppendable.append(')');
            } else {
                pAppendable.append(String.valueOf(pArg));
            }
        }
    }

    /** Converts the given String into an array of lines.
     * @param pValue The string to split into lines.
     * @return The array of lines, which has been read from the input string.
     */
	public static String[] toLines(String pValue) {
		final StringReader sr = new StringReader(pValue);
		final BufferedReader br = new BufferedReader(sr);
		final List<String> list = new ArrayList<>();
		try {
			for (;;) {
				final String line = br.readLine();
				if (line == null) {
					return list.toArray(new String[list.size()]);
				} else {
					list.add(line);
				}
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Returns, whether the given value is null, or empty.
	 * @param pValue The value being checked.
	 * @return True, if the value is null, or empty, otherwise false.
	 */
	public static boolean isEmpty(String pValue) {
		return pValue == null  ||  pValue.length() == 0;
	}

	/**
	 * Returns, whether the given value is null, or empty, after trimming.
	 * @param pValue The value being checked.
	 * @return True, if the value is null, or empty, after trimming,
	 *   otherwise false.
	 */
	public static boolean isTrimmedEmpty(String pValue) {
		return pValue == null  ||  pValue.trim().length() == 0;
	}

	public static String requireNonEmpty(String pValue, String pName) {
		if (pValue == null) {
			throw new NullPointerException("String value must not be null: " + pName);
		}
		if (pValue.length() == 0) {
			throw new IllegalArgumentException("String value must not be empty: " + pName);
		}
		return pValue;
	}

	public static void requireTrimmedNonEmpty(String pValue, String pName) {
		requireNonEmpty(pValue, pName);
		if (pValue.trim().length() == 0) {
			throw new NullPointerException("String value must not be empty (after trimming): " + pName);
		}
	}

	public static int[] parseVersionNumber(String pVersionStr) {
		if (isTrimmedEmpty(pVersionStr)) {
			throw new IllegalArgumentException("Missing, or empty, version string");
		}
		final List<Integer> numbers = new ArrayList<>();
		final StringBuffer sb = new StringBuffer();
		for (int i = 0;  i < pVersionStr.length();  i++) {
			final char c = pVersionStr.charAt(i);
			switch (c) {
			  case '.':
				if (sb.length() == 0) {
					throw new IllegalArgumentException("Invalid version string: " + pVersionStr);
				} else {
					final Integer num = Integer.valueOf(sb.toString());
					numbers.add(num);
					sb.setLength(0);
				}
				break;
			  case '0':
			  case '1':
			  case '2':
			  case '3':
			  case '4':
			  case '5':
			  case '6':
			  case '7':
			  case '8':
			  case '9':
				sb.append(c);
				break;
			  default:
				  throw new IllegalStateException("Invalid character in version string: " + pVersionStr);
			}			
		}
		if (sb.length() == 0) {
			throw new IllegalArgumentException("Invalid version string, ends with '.': " + pVersionStr);
		}
		final Integer num = Integer.valueOf(sb.toString());
		numbers.add(num);
		final int[] result = new int[numbers.size()];
		for (int i = 0;  i < result.length;  i++) {
			result[i] = numbers.get(i).intValue();
		}
		return result;
	}
	/**
	 * Concatenates the given values, using the given separator.
	 * @param pSep The separator to use.
	 * @param pValues The values being concatenated.
	 * @return The concatenations result.
	 */
	public static String join(String pSep, String... pValues) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < pValues.length;  i++) {
			if (i > 0) {
				if (pSep != null) {
					sb.append(pSep);
				}
			}
			sb.append(pValues[i]);
		}
		return sb.toString();
	}
	/**
	 * Concatenates the given values, using the given separator.
	 * @param pSep The separator to use.
	 * @param pValues The values being concatenated.
	 * @return The concatenations result.
	 */
	public static String join(String pSep, Iterable<String> pValues) {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String v : pValues) {
			if (first) {
				first = false;
			} else {
				if (pSep != null) {
					sb.append(pSep);
				}
			}
			sb.append(v);
		}
		return sb.toString();
	}

	/**
	 * Writes the given format string to the given appendable. The format string may
	 * contain curly brace tokens "{}", which are being replaced by the respective
	 * argument object.
	 * 
	 * Example:
	 * <pre>
	 *   final StringBuilder sb = new StringBuilder();
	 *   Strings.formatCb(sb, "Writing to {} failed with the following error: {}.",
	 *                    "myfile", "Out of disk space.");
	 *   // -&gt; Writing to myfile failed with the following error: Out of disk space.
	 * </pre>
	 *     
	 * @param pAppendable The appendable, to which text is being written.
	 * @param pMsg The format string.
	 * @param pArgs The arguments, which are being written.
	 * @throws IOException Appending to the {@link Appendable appendable} failed.
	 */
	public static void formatCb(@Nonnull Appendable pAppendable, @Nonnull String pMsg, @Nullable Object... pArgs) throws IOException {
		int argOffset = 0;
		int offset = 0;
		while (offset < pMsg.length()) {
			final char c = pMsg.charAt(offset++);
			if (c == '{'  &&  offset < pMsg.length()) {
				final char c2 = pMsg.charAt(offset++);
				if (c2 == '}') {
					if (pArgs == null) {
						throw new IllegalArgumentException("Format string requires at least one argument, but none are given.");
					} else if (argOffset >= pArgs.length) {
						throw new IllegalArgumentException("Format string requires at least " + argOffset
								                           + " arguments, but only " + pArgs.length
								                           + " are given.");
					}
					final Object arg = pArgs[argOffset++];
					append(pAppendable, arg);
				}
			} else {
				pAppendable.append(c);
			}
		}
		if (argOffset > 0) {
			if (pArgs == null) {
				throw new IllegalArgumentException("Format string requires at least one argument, but none are given.");
			} else if (argOffset < pArgs.length) {
				throw new IllegalArgumentException("Format string requires only " + argOffset
						                           + " arguments, but " + pArgs.length
						                           + " are given.");
			}
		}
	}

	/**
	 * Returns the given format string, with the given arguments being applied: The
	 * format string may contain curly brace tokens "{}", which are being replaced
	 * by the respective argument object.
	 * @param pMsg The format string, which may contain curly brace tokens ("{}").
	 * @param pArgs The arguments, which ought to replace the curly brace tokens.
	 * @return The formatted string, with the curly brace tokens replaced.
	 */
	public static @Nonnull String formatCb(@Nonnull String pMsg, @Nullable Object... pArgs) {
		final StringBuilder sb = new StringBuilder();
		try {
			formatCb(sb, pMsg, pArgs);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		return sb.toString();
	}

	/**
	 * Formats the given number with leading zeroes.
	 * @param pItem The number being formatted.
	 * @param pTotal The number of items.
	 * @return The number {@code pItem}, possibly prepended with leading zeroes.
	 */
	public static @Nonnull String formatLz(int pItem, int pTotal) {
		Integer item = Integer.valueOf(pItem);
		if (pTotal > 999999) {
			throw new IllegalArgumentException("The total number of items must be lower than " + (999999 +1));
		} else if (pTotal > 99999) {
			return String.format("%06d", item);
		} else if (pTotal > 9999) {
			return String.format("%05d", item);
		} else if (pTotal > 999) {
			return String.format("%04d", item);
		} else if (pTotal > 99) {
			return String.format("%03d", item);
		} else if (pTotal > 9) {
			return String.format("%02d", item);
		} else {
			return String.format("%d", item);
		}
	}

	/** Creates a predicate, which decides, whether an input string matches
	 * the given matcher description. The matcher description will be
	 * interpreted as follows:
	 * <ol>
	 *   <li>A regular expression, introduced by the prefix "re:".</li>
	 *   <li>A glob pattern, containing either of the characters
	 *     '?' (a single, but arbitrary character), or '*' (an arbitrary
	 *     number of arbitrary characters).</li>
	 *   <li>If the matcher string doesn't have the prefix "re:",
	 *     and doesn't contain the characters '?', or '*', then
	 *     
	 * </ol>
	 * @param pMatcher The matcher description.
	 * @return A predicate, which tests, whether the given description
	 *   matches it's input string.
	 */
	public static @Nonnull Predicate<String> matcher(@Nonnull String pMatcher) {
		@Nonnull String matcher = Objects.requireNonNull(pMatcher, "Matcher");
		final boolean caseInsensitive;
		if (matcher.endsWith("/i")) {
			caseInsensitive = true;
			matcher = matcher.substring(0, matcher.length()-2);
		} else {
			caseInsensitive = false;
		}
		final String patternStr;
		if (matcher.startsWith("re:")) {
			patternStr = matcher.substring("re:".length());
		} else if (matcher.indexOf('*') != -1  ||  matcher.indexOf('?') != -1) {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0;  i < matcher.length();  i++) {
				final char c = matcher.charAt(i);
				if ('*' == c) {
					sb.append(".*");					
				} else if ('?' == c) {
					sb.append(".");
				} else if (Character.isLetterOrDigit(c)) {
					sb.append(c);
				} else {
					sb.append("\\");
					sb.append(c);
				}
			}
			patternStr = sb.toString();
		} else {
			if (caseInsensitive) {
				final String lcMatcher = matcher.toLowerCase();
				return (s) -> s.toLowerCase().equals(lcMatcher);
			} else {
				final String m = matcher;
				return (s) -> s.equals(m);
			}
		}
		final Pattern pattern;
		if (caseInsensitive) {
			pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		} else {
			pattern = Pattern.compile(patternStr);
		}
		return (s) -> pattern.matcher(s).matches();
	}
	
	/** Parses the given string into a list of matchers, by tokenizing the string,
	 * using the given separator. For each of the tokens, a subpredicate is created by
	 * invoking {@link #matcher(String)}. The returned predicate will be true, if
	 * either of the subpredicates is true.
	 * 
	 * Example: The matcher string {@code Wm*,Wx*,Foo} with the separator ","
	 * will create three subpredicates. The first subpredicate will be true,
	 * if the tested string has the prefix "Wm". Likewise, the second
	 * subpredicate matches the prefix "Wx". Finally the third
	 * subpredicate is an exact match for the string "Foo".
	 * In summary, the returned predicate will be true, if the input string is
	 * "Foo", or if it starts with "Wx", or "Wm".
	 * @param pMatchers The list of subpredicate descriptions, separated by
	 *   {@code pSeparator}.
	 * @param pSeparator The string, which separates the submatcher strings.
	 * @return A predicate, which is true, if either of the subpredicates,
	 *   created from {@code pMatchers} is true.
	 */
	public static @Nonnull Predicate<String> matchers(@Nonnull String pMatchers, @Nonnull String pSeparator) {
		final @Nonnull String matchers = Objects.requireNonNull(pMatchers, "Matchers");
		final @Nonnull String separator = Objects.requireNonNull(pSeparator, "Separator");
		final List<Predicate<String>> predicates = new ArrayList<>();
		for (StringTokenizer st = new StringTokenizer(matchers, separator);  st.hasMoreTokens();  ) {
			final String matcher = st.nextToken();
			predicates.add(matcher(matcher));
		}
		if (predicates.isEmpty()) {
			throw new IllegalArgumentException("No matcher definitions found.");
		}
		return (s) -> {
			for (Predicate<String> pred : predicates) {
				if (pred.test(s)) {
					return true;
				}
			}
			return false;
		};
	}
}
