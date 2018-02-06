package com.github.jochenw.afw.rcm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Strings {
	public static String notNull(String pValue) {
		return notNull(pValue, "");
	}
	public static String notNull(String pValue, String pDefault) {
		return Objects.notNull(pValue, pDefault);
	}
	public static String requireNonNull(String pValue, String pMessage) {
		return Objects.requireNonNull(pValue, pMessage);
	}

	public static byte[] getBytes(String pContents) {
        return getBytes(pContents, "UTF-8");
    }

    public static byte[] getBytes(String pContents, String pCharset) {
        try {
            return pContents.getBytes(pCharset);
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
    }

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
    public static void append(StringBuilder pSb, Object... pArgs) {
        try {
            append((Appendable) pSb, pArgs);
        } catch (IOException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
    }
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

    public static void append(StringBuilder pSb, Object pArg) {
        try {
            append((Appendable) pSb, pArg);
        } catch (IOException e) {
            throw Exceptions.newUncheckedIOException(e);
        }
    }

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

	public static boolean isEmpty(String pCode) {
		return pCode == null  ||  pCode.length() == 0;
	}

	public static boolean isTrimmedEmpty(String pCode) {
		return pCode == null  ||  pCode.trim().length() == 0;
	}

	public static void requireNonEmpty(String pValue, String pName) {
		if (pValue == null) {
			throw new NullPointerException("String value must not be null: " + pName);
		}
		if (pValue.length() == 0) {
			throw new NullPointerException("String value must not be empty: " + pName);
		}
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

	public static String getZipEntryLocation(File zipFile, String name) {
		return "zip:///" + zipFile.getAbsolutePath() + "!" + name;
	}
}
