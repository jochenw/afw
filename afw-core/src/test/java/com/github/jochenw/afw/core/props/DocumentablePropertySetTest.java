package com.github.jochenw.afw.core.props;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;

import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.props.OrderedPropertySet.DocumentableProperty;


/** Test suite for the {@link OrderedPropertySet}.
 */
public class DocumentablePropertySetTest {
	/**
	 * Test case for parsing an empty file.
	 */
	@Test
	public void testEmptyFile() {
		test("", "REALLY_EMPTY_FILE");
		test("\r\n", "EMPTY_FILE");
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

	/** Creates an {@link OrderedPropertySet} by parsing the given content.
	 * @param pContent The content, that is being parsed.
	 * @param pFileName The file name, for use in error messages.
	 * @return The parsed {@link OrderedPropertySet}
	 */
	protected OrderedPropertySet parseTestFile(String pContent, String pFileName) {
		final IReadable readable = IReadable.of(pFileName, () -> {
			final byte[] bytes = pContent.getBytes(StandardCharsets.UTF_8);
			return new ByteArrayInputStream(bytes);
		});
		return OrderedPropertySet.of(readable, StandardCharsets.UTF_8);
	}

	/** Tests, whether the given content contains the given set of key/value/comment
	 * triplets.
	 * @param pContent The content, that is being parsed.
	 * @param pFileName The file name for use in error messages.
	 * @param pValues The expected key/value/comment triplets.
	 */
	protected void test(String pContent, String pFileName, String... pValues) {
		final BiConsumer<String, OrderedPropertySet> validator = (fileName, pset) -> {
			if (pValues == null) {
				assertTrue(pset.isEmpty());
			} else {
				assertEquals(pValues.length, pset.size()*3);
				@SuppressWarnings("unused")
				final FailableConsumer<DocumentableProperty,?> consumer = new FailableConsumer<DocumentableProperty,RuntimeException>() {
					private int offset = 0;

					@Override
					public void accept(DocumentableProperty pDp) {
						assertEquals(pValues[offset++], pDp.getKey());
						assertEquals(pValues[offset++], pDp.getValue());
						final String comment = pValues[offset++];
						if (comment == null) {
							assertNull(pDp.getComment());
						} else {
							assertEquals(comment, pDp.getComment());
						}
					}
				};
			}
		};
		{
			final OrderedPropertySet dps = parseTestFile(pContent, pFileName + "_CRLF");
			validator.accept(pFileName + "_CRLF", dps);
		}
		{
			final OrderedPropertySet dps = parseTestFile(pContent.replace("\r\n", "\n"), pFileName + "_LF");
			validator.accept(pFileName + "_LF", dps);
		}
	}
}
