package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;


/** Test suite for the {@link Lists} class.
 */
public class ListsTest {
	/** Test case for {@link Lists#of(Object[])}.
	 */
	@Test
	public void testOfOArray() {
		final List<String> list = newStringList();
		assertNotNull(list);
		assertEquals(4, list.size());
		assertEquals("A", list.get(0));
		assertEquals("B", list.get(1));
		assertNull(list.get(2));
		assertEquals("C", list.get(3));

		final List<String> emptyList = Lists.of((String[]) null);
		assertNotNull(emptyList);
		assertTrue(emptyList.isEmpty());
	}

	private static final String[] STR_ARRAY = {"A", "B", null, "C"};
	private @NonNull List<String> newStringList() {
		return Lists.of(STR_ARRAY);
	}

	/** Test case for presence of public defult constructor.
	 */
	@Test
	public void testDefaultConstructor() {
		assertNotNull(new Lists());
	}

	/** Test case for {@link Lists#of(java.util.function.Function, Collection)}.
	 */
	@Test
	public void testOfFunctionOfIOListOfI() {
		@NonNull
		final Function<String, Integer> intMapper = (s) -> {
			if (s == null) { return null; }
			return Integer.valueOf((int) s.charAt(0));
		};
		final List<Integer> list = Lists.of(intMapper, newStringList());
		assertNotNull(list);
		assertEquals(4, list.size());
		assertEquals(65, list.get(0).intValue());
		assertEquals(66, list.get(1).intValue());
		assertNull(list.get(2));
		assertEquals(67, list.get(3).intValue());

		// Test null mapper
		try {
			final @NonNull Function<String,Integer> nullMapper = Objects.notNull(null, Objects.fakeNonNull());
			Lists.of(nullMapper, newStringList());
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("The parameter pSupplier must not be null.", e.getMessage());
		}

		// Test null list.
		final List<Integer> emptyList = Lists.of(intMapper, (List<String>) null);
		assertNotNull(emptyList);
		assertTrue(emptyList.isEmpty());
	}

	/** Test case for {@link Lists#of(Function, Object...)}.
	 */
	@Test
	public void testOfFunctionOfIOIArray() {
		@NonNull
		final Function<String, Integer> intMapper = (s) -> {
			if (s == null) { return null; }
			return Integer.valueOf((int) s.charAt(0));
		};
		final List<Integer> list = Lists.of(intMapper, STR_ARRAY);
		assertNotNull(list);
		assertEquals(4, list.size());
		assertEquals(65, list.get(0).intValue());
		assertEquals(66, list.get(1).intValue());
		assertNull(list.get(2));
		assertEquals(67, list.get(3).intValue());

		// Test null mapper
		try {
			Lists.of(null, STR_ARRAY);
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Mapper function", e.getMessage());
		}

		// Test null list.
		final List<Integer> emptyList = Lists.of(intMapper, (String[]) null);
		assertNotNull(emptyList);
		assertTrue(emptyList.isEmpty());
	}

	/** Test case for {@link Lists#toArray(Collection, Class)}.
	 */
	@Test
	public void testToArray() {
		final BiConsumer<Object[],Object[]> arrayComparator = (expect, actual) -> {
			assertEquals(expect.length, actual.length);
			for (int i = 0;  i < expect.length;  i++) {
				final Object e = expect[i];
				final Object a = actual[i];
				if (e == null) {
					assertNull(a);
				} else {
					assertEquals(e, a);
				}
			}
		};
		final Object[] expect = new Object[] {"a", "b", null};
		@SuppressWarnings("null")
		final @NonNull List<Object> list = Arrays.asList(expect);
		final String[] array = Lists.toArray(list, String.class);
		arrayComparator.accept(expect, array);

		// Parameters must not be null.
		Functions.assertFail(NullPointerException.class, "Type", () -> { Lists.toArray(list, Objects.fakeNonNull()); });
		Functions.assertFail(NullPointerException.class, "List", () -> Lists.toArray(Objects.fakeNonNull(), String.class));

		// Invalid element must trigger a ClassCastException
		Functions.assertFail(ClassCastException.class, "List[2]: " + Boolean.class.getName(), () -> {
			@SuppressWarnings("null")
			final @NonNull List<Object> invalidList = List.of("a", "b", Boolean.TRUE);
			Lists.toArray(invalidList, String.class);
		});
	}
}
