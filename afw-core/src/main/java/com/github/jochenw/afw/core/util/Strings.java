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
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;


/** Utility class for working with strings. As you are always working with strings,
 * this class ought to be useful always.
 */
public class Strings {
	/** Creates a new instance. Private constructor, because
	 * all methods are static.
	 */
	private Strings() {}

	/** Returns the given value, if it is non-null, and non-empty, or
	 * the default value.
	 * @param pValue The value to check for null, or empty.
	 * @param pDefault The non-null, and non-empty default value.
	 * @return A non-null, and non-empty string: Either the given value,
	 * if that is non-null, and non-empty, or the default value.
	 */
	public static String notEmpty(String pValue, String pDefault) {
		if (isEmpty(pValue)) {
			return pDefault;
		} else {
			return pValue;
		}
	}

	/** Returns the given value, if it is non-null, and non-empty, or
	 * the default value.
	 * @param pValue The value to check for null, or empty.
	 * @param pDefault The non-null, and non-empty default value.
	 * @return A non-null, and non-empty string: Either the given value,
	 * if that is non-null, and non-empty, or the default value.
	 */
	public static String notEmpty(String pValue, Supplier<String> pDefault) {
		if (isEmpty(pValue)) {
			return pDefault.get();
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
	public static @NonNull String requireNonNull(@Nullable String pValue, @NonNull String pMessage) {
		return Objects.requireNonNull(pValue, pMessage);
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
     * For all practical purposes, this is identical with
     * {@link #append(Appendable, Object...)}, except that it doesn't throw
     * an {@link IOException}.
     * @param pSb The target stream.
     * @param pArgs The objects being written.
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
     * For all practical purposes, this is identical with
     * {@link #append(Appendable, Object)}, except that it doesn't throw
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

	/**
	 * Returns the given value, if it isn't null, or empty. Otherwise,
	 * throws an Exception.
	 * @param pValue The string value, that is being checked.
	 * @param pName A description of the value, for use in error messages.
	 * @return The given value, if it isn't null, or empty. Otherwise,
	 * the given default value.
	 */
	public static String requireNonEmpty(String pValue, String pName) {
		if (pValue == null) {
			throw new NullPointerException("String value must not be null: " + pName);
		}
		if (pValue.length() == 0) {
			throw new IllegalArgumentException("String value must not be empty: " + pName);
		}
		return pValue;
	}

	/**
	 * Returns the given value, if it isn't null, or empty (after trimming).
	 * Otherwise, throws an Exception.
	 * @param pValue The string value, that is being checked.
	 * @param pName A description of the value, for use in error messages.
	 * @return The given value, if it isn't null, or empty. Otherwise,
	 * the given default value.
	 */
	public static String requireTrimmedNonEmpty(String pValue, String pName) {
		requireNonEmpty(pValue, pName);
		if (pValue.trim().length() == 0) {
			throw new IllegalArgumentException("String value must not be empty (after trimming): " + pName);
		}
		return pValue;
	}

	/**
	 * Parses the given version string into a series of integer values.
	 * For example, "2.3.0" would yield the array {2, 3, 0}.
	 * @param pVersionStr The version string, that is being parsed.
	 * @return A series of integer values, that constitute the given version string.
	 */
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
				  throw new IllegalArgumentException("Invalid character " + (int) c + " in version string: " + pVersionStr);
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
	public static void formatCb(@NonNull Appendable pAppendable, @NonNull String pMsg, @Nullable Object... pArgs) throws IOException {
		if (pArgs == null) {
			formatCb(pAppendable, pMsg, (IListable<String>) null);
		} else {
			formatCb(pAppendable, pMsg, IListable.of(pArgs));
		}
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
	public static void formatCb(@NonNull Appendable pAppendable, @NonNull String pMsg, @Nullable IListable<?> pArgs) throws IOException {
		int argOffset = 0;
		int offset = 0;
		while (offset < pMsg.length()) {
			final char c = pMsg.charAt(offset++);
			if (c == '{'  &&  offset < pMsg.length()) {
				final char c2 = pMsg.charAt(offset++);
				if (c2 == '}') {
					if (pArgs == null) {
						throw new IllegalArgumentException("Format string requires at least one argument, but none are given.");
					} else if (argOffset >= pArgs.getSize()) {
						throw new IllegalArgumentException("Format string requires at least " + argOffset
								                           + " arguments, but only " + pArgs.getSize()
								                           + " are given.");
					}
					final Object arg = pArgs.getItem(argOffset++);
					append(pAppendable, arg);
				}
			} else {
				pAppendable.append(c);
			}
		}
		if (argOffset > 0) {
			if (pArgs == null) {
				throw new IllegalArgumentException("Format string requires at least one argument, but none are given.");
			} else if (argOffset < pArgs.getSize()) {
				throw new IllegalArgumentException("Format string requires only " + argOffset
						                           + " arguments, but " + pArgs.getSize()
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
	public static @NonNull String formatCb(@NonNull String pMsg, @Nullable Object... pArgs) {
		final StringBuilder sb = new StringBuilder();
		try {
			formatCb(sb, pMsg, pArgs);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		@SuppressWarnings("null")
		final @NonNull String result = sb.toString();
		return result;
	}

	/**
	 * Formats the given number with leading zeroes.
	 * @param pItem The number being formatted.
	 * @param pTotal The number of items.
	 * @return The number {@code pItem}, possibly prepended with leading zeroes.
	 */
	public static @NonNull String formatLz(int pItem, int pTotal) {
		@SuppressWarnings("null")
		@NonNull Integer item = Integer.valueOf(pItem);
		if (pTotal > 999999) {
			throw new IllegalArgumentException("The total number of items must be lower than " + (999999 +1));
		} else if (pTotal > 99999) {
			return format("%06d", item);
		} else if (pTotal > 9999) {
			return format("%05d", item);
		} else if (pTotal > 999) {
			return format("%04d", item);
		} else if (pTotal > 99) {
			return format("%03d", item);
		} else if (pTotal > 9) {
			return format("%02d", item);
		} else {
			return format("%d", item);
		}
	}

	/** Null-safe replacement for {@link String#format(String, Object...)}.
	 * @param pFmtString The format string.
	 * @param pParams The parameters, if any.
	 * @return The formatted string, with the parameters applied.
	 */
	public static @NonNull String format(@NonNull String pFmtString, Object... pParams) {
		@SuppressWarnings("null")
		final @NonNull String formattedString = String.format(pFmtString, pParams);
		return formattedString;
	}

	/** Creates a predicate, which decides, whether an input string matches
	 * the given matcher description. The matcher description will be
	 * interpreted as follows:
	 * <ol>
	 *   <li>A leading '!' (if present), inverts the matchers meaning: The matcher
	 *     will match input strings, that do *not* match the remaining pattern.</li>
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
	@SuppressWarnings("null")
	public static @NonNull Predicate<String> matcher(@NonNull String pMatcher) {
		@NonNull String matcher = Objects.requireNonNull(pMatcher, "Matcher");
		final boolean excluding;
		if (matcher.startsWith("!")) {
			excluding = true;
			final @NonNull String match = matcher.substring(1);
			matcher = match; 
		} else {
			excluding = false;
		}
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
				final String lcMatcher = matcher.toLowerCase(Locale.getDefault());
				return (s) -> {
					final boolean stringMatch = s.toLowerCase(Locale.getDefault()).equals(lcMatcher);
					return excluding ? !stringMatch : stringMatch;
				};
			} else {
				final String m = matcher;
				return (s) -> {
					final boolean stringMatch = s.equals(m);
					return excluding ? !stringMatch : stringMatch;
				};
			}
		}
		final Pattern pattern;
		if (caseInsensitive) {
			pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
		} else {
			pattern = Pattern.compile(patternStr);
		}
		return (s) -> {
			final boolean patternMatch = pattern.matcher(s).matches();
			if (excluding) {
				return !patternMatch;
			} else {
				return patternMatch;
			}
		};
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
	public static @NonNull Predicate<String> matchers(@NonNull String pMatchers, @NonNull String pSeparator) {
		final @NonNull String matchers = Objects.requireNonNull(pMatchers, "Matchers");
		final @NonNull String separator = Objects.requireNonNull(pSeparator, "Separator");
		final List<Predicate<String>> includingPredicates = new ArrayList<>();
		final List<Predicate<String>> excludingPredicates = new ArrayList<>();
		for (StringTokenizer st = new StringTokenizer(matchers, separator);  st.hasMoreTokens();  ) {
			final String matcher = st.nextToken();
			if (matcher.startsWith("!")) {
				@SuppressWarnings("null")
				final @NonNull String invertedMatcher = matcher.substring(1);
				excludingPredicates.add(matcher(invertedMatcher));
			} else {
				includingPredicates.add(matcher(matcher));
			}
		}
		if (includingPredicates.isEmpty()  &&  excludingPredicates.isEmpty()) {
			throw new IllegalArgumentException("No matcher definitions found.");
		}
		return (s) -> {
			boolean included;
			if (includingPredicates.isEmpty()) {
				included = true;
			} else {
				included = false;
				for (Predicate<String> pred : includingPredicates) {
					if (pred.test(s)) {
						included = true;
						break;
					}
				}
			}
			if (included) {
				if (!excludingPredicates.isEmpty()) {
					for (Predicate<String> pred : excludingPredicates) {
						if (pred.test(s)) {
							return false;
						}
					}
				}
				return true;
			} else {
				return false;
			}
		};
	}

	/** Parses the given value into a list of strings by splitting the input
	 * value along the given separator. This is mostly like {@link String#split(String)},
	 * except that the separator is not a regular expression, so safe for use with
	 * special characters.
	 * @param pValue The string, that is being splitted.
	 * @param pSeparator The separator string.
	 * @return The splitted string.
	 */
	public static List<String> split(String pValue, String pSeparator) {
		String value = Objects.requireNonNull(pValue, "Value");
		final String sep = Objects.requireNonNull(pSeparator, "Separator");
		final List<String> result = new ArrayList<>();
		while (value.length() > 0) {
			final int offset = value.indexOf(sep);
			if (offset == -1) {
				result.add(value);
				return result;
			} else {
				result.add(value.substring(0, offset));
				value = value.substring(offset + sep.length());
			}
		}
		return result;
	}

	/**
	 * Basically same as {@link Arrays#asList(Object...)}, except that this method
	 * returns an {@link ArrayList} (a modifiable list).
	 * @param pValues The list values.
	 * @return An {@link ArrayList} (a modifiable list) with the given values.
	 * @throws NullPointerException The {@code pValues} parameter is null.
	 */
	public static @NonNull ArrayList<String> list(@NonNull String... pValues) {
		final @NonNull String[] values = Objects.requireNonNull(pValues, "Values");
		final ArrayList<String> list = new ArrayList<>(values.length);
		for (String s : values) {
			list.add(s);
		}
		return list;
	}

	/**
	 * Returns a string array with the given values.
	 * @param pValues The array values.
	 * @return A string array with the given values.
	 * @throws NullPointerException The {@code pValues} parameter is null.
	 */
	public static @NonNull String[] array(@NonNull String... pValues) {
		final @NonNull String[] values = Objects.requireNonNull(pValues, "Values");
		return values;
	}

	/** Concatenates all the names in the given enum class, using the given
	 * separator
	 * @param pSep The separator string.
	 * @param pEnumType The enum class.
	 * @return The conatenated names.
	 */
	public static String join(String pSep, Class<? extends Enum<?>> pEnumType) {
		final Enum<?>[] enums;
		try {
			final Method method = Objects.requireNonNull(pEnumType, "EnumType").getDeclaredMethod("values");
			enums = (Enum<?>[]) method.invoke(null);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < enums.length;  i++) {
			if (i > 0) {
				sb.append(pSep);
			}
			sb.append(enums[i].name());
		}
		return sb.toString();
	}

	/** Converts the given {@code pString} into a stream of tokens,
	 * which are separated by the given {@code pDelimiter}. This is mainly
	 * equivalent to the {@link StringTokenizer}, except that the latter
	 * interprets the delimiter as a set of characters, rater than a
	 * separator string.
	 * @param pDelimiter The separator string, that is supposed to
	 *   separate the various tokens.
	 * @param pString The string, that is being tokenized.
	 * @param pConsumer A consumer, which is being invoked the tokens in
	 *   the order of occurrence.
	 */
	public static void tokenize(@NonNull String pDelimiter,
			                    @NonNull String pString,
			                    @NonNull FailableConsumer<String,?> pConsumer) {
		String string = Objects.requireNonNull(pString, "String");
		final String delim = Objects.requireNonNull(pDelimiter, "Delimiter");
		while (string.length() > 0) {
			final int offset = string.indexOf(delim);
			if (offset == -1) {
				Functions.accept(pConsumer, string);
				break;
			} else {
				Functions.accept(pConsumer, string.substring(0, offset));
				string = string.substring(offset + delim.length());
			}
		}
	}

	/** Converts the given {@code pString} into a stream of tokens,
	 * which are separated by the given {@code pDelimiter}. This is mainly
	 * equivalent to the {@link StringTokenizer}, except that the latter
	 * interprets the delimiter as a set of characters, rater than a
	 * separator string.
	 * @param pDelimiter The separator string, that is supposed to
	 *   separate the various tokens.
	 * @param pString The string, that is being tokenized.
	 * @return The tokens, that were found.
	 */
	public static @NonNull List<String> tokenize(@NonNull String pDelimiter, @NonNull String pString) {
		List<String> tokens = new ArrayList<>();
		tokenize(pDelimiter, pString, tokens::add);
		return tokens;
	}

	/** Creates a string representation of the list, or collection {@code pValues},
	 * like {@link String#join(CharSequence, Iterable)}, except that the current
	 * method accepts arbitrary lists.
	 * @param pContractAndEbIds The value list, which is being converted into a string
	 *   representation.
	 * @param pSeparator String, which separates two consecutive elements in the
	 *   string representation.
	 * @param pTcIdMapper A mapping function, which will be invoked for every element
	 *   in the list, in order to create a string representation of a single
	 *   element.
	 * @param <O> Element type of the list
	 * @return The created string representation.
	 * @throws NullPointerException Either of the parameters is null.
	 */
	public static <O> String joinList(Iterable<O> pContractAndEbIds, CharSequence pSeparator, FailableFunction<O, String, ?> pTcIdMapper) {
		final CharSequence separator = Objects.requireNonNull(pSeparator, "Separator");
		final Iterable<O> values = Objects.requireNonNull(pContractAndEbIds, "Values");
		final FailableFunction<O,String,?> mapper = Objects.requireNonNull(pTcIdMapper, "Mapper");
		final StringBuilder sb = new StringBuilder();
		CharSequence sep = null;
		for (O o : values) {
			if (sep == null) {
				sep = separator;
			} else {
				sb.append(sep);
			}
			sb.append(Functions.apply(mapper, o));
		}
		return sb.toString();
	}

	/** Creates a string representation of the array {@code pValues},
	 * like {@link String#join(CharSequence, CharSequence[])}, except that the current
	 * method accepts arbitrary arrays.
	 * @param <O> Element type of the list
	 * @param pSeparator String, which separates two consecutive elements in the
	 *   string representation.
	 * @param pValues The value list, which is being converted into a string
	 *   representation.
	 * @param pMapper A mapping function, which will be invoked for every element
	 *   in the list, in order to create a string representation of a single
	 *   element.
	 * @return The created string representation.
	 * @throws NullPointerException Either of the parameters is null.
	 * @see #joinList(Iterable, CharSequence, Functions.FailableFunction)
	 */
	public static <O> String join(CharSequence pSeparator, O[] pValues, FailableFunction<O,String,?> pMapper) {
		final O[] array = Objects.requireNonNull(pValues, "Values");
		return joinList(Arrays.asList(array), pSeparator, pMapper);
	}

	/** Creates a string representation of the given {@code pValues},
	 * like {@link #join(String, String[])}, except that the current
	 * method accepts arbitrary values.
	 * @param <O> Element type of the values
	 * @param pSeparator String, which separates two consecutive elements in the
	 *   string representation.
	 * @param pMapper A mapping function, which will be invoked for every element
	 *   in the list, in order to create a string representation of a single
	 *   element.
	 * @param pValues The value list, which is being converted into a string
	 *   representation.
	 * @return The created string representation.
	 * @throws NullPointerException Either of the parameters is null.
	 * @see #joinList(Iterable, CharSequence, Functions.FailableFunction)
	 */
	public static <O> String join(CharSequence pSeparator, FailableFunction<O,String,?> pMapper,
			                      @SuppressWarnings("unchecked") O... pValues) {
		final O[] array = Objects.requireNonNull(pValues, "Values");
		return joinList(Arrays.asList(array), pSeparator, pMapper);
	}

	/** Replacement for {@code new URL(String)}, which is deprecated, as of
	 * Java 20.
	 * @param pUrlStr The string, which is being converted into an URL.
	 * @return The converted URL, if any.
	 * @throws NullPointerException The parameter {@code pUrlStr} is null.
	 * @throws IllegalArgumentException The parameter {@code pUrlStr} is empty.
	 * @throws MalformedURLException The parameter {@code pUrlStr} is otherwise invalid.
	 */
	@SuppressWarnings("deprecation")
	public static @NonNull URL asUrl(@NonNull String pUrlStr) throws MalformedURLException {
		final String urlStr = Objects.requireNonNull(pUrlStr, "UrlStr");
		if (urlStr.trim().length() == 0) {
			throw new IllegalArgumentException("The parameter UrlStr is empty.");
		}
		return new URL(urlStr);
	}
}

