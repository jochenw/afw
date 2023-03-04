package com.github.jochenw.afw.core.props;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions.FailableTriConsumer;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.props.PropertySetParser.CharSource;
import com.github.jochenw.afw.core.props.PropertySetParser.PropertyFileListener;
import com.github.jochenw.afw.core.util.Exceptions;


/** Test suite for the {@link PropertySetParser}.
 */
public class PropertySetParserTest {
	/** Test case for the {@link PropertySetParser.CharSource}.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testCharSource() throws Exception {
		final long l = 1677786244224l; // System.currentTimeMillis() at the time, when
		                              // this test has been created.
		final StringBuilder buffer = new StringBuilder();
		for (int i = 0;  i < 32;  i++) {
			buffer.append("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		}
		final String source = buffer.toString();
		final Random rnd = new Random(l);
		final CharSource cs = new CharSource(new StringReader(source), "testCharSource");
		final StringBuilder sb = new StringBuilder();
		for (;;) {
			final int i = cs.read();
			if (i == -1) {
				break;
			} else {
				final char c = (char) i;
				if (rnd.nextDouble() < 0.1) {
					cs.pushback(c);
				} else {
					sb.append(c);
				}
			}
		}
		assertEquals(source, sb.toString());
		assertEquals(0, cs.getLineNumber());
		assertEquals(source.length(), cs.getColumnNumber());
		assertEquals("testCharSource", cs.getUri());
	}

	/**
	 * Test case for parsing an empty file.
	 */
	@Test
	public void testEmptyFile() {
		test("", "REALLY_EMPTY_FILE");
		test("\r\n", "EMPTY_FILE_CR");
		test("\n", "EMPTY_FILE");
	}

	/** Test case for parsing comment lines, interspersed with white space.
	 */
	@Test
	public void testComments() {
		final String content = "# Comment 1<LF>  <LF>  # Comment 2<LF>";
		final String[] expectedTokens = new String[] {
			"c: Comment 1", ":  ", "c: Comment 2"
		};
		test(content.replaceAll("<LF>", "\n"), "testCommentsLf", expectedTokens);
		test(content.replaceAll("<LF>", "\r\n"), "testCommentsCrLf", expectedTokens);
	}

	/** Test case for parsing a property file with a single property, and no comment.
	 */
	@Test
	public void testParseSingleProperty() {
		test("prop1=prop1Value\r\n", "SINGLE_PROPERTY_FILE", "prop1", "prop1Value", null);
		test("# This is a comment.\r\nprop1=prop1Value\\r\\n",
			 "SINGLE_PROPERTY_COMMENTED_FILE", "prop1", "prop1Value", " This is a comment.");
	}

	/** Test case for parsing a property file with a single property, and a comment.
	 */
	@Test
	public void testParseSinglePropertyWithComment() {
		test("# This is a comment.\r\nprop1=prop1Value\\r\\n",
			 "SINGLE_PROPERTY_COMMENTED_FILE", "prop1", "prop1Value", " This is a comment.");
	}

	/** Test case for parsing a property file with a single property, and a multi-line comment.
	 */
	@Test
	public void testParseSinglePropertyWithMultLineComment() {
		test("# This is a comment.\r\n# The comments second line.\r\nprop1=prop1Value\\r\\n",
			 "SINGLE_PROPERTY_COMMENTED_FILE", "prop1", "prop1Value",
			 " This is a comment." + System.lineSeparator() + " The comments second line.");
	}

	/** Tests parsing the given content.
	 * @param pContent The content, that is being parsed.
	 * @param pFileName The file name, for use in error messages.
	 * @return The set of key/value/comment triplets, that nas been parsed.
	 */
	protected List<String> parseTestFile(String pContent, String pFileName) {
		final List<String> list = new ArrayList<>();
		final PropertyFileListener listener = new PropertyFileListener() {
			@Override
			public void skippedWhitespaceLine(String pWhitespace) {
				list.add(":" + pWhitespace);
			}
			@Override
			public void commentLine(String pComment) {
				list.add("c:" + pComment);
			}
			@Override
			public void propertyLine(String pKey, String pValue, boolean pContinued) {
				list.add("k:" + pKey);
				propertyValueContinuationLine(pValue, pContinued);
			}
			@Override
			public void propertyValueContinuationLine(String pValue, boolean pContinued) {
				if (pContinued) {
					list.add("vc:" + pValue);
				} else {
					list.add("v:" + pValue);
				}
			}
		};
		try {
			new PropertySetParser().parse(new StringReader(pContent),
										  pFileName, listener);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return list;
	}

	/** Tests, whether the given content contains the given set of key/value/comment
	 * triplets.
	 * @param pContent The content, that is being parsed.
	 * @param pFileName The file name for use in error messages.
	 * @param pValues The expected key/value/comment triplets.
	 */
	protected void test(String pContent, String pFileName, String... pValues) {
		final List<String> list = parseTestFile(pContent, pFileName);
		if (pValues == null) {
			assertTrue(list.isEmpty());
		} else {
			if (pValues.length != list.size()) {
				fail("Expected " + String.join(",", pValues)
				     + ", got "  + String.join(",", list));
			}
			for (int i = 0;  i < pValues.length;  i++) {
				assertEquals(String.valueOf(i), pValues[i], list.get(i));
			}
		}
	}
}
