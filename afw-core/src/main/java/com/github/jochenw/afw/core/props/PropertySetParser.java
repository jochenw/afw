package com.github.jochenw.afw.core.props;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.Functions.FailableFunction;
import com.github.jochenw.afw.core.util.Functions.FailableTriConsumer;
import com.github.jochenw.afw.core.util.Objects;

/** A parser class for reading a {@link DocumentablePropertySet} from a stored property file.
 */
public class PropertySetParser {
	private @Nonnull String commentLineSeparator = System.lineSeparator();
	private @Nonnull String valueLineSeparator = System.lineSeparator();

	/** Returns the line separator for multi-line comments.
	 * The default value is {@link System#lineSeparator()}.
	 * @return The line separator for multi-line comments,
	 *   never null.
	 */
	public @Nonnull String getCommentLineSeparator() {
		return commentLineSeparator;
	}
	/** Returns the line separator for multi-line property values.
	 * The default value is {@link System#lineSeparator()}.
	 * @return The line separator for multi-line property values,
	 *   never null.
	 */
	public @Nonnull String getValueLineSeparator() {
		return valueLineSeparator;
	}

	/** Sets the line separator for multi-line comments.
	 * The default value is {@link System#lineSeparator()}.
	 * @param pSeparator The line separator for multi-line comments,
	 *   never null.
	 */
	public void setCommentLineSeparator(String pSeparator) {
		commentLineSeparator = Objects.requireNonNull(pSeparator, "Separator");
	}

	/** Sets the line separator for multi-line property values.
	 * The default value is {@link System#lineSeparator()}.
	 * @param pSeparator The line separator for multi-line property values,
	 *   never null.
	 */
	public void setValueLineSeparator(String pSeparator) {
		valueLineSeparator = Objects.requireNonNull(pSeparator, "Separator");
	}

	/** Parses a property set from the given {@link IReadable readable}.
	 * @param pConsumer The consumer, that receives the properties, that have been parsed.
	 * @param pReadable The document, that contains the stored property set.
	 * @param pCharset The documents character set. May be null, in which case
	 *   {@link StandardCharsets#UTF_8} will be assumed.
	 * @throws IOException Parsing the property set has failed.
	 * @throws NullPointerException The parameter {@code pConsumer}, or {@code pReadable} is null.
	 */
	public void parse(@Nonnull FailableTriConsumer<String,String,String,?> pConsumer, @Nonnull IReadable pReadable, @Nullable Charset pCharset) throws IOException {
		final Charset charset = Objects.notNull(pCharset, StandardCharsets.UTF_8);
		FailableFunction<BufferedReader, Object, ?> function = (r) -> {
			int lineNumber = 0;
			OUTER: for (;;) {
				// Read a possible comment.
				String comment = null;
				final StringBuilder commentSb = new StringBuilder();
				String actualLine = r.readLine();
				++lineNumber;
				if (actualLine == null) {
					break;
				} else {
					String line = removeLeadingWhitespace(actualLine);
					if (isCommentLine(line)) {
						commentSb.append(line.substring(1));
						for(;;) {
							actualLine = r.readLine();
							++lineNumber;
							if (actualLine == null) {
								// Property file ends with comment, but no property definition. For now, we'll simply ignore this.
								break OUTER;
							} else {
								line = removeLeadingWhitespace(actualLine);
								if (isCommentLine(line)) {
									commentSb.append(getCommentLineSeparator());
									commentSb.append(line);
								} else {
									break;
								}
							}
						}
						comment = commentSb.toString();
					}
					if (line.length() == 0) {
						// Ignore empty line.
					} else {
						final StringBuilder propertyDefinitionSb = new StringBuilder();
						final int ln = lineNumber;
						for (;;) {
							if (isLineContinued(line)) {
								propertyDefinitionSb.append(line.substring(0, line.length()-1));
								line = r.readLine();
								++lineNumber;
							} else {
								propertyDefinitionSb.append(line);
								final Function<String,RuntimeException> errorHandler = (s)-> {
									return new IllegalStateException("At file "
											+ pReadable.getName() + ", line " + ln + ": " + s);
								};
								line = decode(propertyDefinitionSb, errorHandler);
								final int offset = Math.max(line.indexOf('='), line.indexOf(':'));
								if (offset == -1) {
									throw errorHandler.apply("Expected '=', or ':' character not found.");
								} else {
									final String key = line.substring(0, offset).trim();
									final String value = removeLeadingWhitespace(line.substring(offset+1));
									pConsumer.accept(key, value, comment);
									break;
								}
							}
						}
					}
				}
			}
			return null;
		};
		pReadable.apply(function, charset);
	}

	protected boolean isLineContinued(String pLine) {
		return pLine.endsWith("\\");
	}
	protected boolean isCommentLine(String pLine) {
		return pLine.startsWith("!")  ||  pLine.startsWith("#");  
	}

	protected String removeLeadingWhitespace(String pValue) {
		int lastWhitespaceOffset = -1;
		for (int i = 0;  i < pValue.length();  i++) {
			if (Character.isWhitespace(pValue.charAt(i))) {
				lastWhitespaceOffset = i;
			} else {
				break;
			}
		}
		if (lastWhitespaceOffset == -1) {
			return pValue;
		} else {
			int offset = ++lastWhitespaceOffset;
			return pValue.substring(offset);
		}
	}

	/** Parses a property set from the given {@link IReadable readable}.
	 * @param pReadable The document, that contains the stored property set.
	 * @param pCharset The documents character set. May be null, in which case
	 *   {@link StandardCharsets#UTF_8} will be assumed.
	 * @return The property set, that has has been parsed from the document.
	 * @throws IOException Parsing the property set has failed.
	 * @throws NullPointerException The parameter {@code pReadable} is null.
	 */
	public @Nonnull DocumentablePropertySet read(@Nonnull IReadable pReadable, @Nullable Charset pCharset) throws IOException {
		final DocumentablePropertySet dps = new DocumentablePropertySet();
		parse(dps::put, pReadable, pCharset);
		return dps;
	}

	protected String decode(CharSequence pLine, Function<String,RuntimeException> pErrorHandler) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0;  i < pLine.length();  i++) {
			final char c1 = pLine.charAt(i);
			switch(c1) {
			  case '\\':
				  if (i+1 < pLine.length()) {
					  final char c2 = pLine.charAt(++i);
					  switch (c2) {
					    case 'r':
					    	sb.append('\r');
					    	break;
					    case 'n':
					    	sb.append('\n');
					    	break;
					    case 't':
					    	sb.append('\t');
					    	break;
					    case '\\':
					    	sb.append('\\');
					    	break;
					    case 'u':
					    	if (i+4 < pLine.charAt(i)) {
					    		final StringBuilder hexSb = new StringBuilder();
					    		for (int j = 0;  j < 4;  j++) {
					    			final char c = pLine.charAt(++i);
					    			if ((c >= '0'  &&  c <= '9')
					    					||  (c >= 'a'  &&  c <= 'f')
					    					||  (c >= 'A'  &&  c <= 'F')) {
					    				hexSb.append(c);
					    			} else {
					    				throw pErrorHandler.apply("Expected four hex digits after \\u");
					    			}
					    			final int hexNum = Integer.parseInt(hexSb.toString(), 16);
					    			sb.append((char) hexNum);
					    		}
					    	} else {
					    		
					    	}
					    	break;
					    default:
							  throw pErrorHandler.apply("Escape character \\ not followed by either of"
									  + " r, n, t, u, or \\");
					  }
				  } else {
					  throw pErrorHandler.apply("Escape character \\ not followed by either of"
							  + " r, n, t, u, or \\");
				  }
				  break;
		      default:
		    	  sb.append(c1);
		    	  break;
			}
		}
		return sb.toString();
	}
}
