/**
 * 
 */
package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;

import com.github.jochenw.afw.core.function.IStreamableIterable;
import com.github.jochenw.afw.core.util.DomHelper.LocalizableException;

/**
 * @author jwi
 *
 */
public class DomHelperTest {
	private static final String NS = "http://namespaces.github.com/jochenw/afw/core/test/DomHelper/1.0.0";
	private static final DomHelper domHelper = newDomHelper();

	private static DomHelper newDomHelper() {
		final DomHelper dh = new DomHelper();
		dh.setDefaultNamespaceUri(NS);
		return dh;
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#getDefaultNamespaceUri()}.
	 */
	@Test
	public void testGetDefaultNamespaceUri() {
		final DomHelper dh = new DomHelper();
		assertNull(dh.getDefaultNamespaceUri());
		assertSame(NS, domHelper.getDefaultNamespaceUri());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#getErrorHandler()}.
	 */
	@Test
	public void testGetErrorHandler() {
		final DomHelper dh = new DomHelper();
		assertNull(dh.getErrorHandler());
		final LocalizableDocument lh = getSampleDocument();
		assertNotNull(lh.getDomHelper().getErrorHandler());
		BiFunction<Locator, String, LocalizableException> errorHandler = (l,s) -> new LocalizableException(null, s);
		dh.setErrorHandler(errorHandler);
		assertSame(errorHandler, dh.getErrorHandler());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#getLocationProvider()}.
	 */
	@Test
	public void testGetLocationProvider() {
		final DomHelper dh = new DomHelper();
		assertNull(dh.getLocationProvider());
		final LocalizableDocument lh = getSampleDocument();
		assertNotNull(lh.getDomHelper().getLocationProvider());
		
	}

	private static final String SAMPLE_DOCUMENT =
			"<html xmlns='" + NS + "' xmlns:t='foo'>\n"
			+ "<head><title>Sample Document</title></head>"
			+ "<body>\n"
			+ "  <p>Paragraph 1</p>\n"
			+ "  <t:p>Paragraph 2</t:p>\n"
			+ "  <p>Paragraph 3</p>\n"
			+ "</body>\n"
			+ "</html>";

	protected LocalizableDocument getSampleDocument() {
		final InputSource isource = new InputSource(new StringReader(SAMPLE_DOCUMENT));
		isource.setSystemId("SAMPLE_DOCUMENT");
		return LocalizableDocument.parse(isource);
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#error(org.w3c.dom.Node, java.lang.String)}.
	 */
	@Test
	public void testError() {
		final LocalizableDocument ldoc = getSampleDocument();
		final LocalizableException lex = ldoc.getDomHelper().error(ldoc.getDocument(), "Not an element");
		assertNotNull(lex);
		Locator loc = lex.getLocator();
		assertNotNull(loc);
		assertTrue(loc.getSystemId().endsWith("SAMPLE_DOCUMENT"));
		assertNull(loc.getPublicId());
		assertEquals(1, loc.getLineNumber());
		assertEquals(1, loc.getColumnNumber());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#getChildren(org.w3c.dom.Node)}.
	 */
	@Test
	public void testGetChildrenNode() {
		final LocalizableDocument ldoc = getSampleDocument();
		final DomHelper dh = ldoc.getDomHelper();
		final Iterable<Element> htmlIterable = dh.getChildren(ldoc.getDocument().getDocumentElement());
		assertNotNull(htmlIterable);
		final Iterator<Element> htmlIterator = htmlIterable.iterator();
		assertNotNull(htmlIterator);
		assertTrue(htmlIterator.hasNext());
		final Element headElement = htmlIterator.next();
		assertNotNull(headElement);
		assertEquals(NS, headElement.getNamespaceURI());
		assertEquals("head", headElement.getLocalName());
		assertTrue(htmlIterator.hasNext());
		final Element bodyElement = htmlIterator.next();
		assertNotNull(bodyElement);
		assertEquals(NS, bodyElement.getNamespaceURI());
		assertEquals("body", bodyElement.getLocalName());
		assertFalse(htmlIterator.hasNext());
		final Iterable<Element> headIterable = dh.getChildren(headElement);
		assertNotNull(headIterable);
		final Iterator<Element> headIterator = headIterable.iterator();
		assertNotNull(headIterator);
		assertTrue(headIterator.hasNext());
		assertTrue(headIterator.hasNext());
		final Element titleElement = headIterator.next();
		assertNotNull(titleElement);
		assertEquals("Sample Document", titleElement.getTextContent());
		assertFalse(headIterator.hasNext());
		assertFalse(headIterator.hasNext());
		final Iterable<Element> titleIterable = dh.getChildren(titleElement);
		assertNotNull(titleIterable);
		final Iterator<Element> titleIterator = titleIterable.iterator();
		assertNotNull(titleIterator);
		assertFalse(titleIterator.hasNext());
		try {
			titleIterator.next();
			fail("Expexted Exception");
		} catch (NoSuchElementException e) {
			assertNull(e.getMessage());
		}
		final IStreamableIterable<Element> bodyIterable = dh.getChildren(bodyElement);
		assertNotNull(bodyIterable);
		final Iterator<Element> bodyIterator = bodyIterable.iterator();
		assertNotNull(bodyIterator);
		assertTrue(bodyIterator.hasNext());
		final Element p1 = bodyIterator.next();
		assertNotNull(p1);
		assertEquals(NS, p1.getNamespaceURI());
		assertEquals("p", p1.getLocalName());
		assertEquals("p", p1.getTagName());
		assertEquals("Paragraph 1", p1.getTextContent());
		assertTrue(bodyIterator.hasNext());
		final Element p2 = bodyIterator.next();
		assertNotNull(p2);
		assertEquals("foo", p2.getNamespaceURI());
		assertEquals("p", p2.getLocalName());
		assertEquals("t:p", p2.getTagName());
		assertEquals("Paragraph 2", p2.getTextContent());
		assertTrue(bodyIterator.hasNext());
		final Element p3 = bodyIterator.next();
		assertNotNull(p3);
		assertEquals(NS, p3.getNamespaceURI());
		assertEquals("p", p3.getLocalName());
		assertEquals("p", p3.getTagName());
		assertEquals("Paragraph 3", p3.getTextContent());
		assertFalse(bodyIterator.hasNext());

		/** Test, whether there is a child element t:p by using the stream API.
		 */
		final Iterator<Element> bodyIterator2 = dh.getChildren(bodyElement).iterator();
		assertTrue(bodyIterator2.hasNext());
		
		assertNotNull(dh.getChildren(bodyElement, "p").iterator().next());
		assertTrue(dh.getChildrenNS(bodyElement, "foo", "p").stream().anyMatch((e) -> e != null));
		assertTrue(dh.getChildren(bodyElement, "p").stream().anyMatch((e) -> {
			return "Paragraph 1".equals(e.getTextContent());
		}));
		assertTrue(dh.getChildren(bodyElement, "p").stream().anyMatch((e) -> {
			return "Paragraph 1".equals(e.getTextContent());
		}));
		assertFalse(dh.getChildren(bodyElement, "p").stream().anyMatch((e) -> "Para 1".equals(e.getTextContent())));
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#getChildren(org.w3c.dom.Node, java.lang.String)}.
	 */
	@Test
	public void testGetChildrenNodeString() {
		final LocalizableDocument ldoc = getSampleDocument();
		final DomHelper dh = ldoc.getDomHelper();
		dh.setDefaultNamespaceUri(NS);
		final Element htmlElement = ldoc.getDocument().getDocumentElement();
		final Element bodyElement = dh.requireFirstChildNS(htmlElement, NS, "body");
		final Iterable<Element> bodyChildren = dh.getChildren(bodyElement, "p");
		assertNotNull(bodyChildren);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertNotNull(bodyChildrenIter);
		assertTrue(bodyChildrenIter.hasNext());
		final Element p1 = bodyChildrenIter.next();
		assertNotNull(p1);
		assertEquals(NS, p1.getNamespaceURI());
		assertEquals("p", p1.getLocalName());
		assertEquals("p", p1.getTagName());
		assertEquals("Paragraph 1", p1.getTextContent());
		assertTrue(bodyChildrenIter.hasNext());
		final Element p3 = bodyChildrenIter.next();
		assertEquals(NS, p3.getNamespaceURI());
		assertEquals("p", p3.getLocalName());
		assertEquals("p", p3.getTagName());
		assertEquals("Paragraph 3", p3.getTextContent());
		assertFalse(bodyChildrenIter.hasNext());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#getChildrenNS(org.w3c.dom.Node, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetChildrenNS() {
		final LocalizableDocument ldoc = getSampleDocument();
		final DomHelper dh = ldoc.getDomHelper();
		dh.setDefaultNamespaceUri(NS);
		final Element htmlElement = ldoc.getDocument().getDocumentElement();
		final Element bodyElement = dh.requireFirstChildNS(htmlElement, NS, "body");
		final Iterable<Element> bodyChildren = dh.getChildrenNS(bodyElement, NS, "p");
		assertNotNull(bodyChildren);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertNotNull(bodyChildrenIter);
		assertTrue(bodyChildrenIter.hasNext());
		final Element p1 = bodyChildrenIter.next();
		assertNotNull(p1);
		assertEquals(NS, p1.getNamespaceURI());
		assertEquals("p", p1.getLocalName());
		assertEquals("p", p1.getTagName());
		assertEquals("Paragraph 1", p1.getTextContent());
		assertTrue(bodyChildrenIter.hasNext());
		final Element p3 = bodyChildrenIter.next();
		assertEquals(NS, p3.getNamespaceURI());
		assertEquals("p", p3.getLocalName());
		assertEquals("p", p3.getTagName());
		assertEquals("Paragraph 3", p3.getTextContent());
		assertFalse(bodyChildrenIter.hasNext());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#isElementNS(org.w3c.dom.Node, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testIsElementNS() {
		final LocalizableDocument ldoc = getSampleDocument();
		final DomHelper dh = ldoc.getDomHelper();
		dh.setDefaultNamespaceUri(NS);
		final Element htmlElement = ldoc.getDocument().getDocumentElement();
		final Element bodyElement = dh.requireFirstChildNS(htmlElement, NS, "body");
		final Iterable<Element> bodyChildren = dh.getChildren(bodyElement);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertTrue(bodyChildrenIter.hasNext());
		final Element p1 = bodyChildrenIter.next();
		assertTrue(dh.isElementNS(p1, NS, "p"));
		assertFalse(dh.isElementNS(p1, NS, "body"));
		assertFalse(dh.isElementNS(p1, "foo", "p"));
		assertFalse(dh.isElementNS(ldoc.getDocument(), NS, "p"));
		assertTrue(bodyChildrenIter.hasNext());
		final Element p2 = bodyChildrenIter.next();
		assertFalse(dh.isElementNS(p2, NS, "p"));
		assertTrue(dh.isElementNS(p2, "foo", "p"));
		assertTrue(bodyChildrenIter.hasNext());
		final Element p3 = bodyChildrenIter.next();
		assertTrue(dh.isElementNS(p3, NS, "p"));
		assertFalse(dh.isElementNS(p3, "foo", "p"));
		assertFalse(bodyChildrenIter.hasNext());
	}
		
	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#isElement(org.w3c.dom.Node, java.lang.String)}.
	 */
	@Test
	public void testIsElement() {
		final LocalizableDocument ldoc = getSampleDocument();
		final DomHelper dh = ldoc.getDomHelper();
		dh.setDefaultNamespaceUri(NS);
		final Element htmlElement = ldoc.getDocument().getDocumentElement();
		final Element bodyElement = dh.requireFirstChildNS(htmlElement, NS, "body");
		final Iterable<Element> bodyChildren = dh.getChildren(bodyElement);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertTrue(bodyChildrenIter.hasNext());
		final Element p1 = bodyChildrenIter.next();
		assertTrue(dh.isElement(p1, "p"));
		assertFalse(dh.isElement(p1, "body"));
		assertFalse(dh.isElement(ldoc.getDocument(), "p"));
		assertTrue(bodyChildrenIter.hasNext());
		final Element p2 = bodyChildrenIter.next();
		assertFalse(dh.isElement(p2, "p"));
		assertTrue(bodyChildrenIter.hasNext());
		final Element p3 = bodyChildrenIter.next();
		assertTrue(dh.isElement(p3, "p"));
		assertFalse(bodyChildrenIter.hasNext());
	}
	

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#assertElement(org.w3c.dom.Node, java.lang.String)}.
	 */
	@Test
	public void testAssertElement() {
		final LocalizableDocument ldoc = getSampleDocument();
		final DomHelper dh = ldoc.getDomHelper();
		dh.setDefaultNamespaceUri(NS);
		final Element htmlElement = ldoc.getDocument().getDocumentElement();
		final Element bodyElement = dh.requireFirstChildNS(htmlElement, NS, "body");
		final Iterable<Element> bodyChildren = dh.getChildren(bodyElement);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertTrue(bodyChildrenIter.hasNext());
		final Element p1 = bodyChildrenIter.next();
		dh.assertElement(p1, "p");
		try {
			dh.assertElement(p1, "body");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected {" + NS + "}body, got p", le.getMessage());
			assertTrue(le.getLocator().getSystemId().endsWith("SAMPLE_DOCUMENT"));
			assertNull(le.getLocator().getPublicId());
			assertEquals(3, le.getLocator().getLineNumber());
			assertEquals(6, le.getLocator().getColumnNumber());
		}
		try {
			dh.assertElement(ldoc.getDocument(), "p");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected element, got " + ldoc.getDocument().getClass().getName(), le.getMessage());
			assertTrue(le.getLocator().getSystemId().endsWith("SAMPLE_DOCUMENT"));
			assertNull(le.getLocator().getPublicId());
			assertEquals(1, le.getLocator().getLineNumber());
			assertEquals(1, le.getLocator().getColumnNumber());
		}
		assertTrue(bodyChildrenIter.hasNext());
		final Element p2 = bodyChildrenIter.next();
		try {
			dh.assertElement(p2, "p");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected {" + NS + "}p, got t:p", le.getMessage());
			assertTrue(le.getLocator().getSystemId().endsWith("SAMPLE_DOCUMENT"));
			assertNull(le.getLocator().getPublicId());
			assertEquals(4, le.getLocator().getLineNumber());
			assertEquals(8, le.getLocator().getColumnNumber());
		}
		assertTrue(bodyChildrenIter.hasNext());
		final Element p3 = bodyChildrenIter.next();
		dh.assertElement(p3, "p");
		assertFalse(bodyChildrenIter.hasNext());

	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.DomHelper#assertElementNS(org.w3c.dom.Node, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAssertElementNS() {
		final LocalizableDocument ldoc = getSampleDocument();
		final DomHelper dh = ldoc.getDomHelper();
		dh.setDefaultNamespaceUri(NS);
		final Element htmlElement = ldoc.getDocument().getDocumentElement();
		final Element bodyElement = dh.requireFirstChildNS(htmlElement, NS, "body");
		final Iterable<Element> bodyChildren = dh.getChildren(bodyElement);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertTrue(bodyChildrenIter.hasNext());
		final Element p1 = bodyChildrenIter.next();
		dh.assertElementNS(p1, NS, "p");
		try {
			dh.assertElementNS(p1, NS, "body");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected {" + NS + "}body, got p", le.getMessage());
			assertTrue(le.getLocator().getSystemId().endsWith("SAMPLE_DOCUMENT"));
			assertNull(le.getLocator().getPublicId());
			assertEquals(3, le.getLocator().getLineNumber());
			assertEquals(6, le.getLocator().getColumnNumber());
		}
		try {
			dh.assertElementNS(p1, "foo", "p");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected {foo}p, got p", le.getMessage());
			assertTrue(le.getLocator().getSystemId().endsWith("SAMPLE_DOCUMENT"));
			assertNull(le.getLocator().getPublicId());
			assertEquals(3, le.getLocator().getLineNumber());
			assertEquals(6, le.getLocator().getColumnNumber());
		}
		try {
			dh.assertElementNS(ldoc.getDocument(), NS, "p");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected element, got " + ldoc.getDocument().getClass().getName(), le.getMessage());
			assertTrue(le.getLocator().getSystemId().endsWith("SAMPLE_DOCUMENT"));
			assertNull(le.getLocator().getPublicId());
			assertEquals(1, le.getLocator().getLineNumber());
			assertEquals(1, le.getLocator().getColumnNumber());
		}
		assertTrue(bodyChildrenIter.hasNext());
		final Element p2 = bodyChildrenIter.next();
		dh.assertElementNS(p2, "foo", "p");
		assertTrue(bodyChildrenIter.hasNext());
		final Element p3 = bodyChildrenIter.next();
		dh.assertElementNS(p3, NS, "p");
		assertFalse(bodyChildrenIter.hasNext());
	}
}
