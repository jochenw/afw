package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.util.Stax.ElementAction;
import com.github.jochenw.afw.core.util.Stax.ElementListener;

/** Test case for the {@link Stax} class.
 */
public class StaxTest {
	/** Test case for {@link Stax#asLocalizedMessage(String, String, int, int)}.
	 */
	@Test
	public void testAsLocalizedMessage() {
		validate("Some error", "Some error", null, -1, -1);
		validate("At SomeFile: Some error", "Some error", "SomeFile", -1, -1);
		validate("At SomeFile, line 15: Some error", "Some error", "SomeFile", 15, -1);
		validate("At SomeFile, column 31: Some error", "Some error", "SomeFile", -1, 31);
		validate("At SomeFile, line 14, column 61: Some error", "Some error", "SomeFile", 14, 61);
		validate("At line 15: Some error", "Some error", null, 15, -1);
		validate("At column 31: Some error", "Some error", null, -1, 31);
		validate("At line 14, column 61: Some error", "Some error", null, 14, 61);
	}

	/** Tests {@link Stax#asLocalizedMessage(String, String, int, int)} by invoking
	 * it with the given parameters, and comparing the result with the expected string.
	 * @param pExpect The expected result string.
	 * @param pMsg The error message, that is being reported.
	 * @param pSystemId The reported system id, or null.
	 * @param pLineNumber The reported line number, or -1.
	 * @param pColumnNumber The reported column number, or -1.
	 */
	protected void validate(String pExpect, String pMsg, String pSystemId, int pLineNumber, int pColumnNumber) {
		assertEquals(pExpect, Stax.asLocalizedMessage(pMsg, pSystemId, pLineNumber, pColumnNumber));
		final Location loc = new Location() {
			@Override
			public int getLineNumber() {
				return pLineNumber;
			}

			@Override
			public int getColumnNumber() {
				return pColumnNumber;
			}

			@Override
			public int getCharacterOffset() {
				return -1;
			}

			@Override
			public String getPublicId() {
				return null;
			}

			@Override
			public String getSystemId() {
				return pSystemId;
			}
		};
		assertEquals(pExpect, Stax.asLocalizedMessage(loc, pMsg));
	}

	private static final String FLAT_SAMPLE_0 = "<xml><a>text</a></xml>";
	private static final String FLAT_SAMPLE_1 = "<xml><a>text<b>Error</b></a></xml>";
	
	/** Test case for {@link Stax#skipElementRecursively(XMLStreamReader)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testSkipElementRecursively() throws Exception {
		final XMLStreamReader rdr = newReader(FLAT_SAMPLE_0, XMLConstants.NULL_NS_URI, "a");
		Stax.skipElementRecursively(rdr);
		Assert.assertTrue(rdr.hasNext());
		Assert.assertEquals(XMLStreamReader.END_ELEMENT, rdr.next());
		Stax.assertDefaultNamespace(rdr);
		Assert.assertEquals("xml", rdr.getLocalName());

		final XMLStreamReader rdr2 = newReader(FLAT_SAMPLE_1, XMLConstants.NULL_NS_URI, "a");
		Stax.skipElementRecursively(rdr2);
		Assert.assertTrue(rdr2.hasNext());
		Assert.assertEquals(XMLStreamReader.END_ELEMENT, rdr2.next());
		Stax.assertDefaultNamespace(rdr2);
		Assert.assertEquals("xml", rdr2.getLocalName());
	}

	/** Test case for {@link Stax#skipElement(XMLStreamReader, ElementListener)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testSkipElementRecursivelyWithListener() throws Exception {
		final XMLStreamReader rdr = newReader(FLAT_SAMPLE_0, XMLConstants.NULL_NS_URI, "a");
		final ElementListener el1 = new ElementListener() {
			@Override
			public ElementAction element(XMLStreamReader pReader, int pLevel, String pNamespaceUri, String pLocalName)
					throws XMLStreamException {
				Assert.fail("Unexpected start element");
				return ElementAction.SKIPPED;
			}
		};
		Stax.skipElement(rdr, el1);
		Assert.assertTrue(rdr.hasNext());
		Assert.assertEquals(XMLStreamReader.END_ELEMENT, rdr.next());
		Stax.assertDefaultNamespace(rdr);
		Assert.assertEquals("xml", rdr.getLocalName());

		final XMLStreamReader rdr2 = newReader(FLAT_SAMPLE_1, XMLConstants.NULL_NS_URI, "a");
		Stax.skipElementRecursively(rdr2);
		Assert.assertTrue(rdr2.hasNext());
		Assert.assertEquals(XMLStreamReader.END_ELEMENT, rdr2.next());
		Stax.assertDefaultNamespace(rdr2);
		Assert.assertEquals("xml", rdr2.getLocalName());
	}

	/** Test case for {@link Stax#skipElementFlat(XMLStreamReader)}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testSkipElementFlat() throws Exception {
		final XMLStreamReader rdr = newReader(FLAT_SAMPLE_0, XMLConstants.NULL_NS_URI, "a");
		Stax.skipElementFlat(rdr);
		Assert.assertTrue(rdr.hasNext());
		Assert.assertEquals(XMLStreamReader.END_ELEMENT, rdr.next());
		Stax.assertDefaultNamespace(rdr);
		Assert.assertEquals("xml", rdr.getLocalName());

		final XMLStreamReader rdr2 = newReader(FLAT_SAMPLE_1, XMLConstants.NULL_NS_URI, "a");
		try {
			Stax.skipElementFlat(rdr2);
			Assert.fail("Expected exception");
		} catch (XMLStreamException xse) {
			Assert.assertTrue(xse.getMessage().endsWith("Unexpected child element: a"));
		}
	}

	/** Creates a new {@link XMLStreamReader}, that parses the given
	 * XML string, using the given namespace URI, and the given local name
	 * @param pXml The XML string, that is being parsed.
	 * @param pUri Namespace URI of an element, that is expected in the XML string.
	 * @param pLocalName Local name of the same element.
	 * @return The created {@link XMLStreamReader}, positioned after the start element
	 *   of the given element.
	 * @throws XMLStreamException Parsing the XML string has failed.
	 */
	protected XMLStreamReader newReader(String pXml, String pUri, String pLocalName) throws XMLStreamException {
		final Reader r = new StringReader(pXml);
		final XMLStreamReader rdr = XMLInputFactory.newFactory().createXMLStreamReader(r);
		while (rdr.hasNext()) {
			final int state = rdr.next();
			switch (state) {
			case XMLStreamReader.START_ELEMENT:
				final String uri = rdr.getNamespaceURI();
				final String localName = rdr.getLocalName();
				if (pUri == null  ||  pUri.length() == 0) {
					if ((uri == null  ||  uri.length() == 0)  &&  pLocalName.equals(localName)) {
						return rdr;
					}
				} else {
					if (pUri.equals(uri)  &&  pLocalName.equals(localName)) {
						return rdr;
					}
				}
			}
		}
		throw Stax.error(rdr, "Element not found: " + Sax.asQName(pUri, pLocalName));
	}
}
