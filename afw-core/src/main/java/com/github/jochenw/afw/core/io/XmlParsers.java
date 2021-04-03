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
package com.github.jochenw.afw.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Sax;


/** A utility class for working with XML parsers.
 */
public class XmlParsers {
	/**
	 * Parses the given {@link InputSource} into a DOM document, which is then
	 * passed to the given {@link Consumer}.
	 * @param pSource The XML documents data source.
	 * @param pDocumentConsumer The consumer, that should receive the created DOM
	 *   document.
	 */
	public static void parse(InputSource pSource, Consumer<Document> pDocumentConsumer) {
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			final Document doc = dbf.newDocumentBuilder().parse(pSource);
			pDocumentConsumer.accept(doc);
		} catch (SAXException|IOException|ParserConfigurationException t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Parses the given {@link InputStream} into a stream of SAX event stream,
	 * which is then passed to the given {@link ContentHandler}.
	 * @param pStream The XML documents data source.
	 * @param pSystemId The documents system id, for use in error messages.
	 * @param pHandler The SAX handler, which is receiving the SAX event stream
	 */
	public static void parse(InputStream pStream, String pSystemId, ContentHandler pHandler) {
		final InputSource isource = new InputSource(pStream);
		isource.setSystemId(pSystemId);
		Sax.parse(isource, pHandler);
	}

	/**
	 * Parses the given {@link InputSource} into a DOM document, which is then
	 * passed to the given {@link Consumer}.
	 * @param pStream The XML documents data source.
	 * @param pSystemId The documents system id, for use in error messages.
	 * @param pDocumentConsumer The consumer, that should receive the created DOM
	 *   document.
	 */
	public static void parse(InputStream pStream, String pSystemId, Consumer<Document> pDocumentConsumer) {
		final InputSource isource = new InputSource(pStream);
		isource.setSystemId(pSystemId);
		parse(isource, pDocumentConsumer);
	}

	/**
	 * Parses the given {@link File} into a stream of SAX event stream,
	 * which is then passed to the given {@link ContentHandler}.
	 * @param pFile The XML documents data source.
	 * @param pHandler The SAX handler, which is receiving the SAX event stream
	 */
	public static void parse(File pFile, ContentHandler pHandler) {
		try (InputStream in = new FileInputStream(pFile)) {
			parse(in, pFile.getAbsolutePath(), pHandler);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Parses the given {@link Path} into a stream of SAX event stream,
	 * which is then passed to the given {@link ContentHandler}.
	 * @param pPath The XML documents data source.
	 * @param pHandler The SAX handler, which is receiving the SAX event stream
	 */
	public static void parse(Path pPath, ContentHandler pHandler) {
		try (InputStream in = Files.newInputStream(pPath)) {
			parse(in, pPath.toAbsolutePath().toString(), pHandler);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Parses the given {@link URL} into a stream of SAX event stream,
	 * which is then passed to the given {@link ContentHandler}.
	 * @param pUrl The XML documents data source.
	 * @param pHandler The SAX handler, which is receiving the SAX event stream
	 */
	public static void parse(URL pUrl, ContentHandler pHandler) {
		try (InputStream in = pUrl.openStream()) {
			parse(in, pUrl.toExternalForm(), pHandler);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Parses the given {@link File} into a DOM document, which is then
	 * passed to the given {@link Consumer}.
	 * @param pFile The XML documents data source.
	 * @param pDocumentConsumer The consumer, that should receive the created DOM
	 *   document.
	 */
	public static void parse(File pFile, Consumer<Document> pDocumentConsumer) {
		try (InputStream in = new FileInputStream(pFile)) {
			parse(in, pFile.getAbsolutePath(), pDocumentConsumer);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Parses the given {@link Path} into a DOM document, which is then
	 * passed to the given {@link Consumer}.
	 * @param pPath The XML documents data source.
	 * @param pDocumentConsumer The consumer, that should receive the created DOM
	 *   document.
	 */
	public static void parse(Path pPath, Consumer<Document> pDocumentConsumer) {
		try (InputStream in = Files.newInputStream(pPath)) {
			parse(in, pPath.toAbsolutePath().toString(), pDocumentConsumer);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	/**
	 * Parses the given {@link URL} into a DOM document, which is then
	 * passed to the given {@link Consumer}.
	 * @param pUrl The XML documents data source.
	 * @param pDocumentConsumer The consumer, that should receive the created DOM
	 *   document.
	 */
	public static void parse(URL pUrl, Consumer<Document> pDocumentConsumer) {
		try (InputStream in = pUrl.openStream()) {
			parse(in, pUrl.toExternalForm(), pDocumentConsumer);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
