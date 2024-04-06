package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.junit.Test;


/** Test suite for the {@link Sax} class.
 */
public class SaxTest {
	/** Tests creating an empty XML document using a {@link Writer}.
	 */
	@Test
	public void testCreateEmptyXmlDocumentUsingWriter() {
		final StringWriter stringWriter = new StringWriter();
		Sax.creator().withoutXmlDeclaration().write(stringWriter, (sw) -> {
			sw.writeElement("test", (sw2) -> {});
		});
		final String actual = stringWriter.toString().replace('"', '\'');
		if (!EMPTY_XML1.equals(actual)  &&  !EMPTY_XML2.equals(actual)) {
			assertEquals(EMPTY_XML1, actual);
		}
	}

	/** Tests creating an empty XML document using an {@link OutputStream}.
	 * @throws IOException The test failed.
	 */
	@Test
	public void testCreateEmptyXmlDocumentUsingOutputStream() throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Sax.creator().withoutXmlDeclaration().write(baos, (sw) -> {
			sw.writeElement("test", (sw2) -> {});
		});
		final String actual = baos.toString("UTF-8").replace('"', '\'');
		if (!EMPTY_XML1.equals(actual)  &&  !EMPTY_XML2.equals(actual)) {
			assertEquals(EMPTY_XML1, actual);
		}
	}

	private final String EMPTY_XML1 = "<test/>";
	private final String EMPTY_XML2 = "<test></test>";
	
}
