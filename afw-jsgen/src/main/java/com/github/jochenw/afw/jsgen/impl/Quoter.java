package com.github.jochenw.afw.jsgen.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Quoter {
	@Nonnull public static final String valueOf(@Nullable String pValue) {
		if (pValue == null) {
			return "null";
		} else if (pValue.length() == 0) {
			return "\"\"";
		} else {
			final StringBuilder sb = new StringBuilder();
			sb.append('"');
			for (int i = 0;  i < pValue.length();  i++) {
				final char c = pValue.charAt(i);
				switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					sb.append(c);
				}
			}
			sb.append('"');
			return sb.toString();
		}
	}
}
