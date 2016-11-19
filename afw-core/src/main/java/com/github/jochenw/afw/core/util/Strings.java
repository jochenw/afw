package com.github.jochenw.afw.core.util;

import java.io.BufferedReader;
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
}
