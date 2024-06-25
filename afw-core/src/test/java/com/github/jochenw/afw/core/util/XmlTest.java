package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import javax.xml.XMLConstants;

import org.junit.Test;

/** Test suite for the {@link Xml} class.
 */
public class XmlTest {
	private static final String XMLNS_SOAP = "http://schemas.xmlsoap.org/soap/envelope/";

	/** Test creation of a basic XML document with Writer consumers.
	 */
	@Test
	public void testBasicXmlDocument() {
		final String actual = Xml.builder().indenting(false).omittingXmlDeclaration()
				.build(w0 -> {
					w0.namespace("soap", XMLNS_SOAP, w1 -> {
					    w1.elementNs(XMLNS_SOAP, "Envelope", w2 -> {
							w2.elementNs(XMLNS_SOAP, "Body", w3 -> {
								w3.namespace("", XMLConstants.NULL_NS_URI, w4 -> {
								    w4.element("Person", null,
										       "surName", "Wiedmann",
										       "firstName", "Jochen");
							    });
						    });
					    });
					});
				}).getString();
		final String expect = "<soap:Envelope xmlns:soap='" + XMLNS_SOAP + "'>"
				+ "<soap:Body><Person surName='Wiedmann' firstName='Jochen'/>"
				+ "</soap:Body></soap:Envelope>";
		assertEquals(expect, actual.replace('"','\''));
	}
	/** Test creation of a basic XML document with Writer consumers.
	 * using the default namespace methods.
	 */
	@Test
	public void testBasicXmlDocumentUsingDefaultNamespace() {
		final String actual = Xml.builder().indenting(false).omittingXmlDeclaration()
				.build(w0 -> {
					w0.namespace("soap", XMLNS_SOAP, w1 -> {
					    w1.elementNs(XMLNS_SOAP, "Envelope", w2 -> {
							w2.elementNs(XMLNS_SOAP, "Body", w3 -> {
								w3.namespace("", XMLConstants.NULL_NS_URI, w4 -> {
								    w4.element("Person", null,
										       "surName", "Wiedmann",
										       "firstName", "Jochen");
							    });
						    });
					    });
					});
				}).getString();
		final String expect = "<soap:Envelope xmlns:soap='" + XMLNS_SOAP + "'>"
				+ "<soap:Body><Person surName='Wiedmann' firstName='Jochen'/>"
				+ "</soap:Body></soap:Envelope>";
		assertEquals(expect, actual.replace('"','\''));
	}
	/** Test creation of a basic XML document with runnables.
	 */
	@Test
	public void testBasicXmlDocumentUsingRunnables() {
		final String actual = Xml.builder().indenting(false).omittingXmlDeclaration()
				.build(w0 -> {
					w0.namespace("soap", XMLNS_SOAP, () -> {
						w0.element(XMLNS_SOAP, "Envelope", () -> {
							w0.element(XMLNS_SOAP, "Body", () -> {
								w0.namespace("", XMLConstants.NULL_NS_URI, () -> {
							        w0.element("Person", null,
										 "surName", "Wiedmann",
										 "firstName", "Jochen");
							    });
						    });
					    });
					});
				}).getString();
		final String expect = "<soap:Envelope xmlns:soap='" + XMLNS_SOAP + "'>"
				+ "<soap:Body><Person surName='Wiedmann' firstName='Jochen'/>"
				+ "</soap:Body></soap:Envelope>";
		assertEquals(expect, actual.replace('"','\''));
	}
	/** Test creation of a basic XML document with Writer consumers.
	 * using the default namespace methods.
	 */
	@Test
	public void testBasicXmlDocumentUsingDefaultNamespaceAndRunnables() {
		final String actual = Xml.builder().indenting(false).omittingXmlDeclaration()
				.build(w0 -> {
					w0.namespace("soap", XMLNS_SOAP, () -> {
					    w0.elementNs(XMLNS_SOAP, "Envelope", () -> {
							w0.elementNs(XMLNS_SOAP, "Body", () -> {
								w0.namespace("", XMLConstants.NULL_NS_URI, () -> {
								    w0.element("Person", null,
										       "surName", "Wiedmann",
										       "firstName", "Jochen");
							    });
						    });
					    });
					});
				}).getString();
		final String expect = "<soap:Envelope xmlns:soap='" + XMLNS_SOAP + "'>"
				+ "<soap:Body><Person surName='Wiedmann' firstName='Jochen'/>"
				+ "</soap:Body></soap:Envelope>";
		assertEquals(expect, actual.replace('"','\''));
	}
}
