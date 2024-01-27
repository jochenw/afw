/**
 * 
 */
package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;

import org.jspecify.annotations.NonNull;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.github.jochenw.afw.core.util.DomHelper.LocalizableException;


/**
 * @author jwi
 *
 */
public class DomTest {
	private static final String SAMPLE_DOCUMENT =
			"<html xmlns:t='foo'>\n"
			+ "<head><title>Sample Document</title></head>"
			+ "<body>\n"
			+ "  <p>Paragraph 1</p>\n"
			+ "  <t:p>Paragraph 2</t:p>\n"
			+ "  <p>Paragraph 3</p>\n"
			+ "</body>\n"
			+ "</html>";

	/** Creates a {@link LocalizableDocument sample document} by parsing
	 * the {@link #SAMPLE_DOCUMENT sample document string}. 
	 * @return The created sample document.
	 */
	protected LocalizableDocument getSampleDocument() {
		final InputSource isource = new InputSource(new StringReader(SAMPLE_DOCUMENT));
		isource.setSystemId("SAMPLE_DOCUMENT");
		return LocalizableDocument.parse(isource);
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Dom#getChildren(org.w3c.dom.Node)}.
	 */
	@Test
	public void testGetChildrenNode() {
		final LocalizableDocument ldoc = getSampleDocument();
		final Iterable<Element> htmlIterable = Dom.getChildren(getRootElement(ldoc));
		assertNotNull(htmlIterable);
		final Iterator<Element> htmlIterator = htmlIterable.iterator();
		assertNotNull(htmlIterator);
		assertTrue(htmlIterator.hasNext());
		final Element headElement = htmlIterator.next();
		assertNotNull(headElement);
		assertNull(headElement.getNamespaceURI());
		assertEquals("head", headElement.getLocalName());
		assertTrue(htmlIterator.hasNext());
		final Element bodyElement = htmlIterator.next();
		assertNotNull(bodyElement);
		assertNull(bodyElement.getNamespaceURI());
		assertEquals("body", bodyElement.getLocalName());
		assertFalse(htmlIterator.hasNext());
		final Iterable<Element> headIterable = Dom.getChildren(headElement);
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
		final Iterable<Element> titleIterable = Dom.getChildren(titleElement);
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
		final Iterable<Element> bodyIterable = Dom.getChildren(bodyElement);
		assertNotNull(bodyIterable);
		final Iterator<Element> bodyIterator = bodyIterable.iterator();
		assertNotNull(bodyIterator);
		assertTrue(bodyIterator.hasNext());
		final Element p1 = bodyIterator.next();
		assertNotNull(p1);
		assertNull(p1.getNamespaceURI());
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
		assertNull(p3.getNamespaceURI());
		assertEquals("p", p3.getLocalName());
		assertEquals("p", p3.getTagName());
		assertEquals("Paragraph 3", p3.getTextContent());
		assertFalse(bodyIterator.hasNext());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Dom#getChildren(org.w3c.dom.Node, java.lang.String)}.
	 */
	@Test
	public void testGetChildrenNodeString() {
		final LocalizableDocument ldoc = getSampleDocument();
		final Element htmlElement = getRootElement(ldoc);
		final Element bodyElement = Dom.requireFirstChildNS(htmlElement, null, "body");
		final Iterable<Element> bodyChildren = Dom.getChildren(bodyElement, "p");
		assertNotNull(bodyChildren);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertNotNull(bodyChildrenIter);
		assertTrue(bodyChildrenIter.hasNext());
		final Element p1 = bodyChildrenIter.next();
		assertNotNull(p1);
		assertNull(p1.getNamespaceURI());
		assertEquals("p", p1.getLocalName());
		assertEquals("p", p1.getTagName());
		assertEquals("Paragraph 1", p1.getTextContent());
		assertTrue(bodyChildrenIter.hasNext());
		final Element p3 = bodyChildrenIter.next();
		assertNull(p3.getNamespaceURI());
		assertEquals("p", p3.getLocalName());
		assertEquals("p", p3.getTagName());
		assertEquals("Paragraph 3", p3.getTextContent());
		assertFalse(bodyChildrenIter.hasNext());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Dom#getChildrenNS(org.w3c.dom.Node, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetChildrenNS() {
		final LocalizableDocument ldoc = getSampleDocument();
		final Element htmlElement = getRootElement(ldoc);
		final Element bodyElement = Dom.requireFirstChildNS(htmlElement, null, "body");
		final Iterable<Element> bodyChildren = Dom.getChildrenNS(bodyElement, null, "p");
		assertNotNull(bodyChildren);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertNotNull(bodyChildrenIter);
		assertTrue(bodyChildrenIter.hasNext());
		final Element p1 = bodyChildrenIter.next();
		assertNotNull(p1);
		assertNull(p1.getNamespaceURI());
		assertEquals("p", p1.getLocalName());
		assertEquals("p", p1.getTagName());
		assertEquals("Paragraph 1", p1.getTextContent());
		assertTrue(bodyChildrenIter.hasNext());
		final Element p3 = bodyChildrenIter.next();
		assertNull(p3.getNamespaceURI());
		assertEquals("p", p3.getLocalName());
		assertEquals("p", p3.getTagName());
		assertEquals("Paragraph 3", p3.getTextContent());
		assertFalse(bodyChildrenIter.hasNext());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Dom#isElementNS(org.w3c.dom.Node, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testIsElementNS() {
		final LocalizableDocument ldoc = getSampleDocument();
		final Element htmlElement = getRootElement(ldoc);
		final Element bodyElement = Dom.requireFirstChildNS(htmlElement, null, "body");
		final Iterable<Element> bodyChildren = Dom.getChildren(bodyElement);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p1 = Objects.requireNonNull(bodyChildrenIter.next());
		assertTrue(Dom.isElementNS(p1, null, "p"));
		assertTrue(Dom.isElementNS(p1, "", "p"));
		assertTrue(Dom.isElementNS(p1, XMLConstants.NULL_NS_URI, "p"));
		assertFalse(Dom.isElementNS(p1, null, "body"));
		assertFalse(Dom.isElementNS(p1, "foo", "p"));
		assertFalse(Dom.isElementNS(ldoc.getDocument(), null, "p"));
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p2 = Objects.requireNonNull(bodyChildrenIter.next());
		assertFalse(Dom.isElementNS(p2, null, "p"));
		assertTrue(Dom.isElementNS(p2, "foo", "p"));
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p3 = Objects.requireNonNull(bodyChildrenIter.next());
		assertTrue(Dom.isElementNS(p3, null, "p"));
		assertFalse(Dom.isElementNS(p3, "foo", "p"));
		assertFalse(bodyChildrenIter.hasNext());
	}
		
	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Dom#isElement(org.w3c.dom.Node, java.lang.String)}.
	 */
	@Test
	public void testIsElement() {
		final LocalizableDocument ldoc = getSampleDocument();
		final @NonNull Element htmlElement = getRootElement(ldoc);
		final @NonNull Element bodyElement = Objects.requireNonNull(Dom.requireFirstChild(htmlElement, "body"));
		final Iterable<Element> bodyChildren = Dom.getChildren(bodyElement);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p1 = Objects.requireNonNull(bodyChildrenIter.next());
		assertTrue(Dom.isElement(p1, "p"));
		assertFalse(Dom.isElement(p1, "body"));
		assertFalse(Dom.isElement(ldoc.getDocument(), "p"));
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p2 = Objects.requireNonNull(bodyChildrenIter.next());
		assertFalse(Dom.isElement(p2, "p"));
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p3 = Objects.requireNonNull(bodyChildrenIter.next());
		assertTrue(Dom.isElement(p3, "p"));
		assertFalse(bodyChildrenIter.hasNext());
	}

	private @NonNull Element getRootElement(final LocalizableDocument ldoc) {
		return Objects.requireNonNull(ldoc.getDocument().getDocumentElement());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Dom#assertElement(org.w3c.dom.Node, java.lang.String)}.
	 */
	@Test
	public void testAssertElement() {
		final LocalizableDocument ldoc = getSampleDocument();
		final @NonNull Element htmlElement = getRootElement(ldoc);
		final @NonNull Element bodyElement = Objects.requireNonNull(Dom.requireFirstChild(htmlElement, "body"));
		final Iterable<Element> bodyChildren = Dom.getChildren(bodyElement);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p1 = Objects.requireNonNull(bodyChildrenIter.next());
		Dom.assertElement(p1, "p");
		try {
			Dom.assertElement(p1, "body");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected body, got p", le.getMessage());
			assertNull(le.getLocator());
		}
		try {
			Dom.assertElement(ldoc.getDocument(), "p");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected element, got " + ldoc.getDocument().getClass().getName(), le.getMessage());
			assertNull(le.getLocator());
		}
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p2 = Objects.requireNonNull(bodyChildrenIter.next());
		try {
			Dom.assertElement(p2, "p");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected p, got t:p", le.getMessage());
			assertNull(le.getLocator());
		}
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p3 = Objects.requireNonNull(bodyChildrenIter.next());
		Dom.assertElement(p3, "p");
		assertFalse(bodyChildrenIter.hasNext());
	}

	/**
	 * Test method for {@link com.github.jochenw.afw.core.util.Dom#assertElementNS(org.w3c.dom.Node, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testAssertElementNS() {
		final LocalizableDocument ldoc = getSampleDocument();
		final @NonNull Element htmlElement = getRootElement(ldoc);
		final @NonNull Element bodyElement = Objects.requireNonNull(Dom.requireFirstChild(htmlElement, "body"));
		final Iterable<Element> bodyChildren = Dom.getChildren(bodyElement);
		final Iterator<Element> bodyChildrenIter = bodyChildren.iterator();
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p1 = Objects.requireNonNull(bodyChildrenIter.next());
		Dom.assertElementNS(p1, null, "p");
		try {
			Dom.assertElementNS(p1, null, "body");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected body, got p", le.getMessage());
			assertNull(le.getLocator());
		}
		try {
			Dom.assertElementNS(p1, "foo", "p");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected {foo}p, got p", le.getMessage());
			assertNull(le.getLocator());
		}
		try {
			Dom.assertElementNS(ldoc.getDocument(), null, "p");
			fail("Expected Exception");
		} catch (LocalizableException le) {
			assertEquals("Expected element, got " + ldoc.getDocument().getClass().getName(), le.getMessage());
			assertNull(le.getLocator());
		}
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p2 = Objects.requireNonNull(bodyChildrenIter.next());
		Dom.assertElementNS(p2, "foo", "p");
		assertTrue(bodyChildrenIter.hasNext());
		final @NonNull Element p3 = Objects.requireNonNull(bodyChildrenIter.next());
		Dom.assertElementNS(p3, null, "p");
		assertFalse(bodyChildrenIter.hasNext());
	}
}
