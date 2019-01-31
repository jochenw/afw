/**
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
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.LocatorImpl;

public class LocalizableDocument {
	private static final String KEY = Locator.class.getName();

	private static class Handler implements ContentHandler {
		private final Document nodeFactory;
		private Node currentNode;
		private Locator locator;

		Handler(Document pDocument) {
			nodeFactory = pDocument;
		}

		@Override
		public void setDocumentLocator(Locator pLocator) {
			locator = pLocator;
		}

		private <N extends Node> N setUserData(N pNode) {
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
			final Element e = setUserData(nodeFactory.createElementNS(pUri, pQName));
			if (pAttrs != null) {
				for (int i = 0;  i < pAttrs.getLength();  i++) {
					final Attr attr = setUserData(nodeFactory.createAttributeNS(pAttrs.getURI(i), pAttrs.getQName(i)));
					attr.setNodeValue(pAttrs.getValue(i));
					e.setAttributeNodeNS(attr);
				}
			}
			currentNode.appendChild(e);
			currentNode = e;
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			final Element e = (Element) currentNode;
			currentNode = e.getParentNode();
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			final Text text = setUserData(nodeFactory.createTextNode(new String(ch, start, length)));
			currentNode.appendChild(text);
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			characters(ch, start, length);
		}

		@Override
		public void processingInstruction(String target, String data) throws SAXException {
			currentNode.appendChild(setUserData(nodeFactory.createProcessingInstruction(target, data)));
		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			currentNode.appendChild(setUserData(nodeFactory.createEntityReference(name)));
		}
	}

	private final Document document;

	private LocalizableDocument(Document pDocument) {
		document = pDocument;
	}
	
	public static LocalizableDocument parse(InputSource pSource) {
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			final Document doc = dbf.newDocumentBuilder().newDocument();
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			spf.setNamespaceAware(true);
			final Handler h = new Handler(doc);
			final XMLReader reader = spf.newSAXParser().getXMLReader();
			reader.setContentHandler(h);
			reader.parse(pSource);
			return new LocalizableDocument(doc);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static LocalizableDocument parse(File pFile) {
		try (InputStream istream = new FileInputStream(pFile)) {
			final InputSource isource = new InputSource(istream);
			isource.setSystemId(pFile.getAbsolutePath());
			return parse(isource);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static LocalizableDocument parse(URL pUrl) {
		try (InputStream istream = pUrl.openStream()) {
			final InputSource isource = new InputSource(istream);
			isource.setSystemId(pUrl.toExternalForm());
			return parse(isource);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static LocalizableDocument parse(InputStream pStream) {
		final InputSource isource = new InputSource(pStream);
		return parse(isource);
	}

	public Locator getLocator(Node pNode) {
		return (Locator) pNode.getUserData(KEY);
	}

	public Document getDocument() {
		return document;
	}
}
