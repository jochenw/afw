package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Lists.Collector;


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
			final @NonNull List<Object> invalidList = Arrays.asList("a", "b", Boolean.TRUE);
			Lists.toArray(invalidList, String.class);
		});
	}

	/** Test case for {@link Lists#map(List, Function)}.
	 */
	@Test
	public void testMap() {
		final Function<Boolean,String> mapper = (b) -> b.toString();
		Functions.assertFail(NullPointerException.class, "List",
				() -> Lists.map(Objects.fakeNonNull(), mapper));
		Functions.assertFail(NullPointerException.class, "Mapper",
				() -> Lists.map(new ArrayList<Boolean>(), Objects.fakeNonNull()));
		@SuppressWarnings("null")
		final @NonNull List<Boolean> list = Arrays.asList(Boolean.TRUE, Boolean.FALSE,
				Boolean.TRUE, Boolean.FALSE);
		final List<String> result = Lists.map(list, mapper);
		assertNotNull(result);
		assertEquals(list.size(), result.size());
		assertTrue(result instanceof ArrayList);
		for (int i = 0;  i < list.size(); i++) {
			assertEquals(list.get(i).toString(), result.get(i));
		}
	}

	/** Test case for {@link Lists.Collector#toList()}.
	 */
	@Test
	public void testCollectorToList() {
		final Collector<String> collector = Lists.collect();
		assertNotNull(collector);
		final List<String> list = collector.add("foo").add("bar").add("baz").toList();
		assertNotNull(list);
		assertEquals(3, list.size());
		assertEquals("foo", list.get(0));
		assertEquals("bar", list.get(1));
		assertEquals("baz", list.get(2));
		Functions.assertFail(UnsupportedOperationException.class, (String) null, () -> list.add(null));
		Functions.assertFail(IllegalStateException.class, "The element type is null.", () -> collector.toArray());
	}

	/** Test case for {@link Lists.Collector#toMutableList()}.
	 */
	@Test
	public void testCollectorToMutableList() {
		final Collector<String> collector = Lists.collect();
		assertNotNull(collector);
		final List<String> list = collector.add("foo").add("bar").add("baz").toMutableList();
		assertNotNull(list);
		assertEquals(3, list.size());
		assertEquals("foo", list.get(0));
		assertEquals("bar", list.get(1));
		assertEquals("baz", list.get(2));
		list.add(null);
		assertEquals(4, list.size());
		Functions.assertFail(IllegalStateException.class, "The element type is null.", () -> collector.toArray());
	}

	/** Test case for {@link Lists.Collector#toArray()}.
	 */
	@Test
	public void testCollectorToArray() {
		@SuppressWarnings("null")
		final @NonNull Collector<String> collector = Lists.collect(String.class);
		assertNotNull(collector);
		final String[] array = collector.add("foo").add("bar").add("baz").toArray();
		assertNotNull(array);
		assertEquals(3, array.length);
		assertEquals("foo", array[0]);
		assertEquals("bar", array[1]);
		assertEquals("baz", array[2]);
	}

	/** Test case for {@link Lists.Collector#forEach(Consumer)}.
	 */
	@Test
	public void testCollectorForEachConsumer() {
		@SuppressWarnings("null")
		final @NonNull Collector<String> collector = Lists.collect(String.class).add("foo").add("bar").add("baz");
		assertNotNull(collector);
		final List<String> list = new ArrayList<String>();
		collector.forEach((Consumer<String>) list::add);
		assertEquals(3, list.size());
		assertEquals("foo", list.get(0));
		assertEquals("bar", list.get(1));
		assertEquals("baz", list.get(2));
		list.clear();
		assertEquals(0, list.size());
		final IllegalArgumentException iae = new IllegalArgumentException();
		final Consumer<String> consumer = (s) -> {
			if ("baz".equals(s)) {
				throw iae;
			} else {
				list.add(s);
			}
		};
		Functions.assertFail(IllegalArgumentException.class, (String) null, () -> collector.forEach(consumer));
		assertEquals(2, list.size());
		assertEquals("foo", list.get(0));
		assertEquals("bar", list.get(1));
	}

	/** Test case for {@link Lists.Collector#forEach(Functions.FailableConsumer)}.
	 */
	@Test
	public void testCollectorForEachFailableConsumer() {
		@SuppressWarnings("null")
		final @NonNull Collector<String> collector = Lists.collect(String.class).add("foo", "bar", "baz").add((String[]) null);
		assertNotNull(collector);
		final List<String> list = new ArrayList<String>();
		collector.forEach((FailableConsumer<String,RuntimeException>) list::add);
		assertEquals(3, list.size());
		assertEquals("foo", list.get(0));
		assertEquals("bar", list.get(1));
		assertEquals("baz", list.get(2));
		list.clear();
		assertEquals(0, list.size());
		final IllegalArgumentException iae = new IllegalArgumentException();
		final FailableConsumer<String,RuntimeException> consumer = (s) -> {
			if ("baz".equals(s)) {
				throw iae;
			} else {
				list.add(s);
			}
		};
		Functions.assertFail(IllegalArgumentException.class, (String) null, () -> collector.forEach(consumer));
		assertEquals(2, list.size());
		assertEquals("foo", list.get(0));
		assertEquals("bar", list.get(1));
	}

	/** Test case for {@link Lists#iter(Object...)}.
	 */
	@Test
	public void testIterObjects() {
		final String[] expected = {"foo", "bar", "baz"};
		final List<String> list = new ArrayList<>();
		for (String s : Lists.iter(expected)) {
			list.add(s);
		}
		Functions.assertFail(NullPointerException.class, "Values", () -> Lists.iter((String[]) null));
		assertArrayEquals(expected, list.toArray());
	}

	/** Test case for {@link Lists#iter(Function, Object...)}.
	 */
	@Test
	public void testIterFunctionObjects() {
		final Integer[] expectedInts = new Integer[] {Integer.valueOf(2), Integer.valueOf(4), Integer.valueOf(3)};
		final String[] expectedStrings = Stream.of(expectedInts).map((i) -> i.toString()).collect(Collectors.toList()).toArray(new String[0]);
		final List<Integer> list = new ArrayList<>();
		final Function<String, Integer> mapper = Integer::parseInt;
		for (Integer i : Lists.iter(mapper, expectedStrings)) {
			list.add(i);
		}
		Functions.assertFail(NullPointerException.class, "Mapper", () -> Lists.iter(null, expectedStrings));
		Functions.assertFail(NullPointerException.class, "Values", () -> Lists.iter(mapper, (String[]) null));
		assertArrayEquals(expectedInts, list.toArray());
	}

	/** Test case for {@link Lists#iter(Function, Iterable)}.
	 */
	@Test
	public void testIterFunctionIterable() {
		final List<Integer> expectedInts = Lists.asList(Integer.valueOf(2), Integer.valueOf(4), Integer.valueOf(3));
		final List<String> expectedStrings = expectedInts.stream().map((i) -> i.toString()).collect(Collectors.toList());
		final List<Integer> list = new ArrayList<>();
		final Function<String, Integer> mapper = Integer::parseInt;
		for (Integer i : Lists.iter(mapper, expectedStrings)) {
			list.add(i);
		}
		Functions.assertFail(NullPointerException.class, "Mapper", () -> Lists.iter(null, expectedStrings));
		Functions.assertFail(NullPointerException.class, "Values", () -> Lists.iter(mapper, (String[]) null));
		assertArrayEquals(expectedInts.toArray(), list.toArray());
	}

	/** Not a real test case, just coverage completion.
	 */
	@Test
	public void testAsList() {
		final List<String> list = Lists.asList((String[]) null);
		assertNotNull(list);
		assertTrue(list.isEmpty());
	}

	/** Test case for {@link Lists#forEach(Consumer,Object[])}.
	 */
	@Test
	public void testForEachConsumerObjects() {
		final String[] expected = {"foo", "bar", "baz"};
		final List<String> list = new ArrayList<>();
		Lists.forEach(list::add, expected);
		Functions.assertFail(NullPointerException.class, "Action", () -> Lists.forEach(null, expected));
		Functions.assertFail(NullPointerException.class, "Values", () -> Lists.forEach(list::add, (String[]) null));
		assertArrayEquals(expected, list.toArray());
	}

	/** Test case for {@link Lists#forEach(Function, Consumer, Object...)}.
	 */
	@Test
	public void testForEachFunctionConsumerObjects() {
		final Integer[] expectedInts = new Integer[] {Integer.valueOf(2), Integer.valueOf(4), Integer.valueOf(3)};
		final List<Integer> list = new ArrayList<>();
		final Function<Integer,String> mapper = (i) -> i.toString();
		final Consumer<String> action = (s) -> list.add(Integer.valueOf(s));
		Lists.forEach(mapper, action, expectedInts);
		Functions.assertFail(NullPointerException.class, "Mapper", () -> Lists.forEach(null, action, expectedInts));
		Functions.assertFail(NullPointerException.class, "Action", () -> Lists.forEach(mapper, null, expectedInts));
		Functions.assertFail(NullPointerException.class, "Values", () -> Lists.forEach(mapper, action, (Integer[]) null));
		assertArrayEquals(expectedInts, list.toArray());
	}

	/** Test case for {@link Lists#iter(Function, Iterable)}.
	 */
	@Test
	public void testForEachFunctionConsumerIterable() {
		final List<Integer> expectedInts = Arrays.asList(Integer.valueOf(2), Integer.valueOf(4), Integer.valueOf(3));
		final List<Integer> list = new ArrayList<>();
		final Function<Integer, String> mapper = (i) -> i.toString();
		final Consumer<String> action = (s) -> list.add(Integer.valueOf(s));
		Functions.assertFail(NullPointerException.class, "Mapper", () -> Lists.forEach(null, action, expectedInts));
		Functions.assertFail(NullPointerException.class, "Action", () -> Lists.forEach(mapper, null, expectedInts));
		Functions.assertFail(NullPointerException.class, "Values", () -> Lists.forEach(mapper, action, (Integer[]) null));
		Lists.forEach(mapper, action, expectedInts);
		assertArrayEquals(expectedInts.toArray(), list.toArray());
	}

}
