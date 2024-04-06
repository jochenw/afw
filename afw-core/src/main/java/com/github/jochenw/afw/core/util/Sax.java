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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.jspecify.annotations.NonNull;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;


/**
 * Utility class for working with SAX handlers.
 */
public class Sax {
	/** Abstract base class for implementing SAX handlers.
	 */
	public abstract static class AbstractContentHandler implements ContentHandler {
		private int level;
		private Locator locator;
		private StringBuilder sb;
		private FailableConsumer<String,SAXException> textElementConsumer;
		private int textElementLevel;

		/** Returns the handlers current location.
		 * @return The handlers current location.
		 */
		public Locator getDocumentLocator() {
			if (locator == null) {
				return null;
			} else {
				return new LocatorImpl(locator);
			}
		}

		/**
		 * Indicates, that a text element begins.
		 * @param pConsumer A consumer, that will be invoked with the elements content,
		 *     when the text element ends.
		 */
		protected void startTextElement(FailableConsumer<String,SAXException> pConsumer) {
			startTextElement(level, pConsumer);
		}

		/**
		 * Indicates, that a text element begins.
		 * @param pLevel The text elements end level.
		 * @param pConsumer A consumer, that will be invoked with the elements content,
		 *     when the given element level is reached.
		 */
		protected void startTextElement(int pLevel, FailableConsumer<String,SAXException> pConsumer) {
			sb = new StringBuilder();
			textElementConsumer = pConsumer;
			textElementLevel = pLevel;
		}

		/** Increments the current element level by 1.
		 * @return The new (incremented) element level.
		 * @throws SAXException An attempt was made, to increment the element level
		 * within a text element.
		 */
		protected int incLevel() throws SAXException {
			if (sb != null) {
				throw error("Unexpected element within text.");
			}
			return ++level;
		}

		/** Decrements the current element level by 1.
		 * Invokes the text element consumer, if the requested element level is reached.
		 * @return The element level, before decrement. (The
		 *   previous element level.)
		 * @throws SAXException The text element consumer reported an error.
		 */
		protected int decLevel() throws SAXException {
			final int l = level--;
			if (sb != null  &&  l == textElementLevel) {
				textElementConsumer.accept(sb.toString());
				textElementConsumer = null;
				textElementLevel = -1;
				sb = null;
			}
			return l;
		}

		/** Returns the current element level.
		 * @return The current element level.
		 */
		protected int getLevel() {
			return level;
		}
		
		@Override
		public void setDocumentLocator(Locator pLocator) {
			locator = pLocator;
		}

		/** Called to report an error with the given message, and the current location.
		 * @param pMsg The error message.
		 * @return The created exception, ready for throwing.
		 * @see #error(String, Locator)
		 */
		protected SAXParseException error(String pMsg) {
			return error(pMsg, getDocumentLocator());
		}

		/** Called to report an error with the given message, and the given location.
		 * @param pMsg The error message.
		 * @param pLocator The errors location.
		 * @return The created exception, ready for throwing.
		 * @see #error(String)
		 */
		protected SAXParseException error(String pMsg, Locator pLocator) {
			return new SAXParseException(pMsg, pLocator);
		}

		/** Called to report an error with the given message, the given
		 * cause, and the current location.
		 * @param pMsg The error message.
		 * @param pCause The errors cause.
		 * @return The created exception, ready for throwing.
		 * @see #error(String, Locator)
		 */
		protected SAXParseException error(String pMsg, Throwable pCause) {
			final SAXParseException spe = new SAXParseException(pMsg, getDocumentLocator());
			spe.initCause(pCause);
			return spe;
		}

		@Override
		public void processingInstruction(String pTarget, String pData) throws SAXException {
			if (sb != null) {
				throw error("Unexpected PI within text.");
			}
			throw error("Unexpected PI: target=" + pTarget + ", data=" + pData);
		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			if (sb != null) {
				throw error("Unexpected skipped entity within text.");
			}
			throw error("Unexpected skipped entity: " + name);
		}

		@Override
		public void startPrefixMapping(String pPrefix, String pUri) throws SAXException {
			// Do nothing
		}

		@Override
		public void endPrefixMapping(String pPrefix) throws SAXException {
			// Do nothing
		}

		@Override
		public void characters(char[] pChars, int pOffset, int pLength) throws SAXException {
			if (sb != null) {
				sb.append(pChars, pOffset, pLength);
			}
		}

		@Override
		public void ignorableWhitespace(char[] pChars, int pOffset, int pLength) throws SAXException {
			if (sb != null) {
				sb.append(pChars, pOffset, pLength);
			}
		}

		/** Creates a string representation of the given qualified name.
		 * @param pUri The qualified name's namespace URI.
		 * @param pLocalName The qualified name's local part.
		 * @return The created string representation. Either just the local part
		 * (if the namespace URI is trivial), or "{uri}localPart".
		 */
		protected String asQName(String pUri, String pLocalName) {
			if (pUri == null  ||  pUri.length() == 0) {
				return pLocalName;
			} else {
				return '{' + pUri + '}' + pLocalName;
			}
		}
	}

	/** Parses the given XML file, using the given SAX handler.
	 * @param pFile The XML file, which is being parsed.
	 * @param pHandler The SAX handler, which is being invoked to consume the
	 *   XML file's SAX events.
	 */
	public static void parse(File pFile, ContentHandler pHandler) {
		try (InputStream in = new FileInputStream(pFile)) {
			final InputSource isource = new InputSource(in);
			isource.setSystemId(pFile.toURI().toURL().toExternalForm());
			parse(isource, pHandler);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	/** Parses the given XML file, using the given SAX handler.
	 * @param pUrl The URL, from which to read the XML file, that is being parsed.
	 * @param pHandler The SAX handler, which is being invoked to consume the
	 *   XML file's SAX events.
	 */
	public static void parse(URL pUrl, ContentHandler pHandler) {
		try (InputStream in = pUrl.openStream()) {
			final InputSource isource = new InputSource(in);
			isource.setSystemId(pUrl.toExternalForm());
			parse(isource, pHandler);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	/** Parses the given XML file, using the given SAX handler.
	 * @param pPath The XML file, which is being parsed.
	 * @param pHandler The SAX handler, which is being invoked to consume the
	 *   XML file's SAX events.
	 */
	public static void parse(Path pPath, ContentHandler pHandler) {
		try (InputStream in = Files.newInputStream(pPath)) {
			final InputSource isource = new InputSource(in);
			isource.setSystemId(pPath.toUri().toURL().toExternalForm());
			parse(isource, pHandler);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}


	/** Parses the given XML file, using the given SAX handler.
	 * @param pIn The {@link InputStream}, from which to read the XML file, that is being parsed.
	 * @param pHandler The SAX handler, which is being invoked to consume the
	 *   XML file's SAX events.
	 */
	public static void parse(InputStream pIn, ContentHandler pHandler) {
		final InputSource isource = new InputSource(pIn);
		parse(isource, pHandler);
	}

	/** Parses the given XML file, using the given SAX handler.
	 * @param pSource The {@link InputSource}, from which to read the XML file, that is being parsed.
	 * @param pHandler The SAX handler, which is being invoked to consume the
	 *   XML file's SAX events.
	 */
	public static void parse(InputSource pSource, ContentHandler pHandler) {
		try {
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			spf.setNamespaceAware(true);
			spf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			final XMLReader xr = spf.newSAXParser().getXMLReader();
			xr.setContentHandler(pHandler);
			xr.parse(pSource);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/** Converts the given namespace URI, and local name, into a fully qualified
	 * element name.
	 * @param pUri The elements namespace URI.
	 * @param pLocalName The elements local name.
	 * @return An equivalent, and fully qualified name.
	 */
	public static String asQName(String pUri, String pLocalName) {
		final String localName = Objects.requireNonNull(pLocalName, "LocalName");
		if (pUri == null  ||  pUri.length() == 0) {
			return localName;
		} else {
			return "{" + pUri + "}" + pLocalName;
		}
	}

	/**
	 * Returns a string, which includes the given message, and the given location information.
	 * @param pLoc The object providing the location information. If there is no such object
	 *   (the value is null), then the message string will be returned, as it is.
	 * @param pMsg The message string, which is being augmented with location information.
	 * @return A string in the format "At &lt;LOCATION_INFORMATION&gt;: &lt;MESSAGE&gt;", if location
	 *   information is provided. Otherwise, returns the message string, as it is.
	 */
	public static String asLocalizedMessage(Locator pLoc, String pMsg) {
		if (pLoc == null) {
			return pMsg;
		} else {
			return asLocalizedMessage(pMsg, pLoc.getSystemId(), pLoc.getLineNumber(), pLoc.getColumnNumber());
		}
	}

	/**
	 * Returns a string, which includes the given message, and the given location information.
	 * @param pMsg The message string, which is being augmented with location information.
	 * @param pSystemId System id of the location information (if available), or null.
	 * @param pLineNumber Line number of the location information (if available), or -1.
	 * @param pColumnNumber Column number of the location information (if available), or -1
	 * @return A string in the format "At &lt;LOCATION_INFORMATION&gt;: &lt;MESSAGE&gt;", if location
	 *   information is provided. Otherwise, returns the message string, as it is.
	 */
	public static String asLocalizedMessage(String pMsg, String pSystemId, int pLineNumber, int pColumnNumber) {
		if (pSystemId == null  &&  pLineNumber == -1  &&  pColumnNumber == -1) {
			return pMsg;
		} else {
			final StringBuilder sb = new StringBuilder();
			String sep = "At ";
			if (pSystemId != null) {
				sb.append(sep);
				sb.append(pSystemId);
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
			}
			sb.append(": ");
			sb.append(pMsg);
			return sb.toString();
		}
	}

	/** Creates a clone of the given {@link Locator}.
	 * @param pLocator The {@link Locator}, which is being cloned. May be null.
	 * @return The clone, that has been created, or null. (If the input has been null.)
	 */
	public static @Nullable Locator clone(@Nullable Locator pLocator) {
		if (pLocator == null) {
			return null;
		} else {
			return new LocatorImpl(pLocator);
		}
	}

	/** Creates a {@link SaxWriter SAX Writer}, an object for writing XML via the SAX API.
	 * @return The created {@link SaxWriter}.
	 */
	public static SaxWriter creator() {
		return new SaxWriter();
	}

	/** A {@link SaxWriter SAX writer} is an object, which allows
	 * to create XML documents via an API, that is SAX aware
	 * (in fact, it is based completely based on SAX), thus
	 * guaranteed to produce well formed output.
	 */
	public static class SaxWriter {
		private boolean indenting;
		private boolean omittingXmlDeclaration;
		private String encoding;
		private String prefix, namespaceUri;
		private DateTimeFormatter dateTimeFormatter;
		private boolean immutable = false;
		private TransformerHandler transformerHandler;

		/** Returns, whether this {@link SaxWriter SAX writer} is
		 * indenting, aka pretty printing.
		 * @return True, if pretty printing is activated, otherwise
		 *   false (default).
		 * @see #withIndentation(boolean)
		 * @see #withIndentation()
		 */
		public boolean isIndenting() { return indenting; }

		/** Returns the encoding, which this {@link SaxWriter SAX writer}
		 * is using.
		 * @return The configured encoding, or null ({@code UTF-8} is being
		 *   used),
		 * @see #withEncoding(String)
		 */
		public String getEncoding() { return encoding; }
	
		/** Returns the default prefix, which this {@link SaxWriter SAX writer}
		 * is using.
		 * @return The configured default prefix, or
		 * {@link XMLConstants#DEFAULT_NS_PREFIX}.
		 * @see #withPrefix(String)
		 */
		public String getPrefix() { return prefix; }

		/** Returns the default namespace URI, which this
		 * {@link SaxWriter SAX writer} is using.
		 * @return The configured default prefix, or
		 * {@link XMLConstants#NULL_NS_URI}.
		 * @see #withPrefix(String)
		 */
		public String getNamespaceUri() { return namespaceUri; }

		/** Returns the {@link DateTimeFormatter}, which is being used to
		 * convert temporal attribute values to strings.
		 * 
		 * @return The {@link DateTimeFormatter}, which
		 *   is being used, or null, if the default ({@link DateTimeFormatter#ISO_DATE_TIME})
		 *   is.
		 */
		public DateTimeFormatter getDateTimeFormatter() { return dateTimeFormatter; }

		/** Returns the {@link TransformerHandler}, which is
		 * used internally.
		 * @return The internally used {@link TransformerHandler}.
		 */
		public TransformerHandler getTransformerHandler() { return transformerHandler; }

		/** Sets, whether to activate pretty print.
		 * 
		 * @param pIndentation True, if the generated XML should be indented,
		 *   or not. The default value is false.
		 * @return This {@link SaxWriter}.
		 */
		public SaxWriter withIndentation(boolean pIndentation) {
			assertMutable();
			indenting = pIndentation;
			return this;
		}

		/** Sets the {@link DateTimeFormatter}, which is being used to
		 * convert temporal attribute values to strings.
		 * 
		 * @param pDateTimeFormatter The {@link DateTimeFormatter}, which
		 *   is being used. Defaults to {@link DateTimeFormatter#ISO_DATE_TIME}.
		 * @return This {@link SaxWriter}.
		 */
		public SaxWriter withDateTimeFormatter(DateTimeFormatter pDateTimeFormatter) {
			dateTimeFormatter = pDateTimeFormatter;
			return this;
		}

		/** Ensures, that the XML prolog is not yet written.
		 * (If the XML prolog has been written, then configuration
		 * changes are no longer applicable.)
		 */
		protected void assertMutable() {
			if (immutable) {
				throw new IllegalStateException("The XML prolog has been written, and the SAX writer is no longer mutable.");
			}
		}

		/** Activates pretty print. Equivalent to
		 * <pre>withIndentation(true)</pre>.
		 * 
		 * @return This {@link SaxWriter}.
		 */
		public SaxWriter withIndentation() {
			return withIndentation(true);
		}

		/** Sets the Sax writers default namespace URI. The default
		 * value is {@link XMLConstants#NULL_NS_URI}.
		 * @param pNamespaceUri The default namespace Uri.
		 * @return This {@link SaxWriter}.
		 */
		public SaxWriter withNamespaceUri(String pNamespaceUri) {
			assertMutable();
			namespaceUri = pNamespaceUri;
			return this;
		}

		/** Sets the Sax writers default prefix. The default
		 * value is {@link XMLConstants#DEFAULT_NS_PREFIX}.
		 * @param pPrefix The default prefix.
		 * @return This {@link SaxWriter}.
		 */
		public SaxWriter withPrefix(String pPrefix) {
			assertMutable();
			prefix = pPrefix;
			return this;
		}

		/** Sets the Sax writers encoding to the name of
		 * the given character set. Equivalent to
		 * <pre>withEncoding(pCharset.name())</pre>.
		 * The default encoding is
		 * {@code UTF-8}.
		 * @param pCharset The character set.
		 * @return This {@link SaxWriter}.
		 */
		public SaxWriter withCharset(Charset pCharset) {
			if (pCharset == null) {
				return withEncoding(null);
			} else {
				return withEncoding(pCharset.name());
			}
		}

		/** Sets the Sax writers encoding. The default
		 * encoding is {@code UTF-8}.
		 * @param pEncoding The encoding.
		 * @return This {@link SaxWriter}.
		 */
		public SaxWriter withEncoding(String pEncoding) {
			assertMutable();
			encoding = pEncoding;
			return null;
		}

		/** Returns, whether an XML declaration should be omitted.
		 * @return True, if an XML declaration should be omitted, if possible.
		 *   Otherwise false, which is the default.
		 */
		public boolean isOmittingXmlDeclaration() {
			return omittingXmlDeclaration;
		}
		
		/** Requests, that no XML declaration should be written.
		 * Equivalent to <pre>withoutXmlDeclaration(true)</pre>.
		 * @return This {@link SaxWriter SAX writer}.
		 */
		public SaxWriter withoutXmlDeclaration() {
			return withoutXmlDeclaration(true);
		}

		/** Sets, whether an XML declaration should be written.
		 * @param pOmitXmlDeclaration True, if an XML declaration should be omitted, if possible.
		 *   Otherwise false, which is the default.
		 * @return This {@link SaxWriter SAX writer}.
		 */
		public SaxWriter withoutXmlDeclaration(boolean pOmitXmlDeclaration) {
			assertMutable();
			omittingXmlDeclaration = pOmitXmlDeclaration;
			return this; 
		}

		/** Writes an XML document to the given {@link OutputStream},
		 * applying the configured values for
		 * {@link #withIndentation(boolean) indentation},
		 * {@link #withEncoding(String) encoding},
		 * {@link #withNamespaceUri(String) namespace URI}, and
		 * {@link #withPrefix(String)} prefix.
		 * @param pOut The output stream, to which the XML document is
		 *   being written.
		 * @param pConsumer A consumer, which is being invoked to create
		 *   the XML documents content by using methods like
		 *   {@link #writeElement(String, Functions.FailableConsumer, Object...)}, etc.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public void write(OutputStream pOut, FailableConsumer<SaxWriter,SAXException> pConsumer) {
			final StreamResult sr = new StreamResult(Objects.requireNonNull(pOut, "OutputStream"));
			write(sr, pConsumer);
		}

		/** Writes an XML document to the given {@link Writer},
		 * applying the configured values for
		 * {@link #withIndentation(boolean) indentation},
		 * {@link #withEncoding(String) encoding},
		 * {@link #withNamespaceUri(String) namespace URI}, and
		 * {@link #withPrefix(String)} prefix.
		 * @param pOut The writer, to which the XML document is
		 *   being written.
		 * @param pConsumer A consumer, which is being invoked to create
		 *   the XML documents content by using methods like
		 *   {@link #writeElement(String, Functions.FailableConsumer, Object...)}, etc.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public void write(Writer pOut, FailableConsumer<SaxWriter,SAXException> pConsumer) {
			final StreamResult sr = new StreamResult(Objects.requireNonNull(pOut, "OutputStream"));
			write(sr, pConsumer);
		}

		/** Writes an XML document to the given {@link Path file},
		 * applying the configured values for
		 * {@link #withIndentation(boolean) indentation},
		 * {@link #withEncoding(String) encoding},
		 * {@link #withNamespaceUri(String) namespace URI}, and
		 * {@link #withPrefix(String)} prefix.
		 * @param pOut The writer, to which the XML document is
		 *   being written.
		 * @param pConsumer A consumer, which is being invoked to create
		 *   the XML documents content by using methods like
		 *   {@link #writeElement(String, Functions.FailableConsumer, Object...)}, etc.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public void write(Path pOut, FailableConsumer<SaxWriter,SAXException> pConsumer) {
			final Path file = Objects.requireNonNull(pOut, "Path");
			try (OutputStream out = Files.newOutputStream(file)) {
				write(out, pConsumer);
			} catch (IOException e) {
				throw Exceptions.show(e);
			}
		}

		/** Writes an XML document to the given {@link File file},
		 * applying the configured values for
		 * {@link #withIndentation(boolean) indentation},
		 * {@link #withEncoding(String) encoding},
		 * {@link #withNamespaceUri(String) namespace URI}, and
		 * {@link #withPrefix(String)} prefix.
		 * @param pOut The writer, to which the XML document is
		 *   being written.
		 * @param pConsumer A consumer, which is being invoked to create
		 *   the XML documents content by using methods like
		 *   {@link #writeElement(String, Functions.FailableConsumer, Object...)}, etc.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public void write(File pOut, FailableConsumer<SaxWriter,SAXException> pConsumer) {
			final File file = Objects.requireNonNull(pOut, "File");
			try (OutputStream out = new FileOutputStream(file)) {
				write(out, pConsumer);
			} catch (IOException e) {
				throw Exceptions.show(e);
			}
		}

		/** Writes an XML document to the given {@link Result},
		 * applying the configured values for
		 * {@link #withIndentation(boolean) indentation},
		 * {@link #withEncoding(String) encoding},
		 * {@link #withNamespaceUri(String) namespace URI}, and
		 * {@link #withPrefix(String)} prefix.
		 * @param pResult The transformer {@link Result result}.
		 * @param pConsumer A consumer, which is being invoked to create
		 *   the XML documents content by using methods like
		 *   {@link #writeElement(String, Functions.FailableConsumer, Object...)}, etc.
		 * @throws NullPointerException Either of the parameters is null.
		 */
		public void write(Result pResult, FailableConsumer<SaxWriter,SAXException> pConsumer) {
			final Result result = Objects.requireNonNull(pResult, "Result");
			final FailableConsumer<SaxWriter,SAXException> consumer = Objects.requireNonNull(pConsumer, "Consumer");
			immutable = true;
			try {
				final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
				transformerHandler = stf.newTransformerHandler();
				final Transformer transformer = transformerHandler.getTransformer();
				if (isIndenting()) {
					transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				}
				final String encoding = getEncoding();
				if (encoding != null) {
					transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
				}
				if (isOmittingXmlDeclaration()) {
					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
				}
				transformerHandler.setResult(result);
				transformerHandler.startDocument();
				consumer.accept(this);
				transformerHandler.endDocument();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		private static final @NonNull Attributes NO_ATTRS = new AttributesImpl();

		/** Writes an atomic element with the given local name, the
		 * {@link #withNamespaceUri(String) default namespace uri},
		 * and the given attributes. 
		 * @param pElementName The elements local name.
		 * @param pBody The elements content creator.
		 * May be null, in which case an empty element is being written.
		 * @param pAttributes The elements attributes, as a list of
		 * key/value pairs. The keys must be strings, the values will
		 * be converted to strings by invoking #toAttributeValue(Object).
		 */
		public void writeElement(String pElementName, String pBody, Object... pAttributes) {
			writeElement(pElementName, (sw) -> {
				writeText(pBody);
			}, pAttributes);
		}

		/** Writes a text value to the body of the XML document.
		 * @param pText The text value, which is being written.
		 */
		public void writeText(String pText) {
			final TransformerHandler th = getTransformerHandler();
			final char[] chars = pText.toCharArray();
			try {
				th.characters(chars, 0, chars.length);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
		
		/** Writes an XML element with the given local name, the
		 * {@link #withNamespaceUri(String) default namespace uri},
		 * and the given attributes.
		 * @param pElementName The elements local name.
		 * @param pBodyCreator The elements content creator.
		 * May be null, in which case an empty element is being written.
		 * @param pAttributes The elements attributes, as a list of
		 * key/value pairs. The keys must be strings, the values will
		 * be converted to strings by invoking #toAttributeValue(Object).
		 */
		public void writeElement(String pElementName, FailableConsumer<SaxWriter,SAXException> pBodyCreator,
				                 Object... pAttributes) {
			try {
				final Attributes attrs;
				if (pAttributes == null  ||  pAttributes.length == 0) {
					attrs = NO_ATTRS;
				} else {
					if ((pAttributes.length % 2) != 0) {
						throw new IllegalArgumentException("The attributes are supposed to"
								+ " be a list of key/value pairs, so the number of objects should be even.");
					}
					final AttributesImpl atts = new AttributesImpl();
					for (int i = 0;  i < pAttributes.length;  ) {
						final Object keyObj = pAttributes[i++];
						final Object valueObj = pAttributes[i++];
						if (valueObj != null) {
							if (keyObj == null) {
								throw new IllegalArgumentException("The attributes are supposed to"
										+ " be a list of key/value pairs, with non-null keys.");
							}
							final String key = keyObj.toString();
							final String value = toAttributeValue(valueObj);
							atts.addAttribute(XMLConstants.NULL_NS_URI, key, key, "CDATA", value);
						}
					}
					attrs = atts;
				}
				writeElement(pElementName, pBodyCreator, attrs);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Converts the given attribute value object to a string. The conversion
		 * depends on the type of the value object;
		 * <ol>
		 *   <li>Strings are returned as-is.</li>
		 *   <li>Instances of {@link BigDecimal} are being converted using {@link BigDecimal#toPlainString()}.
		 *   <li>[@link Object#toString()} is used for other numbers, and booleans.</li>
		 *   <li>The {@link #getDateTimeFormatter() default date/time formatter} is being used for
		 *     {@link TemporalAccessor temporal values}, like {@link ZonedDateTime}, {@link LocalDateTime},
		 *     {@link LocalDate}, and {@link LocalTime}.</li>
		 *   <li>An {@link IllegalArgumentException} is thrown for other object types.
		 * </ol>
		 * @param pValue The attribute value, which is being converted.
		 * @return The converted value.
		 * 
		 */
		public @NonNull String toAttributeValue(@NonNull Object pValue) {
			if (pValue instanceof String) {
				return (String) pValue;
			} else if (pValue instanceof BigDecimal) {
				@SuppressWarnings("null")
				final @NonNull String plainString = ((BigDecimal) pValue).toPlainString();
				return plainString;
			} else if (pValue instanceof Number  ||  pValue instanceof Boolean) {
				@SuppressWarnings("null")
				final @NonNull String v = pValue.toString();
				return v;
			} else if (pValue instanceof Calendar) {
				final @NonNull Calendar calendar = (Calendar) pValue;
				@SuppressWarnings("null")
				final @NonNull LocalDateTime localDateTime = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault());
				return toAttributeValue(localDateTime);
			} else if (pValue instanceof Date) {
				final @NonNull Date date = (Date) pValue;
				@SuppressWarnings("null")
				final @NonNull LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
				return toAttributeValue(localDateTime);
			} else if (pValue instanceof TemporalAccessor) {
				final DateTimeFormatter dtf = Objects.notNull(getDateTimeFormatter(), () -> DateTimeFormatter.ISO_DATE_TIME);
				@SuppressWarnings("null")
				final @NonNull String v = dtf.format((TemporalAccessor) pValue);
				return v;
			} else {
				throw new IllegalArgumentException("Expected String, Number, Boolean, or TemporalAccessor as attribute value, got " + pValue.getClass().getName());
			}
		}

		/** Writes an XML element with the given local name, the
		 * {@link #withNamespaceUri(String) default namespace uri},
		 * and the given attributes.
		 * @param pElementName The elements local name.
		 * @param pBodyCreator The elements body creator.
		 * May be null, in which case an empty element is being written.
		 * @param pAttributes The elements attributes, as a list of
		 * SAX attributes.
		 */
		public void writeElement(String pElementName, FailableConsumer<SaxWriter,SAXException> pBodyCreator,
				Attributes pAttributes) {
			final String localName = Objects.requireNonNull(pElementName, "Element Name");
			final Attributes attrs = Objects.notNull(pAttributes, NO_ATTRS);
			final String namespaceUri = Objects.notNull(getNamespaceUri(), XMLConstants.NULL_NS_URI);
			final String prefix = getPrefix();
			final String qName;
			if (prefix == null  ||  prefix.length() == 0) {
				qName = localName;
			} else {
				qName = prefix + ":" + localName;
			}
			final TransformerHandler th = getTransformerHandler();
			try {
				th.startElement(namespaceUri, localName, qName, attrs);
				if (pBodyCreator != null) {
					pBodyCreator.accept(this);
				}
				th.endElement(namespaceUri, localName, qName);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}


	}
}
