package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.function.Consumer;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


/** Test for the {@link XmlParsers} class.
 */
public class XmlParsersTest {
	private static final String NS = "http://namespaces.github.com/jochenw/afw/core/io/XmlParsersTest-1.0.0";
	private static final String XML =
			"<?xml version='1.0'?>\n"
			+ "<document xmlns='" + NS + "'>\n"
			+ "</document>";

	/** Test case for creating a document.
	 */
	@Test
	public void testDocumentCreator() {
		final InputSource isource = new InputSource(new StringReader(XML));
		final Consumer<Document> consumer = (doc) -> {
			assertNotNull(doc);
			final Element e = doc.getDocumentElement();
			assertNotNull(e);
			assertEquals(NS, e.getNamespaceURI());
			assertEquals("document", e.getTagName());
		};
		XmlParsers.parse(isource, consumer);
	}

}
