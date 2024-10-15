package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.function.Functions.FailablePredicate;
import com.github.jochenw.afw.core.util.ObjectStreams.FailableStream;
import com.github.jochenw.afw.core.util.ObjectStreams.PStream;


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

	/** Test for {@link ObjectStreams.PStream#collect(Consumer)}
	 */
	@Test
	public void testPStreamCollectConsumer() {
		final List<Integer> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		final Consumer<Integer> collector = list::add;
		@SuppressWarnings("null")
		final @NonNull Stream<Integer> stream = IntStream.range(0, 10).boxed();
		ps.collect(collector).push(stream);
		assertEquals(10, list.size());
		for (int i = 0;  i < 10;  i++) {
			assertEquals(Integer.valueOf(i), list.get(i));
		}
	}

	/** Test for {@link ObjectStreams.PStream#collect(FailableConsumer)}
	 */
	@Test
	public void testPStreamCollectFailableConsumer() {
		final List<Integer> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		final FailableConsumer<Integer,?> collector = list::add;
		@SuppressWarnings("null")
		final @NonNull Stream<Integer> stream = IntStream.range(0, 10).boxed();
		ps.collect(collector).push(stream);
		assertEquals(10, list.size());
		for (int i = 0;  i < 10;  i++) {
			assertEquals(Integer.valueOf(i), list.get(i));
		}
	}

	/** Test for {@link ObjectStreams.PStream#collect(Collection)}
	 */
	@Test
	public void testPStreamCollectCollection() {
		final List<Integer> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		@SuppressWarnings("null")
		final @NonNull Stream<Integer> stream = IntStream.range(0, 10).boxed();
		ps.collect(list).push(stream);
		assertEquals(10, list.size());
		for (int i = 0;  i < 10;  i++) {
			assertEquals(Integer.valueOf(i), list.get(i));
		}
	}

	/** Test for {@link PStream#push(Object[])}
	 */
	@Test
	public void testPStreamPushArray() {
		final List<Integer> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		@SuppressWarnings("null")
		final @NonNull List<Integer> rangeList = IntStream.range(0, 10).boxed().collect(Collectors.toList());
		@SuppressWarnings("null")
		Integer @NonNull[] rangeArray = rangeList.toArray(new Integer[rangeList.size()]);
		ps.collect(list).push(rangeArray);
		assertEquals(10, list.size());
		for (int i = 0;  i < 10;  i++) {
			assertEquals(Integer.valueOf(i), list.get(i));
		}
	}

	/** Test for {@link PStream#push(Iterable)}
	 */
	@Test
	public void testPStreamPushIterable() {
		final List<Integer> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		@SuppressWarnings("null")
		final @NonNull List<Integer> rangeList = IntStream.range(0, 10).boxed().collect(Collectors.toList());
		ps.collect(list).push(rangeList);
		assertEquals(10, list.size());
		for (int i = 0;  i < 10;  i++) {
			assertEquals(Integer.valueOf(i), list.get(i));
		}
	}

	/** Test for {@link PStream#push(Iterator)}
	 */
	@Test
	public void testPStreamPushIterator() {
		final List<Integer> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		@SuppressWarnings("null")
		final @NonNull List<Integer> rangeList = IntStream.range(0, 10).boxed().collect(Collectors.toList());
		@SuppressWarnings("null")
		final @NonNull Iterator<Integer> rangeIterator = rangeList.iterator();
		ps.collect(list).push(rangeIterator);
		assertEquals(10, list.size());
		for (int i = 0;  i < 10;  i++) {
			assertEquals(Integer.valueOf(i), list.get(i));
		}
	}

	/** Test for {@link PStream#push(Enumeration)}
	 */
	@Test
	public void testPStreamPushEnumeration() {
		final List<Integer> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		@SuppressWarnings("null")
		final @NonNull List<Integer> rangeList = IntStream.range(0, 10).boxed().collect(Collectors.toList());
		@SuppressWarnings("null")
		final @NonNull Enumeration<Integer> rangeIterator = Enumerations.asEnumeration(rangeList.iterator());
		ps.collect(list).push(rangeIterator);
		assertEquals(10, list.size());
		for (int i = 0;  i < 10;  i++) {
			assertEquals(Integer.valueOf(i), list.get(i));
		}
	}

	/** Test for {@link ObjectStreams.PStream#filter(Predicate)}
	 */
	@Test
	public void testPStreamFilterPredicate() {
		final List<Integer> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		final Consumer<Integer> collector = list::add;
		final Predicate<Integer> filter = (i) -> i.intValue() % 2 == 0;
		@SuppressWarnings("null")
		final @NonNull Stream<Integer> stream = IntStream.range(0, 10).boxed();
		ps.filter(filter).collect(collector).push(stream);
		assertEquals(5, list.size());
		for (int i = 0;  i < 5;  i++) {
			assertEquals(Integer.valueOf(i*2), list.get(i));
		}
	}

	/** Test for {@link ObjectStreams.PStream#filter(FailablePredicate)}
	 */
	@Test
	public void testPStreamFilterFailablePredicate() {
		final List<Integer> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		final Consumer<Integer> collector = list::add;
		final FailablePredicate<Integer,?> filter = (i) -> i.intValue() % 2 == 0;
		@SuppressWarnings("null")
		final @NonNull Stream<Integer> stream = IntStream.range(0, 10).boxed();
		ps.filter(filter).collect(collector).push(stream);
		assertEquals(5, list.size());
		for (int i = 0;  i < 5;  i++) {
			assertEquals(Integer.valueOf(i*2), list.get(i));
		}
	}

	/** Test for {@link PStream#filter(Predicate)}, in conjunction with
	 * {@link PStream#map(Function)}
	 */
	@Test
	public void testPStreamFilterPredicateThenMapFunction() {
		final List<Long> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		final Consumer<Long> collector = list::add;
		final Function<Integer,Long> mapper = (i) -> Long.valueOf((long) i);
		final Predicate<Integer> filter = (i) -> i.intValue() % 2 == 0;
		@SuppressWarnings("null")
		final @NonNull Stream<Integer> stream = IntStream.range(0, 10).boxed();
		ps.filter(filter).map(mapper).collect(collector).push(stream);
		assertEquals(5, list.size());
		for (int i = 0;  i < 5;  i++) {
			assertEquals(Long.valueOf(i*2l), list.get(i));
		}
	}

	/** Test for {@link PStream#map(Function)}, in conjunction with
	 * {@link PStream#filter(Predicate)}
	 */
	@Test
	public void testPStreamMapFunctionThenFilterPredicate() {
		final List<Long> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		final Consumer<Long> collector = list::add;
		final Function<Integer,Long> mapper = (i) -> Long.valueOf((long) i);
		final Predicate<Long> filter = (l) -> l.longValue() % 2 == 0;
		@SuppressWarnings("null")
		final @NonNull Stream<Integer> stream = IntStream.range(0, 10).boxed();
		ps.map(mapper).filter(filter).collect(collector).push(stream);
		assertEquals(5, list.size());
		for (int i = 0;  i < 5;  i++) {
			assertEquals(Long.valueOf(i*2l), list.get(i));
		}
	}

	/** Test for {@link PStream#filter(FailablePredicate)}, in conjunction with
	 * {@link PStream#map(FailableFunction)}
	 */
	@Test
	public void testPStreamFilterFailablePredicateThenMapFailableFunction() {
		final List<Long> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		final Consumer<Long> collector = list::add;
		final FailableFunction<Integer,Long,?> mapper = (i) -> Long.valueOf((long) i);
		final FailablePredicate<Integer,?> filter = (i) -> i.intValue() % 2 == 0;
		@SuppressWarnings("null")
		final @NonNull Stream<Integer> stream = IntStream.range(0, 10).boxed();
		ps.filter(filter).map(mapper).collect(collector).push(stream);
		assertEquals(5, list.size());
		for (int i = 0;  i < 5;  i++) {
			assertEquals(Long.valueOf(i*2l), list.get(i));
		}
	}

	/** Test for {@link PStream#map(FailableFunction)}, in conjunction with
	 * {@link PStream#filter(FailablePredicate)}
	 */
	@Test
	public void testPStreamMapFailableFunctionThenFilterFailablePredicate() {
		final List<Long> list = new ArrayList<>();
		final PStream<Integer,Integer> ps = PStream.of();
		final Consumer<Long> collector = list::add;
		final FailableFunction<Integer,Long,?> mapper = (i) -> Long.valueOf((long) i);
		final FailablePredicate<Long,?> filter = (l) -> l.longValue() % 2 == 0;
		@SuppressWarnings("null")
		final @NonNull Stream<Integer> stream = IntStream.range(0, 10).boxed();
		ps.map(mapper).filter(filter).collect(collector).push(stream);
		assertEquals(5, list.size());
		for (int i = 0;  i < 5;  i++) {
			assertEquals(Long.valueOf(i*2l), list.get(i));
		}
	}
}
