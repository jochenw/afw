package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Sax.SaxWriter;


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
		final FailableConsumer<SaxWriter, SAXException> contentCreator = (sw) -> {
			sw.writeElement("test", (sw2) -> {});
		};
		final String actual = createUsingBytes(contentCreator);
		if (!EMPTY_XML1.equals(actual)  &&  !EMPTY_XML2.equals(actual)) {
			assertEquals(EMPTY_XML1, actual);
		}
	}

	/** Creates an XML document as a string by applying the given content creator.
	 * @param pContentCreator The content creator.
	 * @return The created XML document, as a string.
	 */
	protected String createUsingBytes(FailableConsumer<SaxWriter, SAXException> pContentCreator) {
		final byte[] bytes = Sax.creator().withoutXmlDeclaration().writeBytes(pContentCreator);
		return new String(bytes, StandardCharsets.UTF_8).replace('"', '\'');
	}

	/** Tests, whether immutability is detected.
	 */
	@Test
	public void testImmutableSaxWriter() {
		final FailableConsumer<SaxWriter, SAXException> contentCreator = (sw) -> {
			sw.writeElement("test", (sw2) -> {});
		};
		SaxWriter creator = Sax.creator();
		assertFalse(creator.isImmutable());
		creator.withoutXmlDeclaration().writeBytes(contentCreator);
		assertTrue(creator.isImmutable());
		Functions.assertFail(IllegalStateException.class, "The XML prolog has been written, and the SAX writer is no longer mutable.",
				() -> creator.assertMutable());
	}

	/** Tests pretty printing.
	 */
	@Test
	public void testPrettyPrinting() {
		final FailableConsumer<SaxWriter, SAXException> contentCreator = (sw) -> {
			sw.writeElement("test", (sw2) -> {
				sw2.writeElement("e", "ok");
			});
		};
		{
			// Without pretty printing.
			SaxWriter creator = Sax.creator();
			assertFalse(creator.isIndenting());
			final byte[] actualBytes = creator.withoutXmlDeclaration().writeBytes(contentCreator);
			final String actual = new String(actualBytes, StandardCharsets.UTF_8);
			assertEquals("<test><e>ok</e></test>", actual);
		}
		{
			// With pretty printing.
			SaxWriter creator = Sax.creator();
			assertFalse(creator.isIndenting());
			final byte[] actualBytes = creator.withoutXmlDeclaration().withIndentation().writeBytes(contentCreator);
			assertTrue(creator.isIndenting());
			final String actual = new String(actualBytes, StandardCharsets.UTF_8).replace("\r\n", "\n");
			
			assertEquals("<test>\n"
					     + "    <e>ok</e>\n"
					     + "</test>\n", actual);
		}
	}

	/** Test using an alternative namespace URI.
	 */
	@Test
	public void testNamespaceUri() {
		final FailableConsumer<SaxWriter, SAXException> contentCreator = (sw) -> {
			sw.writeElement("test", (sw2) -> {
				sw2.writeElement("e", "ok");
			});
		};
		SaxWriter creator = Sax.creator();
		assertNull(creator.getNamespaceUri());
		final byte[] actualBytes = creator.withoutXmlDeclaration().withNamespaceUri("foo").withIndentation().writeBytes(contentCreator);
		assertEquals("foo", creator.getNamespaceUri());
		assertTrue(creator.isIndenting());
		final String actual = new String(actualBytes, StandardCharsets.UTF_8).replace("\r\n", "\n").replace("\"", "'");
		
		assertEquals("<test xmlns='foo'>\n"
				     + "    <e>ok</e>\n"
				     + "</test>\n", actual);
	}

	/** Test using a prefix.
	 */
	@Test
	public void testPrefix() {
		final FailableConsumer<SaxWriter, SAXException> contentCreator = (sw) -> {
			sw.writeElement("test", (sw2) -> {
				sw2.writeElement("e", "ok");
			});
		};
		SaxWriter creator = Sax.creator();
		assertNull(creator.getPrefix());
		final byte[] actualBytes =
				creator
				    .withoutXmlDeclaration()
				    .withNamespaceUri("foo")
				    .withPrefix("p")
				    .withIndentation()
				    .writeBytes(contentCreator);
		assertEquals("p", creator.getPrefix());
		assertTrue(creator.isIndenting());
		final String actual = new String(actualBytes, StandardCharsets.UTF_8).replace("\r\n", "\n").replace("\"", "'");
		
		assertEquals("<p:test xmlns:p='foo'>\n"
				     + "    <p:e>ok</p:e>\n"
				     + "</p:test>\n", actual);

		Functions.assertFail(IllegalStateException.class,
				"A default prefix has been specified, but no namespace URI.",
				() -> { Sax.creator()
		                    .withoutXmlDeclaration()
		                    .withPrefix("p")
		                    .withIndentation()
		                    .writeBytes(contentCreator);
				});
	}

	/** Tests conversion of attributes.
	 */
	@Test
	public void testAttributeConversion() {
		final BigDecimal fourtyTwo = new BigDecimal("42.000");
		final BigInteger fourtyTwoInt = fourtyTwo.toBigIntegerExact();
		final LocalDateTime now = LocalDateTime.of(2024, 4, 7, 17, 13);
		final ZonedDateTime nowZoned = ZonedDateTime.of(now, ZoneId.of("Europe/Berlin"));
		final Boolean bool = Boolean.TRUE;
		final Double fourtyTwoDouble = Double.valueOf(fourtyTwo.doubleValue());
		final String whatever = "Whatever works.";
		final String actual = createUsingBytes((sw) -> {
			sw.writeElement("test", (sw2) -> {
				sw2.writeElement("e", (sw3) -> {}, "bigDecimal", fourtyTwo);
				sw2.writeElement("e", (sw3) -> {}, "bigInteger", fourtyTwoInt);
				sw2.writeElement("e", (sw3) -> {}, "localDateTime", now);
				sw2.writeElement("e", (sw3) -> {}, "zonedDateTime", nowZoned);
				sw2.writeElement("e", (sw3) -> {}, "bool", bool);
				sw2.writeElement("e", (sw3) -> {}, "double", fourtyTwoDouble);
				sw2.writeElement("e", (sw3) -> {}, "string", whatever);
			});
		});
		assertTrue(actual.contains(" bigDecimal='42.000'"));
		assertTrue(actual.contains(" bigInteger='42'"));
		assertTrue(actual.contains(" localDateTime='2024-04-07T17:13:00'"));
		assertTrue(actual.contains(" zonedDateTime='2024-04-07T17:13:00+02:00[Europe/Berlin]'"));
		assertTrue(actual.contains(" bool='true'"));
		assertTrue(actual.contains(" double='42.0'"));
		assertTrue(actual.contains(" string='Whatever works.'"));
	}
	
	private final String EMPTY_XML1 = "<test/>";
	private final String EMPTY_XML2 = "<test></test>";
	
}
