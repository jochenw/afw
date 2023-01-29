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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nullable;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;

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

}
