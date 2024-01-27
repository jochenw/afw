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
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.LocatorImpl;

import com.github.jochenw.afw.core.util.DomHelper.LocalizableException;



/** This class is basically a DOM parser with the ability to provide
 * location information for the nodes in the parsed document.
 */
public class LocalizableDocument {
	private static final String KEY = Locator.class.getName();

	/** An object, which can provide a {@link Locator}, thus indicating a location,
	 * from which the object has been created.
	 */
	public static class Localizable {
		private final Locator locator;

		/**
		 * Creates a new instance with the given {@link Locator}.
		 * @param pLocator The {@link Locator}, that is being
		 *   provided by this object.
		 */
		public Localizable(Locator pLocator) {
			locator = pLocator;
		}

		/** Returns the objects {@link Locator location}.
		 * @return The objects {@link Locator location}.
		 */
		public Locator getLocator() {
			return locator;
		}
	}

	/** A SAX parser, that is being used to create a {@link LocalizableDocument}.
	 */
	public static class Handler implements ContentHandler, LexicalHandler {
		private final @NonNull Document nodeFactory;
		private @Nullable Node currentNode;
		private @Nullable Locator locator;

		/** Creates a new instance, that uses the given {@link Document}
		 * as a {@link Node node} factory.
		 * @param pDocument A document, that is being used as a as
		 * {@link Node node} factory.
		 */
		public Handler(@NonNull Document pDocument) {
			nodeFactory = pDocument;
		}

		@Override
		public void setDocumentLocator(Locator pLocator) {
			locator = pLocator;
		}

		private <N extends Node> N setUserData(@NonNull N pNode) {
			if (locator != null) {
				pNode.setUserData(KEY, new LocatorImpl(locator), null);
			}
			return pNode;
		}
		
		@Override
		public void startDocument() throws SAXException {
			currentNode = setUserData(nodeFactory);
		}

		@Override
		public void endDocument() throws SAXException {
			if (currentNode == nodeFactory) {
				currentNode = null;
			} else {
				throw new IllegalStateException("Current Node = " + currentNode);
			}
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			// Ignore this
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			// Ignore this
		}

		@Override
		public void startElement(String pUri, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
			@SuppressWarnings("null")
			final @NonNull Element el = nodeFactory.createElementNS(pUri, pQName);
			final @NonNull Element e = setUserData(el);
			if (pAttrs != null) {
				for (int i = 0;  i < pAttrs.getLength();  i++) {
					@SuppressWarnings("null")
					final @NonNull Attr attrbt = nodeFactory.createAttributeNS(pAttrs.getURI(i), pAttrs.getQName(i));
					final Attr attr = setUserData(attrbt);
					attr.setNodeValue(pAttrs.getValue(i));
					e.setAttributeNodeNS(attr);
				}
			}
			final @NonNull Node cn = Objects.requireNonNull(currentNode);
			cn.appendChild(e);
			currentNode = e;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			final @NonNull Element e = Objects.requireNonNull((Element) currentNode);
			currentNode = e.getParentNode();
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			@SuppressWarnings("null")
			final @NonNull Text txt = nodeFactory.createTextNode(new String(ch, start, length));
			final Text text = setUserData(txt);
			Objects.requireNonNull(currentNode).appendChild(text);
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			characters(ch, start, length);
		}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			@SuppressWarnings("null")
			final @NonNull ProcessingInstruction pi = nodeFactory.createProcessingInstruction(target, data);
			Objects.requireNonNull(currentNode).appendChild(setUserData(pi));
		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			@SuppressWarnings("null")
			final @NonNull EntityReference eRef = nodeFactory.createEntityReference(name);
			Objects.requireNonNull(currentNode).appendChild(setUserData(eRef));
		}

		@Override
		public void startDTD(String name, String publicId, String systemId) throws SAXException {
			// Ignore DTD
		}

		@Override
		public void endDTD() throws SAXException {
			// Ignore DTD
		}

		@Override
		public void startEntity(String name) throws SAXException {
			// Ignore the fact, that we are now in an included entity.
		}

		@Override
		public void endEntity(String name) throws SAXException {
			// Ignore the fact, that we are no longer in an included entity.
		}

		@Override
		public void startCDATA() throws SAXException {
			// We don't distinguish between CDATA, and plain text.
		}

		@Override
		public void endCDATA() throws SAXException {
			// We don't distinguish between CDATA, and plain text.
		}

		@Override
		public void comment(char[] ch, int start, int length) throws SAXException {
			@SuppressWarnings("null")
			final @NonNull Text txt = (Text) nodeFactory.createComment(new String(ch, start, length));
			final @NonNull Text text = setUserData(txt);
			Objects.requireNonNull(currentNode).appendChild(text);
		}
	}

	private final @NonNull Document document;
	private final @NonNull DomHelper domHelper = new DomHelper();

	/**
	 * Creates a new instance with the given {@link Document}.
	 * @param pDocument A {@link Document}, that has been parsed.
	 */
	public LocalizableDocument(@NonNull Document pDocument) {
		document = pDocument;
		domHelper.setDefaultNamespaceUri(pDocument.getDocumentElement().getNamespaceURI());
		domHelper.setLocationProvider((node) -> getLocator(node));
		domHelper.setErrorHandler((loc,msg) -> new LocalizableException(loc,msg));
	}

	/**
	 * Parses the given {@link InputSource}, and returns a document
	 * with localization info.
	 * @param pSource The source, from which the parsed XML document
	 * is being read.
	 * @return A document with localization info.
	 */
	public static LocalizableDocument parse(InputSource pSource) {
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			@SuppressWarnings("null")
			final @NonNull Document doc = dbf.newDocumentBuilder().newDocument();
			final Handler h = new Handler(doc);
			Sax.parse(pSource, h);
			return new LocalizableDocument(doc);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Parses the given {@link File}, and returns a document
	 * with localization info.
	 * @param pFile The file, from which the parsed XML document
	 * is being read.
	 * @return A document with localization info.
	 */
	public static LocalizableDocument parse(File pFile) {
		try (InputStream istream = new FileInputStream(pFile)) {
			final InputSource isource = new InputSource(istream);
			isource.setSystemId(pFile.getAbsolutePath());
			return parse(isource);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Parses the given {@link URL}, and returns a document
	 * with localization info.
	 * @param pUrl The {@link URL}, from which the parsed XML document
	 * is being read.
	 * @return A document with localization info.
	 */
	public static LocalizableDocument parse(URL pUrl) {
		try (InputStream istream = pUrl.openStream()) {
			final InputSource isource = new InputSource(istream);
			isource.setSystemId(pUrl.toExternalForm());
			return parse(isource);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Parses the given {@link InputStream}, and returns a document
	 * with localization info.
	 * @param pStream The byte stream, from which the parsed XML document
	 * is being read.
	 * @return A document with localization info.
	 */
	public static LocalizableDocument parse(InputStream pStream) {
		final InputSource isource = new InputSource(pStream);
		return parse(isource);
	}

	/** Returns location information for the given node.
	 * The node must have been created (as part of a {@link Document})
	 * by invoking either of the various parse methods.
	 * @param pNode The node, for which location information is
	 *   being queried.
	 * @return The requested location information, or null, if the
	 *   node is from a foreign document, and no location information
	 *   is available.
	 * @see #parse(InputSource)
	 * @see #parse(File)
	 * @see #parse(URL)
	 * @see #parse(InputStream)
	 */
	public Locator getLocator(Node pNode) {
		return (Locator) pNode.getUserData(KEY);
	}

	/** Returns the document, that has been parsed. For any node in
	 * this document, {@link #getLocator(Node)} will provide
	 * location information.
	 * @return The document, that has been parsed.
	 */
	public @NonNull Document getDocument() {
		return document;
	}

	/**
	 * Returns a {@link DomHelper} with suitable {@link DomHelper#getErrorHandler() error handler},
	 * and {@link DomHelper#getLocationProvider() location provider}.
	 * @return a {@link DomHelper} with suitable {@link DomHelper#getErrorHandler() error handler},
	 * and {@link DomHelper#getLocationProvider() location provider}.
	 */
	public DomHelper getDomHelper() {
		return domHelper;
	}

}
