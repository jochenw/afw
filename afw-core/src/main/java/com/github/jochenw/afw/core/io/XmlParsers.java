package com.github.jochenw.afw.core.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.github.jochenw.afw.core.util.Exceptions;


public class XmlParsers {
	public static void parse(InputSource pSource, ContentHandler pHandler) {
		try {
			final SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			spf.setNamespaceAware(true);
			final XMLReader reader = spf.newSAXParser().getXMLReader();
			reader.setContentHandler(pHandler);
			reader.parse(pSource);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static void parse(InputSource pSource, Consumer<Document> pDocumentConsumer) {
		try {
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			final Document doc = dbf.newDocumentBuilder().parse(pSource);
			pDocumentConsumer.accept(doc);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static void parse(InputStream pStream, String pSystemId, ContentHandler pHandler) {
		final InputSource isource = new InputSource(pStream);
		isource.setSystemId(pSystemId);
		parse(isource, pHandler);
	}

	public static void parse(InputStream pStream, String pSystemId, Consumer<Document> pDocumentConsumer) {
		final InputSource isource = new InputSource(pStream);
		isource.setSystemId(pSystemId);
		parse(isource, pDocumentConsumer);
	}

	public static void parse(File pFile, ContentHandler pHandler) {
		try (InputStream in = new FileInputStream(pFile)) {
			parse(in, pFile.getAbsolutePath(), pHandler);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static void parse(Path pPath, ContentHandler pHandler) {
		try (InputStream in = Files.newInputStream(pPath)) {
			parse(in, pPath.toAbsolutePath().toString(), pHandler);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static void parse(URL pUrl, ContentHandler pHandler) {
		try (InputStream in = pUrl.openStream()) {
			parse(in, pUrl.toExternalForm(), pHandler);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static void parse(File pFile, Consumer<Document> pDocumentConsumer) {
		try (InputStream in = new FileInputStream(pFile)) {
			parse(in, pFile.getAbsolutePath(), pDocumentConsumer);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static void parse(Path pPath, Consumer<Document> pDocumentConsumer) {
		try (InputStream in = Files.newInputStream(pPath)) {
			parse(in, pPath.toAbsolutePath().toString(), pDocumentConsumer);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static void parse(URL pUrl, Consumer<Document> pDocumentConsumer) {
		try (InputStream in = pUrl.openStream()) {
			parse(in, pUrl.toExternalForm(), pDocumentConsumer);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}


}
