package com.github.jochenw.afw.core.extprop;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.Test;

import com.github.jochenw.afw.core.extprop.ExtProperties.ExtProperty;
import com.github.jochenw.afw.core.util.Objects;


/** Test suite for the {@link ExtProperties} class.
 */
public class ExtPropertiesTest {
	private static final String[] ONE_PROPERTY =
		{"foo", "bar", "The foo property with value bar."};
	private static final @Nullable String[] TWO_PROPERTIES =
		{"foo", "bar", "The foo property with value bar.",
         "answer", "42", null};
	private static final @Nullable String[] THREE_PROPERTIES =
		{"foo", "bar", "The foo property with value bar.",
         "answer", "42", null,
         "Whatever", "works", "The property Whatever (works"};
	private static final @Nullable String[] THREE_PROPERTIES_SORTED =
		{"answer", "42", null,
		 "foo", "bar", "The foo property with value bar.",
         "Whatever", "works", "The property Whatever (works"};

	/** Test case for {@link ExtProperties#of(String[])}.
	 */
	@Test
	public void testOfStrings() {
		final ExtProperties ep0 = ExtProperties.of();
		assertTrue(ep0.isEmpty());
		assertEquals(0, ep0.size());
		assertValues(ep0);

		final String[] values1 = ONE_PROPERTY;
		final ExtProperties ep1 = ExtProperties.of(values1);
		assertValues(ep1, values1);

		final String[] values2 = TWO_PROPERTIES;
		final ExtProperties ep2 = ExtProperties.of(values2);
		assertValues(ep2, values2);

		final String[] values3 = THREE_PROPERTIES;
		final ExtProperties ep3 = ExtProperties.of(values3);
		assertValues(ep3, values3);

		// Test for a missing comment.
		try {
			ExtProperties.of("a", "b");
		} catch (IllegalArgumentException e) {
			assertEquals("The number of elements in the string list"
					     + " (pValues) is 2, and not a multiple of 3.", e.getMessage());
		}
		// Test for a null key.
		try {
			ExtProperties.of((String) null, "b", "c");
		} catch (NullPointerException e) {
			assertEquals("The key at index 0 in the string list"
					     + " (pValues) is null", e.getMessage());
		}
		// Test for a null value.
		try {
			ExtProperties.of("a", null, "c");
		} catch (NullPointerException e) {
			assertEquals("The value at index 1 in the string list"
					     + " (pValues) is null", e.getMessage());
		}
	}

	/** Test case for {@link ExtProperties#setValue(String, String)}.
	 */
	@Test
	public void testSetValue() {
		final String[] values = ONE_PROPERTY;
		final ExtProperties ep = ExtProperties.of(values);
		assertValues(ep, values);
		// Change an existing property.
		ep.setValue("foo", "baz");
		assertValues(ep, "foo", "baz", "The foo property with value bar.");
		// Create a new property without comment.
		ep.setValue("answer", "42");
		assertValues(ep, "foo", "baz", "The foo property with value bar.",
				         "answer", "42", null);
		// "Change" an existing property with the same value.
		final ExtProperty ep1 = ep.requireProperty("answer");
		assertNotNull(ep1);
		ep.setValue("answer", "42");
		final ExtProperty ep2 = ep.requireProperty("answer");
		assertSame(ep1, ep2);
	}

	/** Test case for {@link ExtProperties#setComments(String,String[])}.
	 */
	@Test
	public void testSetComments() {
		final String[] values = ONE_PROPERTY;
		final ExtProperties ep = ExtProperties.of(values);
		assertValues(ep, values);
		// Change an existing property.
		ep.setComments("foo", new @Nullable String[]{"Property foo"});
		assertValues(ep, "foo", "bar", "Property foo");
		// Create a new property without comment.
		ep.setComments("answer", null);
		assertValues(ep, "foo", "bar", "Property foo",
				         "answer", "", null);
		// "Change" an existing comment with the same value.
		final ExtProperty ep1 = ep.getProperty("answer");
		assertNotNull(ep1);
		ep.setComments("answer", null);
		assertSame(ep1, ep.getProperty("answer"));
		ep.setComments("answer", new @Nullable String[]{"*The* answer"});
		final ExtProperty ep2 = ep.getProperty("answer");
		assertNotSame(ep1, ep2);
		ep.setComments("answer", new @Nullable String[]{"*The* answer"});
		assertSame(ep2, ep.getProperty("answer"));
	}

	/** Test case for {@link ExtProperties#forEach(Consumer, Comparator)}
	 */
	@Test
	public void testForEachConsumerComparator() {
		final ExtProperties ep = ExtProperties.of(THREE_PROPERTIES);
		assertValues(ep, THREE_PROPERTIES);
		assertValues(ep, String::compareToIgnoreCase, THREE_PROPERTIES_SORTED);
		final ExtProperties epSorted = ExtProperties.create(String::compareToIgnoreCase);
		for (int i = 0;  i < THREE_PROPERTIES.length;  i += 3) {
			final @NonNull String key = Objects.requireNonNull(THREE_PROPERTIES[i]);
			final @NonNull String value = Objects.requireNonNull(THREE_PROPERTIES[i+1]);
			epSorted.setProperty(key,
					             value,
					             new String[] {THREE_PROPERTIES[i+2]});
		}
		assertValues(epSorted, THREE_PROPERTIES_SORTED);
	}

	/**
	 * Test case for {@link ExtProperties#getPropertyMap()}.
	 */
	@Test
	public void testGetPropertyMap() {
		final Consumer<String[]> tester = (arr) -> {
			final ExtProperties ep = ExtProperties.of(arr);
			final Map<@NonNull String, @NonNull ExtProperty> map = ep.getPropertyMap();
			assertTrue(arr.length % 3 == 0);
			assertEquals(arr.length/3, map.size());
			for (int i = 0;  i < arr.length;  i += 3) {
				final String key = Objects.requireNonNull(arr[i]);
				final String value = Objects.requireNonNull(arr[i+1]);
				final String comment = arr[i+2];
				assertNotNull(key);
				assertNotNull(value);
				@SuppressWarnings("null")
				final @NonNull ExtProperty ep1 = map.get(key);
				assertNotNull(ep1);
				assertEquals(key, ep1.getKey());
				assertEquals(value, ep1.getValue());
				assertStringEquals(comment, ep1.getComments()[0]);
				final ExtProperty ep2 = ep.getProperty(key);
				assertSame(ep1, ep2);
				final ExtProperty ep3 = ep.requireProperty(key);
				assertSame(ep1, ep3);
			}
			// Test, that the map is unmodifiable.
			try {
				map.remove(arr[0]);
			} catch (UnsupportedOperationException e) {
				assertNull(e.getMessage());
			}
		};
		tester.accept(ONE_PROPERTY);
		tester.accept(TWO_PROPERTIES);
		tester.accept(THREE_PROPERTIES);
	}

	/** Test case for {@link ExtProperties#requireProperty(String)},
	 * and {@link ExtProperties#getProperty(String)}.
	 */
	@Test
	public void testRequireProperty() {
		final ExtProperties epr = ExtProperties.of(ONE_PROPERTY);
		@SuppressWarnings("null")
		final @NonNull String key = ONE_PROPERTY[0];
		final ExtProperty ep1 = epr.requireProperty(key);
		assertEqual(ep1, key, ONE_PROPERTY[1], ONE_PROPERTY[2]);
		final ExtProperty ep2 = epr.getProperty(key);
		assertSame(ep1, ep2);
		try {
			epr.requireProperty("$$");
		} catch (NoSuchElementException e) {
			assertEquals("No property with this key exists: $$", e.getMessage());
		}
		assertNull(epr.getProperty("$$"));
	}

	/** Test case for {@link ExtProperties#getValue(String)}.
	 */
	@Test
	public void testGetValue() {
		final ExtProperties epr = ExtProperties.of(ONE_PROPERTY);
		@SuppressWarnings("null")
		final @NonNull String key = ONE_PROPERTY[0];
		assertEquals(ONE_PROPERTY[1], epr.getValue(key));
		assertNull(epr.getValue("$$"));
	}

	/** Test case for {@link ExtProperties#getComment(String)}.
	 */
	@Test
	public void testGetComment() {
		final ExtProperties epr = ExtProperties.of(TWO_PROPERTIES);
		final @NonNull String key = Objects.requireNonNull(TWO_PROPERTIES[0]);
		final String[] fooComment = epr.getComment(key);
		assertEquals(TWO_PROPERTIES[2], fooComment[0]);
		assertNull(epr.getComment("$$"));
		final @NonNull String answerKey = Objects.requireNonNull(TWO_PROPERTIES[3]);
		String[] answerComment = epr.getComment(answerKey);
		assertNull(answerComment[0]);
	}

	/** Test case for {@link ExtProperties#createSynchronized(ExtProperties)}.
	 */
	@Test
	public void testCreateSynchronized() {
		final Consumer<ExtProperties> tester = (ep) -> {
			final ExtProperties epSync = ExtProperties.createSynchronized(ep);
			assertNotNull(epSync);
			final String[] values = getValues(ep, null);
			assertValues(epSync, values);
			final Map<@NonNull String, @NonNull ExtProperty> entries = ep.getEntries();
			final Map<@NonNull String, @NonNull ExtProperty> entriesSync = epSync.getEntries();
			assertNotSame(entries, entriesSync);
			assertTrue(entries instanceof LinkedHashMap  ||  entries instanceof TreeMap);
			assertFalse(entriesSync instanceof LinkedHashMap);
			assertFalse(entriesSync instanceof TreeMap);
			assertEquals(Collections.class.getName() + ".SynchronizedMap", entriesSync.getClass().getCanonicalName());
		};
		tester.accept(ExtProperties.of(THREE_PROPERTIES));
		tester.accept(ExtProperties.of(String::compareToIgnoreCase, THREE_PROPERTIES)); 
		try {
			final Map<@NonNull String, @NonNull ExtProperty> map = new HashMap<>();
			final ExtProperties ep = new ExtProperties(map);
			ExtProperties.createSynchronized(ep);
		} catch (IllegalStateException e) {
			assertEquals("Unable to clone an instance of java.util.HashMap", e.getMessage());
		}
	}

	/** Compares the given property set with the given list of
	 * key / value / comment triplets.
	 * @param pEp The actual property set.
	 * @param pValues The expected values.
	 */
	protected void assertValues(ExtProperties pEp, String... pValues) {
		final String[] list = getValues(pEp, null);
		assertEquals(pValues.length, list.length);
		for (int i = 0;  i < pValues.length;  i++) {
			String v = pValues[i];
			if (v == null) {
				assertNull(String.valueOf(i), list[i]);
			} else {
				assertEquals(String.valueOf(i), pValues[i], list[i]);
			}
		}
	}

	private String[] getValues(ExtProperties pEp, Comparator<String> pSorter) {
		final List<String> list = new ArrayList<>();
		final Consumer<ExtProperty> consumer = (ep) -> {
			list.add(ep.getKey());
			list.add(ep.getValue());
			final String[] comments = ep.getComments();
			if (comments != null  &&  comments.length > 0) {
				list.add(comments[0]);
			} else {
				list.add(null);
			}
		};
		if (pSorter == null) {
			pEp.forEach(consumer);
		} else {
			pEp.forEach(consumer, pSorter);
		}
		return list.toArray(new String[list.size()]);
	}

	/** Compares the given property set with the given list of
	 * key / value / comment triplets, applying the given
	 * {@link Comparator} for sorting.
	 * @param pEp The actual property set.
	 * @param pSorter A comparator, that must be applied to
	 *   sort the extended properties.
	 * @param pValues The expected values.
	 */
	protected void assertValues(ExtProperties pEp, Comparator<String> pSorter,
			                    String... pValues) {
		final String[] list = getValues(pEp, pSorter);
		assertEquals(pValues.length, list.length);
		for (int i = 0;  i < pValues.length;  i++) {
			String v = pValues[i];
			if (v == null) {
				assertNull(String.valueOf(i), list[i]);
			} else {
				assertEquals(String.valueOf(i), pValues[i], list[i]);
			}
		}
	}

	/** Asserts, that an actual string value is equal to an expected
	 * string value.
	 * @param pExpect The expected string value. May be null.
	 * @param pActual The actual string value. May be null.
	 */
	protected void assertStringEquals(String pExpect, String pActual) {
		if (pExpect == null) {
			assertNull(pActual);
		} else {
			assertEquals(pExpect, pActual);
		}
	}

	/** Asserts, that the given ExtProperty has the given key, value,
	 * and comment.
	 * @param pActual The actual property value, that is being validated.
	 * @param pKey The expected property key.
	 * @param pValue The expected property value.
	 * @param pComment The expected property comment.
	 */
	protected void assertEqual(ExtProperty pActual, String pKey, String pValue,
			                   String pComment) {
		assertEquals(pKey, pActual.getKey());
		assertEquals(pValue, pActual.getValue());
		assertStringEquals(pComment, pActual.getComments()[0]);
	}
}
