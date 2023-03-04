package com.github.jochenw.afw.core.props;

import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jochenw.afw.core.function.Functions.BooleanBiConsumer;
import com.github.jochenw.afw.core.function.Functions.BooleanConsumer;

/** A parser for property files, creating instances of {@link PropertySet}.
  */
public class PropertySetParser {
	/** Interface of a listener for events in the property file.
	 * Basically the event of a tokens in a grammar.
	 */
	public interface PropertyFileListener {
		/** Called, when the parsing begins. The listener is supposed to reset it's state.
		 */
		default void start() {}
		/** Called, when the parser has detected a line, which is all whitespace.
		 * The listener is supposed to ignore this.
		 * @param pWs The whitespace content, that is being ignored.
		 */
		default void skippedWhitespaceLine(String pWs) {}
		/** Called, when the parser has detected a comment line.
		 * @param pComment The comment (the part of the line after the '#' character.
		 */
		default void commentLine(String pComment) {}
		/** Called, when the parser has detected a line key = value.
		 * @param pKey The property key.
		 * @param pValue The property value.
		 * @param pContinued True, if the value is being continued on the next line.
		 *   (The line ends with '\'.) Otherwise false.
		 */
		default void propertyLine(String pKey, String pValue, boolean pContinued) {}
		/** Called, when the parser has detected a continued value line.
		 * @param pValue The continued part of the property line.
		 * @param pContinued True, if the value is being continued on the next line.
		 *   (The line ends with '\'.) Otherwise false.
		 */
		default void propertyValueContinuationLine(String pValue, boolean pContinued) {}
		/** Called to indicate, that the parser has finished reading the file, and no
		 * more events will follow.
		 */
		default void endOfFile() {}
	}
	/** Implementation of a {@link PropertySetParser.PropertyFileListener}, which
	 * uses the events to build a {@link PropertySet}.
	 */
	public static class PropertySetBuildingListener implements PropertyFileListener {
		private final String commentLineSeparator;
		private final String valueLineSeparator;
		private final PropertySet propertySet = new PropertySet();
		private StringBuilder propertyValueCollector;
		private String propertyKey;
		private StringBuilder commentCollector;

		/** Creates a new instance with the given comment line, and value line separator.
		 * @param pCommentLineSeparator The comment line separator.
		 * @param pValueLineSeparator The value line separator.
		 */
		public PropertySetBuildingListener(String pCommentLineSeparator, String pValueLineSeparator) {
			commentLineSeparator = pCommentLineSeparator;
			valueLineSeparator = pValueLineSeparator;
		}
	
		private boolean eofSeen;
		/** Returns the property set, that has been parsed. Calling this method is valid
		 * before the parsing has been finished, is invalid, and will trigger an exception.
		 * @return The property set, that has been built.
		 * @throws IllegalStateException The
		 * {@link PropertySetParser.PropertyFileListener#endOfFile() end-of-file event}
		 *   has not yet been seen.
		 */
		public PropertySet getPropertySet() {
			if (eofSeen) {
				return propertySet;
			} else {
				throw new IllegalStateException("No end-of-file event has been sent by the parser.");
			}
		}
		@Override
		public void endOfFile() {
			if (eofSeen) {
				throw new IllegalStateException("A second end-of-file event has been sent by the parser.");
			}
			eofSeen = true;
		}
		@Override
		public void skippedWhitespaceLine(String pWs) {
			resetToStartOfLine();
		}
		/** Called to reset the listeners state to the beginning of a new line.
		 */
		protected void resetToStartOfLine() {
			if (propertyValueCollector != null  &&  propertyKey != null) {
				final String comment = commentCollector == null ? null : commentCollector.toString();
				if (propertySet.put(propertyKey, propertyValueCollector.toString(), comment) != null) {
					throw new PropertyFileSyntaxException(null, "Duplicate property key: " + propertyKey);
				}
			}
			propertyValueCollector = null;
			commentCollector = null;
		}
		@Override
		public void commentLine(String pComment) {
			if (commentCollector == null) {
				commentCollector = new StringBuilder();
			} else {
				if (commentLineSeparator != null) {
					commentCollector.append(commentLineSeparator);
				}
			}
			commentCollector.append(pComment);
		}
		@Override
		public void propertyLine(String pKey, String pValue, boolean pContinued) {
			if (pContinued) {
				propertyKey = pKey;
				propertyValueCollector = new StringBuilder();
				propertyValueCollector.append(pValue);
			} else {
				final String comment = commentCollector == null ? null : commentCollector.toString();
				if (propertySet.put(propertyKey, pValue, comment) != null) {
					throw new PropertyFileSyntaxException(null, "Duplicate property key: " + propertyKey);
				}
			}
		}
		@Override
		public void propertyValueContinuationLine(String pValue, boolean pContinued) {
			if (propertyValueCollector == null) {
				throw new PropertyFileSyntaxException(null, "Unexpected property continuation event.");
			} else {
				if (valueLineSeparator != null) {
					propertyValueCollector.append(valueLineSeparator);
				}
				propertyValueCollector.append(pValue);
			}
			if (!pContinued) {
				final String comment = commentCollector == null ? null : commentCollector.toString();
				if (propertySet.put(propertyKey, pValue, comment) != null) {
					throw new PropertyFileSyntaxException(null, "Duplicate property key: " + propertyKey);
				}
			}
		}
	}
	/** A simplified version of a {@link Reader} with the additional possibility to
	 * push back characters, roughly like the {@link PushbackReader}.
	 */
	public static class CharSource {
		private final Reader reader;
		private final String uri;
		private int lineNumber, columnNumber;
		private boolean eofSeen;
		/** Creates a new instance, that returns the given readers input.
		 * @param pReader The reader, from which to read text. An end-of-file
		 * from that reader will trigger an end-of-file from the {@link CharSource}.
		 * @param pUri The URI of the file, that is being parsed, for use in
		 * error messages.
		 */
		public CharSource(Reader pReader, String pUri) {
			reader = pReader;
			uri = pUri;
		}
		final StringBuilder sb = new StringBuilder();
		/** Called to return a single character. If there are pushed back characters
		 * in the LIFO buffer, the buffer will be emptied by returning the last character
		 * from the buffer.
		 * @return The next character from the text, or -1 to indicate end-of-file.
		 * @throws IOException Reading the text from the reader has failed.
		 */
		public int read() throws IOException {
			if (sb.length() > 0) {
				final int offset = sb.length()-1;
				final char c = sb.charAt(offset);
				sb.setLength(offset);
				return (int) c;
			}
			if (eofSeen) {
				return -1;
			} else {
				++columnNumber;
				final int i = reader.read();
				if (i == -1) {
					eofSeen = true;
				}
				if (i == '\n' ) {
					++lineNumber;
					columnNumber = 0;
				}
				return i;
			}
		}
		/** 
		 * Pushes the given character to the input. The next call to {@link #read()}}
		 * will return this character.
		 * @param pChar The character, that is being pushed back.
		 */
		public void pushback(char pChar) {
			sb.append(pChar);
		}
		/** Returns the line number of the last character, that has been
		 * read from the underlying {@link Reader reader}. In other words,
		 * this number is affected by invocations of {@link #read()}, but
		 * not by invocations of {@link #pushback(char)}.
		 * @return The current line number.
		 */
		public int getLineNumber() { return lineNumber; }
		/** Returns the column number of the last character, that has been
		 * read from the underlying {@link Reader reader}. In other words,
		 * this number is affected by invocations of {@link #read()}, but
		 * not by invocations of {@link #pushback(char)}.
		 * @return The current column number.
		 */
		public int getColumnNumber() { return columnNumber; }
		/** Returns the URI of the file, that is currently being parsed.
		 * For example, for a {@link Path}, or {@link File}, this would
		 * be the file name.
		 * @return The URI of the file, that is currently being parsed.
		 */
		public String getUri() { return uri; }
	}
	/** Exception, that is being thrown in case of syntax errors.
	 */
	public static class PropertyFileSyntaxException extends RuntimeException {
		private static final long serialVersionUID = -4140418546065225152L;
		private final String uri;
		private final int lineNumber, columnNumber;
		/** Creates a new instance. The exceptions message will be built by invoking
		 * {@link #asMsg(String, int, int, String)} with the constructor parameters.
		 * @param pUri The error locations Uri.
		 * @param pLineNumber The error locations line number
		 * @param pColumnNumber The error locations column number.
		 * @param pMsg The actual error message.
		 */
		public PropertyFileSyntaxException(String pUri, int pLineNumber, int pColumnNumber,
				                           String pMsg) {
			super(asMsg(pUri, pLineNumber, pColumnNumber, pMsg));
			uri = pUri;
			lineNumber = pLineNumber;
			columnNumber = pColumnNumber;
		}
		/** Creates a new instance with the given error message. The errors location is
		 * taken from the given source.
		 * @param pSource The source, from which the errors location will be read
		 *   by invoking {@link PropertySetParser.CharSource#getUri()},
		 *   {@link PropertySetParser.CharSource#getLineNumber()}, and
		 *   {@link PropertySetParser.CharSource#getColumnNumber()}
		 * @param pMsg The actual error message.
		 */
		public PropertyFileSyntaxException(CharSource pSource, String pMsg) {
			this(pSource.getUri(), pSource.getLineNumber(), pSource.getColumnNumber(), pMsg);
		}
		/** Creates an error description, that includes the given URI, line number, column number,
		 * (the errors location) and the given error message.
		 * @param pUri The URI of the errors location. May be null, in which case it is ignored.
		 * @param pLineNumber The line number of the errors location.
		 *   May be null, in which case it is ignored.
		 * @param pColumnNumber The column number of the errors location.
		 *   May be null, in which case it is ignored.
		 * @param pMsg The actual error message.
		 * @return The created error description.
		 */
		public static String asMsg(String pUri, int pLineNumber, int pColumnNumber, String pMsg) {
			final StringBuilder sb = new StringBuilder();
			String sep = "At ";
			if (pUri != null) {
				sb.append(sep);
				sb.append(pUri);
				sep = ", ";
			}
			if (pLineNumber != -1) {
				sb.append(sep);
				sb.append("line ");
				sb.append(pLineNumber);
				sep = ", ";
			}
			if (pColumnNumber != -1) {
				sb.append(sep);
				sb.append("column ");
				sb.append(pColumnNumber);
				sep = ", ";
			}
			sb.append(pMsg);
			return sb.toString();
		}
		/** Returns the error locations Uri, if any, or null.
		 * @return The error locations Uri, if any, or null.
		 */
		public String getUri() { return uri; }
		/** Returns the error locations line number, if any, or null.
		 * @return The error locations line number, if any, or -1.
		 */
		public int getLineNumber() { return lineNumber; }
		/** Returns the error locations column number, if any, or null.
		 * @return The error locations column number, if any, or -1.
		 */
		public int getColumnNumber() { return columnNumber; }
	}
	/** Called to parse a property set from the given text.
	 * Internally, this will delegate to
	 * {@link #parse(Reader, String, PropertySetParser.PropertyFileListener)} with a
	 * {@link PropertySetParser.PropertySetBuildingListener} as the listener.
	 * This is equivalent to
	 * <pre>
	 *   parse(pReader, pUri, "\n", "\n");
	 * </pre>
	 * @param pReader The text, that is being read.
	 * @param pUri The URI of the file, that is being parsed, for use in error messages.
	 * @return The parsed {@link PropertySet}.
	 * @throws IOException Reading the text has failed.
	 */
	public PropertySet parse(Reader pReader, String pUri) throws IOException {
		return parse (pReader, pUri, "\n", "\n");
	}
	/** Called to parse a property set from the given text.
	 * Internally, this will delegate to
	 * {@link #parse(Reader, String, PropertySetParser.PropertyFileListener)} with a
	 * {@link PropertySetParser.PropertySetBuildingListener} as the listener.
	 * @param pReader The text, that is being read.
	 * @param pUri The URI of the file, that is being parsed, for use in error messages.
	 * @param pCommentLineSeparator The separator for comment lines. May be null, in which case multi-line
	 *   comments are being merged into a single line.
	 * @param pValueLineSeparator The separator for value lines. May be null, in which case multi-line
	 *   values are being merged into a single line.
	 * @return The parsed {@link PropertySet}.
	 * @throws IOException Reading the text has failed.
	 */
	public PropertySet parse(Reader pReader, String pUri, String pCommentLineSeparator, String pValueLineSeparator) throws IOException {
		final PropertySetBuildingListener listener = new PropertySetBuildingListener(pCommentLineSeparator, pValueLineSeparator);
		parse (pReader, pUri, listener);
		return listener.getPropertySet();
	}

	/** Called to parse a property set from the given text. The given
	 * listener will be notified about the tokens, that have been found
	 * in the text.
	 * @param pReader The text, that is being read.
	 * @param pUri The URI of the file, that is being parsed, for use in error messages.
	 * @param pListener The listener, that is being notified.
	 * @throws IOException An I/O error was reported while invoking the {@code pReader}.
	 * @throws PropertyFileSyntaxException A syntax error was found in the text,
	 *   that is being parsed.
	 */
	public void parse(Reader pReader, String pUri, PropertyFileListener pListener)
	        throws IOException {
		final CharSource cs = new CharSource(pReader, pUri);
		/* -1 = End of file
		 * 0 = At the beginning of a line
		 * 1 = A property definition is being continued (backslash character at
		 *   the end of the previous line)
		 */
		int state = 0;
		pListener.start();
		while (state != -1) {
			switch (state) {
			  case 0: {
				final StringBuilder whiteSpacePrefix = new StringBuilder();
				final int i1 = cs.read();
				if (i1 == -1) {
					pListener.skippedWhitespaceLine(whiteSpacePrefix.toString());
					state = -1; 
				} else {
					final char c1 = (char) i1;
					if (Character.isWhitespace(c1)) {
						whiteSpacePrefix.append(c1);
						break;
					} else if ('#' == c1  ||  '!' == c1) {
						state = parseCommentLine(cs, pListener);
					} else {
						state = parsePropertyDefinitionLine(cs, pListener, c1);
					}
				}
				break;
			  }
			  case 1:
				  state = parsePropertyContinuationLine(cs, (b, s) -> {
					  pListener.propertyValueContinuationLine(s, b);
				  });
			}
		}
	}

	/** Called to parse a comment line, after the comment character ('#', or '!') has
	 * already been consumed.
	 * @param pCharSource The character source, from which to read the content.
	 * @param pListener The event listener, which is being notified with the comment.
	 * @return The next parser state, either -1 (end of file), or 0 (starting a new line)
	 * @throws IOException An I/O error was reported while invoking the {@code pReader}.
	 */
	protected int parseCommentLine(CharSource pCharSource, PropertyFileListener pListener) throws IOException {
		final StringBuilder commentSb = new StringBuilder();
		for (;;) {
			final int i = pCharSource.read();
			if (i == -1) {
				return -1;
			} else if (i == '\n') {
				break;
			} else {
				commentSb.append((char) i);
			}
		}
		pListener.commentLine(unEscape(commentSb.toString(), true));
		return 0;
	}

	/** Called to parse a property definition line in the form <pre>key '=' value</pre>
	 * @param pCharSource The character source, from which to read the content.
	 * @param pListener The event listener, which is being notified with the property key, and value.
	 * @param pFirstChar The first character of the property key.
	 * @return The next parser state, either -1 (end of file), or 0 (starting a new line)
	 * @throws IOException An I/O error was reported while invoking the {@code pReader}.
	 */
	protected int parsePropertyDefinitionLine(CharSource pCharSource, PropertyFileListener pListener, char pFirstChar) throws IOException {
		final StringBuilder propertyKeySb = new StringBuilder();
		int i1 = pFirstChar;
		for (;;) {
			if (i1 == -1) {
				throw new PropertyFileSyntaxException(pCharSource, "Incomplete property definition: Expected '=', or ':' after property key, got end of file.");
			}
			final char c1 = (char) i1;
			if (c1 == '='  ||  c1 == ':') {
				final String propertyKey = unEscape(propertyKeySb.toString().trim(), false);
				if (propertyKey.length() == 0) {
					throw new PropertyFileSyntaxException(pCharSource, "Invalid property definition: Property key (left hand side of '" + c1
							+ "' character) is empty.");
				}
				return parsePropertyContinuationLine(pCharSource, (b, s) -> {
					pListener.propertyLine(propertyKey, s, b);
				});
			} else {
				propertyKeySb.append(c1);
			}
		}
	}

	/** Called to parse a property continuation line.
	 * @param pCharSource The character source, from which to read the content.
	 * @param pValueConsumer The property value consumer, which is being invoked, if the property value has been successully parsed.
	 * @return The next parser state, either -1 (end- of file), 0 (starting a new line, or 2 (property value continued on next line).
	 * @throws IOException An I/O error was reported while invoking the {@code pReader}.
	 */
	protected int parsePropertyContinuationLine(CharSource pCharSource, BooleanBiConsumer<String> pValueConsumer) throws IOException {
		final StringBuilder propertyValueSb = new StringBuilder();
		for (;;) {
			final int i1 = pCharSource.read();
			if (i1 == -1) {
				return -1;
			} else {
				final char c1 = (char) i1;
				if (c1 == '\\') {
					final int i2 = pCharSource.read();
					if (i2 == -1) {
						throw new PropertyFileSyntaxException(pCharSource, "Unexpected end of file (Expected property continuation line after '\\')");
					} else {
						final char c2 = (char) i2;
						if (c2 == '\n') {
							pValueConsumer.accept(true, unEscape(propertyValueSb.toString(), false));
							return 1;
						} else if (c2 == '\r') {
							final int i3 = pCharSource.read();
							if (i3 == -1) {
								throw new PropertyFileSyntaxException(pCharSource, "Unexpected end of file (Expected property continuation line after '\\', and \\r')");
							} else {
								final char c3 = (char) i3;
								if (c3 == '\n') {
									pValueConsumer.accept(true, unEscape(propertyValueSb.toString(), false));
									return 1;
								} else {
									propertyValueSb.append(c1);
									propertyValueSb.append(c2);
									propertyValueSb.append(c3);
								}
							}
						} else {
							propertyValueSb.append(c1);
							propertyValueSb.append(c2);
						}
					}
				} else if (c1 == '\n') {
					pValueConsumer.accept(false, unEscape(propertyValueSb.toString(), false));
					return 0;
				} else if (c1 == '\r') {
					final int i2 = pCharSource.read();
					if (i2 == -1) {
						throw new PropertyFileSyntaxException(pCharSource, "Unexpected end of file (Expected '\\n' character after '\\r' character at the end of a line.");
					} else {
						final char c2 = (char) i2;
						if (c2 == '\n') {
							pValueConsumer.accept(false, unEscape(propertyValueSb.toString(), false));
							return 0;
						} else {
							propertyValueSb.append(c1);
							propertyValueSb.append(c2);
						}
					}
				} else {
					propertyValueSb.append(c1);
				}
			}
		}
	}

	private static final Pattern unicodePattern =
			Pattern.compile("\\\\u([0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F])");
	static String unEscape(String pValue, boolean pCrOnly) {
		String value = pValue;
		// Remove a trailing \r, if any.
		value = value.replaceFirst("\\r$", "");
		if (!pCrOnly) {
			value = value.replaceAll("\\r", "\r")
					     .replaceAll("\\n", "\n")
					     .replaceAll("\\t", "\t")
					     .replaceAll("\\f", "\f");
			for (;;) {
				final Matcher matcher = unicodePattern.matcher(value);
				if (matcher.matches()) {
					final String hexChars = matcher.group(0);
					int codePoint = 0;
					for (int i = 3;  i >= 0;  i--) {
						final char hexDigit = hexChars.charAt(i);
						final int nibble;
						if (hexDigit >= '0'  &&  hexDigit <= '9') {
							nibble = ((int) hexDigit) - ((int) '0');
						} else if (hexDigit >= 'a'  &&  hexDigit <= 'f') {
							nibble = ((int) hexDigit) - ((int) 'a') + 10;
						} else if (hexDigit >= 'A'  &&  hexDigit <= 'F') {
							nibble = ((int) hexDigit) - ((int) 'A') + 10;
						} else {
							throw new IllegalStateException("Invalif hex digit: " + hexDigit);
						}
						codePoint = (codePoint << 4) + nibble;
					}
					matcher.replaceFirst("" + Character.valueOf((char) codePoint));
				} else {
					break;
				}
			}
			
		}
		return value;
	}
}
