package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.github.jochenw.afw.core.util.ObjectStreams.FailableStream;


/** Test suite for the {@link ObjectStreams} class.
 */
public class ObjectStreamsTest {
	/** Tests, whether an exception in a filter is properly handled.
	 */
	@Test
	public void testExceptionInFilter() {
		final List<String> list = Arrays.asList("0", "1", "2", "3", "4", "5");
		final FailableStream<String> fs = ObjectStreams.failable(list.stream());
		try {
			fs.filter((s) -> {
				Integer number = Integer.valueOf(s);
				if (number.intValue() % 2 == 1) {
					throw new IOException("Even number detected: " + s);
				}
				return true;
			}).collect(Collectors.toList());
			fail("Expected Exception");
		} catch (UncheckedIOException e) {
			final IOException cause = e.getCause();
			assertEquals("Even number detected: 1", cause.getMessage());
		}
	}

	/** Tests, whether a filter, that doesn't throw an exception, works
	 * as expected.
	 */
	@Test
	public void testFilterOkay() {
		final List<String> list = Arrays.asList("0", "1", "2", "3", "4", "5");
		final FailableStream<String> fs = ObjectStreams.failable(list.stream());
		final List<String> oddNumbers= fs.filter((s) -> {
			Integer number = Integer.valueOf(s);
			return number %2 == 1;
		}).collect(Collectors.toList());
		final List<String> evenNumbers= ObjectStreams.failable(list.stream()).filter((s) -> {
			Integer number = Integer.valueOf(s);
			return number %2 == 0;
		}).collect(Collectors.toList());
		assertListEquals(oddNumbers, "1", "3", "5");
		assertListEquals(evenNumbers, "0", "2", "4");
	}

	/** Tests the filter result by comparing it against a list of expected objects.
	 * @param pExpect The expected filter result, as an array.
	 * @param pGot The actual filter result, as a list.
	 */
	protected <O> void assertListEquals(List<O> pGot,
			                            @SuppressWarnings("unchecked") O... pExpect) {
		assertEquals(pExpect.length, pGot.size());
		for (int i = 0;  i < pGot.size();  i++) {
			assertEquals(String.valueOf(i), pExpect[i], pGot.get(i));
		}
	}
}
