package com.github.jochenw.afw.core.props;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.jochenw.afw.core.props.PropertySetParser.Context;
import com.github.jochenw.afw.core.props.PropertySetParser.PropertyFileListener;


/** Test suite for the {@link PropertySetParser}.
 */
public class PropertySetParserTest {
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
		test("prop1=prop1Value\r\n", "SINGLE_PROPERTY_FILE_CRLF", "prop1", "prop1Value", null);
		test("prop1=prop1Value\n", "SINGLE_PROPERTY_FILE", "prop1", "prop1Value", null);
	}

	/** Test case for parsing a property file with a single property, and a comment.
	 */
	@Test
	public void testParseSinglePropertyWithComment() {
		test("# This is a comment.\r\nprop1=prop1Value\r\n",
			 "SINGLE_PROPERTY_COMMENTED_FILE_CRLF", "prop1", "prop1Value", " This is a comment.");
	}

	/** Test case for parsing a property file with a single property, and a multi-line comment.
	 */
	@Test
	public void testParseSinglePropertyWithMultLineComment() {
		test("# This is a comment.\r\n# The comments second line.\r\nprop1=prop1Value\r\n",
			 "SINGLE_PROPERTY_COMMENTED_FILE", "prop1", "prop1Value",
			 " This is a comment.\n The comments second line.");
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
			public void propertyDefinition(Context pCtx, String pKey, String pValue, String pComment) {
				list.add(pKey);
				list.add(pValue);
				list.add(pComment);
			}
		};
		try {
			final StringReader sr = new StringReader(pContent);
			final BufferedReader br = new BufferedReader(sr);
			new PropertySetParser("\n").parse(listener, pFileName, br);
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
