package com.github.jochenw.afw.core.props;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jochenw.afw.core.function.Functions.BooleanBiConsumer;
import com.github.jochenw.afw.core.util.MutableInteger;

/** A parser for property files, creating instances of {@link PropertySet}.
  */
public class PropertySetParser {
	/** The parser context, which holds the parsers current state.
	 */
	public static class Context {
		private final BufferedReader br;
		private final String uri;
		private String currentLine;
		int lineNumber, columnNumber;

		/** Creates a new instance, which reads from the given {@code pBufferedReader}.
		 * @param pReader The reader provides the text, which is being parsed.
		 * @param pUri The URI of the file, thatrt is being parsed, for use in
		 *   error messages.
		 */
		public Context(BufferedReader pReader, String pUri) {
			br = pReader;
			uri = pUri;
		}

		/** Returns the URI of the file, that is being parsed.
		 * This is being used in error messages.
		 * @return The URI of the file, that is being parsed.
		 */
		public String getUri() { return uri; }
		/** Returns the current line number of the file, that is being parsed.
		 * This is being used in error messages.
		 * @return The current line number of the file, that is being parsed.
		 */
		public int getLineNumber() { return lineNumber; }
		/** Returns the current column number of the file, that is being parsed.
		 * This is being used in error messages.
		 * @return The current column number of the file, that is being parsed.
		 */
		public int getColumnNumber() { return columnNumber; }

		/** Returns the next character in the current line, if available, or -1
		 *   to indicate the end of the line.
		 * @return The next character of the current line, or -1.
		 */
		public int read() {
			if (columnNumber < currentLine.length()) {
				return currentLine.charAt(columnNumber++);
			} else {
				return -1;
			}
		}

		/** Advances the parser to the next line.
		 * @return True, if another line was available, otherwise false.
		 */
		public boolean nextLine() {
			try {
				currentLine = br.readLine();
				columnNumber = 0;
				lineNumber++;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return currentLine != null;
		}

		/** Called to indicate a syntax error at the parsers current
		 * position.
		 * @param pMsg The error message.
		 * @return The exception, which is being thrown.
		 */
		public PropertyFileSyntaxException syntaxError(String pMsg) {
			return new PropertyFileSyntaxException(this, pMsg);
		}
	}
	/** Interface of a listener for events in the property file.
	 * Basically the event of a tokens in a grammar.
	 */
	public interface PropertyFileListener {
		/** Called, when the parsing begins. The listener is supposed to reset it's state.
		 * @param pCtx The parser context.
		 */
		default void start(Context pCtx) {}
		/** Called, when the parser has detected a line key = value.
		 * @param pCtx The parser context.
		 * @param pKey The property key.
		 * @param pValue The property value.
		 * @param pComment The property's comment.
		 */
		default void propertyDefinition(Context pCtx, String pKey, String pValue, String pComment) {}
		/** Called to indicate, that the parser has finished reading the file, and no
		 * more events will follow.
		 * @param pCtx The parser context.
		 */
		default void endOfFile(Context pCtx) {}
	}
	/** Implementation of a {@link PropertySetParser.PropertyFileListener}, which
	 * uses the events to build a {@link PropertySet}.
	 */
	public static class PropertySetBuildingListener implements PropertyFileListener {
		private final PropertySet propertySet = new PropertySet();
		private StringBuilder propertyValueCollector;
		private String propertyKey;
		private StringBuilder commentCollector;
		private boolean eofSeen;

		/** Returns the property set, that has been parsed. Calling this method is valid
		 * before the parsing has been finished, is invalid, and will trigger an exception.
		 * @return The property set, that has been built.
		 * @throws IllegalStateException The
		 * {@link PropertySetParser.PropertyFileListener#endOfFile(PropertySetParser.Context) end-of-file event}
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
		public void endOfFile(Context pCtx) {
			if (eofSeen) {
				throw new IllegalStateException("A second end-of-file event has been sent by the parser.");
			}
			eofSeen = true;
		}
		/** Called to reset the listeners state to the beginning of a new line.
		 * @param pCtx The parser context.
		 */
		protected void resetToStartOfLine(Context pCtx) {
			if (propertyValueCollector != null  &&  propertyKey != null) {
				final String comment = commentCollector == null ? null : commentCollector.toString();
				if (propertySet.put(propertyKey, propertyValueCollector.toString(), comment) != null) {
					throw pCtx.syntaxError("Duplicate property key: " + propertyKey);
				}
			}
			propertyValueCollector = null;
			commentCollector = null;
		}
		@Override
		public void propertyDefinition(Context pCtx, String pKey, String pValue, String pComment) {
			if (propertySet.put(pKey, pValue, pComment) != null) {
				throw pCtx.syntaxError("Duplicate property key: " + pKey);
			}
		}
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
		 * @param pLoc The source, from which the errors location will be read
		 *   by invoking {@link PropertySetParser.Context#getUri()},
		 *   {@link PropertySetParser.Context#getLineNumber()}, and
		 *   {@link PropertySetParser.Context#getColumnNumber()}
		 * @param pMsg The actual error message.
		 */
		public PropertyFileSyntaxException(Context pLoc, String pMsg) {
			this(pLoc.getUri(), pLoc.getLineNumber(), pLoc.getColumnNumber(), pMsg);
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

	private final String commentLineSeparator;

	/** Creates a new instance with the given
	 * as the comment line separator, and the property value line separator.
	 * @param pCommentLineSeparator The separator for comment lines.
	 */
	public PropertySetParser(String pCommentLineSeparator) {
		commentLineSeparator = pCommentLineSeparator;
	}

	/** Creates a new instance with the system's default line separator
	 * as the comment line separator, and the property value line separator.
	 * In other words, this constructor is equivalent to
	 * <pre>
	 *   PropertySetParser(System.lineSeparator())
	 * </pre>
	 */
	public PropertySetParser() {
		this(System.lineSeparator());
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
		final PropertySetBuildingListener listener = new PropertySetBuildingListener();
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
		final BufferedReader br;
		if (pReader instanceof BufferedReader) {
			br = (BufferedReader) pReader;
		} else {
			br = new BufferedReader(pReader);
		}
		parse(pListener, pUri, br);
	}

	/** Called internally to do to the actual parsing.
	 * @param pListener The listener, which is being notified about
	 *   parsed tokens.
	 * @param pUri The URI, of the file, that is being parsed, for
	 *   use in error messages.
	 * @param pBr The text, which is being read.
	 * @throws IOException An I/O error occurred, while reading the text.
	 */
	protected void parse(PropertyFileListener pListener, String pUri, final BufferedReader pBr) throws IOException {
		final Context ctx = new Context(pBr, pUri);
		pListener.start(ctx);
		StringBuilder comment = null;
		StringBuilder propertyValue = null;
		String propertyKey = null;
		while(ctx.nextLine()) {
			for (;;) {
				final int i = ctx.read();
				if (i == -1) {
					// An all-whitespace line, ignore it.
					break;
				} else {
					final char c = (char) i;
					if (c == '#'  ||  c == '!') {
						if (comment == null) {
							comment = new StringBuilder();
						} else {
							comment.append(commentLineSeparator);
						}
						parseComment(ctx, comment);
						break;
					} else if (Character.isWhitespace(c)) {
						// Ignore leading whitespace, and continue.
					} else {
						if (propertyKey == null) {
							propertyKey = parsePropertyKey(ctx, c);
							propertyValue = new StringBuilder();
							if (!parsePropertyValue(ctx, propertyValue)) {
								final String cmmnt;
								if (comment == null) {
									cmmnt = null;
								} else {
									cmmnt = comment.toString();
								}
								comment = null;
								pListener.propertyDefinition(ctx, propertyKey, propertyValue.toString(), cmmnt);
								propertyKey = null;
								propertyValue = null;
							}
						} else {
							if (parsePropertyValue(ctx, propertyValue)) {
								/* Property value is being continued on the next line.
								 * Do nothing.
								 */
							} else {
								final String cmmnt;
								if (comment == null) {
									cmmnt = null;
								} else {
									cmmnt = comment.toString();
								}
								comment = null;
								pListener.propertyDefinition(ctx, propertyKey, propertyValue.toString(), cmmnt);
								propertyKey = null;
								propertyValue = null;
							}
						}
						break;
					}
				}
			}
			
		}
	}

	/** Called to parse a comment line, after the comment introducer
	 * ('#', or '!') has been consumed.
	 * @param pContext The parser contextt.
	 * @param pSb The destination, where to store
	 *   the parsed characters.
	 */
	protected void parseComment(Context pContext, StringBuilder pSb) {
		for (;;) {
			final int i = pContext.read();
			if (i == -1) {
				return;
			} else {
				final char c = (char) i;
				pSb.append(c);
			}
		}
	}

	/** Called to parse a property key from a property definition
	 * line. The property definition line has the format
	 * <pre>
	 *   key=value
	 *   key = value
	 *   key:value
	 *   key : value
	 * </pre>
	 * The method is being invoked, after the first non-whitespace
	 * character has been consumed.
	 * @param pContext The parser context.
	 * @param pFirstChar The first non-whitespace character in the
	 *   property definition line (the character, that triggered this
	 *   methods invocation).
	 * @return The property key, that has been parsed from the property
	 * definition line.
	 */
	protected String parsePropertyKey(Context pContext, char pFirstChar) {
		final StringBuilder sb = new StringBuilder();
		int i = (int) pFirstChar;
		for (;;) {
			if (i == -1) {
				throw pContext.syntaxError("Expected separator char ('=', or ':')"
						+ " in property definition line.");
			}
			final char c = (char) i;
			if (c == ':'  ||  c == '=') {
				final String propertyKey = sb.toString().trim();
				if (propertyKey.length() == 0) {
					throw pContext.syntaxError("Expected property key"
					    + " in property definition line.");
				}
				return propertyKey;
			} else if (c == '\\') {
				final int i1 = parseEscapeSequence(pContext);
				sb.append((char) i1);
			} else {
				sb.append(c);
			}
			i = pContext.read();
		}
	}

	/** Called to parse a property value from the current line.
	 * This method is being invoked in the following situations:
	 * <ol>
	 *   <li>The current line is a property definition line,
	 *     and the separator character ('=', or ':') has
	 *     been consumed.</li>
	 *   <li>The current line is a property value continuation
	 *     line (The previous line has been a property definition line,
	 *     or a property value continuation line, and has ended with
	 *     a backslash character.)</li>
	 * </ol>
	 * In both cases, leading whitespace is being ignored, and the
	 * property value (the remainder of the line, beginning with
	 * the first non-whitespace character) is placed into the
	 * destination {@code pSb}, after handling escape sequences,
	 * like '\\t', '\\r', '\\n', '\\\\', or '\\u'.
	 * @param pContext The parser context.
	 * @param pSb The {@link StringBuilder}, where to append the
	 *   property value characters.
	 * @return True, if the current line ends with a backslash
	 *   character, and the property value is being continued
	 *   on the next line.
	 */
	protected boolean parsePropertyValue(Context pContext, StringBuilder pSb) {
		boolean withinLeadingWhitespace = true;
		for (;;) {
			final int i = pContext.read();
			if (i == -1) {
				return false;
			} else {
				final char c = (char) i;
				if (Character.isWhitespace(c)) {
					if (withinLeadingWhitespace) {
						// Ignore this whitespace character. (Do nothing.)
					} else {
						pSb.append(c);
					}
				} else if (c == '\\') {
					final int j = parseEscapeSequence(pContext);
					if (j == -1) {
						return true;
					} else {
						pSb.append((char) j);
					}
				} else {
					withinLeadingWhitespace = false;
					pSb.append(c);
				}
			}
		}
	}

	/** Called after consuming the escape character ('\\')
	 * to interpret the escape sequence.
	 * @param pContext The parser context.
	 * @return True, if the line ended with a backslash character,
	 *   and the property value is being continued on the next line.
	 *   Otherwise false.
	 */
	protected int parseEscapeSequence(Context pContext) {
		final int i = pContext.read();
		if (i == -1) {
			return -1;
		} else {
			final char c;
			final char ch = (char) i;
			if (ch == 'n') {
				c = '\n';
			} else if (ch == 'f') {
				c = '\f';
			} else if (ch == 'r') {
				c = '\r';
			} else if (ch == 't') {
				c = '\t';
			} else if (ch == '\\') {
				c = '\\';
			} else if (ch == 'u') {
				int codePoint = 0;
				for (int j = 0;  j < 4;  j++) {
					int i2 = pContext.read();
					final int nibble;
					if (i2 == -1) {
						nibble = -1;
					} else {
						final char hexChar = (char) i2;
						if (hexChar >= '0'  &&  hexChar <= '9') {
							nibble = hexChar - '0';
						} else if (hexChar >= 'a'  &&  hexChar <= 'f') {
							nibble = hexChar - 'a';
						} else if (hexChar >= 'A'  &&  hexChar <= 'F') {
							nibble = hexChar - 'A';
						} else {
							nibble = -1;
						}
					}
					if (nibble == -1) {
						throw pContext.syntaxError("Expected four hex digits after \\u");
					}
					codePoint = (codePoint << 4) + nibble;
				}
				c = (char) codePoint;
			} else {
				throw pContext.syntaxError("Expected f, n, r, t, u, or '\\' character"
						+ " after the escape character '\\', got '" + ch + "'");
			}
			return c;
		}
	}
}
