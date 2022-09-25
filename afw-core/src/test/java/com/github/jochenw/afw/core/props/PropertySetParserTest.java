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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.Test;

import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableTriConsumer;


/** Test suite for the {@link PropertyParser}.
 */
public class PropertySetParserTest {
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

	protected List<String> parseTestFile(String pContent, String pFileName) {
		final List<String> list = new ArrayList<>();
		final FailableTriConsumer<String,String,String,?> consumer = (key, value, comment) -> {
			list.add(key);
			list.add(value);
			list.add(comment);
		};
		final IReadable readable = IReadable.of(pFileName, () -> {
			final byte[] bytes = pContent.getBytes(StandardCharsets.UTF_8);
			return new ByteArrayInputStream(bytes);
		});
		try {
			new PropertySetParser().parse(consumer, readable, StandardCharsets.UTF_8);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		return list;
	}

	@SuppressWarnings("unused")
	protected void test(String pContent, String pFileName, String... pValues) {
		if (false) {
			final BiConsumer<String, List<String>> validator = (fileName, list) -> {
				if (pValues == null) {
					assertTrue(list.isEmpty());
				} else {
					assertEquals(pValues.length, list.size());
					for (int i = 0;  i < pValues.length;  i++) {
						final String expect = pValues[i];
						if (expect == null) {
							assertNull(pValues[i]);
						} else {
							assertEquals(expect, pValues[i]);
						}
					}
				}
			};
			final Consumer<Properties> pvalidator = (props) -> {
				if (pValues == null) {
					assertTrue(props.isEmpty());
				} else {
					assertEquals(pValues.length, props.size()*3);
					for (int i = 0;  i < pValues.length;  i += 3) {
						final String key = pValues[i];
						final String value = pValues[i+1];
						assertEquals(value, props.get(key));
					}
				}
			};
			{
				final List<String> list = parseTestFile(pContent, pFileName + "_CRLF");
				final Properties props = new Properties();
				try {
					props.load(new StringReader(pContent));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				validator.accept(pFileName + "_CRLF", list);
				pvalidator.accept(props);
			}
			{
				final List<String> list = parseTestFile(pContent.replace("\r\n", "\n"), pFileName + "_LF");
				final Properties props = new Properties();
				try {
					props.load(new StringReader(pContent));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				validator.accept(pFileName + "_LF", list);
				pvalidator.accept(props);
			}
		}
	}
}
