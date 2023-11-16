package com.github.jochenw.afw.core.extprop;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import org.junit.Test;

/** Test case for the {@link DefaultExtPropertiesWriter}.
 */
public class DefaultExtPropertiesWriterTest {
	/** Test creating an empty property file, using a {@link Writer}.
	 */
	@Test
	public void testEmptyPropertiesToWriter() {
		final ExtProperties ep = ExtProperties.create();
		testUsingWriter(ep, (result) -> {
			assertNotNull(result);
			assertTrue(result.isEmpty());
		});
	}

	private void testUsingWriter(final ExtProperties ep, Consumer<String> pValidator) {
		final StringWriter sw = new StringWriter();
		ExtProperties.writer().write(ep, sw);
		final String result = sw.toString();
		if (pValidator != null) {
			pValidator.accept(result);
		}
	}

	/** Test creating an empty property file, using an {@link OutputStream}.
	 */
	@Test
	public void testEmptyPropertiesToOutputStream() {
		final ExtProperties ep = ExtProperties.create();
		testUsingOutputStream(ep, (result) -> {
			assertNotNull(result);
			assertTrue(result.length == 0);
		});
	}

	private void testUsingOutputStream(final ExtProperties ep, Consumer<byte[]> pValidator) {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ExtProperties.writer(StandardCharsets.UTF_8, "\n").write(ep, baos);
		final byte[] result = baos.toByteArray();
		if (pValidator != null) {
			pValidator.accept(result);
		}
	}

	/** Test creating a property file with a single entry, using a {@link Writer}.
	 */
	@Test
	public void testSinglePropertyUsingWriter() {
		final ExtProperties ep = ExtProperties.of("foo", "bar", "The foo property");
	    final String expect = "# The foo property" + System.lineSeparator()
        + "foo=bar" + System.lineSeparator();
		testUsingWriter(ep, (result) -> {
			assertEquals(expect, result);
		});
	}

	/** Test creating a property file with a single entry, using an {@link OutputStream}.
	 */
	@Test
	public void testSinglePropertyUsingOutputStream() {
		final ExtProperties ep = ExtProperties.of("foo", "bar", "The foo property");
	    final String expect = "# The foo property\n"
        + "foo=bar\n";
		testUsingOutputStream(ep, (result) -> {
			final String actual = new String(result, StandardCharsets.UTF_8);
			assertEquals(expect, actual);
		});
	}

	/** Test creating a property file with several entries, including values with
	 * german Umlaut's.
	 */
	@Test
	public void testSeveralPropertiesWithUmlautsUsingOutputStream() {
		final ExtProperties ep = ExtProperties.create();
		ep.setProperty("foo", "bar", null);
		ep.setProperty("AeProperty", "abc\u00e4def\u00c4", new String[] {"German Umlaut ae"});
		ep.setProperty("Whatever", "works", null);
		testUsingOutputStream(ep, (bytes) -> {
			final String actual = new String(bytes, StandardCharsets.UTF_8);
			final String expect = "foo=bar\n"
					            + "# German Umlaut ae\n"
					            + "AeProperty=abc\u00e4def\u00c4\n"
					            + "Whatever=works\n";
			assertEquals(expect, actual);
		});
		testUsingOutputStream(ep, null);
	}
}
