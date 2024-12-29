package com.github.jochenw.afw.core.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.function.Functions.FailablePredicate;

/**
 * This class provides utility functions, and classes for working with the
 * java.util.stream package, or more generally, with Java 8 lambdas. More
 * specifically, it attempts to address the fact that lambdas are supposed
 * not to throw Exceptions, at least not checked Exceptions, aka instances
 * of {@link Exception}. This enforces the use of constructs like
 * <pre>
 *     Consumer&lt;java.lang.reflect.Method&gt; consumer = (m) -&gt; {
 *         try {
 *             m.invoke(o, args);
 *         } catch (Throwable t) {
 *             throw Functions.rethrow(t);
 *         }
 *    };
 *    stream.forEach(consumer);
 * </pre>
 * Using a {@link FailableStream}, this can be rewritten as follows:
 * <pre>
 *     Streams.failable(stream).forEach((m) -&gt; m.invoke(o, args));
 * </pre>
 * Obviously, the second version is much more concise and the spirit of
 * Lambda expressions is met better than in the first version.
 */
public class ObjectStreams {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	private ObjectStreams() {}

	/** A reduced, and simplified version of a {@link Stream} with
	 * failable method signatures.
	 * @param <O> The streams element type.
	 */
	public static class FailableStream<O extends Object> {
		private Stream<O> stream;
		private boolean terminated;

		/** Creates a new instance with the same sequence of elements as the given
		 * stream.
		 * @param pStream The stream providing the element stream.
		 */
		public FailableStream(Stream<O> pStream) {
			stream = pStream;
		}

	    /**
	     * Returns a FailableStream consisting of the elements of this stream that match
	     * the given FailablePredicate.
	     *
	     * This is an intermediate operation.
	     *
	     * @param pPredicate a non-interfering, stateless predicate to apply to each
	     * element to determine if it should be included.
	     * @return the new stream
	     */
		public FailableStream<O> filter(FailablePredicate<O,?> pPredicate){
			stream = stream.filter((o) -> {
				try {
					return pPredicate.test(o);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			});
			return this;
		}

	    /**
	     * Performs an action for each element of this stream.
	     *
	     * This is a terminal operation.
	     *
	     * <p>The behavior of this operation is explicitly nondeterministic.
	     * For parallel stream pipelines, this operation does <em>not</em>
	     * guarantee to respect the encounter order of the stream, as doing so
	     * would sacrifice the benefit of parallelism.  For any given element, the
	     * action may be performed at whatever time and in whatever thread the
	     * library chooses.  If the action accesses shared state, it is
	     * responsible for providing the required synchronization.
	     *
	     * @param pAction a non-interfering action to perform on the elements
	     */
		public void forEach(FailableConsumer<O,?> pAction) {
			terminated = true;
			stream().forEach((o) -> {
				try {
					pAction.accept(o);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			});
		}

		/** Marks the stream as terminated.
		 */
		protected void makeTerminated() {
			if (terminated) {
				throw new IllegalStateException("This stream is already terminated.");
			}
			terminated = true;
		}

		/**
	     * Performs a mutable reduction operation on the elements of this stream using a
	     * {@code Collector}.  A {@code Collector}
	     * encapsulates the functions used as arguments to
	     * {@link #collect(Supplier, BiConsumer, BiConsumer)}, allowing for reuse of
	     * collection strategies and composition of collect operations such as
	     * multiple-level grouping or partitioning.
	     *
	     * If the underlying stream is parallel, and the {@code Collector}
	     * is concurrent, and either the stream is unordered or the collector is
	     * unordered, then a concurrent reduction will be performed
	     * (see {@link Collector} for details on concurrent reduction.)
	     *
	     * This is a terminal operation.
	     *
	     * When executed in parallel, multiple intermediate results may be
	     * instantiated, populated, and merged so as to maintain isolation of
	     * mutable data structures.  Therefore, even when executed in parallel
	     * with non-thread-safe data structures (such as {@code ArrayList}), no
	     * additional synchronization is needed for a parallel reduction.
	     *
	     * <em>Note:</em>
	     * The following will accumulate strings into an ArrayList:
	     * <pre>{@code
	     *     List<String> asList = stringStream.collect(Collectors.toList());
	     * }</pre>
	     *
	     * <p>The following will classify {@code Person} objects by city:
	     * <pre>{@code
	     *     Map<String, List<Person>> peopleByCity
	     *         = personStream.collect(Collectors.groupingBy(Person::getCity));
	     * }</pre>
	     *
	     * <p>The following will classify {@code Person} objects by state and city,
	     * cascading two {@code Collector}s together:
	     * <pre>{@code
	     *     Map<String, Map<String, List<Person>>> peopleByStateAndCity
	     *         = personStream.collect(Collectors.groupingBy(Person::getState,
	     *                                                      Collectors.groupingBy(Person::getCity)));
	     * }</pre>
	     *
	     * @param <R> the type of the result
	     * @param <A> the intermediate accumulation type of the {@code Collector}
	     * @param pCollector the {@code Collector} describing the reduction
	     * @return the result of the reduction
	     * @see #collect(Supplier, BiConsumer, BiConsumer)
	     * @see Collectors
	     */
		public <A,R> R collect(Collector<? super O,A,R> pCollector) {
			makeTerminated();
			return stream().collect(pCollector);
		}

	    /**
	     * Performs a mutable reduction operation on the elements of this FailableStream.
	     * A mutable reduction is one in which the reduced value is a mutable result
	     * container, such as an {@code ArrayList}, and elements are incorporated by updating
	     * the state of the result rather than by replacing the result. This produces a result equivalent to:
	     * <pre>{@code
	     *     R result = supplier.get();
	     *     for (T element : this stream)
	     *         accumulator.accept(result, element);
	     *     return result;
	     * }</pre>
	     *
	     * <p>Like {@link #reduce(Object, BinaryOperator)}, {@code collect} operations
	     * can be parallelized without requiring additional synchronization.
	     *
	     * <p>This is a terminal operation.
	     *
	     * <em>Note:</em>
	     * There are many existing classes in the JDK whose signatures are
	     * well-suited for use with method references as arguments to {@code collect()}.
	     * For example, the following will accumulate strings into an {@code ArrayList}:
	     * <pre>{@code
	     *     List<String> asList = stringStream.collect(ArrayList::new, ArrayList::add,
	     *                                                ArrayList::addAll);
	     * }</pre>
	     *
	     * <p>The following will take a stream of strings and concatenates them into a
	     * single string:
	     * <pre>{@code
	     *     String concat = stringStream.collect(StringBuilder::new, StringBuilder::append,
	     *                                          StringBuilder::append)
	     *                                 .toString();
	     * }</pre>
	     *
	     * @param <R> type of the result
	     * @param pSupplier a function that creates a new result container. For a
	     *                 parallel execution, this function may be called
	     *                 multiple times and must return a fresh value each time.
	     * @param pAccumulator An associative, non-interfering, stateless function for
	     *   incorporating an additional element into a result
	     * @param pCombiner An associative, non-interfering, stateless
	     *   function for combining two values, which must be compatible with the
	     *   accumulator function
	     * @return The result of the reduction
	     */
		public <R> R collect(Supplier<R> pSupplier, BiConsumer<R,? super O> pAccumulator, BiConsumer<R,R> pCombiner) {
			makeTerminated();
			return stream().collect(pSupplier, pAccumulator, pCombiner);
		}

	    /**
	     * Performs a reduction on the elements of this stream, using the provided
	     * identity value and an associative accumulation function, and returns
	     * the reduced value.  This is equivalent to:
	     * <pre>{@code
	     *     T result = identity;
	     *     for (T element : this stream)
	     *         result = accumulator.apply(result, element)
	     *     return result;
	     * }</pre>
	     *
	     * but is not constrained to execute sequentially.
	     *
	     * <p>The {@code identity} value must be an identity for the accumulator
	     * function. This means that for all {@code t},
	     * {@code accumulator.apply(identity, t)} is equal to {@code t}.
	     * The {@code accumulator} function must be an associative function.
	     *
	     * This is a terminal operation.
	     *
	     * <em>Note:</em>
	     * Sum, min, max, average, and string concatenation are all special
	     * cases of reduction. Summing a stream of numbers can be expressed as:
	     *
	     * <pre>{@code
	     *     Integer sum = integers.reduce(0, (a, b) -> a+b);
	     * }</pre>
	     *
	     * or:
	     *
	     * <pre>{@code
	     *     Integer sum = integers.reduce(0, Integer::sum);
	     * }</pre>
	     *
	     * While this may seem a more roundabout way to perform an aggregation
	     * compared to simply mutating a running total in a loop, reduction
	     * operations parallelize more gracefully, without needing additional
	     * synchronization and with greatly reduced risk of data races.
	     *
	     * @param pIdentity the identity value for the accumulating function
	     * @param pAccumulator an associative, non-interfering, stateless
	     *                    function for combining two values
	     * @return the result of the reduction
	     */
		public O reduce(O pIdentity, BinaryOperator<O> pAccumulator) {
			makeTerminated();
			return stream().reduce(pIdentity, pAccumulator);
		}

	    /**
	     * Returns a stream consisting of the results of applying the given
	     * function to the elements of this stream.
	     *
	     * <p>This is an intermediate operation.
	     *
	     * @param <R> The element type of the new stream
	     * @param pMapper A non-interfering, stateless function to apply to each element
	     * @return the new stream
	     */
		public <R> FailableStream<R> map(FailableFunction<O,R,?> pMapper) {
			return new FailableStream<R>(stream.map((o) -> {
				try {
					return pMapper.apply(o);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}));
		}

		/**
		 * Converts the FailableStream into an equivalent stream.
		 * @return A stream, which will return the same elements, which this FailableStream would return.
		 */
		public Stream<O> stream() {
			return stream;
		}

	    /**
	     * Returns whether all elements of this stream match the provided predicate.
	     * May not evaluate the predicate on all elements if not necessary for
	     * determining the result.  If the stream is empty then {@code true} is
	     * returned and the predicate is not evaluated.
	     *
	     * <p>This is a short-circuiting terminal operation.
	     *
	     * <em>Note:</em>
	     * This method evaluates the <em>universal quantification</em> of the
	     * predicate over the elements of the stream (for all x P(x)).  If the
	     * stream is empty, the quantification is said to be <em>vacuously
	     * satisfied</em> and is always {@code true} (regardless of P(x)).
	     *
	     * @param pPredicate A non-interfering, stateless predicate to apply to
	     * elements of this stream
	     * @return {@code true} If either all elements of the stream match the
	     * provided predicate or the stream is empty, otherwise {@code false}.
	     */
		public boolean allMatch(FailablePredicate<O,?> pPredicate) {
			return stream().allMatch((o) -> {
				try {
					return pPredicate.test(o);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			});
		}

	    /**
	     * Returns whether any elements of this stream match the provided
	     * predicate.  May not evaluate the predicate on all elements if not
	     * necessary for determining the result.  If the stream is empty then
	     * {@code false} is returned and the predicate is not evaluated.
	     *
	     * This is a short-circuiting terminal operation.
	     *
	     * <em>Note:</em>
	     * This method evaluates the <em>existential quantification</em> of the
	     * predicate over the elements of the stream (for some x P(x)).
	     *
	     * @param pPredicate A non-interfering, stateless predicate to apply to
	     * elements of this stream
	     * @return {@code true} if any elements of the stream match the provided
	     * predicate, otherwise {@code false}
	     */
		public boolean anyMatch(FailablePredicate<O,?> pPredicate) {
			return stream().anyMatch((o) -> {
				try {
					return pPredicate.test(o);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			});
		}
	}

	/** Creates a new instance of {@link FailableStream}, which consists of
	 * the same elements, than the input stream.
	 * @param <O> The streams element type.
	 * @param pStream The underlying stream.
	 * @return A new instance of {@link FailableStream}, which consists of
	 *   the same elements, than the input stream.
	 */
	public static <O> FailableStream<O> failable(Stream<O> pStream) {
		return new FailableStream<O>(pStream);
	}

	/** <p>A {@link PStream pushable stream} is a data pipeline,
	 * roughly comparable to a {@link Stream}. The main
	 * difference is, that the former is controlled by the
	 * sending side of the pipeline, which "pushes" data
	 * into the stream, while the latter is controlled by
	 * the receiving side, that "pulls" data from the stream.</p>
	 * <p>The difference affects the API, which is a bit more
	 * natural, and convenient, for {@link Stream
	 * standard streams}. On the other hand, the {@link PStream}
	 * might come in handy in a event-driven, or "reactive"
	 * environment.</p>
	 * <p>The {@link PStream} is implemented as an ordered chain
	 * of {@link PLink link} elements, each of which
	 * processes incoming data, and passes it on to the next
	 * link in the chain.</p> 
	 * @param <I> The pushable streams input type. (Type of the
	 *   items, that are being pushed into the stream.)
	 * @param <O> The pushable streams output type. (Type of the
	 *   items, that the streams consumers are receiving.)
	 */
	public static class PStream<I,O> implements Consumer<I> {
		private final @NonNull PLink<I,?> firstLink;
		private @NonNull PLink<?,O> lastLink;

		private PStream(@NonNull PLink<I,?> pFirstLink, @NonNull PLink<?,O> pLastLink) {
			firstLink = pFirstLink;
			lastLink = pLastLink;
		}

		@Override
		public void accept(I pItem) {
			firstLink.accept(pItem);
		}

		/** Pushes an item into the data stream by invoking {@link #accept(Object)}.
		 * @param pItem The item, which is being pushed into the data stream.
		 * @return This stream, ready to receive more items.
		 */
		public PStream<I,O> push(I pItem) {
			accept(pItem);
			return this;
		}

		/** Pushes all of the given items into the data stream by invoking
		 * {@link #accept(Object)} for every item, one-by-one, and in the
		 * given order.
		 * @param pItems The items, which are being pushed into the data stream.
		 * @return This stream, ready to receive more items.
		 */
		public PStream<I,O> push(@SuppressWarnings("unchecked") I @NonNull... pItems) {
			for (I item : pItems) {
				accept(item);
			}
			return this;
		}

		/** Pushes all of the given items into the data stream by invoking
		 * {@link #accept(Object)} for every item, one-by-one, and in the
		 * iterable's natural order.
		 * @param pItems The items, which are being pushed into the data stream.
		 * @return This stream, ready to receive more items.
		 */
		public PStream<I,O> push(@NonNull Iterable<I> pItems) {
			for (I item : pItems) {
				accept(item);
			}
			return this;
		}

		/** Pushes all of the given items into the data stream by invoking
		 * {@link #accept(Object)} for every item, one-by-one, and in the
		 * streams natural order.
		 * @param pItems The items, which are being pushed into the data stream.
		 * @return This stream, ready to receive more items.
		 */
		public PStream<I,O> push(@NonNull Stream<I> pItems) {
			pItems.forEach(this::accept);
			return this;
		}

		/** Pushes all of the given items into the data stream by invoking
		 * {@link #accept(Object)} for every item, one-by-one, and in the
		 * iteration's natural order.
		 * @param pItems The items, which are being pushed into the data stream.
		 * @return This stream, ready to receive more items.
		 */
		public PStream<I,O> push(@NonNull Iterator<I> pItems) {
			while(pItems.hasNext()) {
				accept(pItems.next());
			}
			return this;
		}

		/** Pushes all of the given items into the data stream by invoking
		 * {@link #accept(Object)} for every item, one-by-one, and in the
		 * enumeration's natural order.
		 * @param pItems The items, which are being pushed into the data stream.
		 * @return This stream, ready to receive more items.
		 */
		public PStream<I,O> push(@NonNull Enumeration<I> pItems) {
			while(pItems.hasMoreElements()) {
				accept(pItems.nextElement());
			}
			return this;
		}

		/** Extends the stream by adding a filter: The
		 * stream will now discard any output items,
		 * for which the given {@link Predicate}
		 * evaluates to false.
		 * @param pPredicate The filter predicate,
		 *   which will be invoked for any potential
		 *   output item. If the predicate returns
		 *   {@code false}, then the output item
		 *   will be silently discarded.
		 * @return This stream, with the
		 *   requested extension applied.
		 */
		public PStream<I,O> filter(@NonNull Predicate<O> pPredicate) {
			final PLink<O,O> link = new PLink<O,O>() {
				@Override
				public void accept(O pItem) {
					if (pPredicate.test(pItem)) {
						pushUp(pItem);
					}
				}
			};
			lastLink.nextLink = link;
			lastLink = link;
			return this;
		}

		/** Failable version of {@link #filter(Predicate)}: If the
		 * given predicate fails, then the thrown Exception will
		 * be mapped to a {@link RuntimeException}, not affecting
		 * the filters signature.
		 * @param pPredicate The filter predicate,
		 *   which will be invoked for any potential
		 *   output item. If the predicate returns
		 *   {@code false}, then the output item
		 *   will be silently discarded. If the predicate
		 *   fails, throwing an Exception, then that
		 *   Exception will be trapped, and wrapped into
		 *   a {@link RuntimeException}, which will then
		 *   be thrown.
		 * @return This stream, with the
		 *   requested extension applied.
		 */
		public PStream<I,O> filter(@NonNull FailablePredicate<O,?> pPredicate) {
			@SuppressWarnings("null")
			final @NonNull Predicate<O> predicate = Functions.asPredicate(pPredicate);
			return filter(predicate);
		}

		/** Extends the stream by adding a transformation:
		 * For any potential output item, the given
		 * {@link Function mapping function}, which
		 * transforms the potential output item
		 * into the actual output item.
		 * @param pMapper The mapping function, which
		 * will be invoked to transform the output
		 * items.
		 * @return A new stream, which extends the
		 * current stream by adding the requested
		 * transformation.
		 */
		public <R> PStream<I,R> map(Function<O,R> pMapper) {
			final  PLink<O,R> link = new PLink<O,R>(){
				@Override
				public void accept(O pItem) {
					final R r = pMapper.apply(pItem);
					pushUp(r);
				}
			};
			lastLink.nextLink = link;
			@SuppressWarnings("unchecked")
			final PStream<I,R> ps = (PStream<I,R>) this;
			ps.lastLink = link;
			return ps;
		}

		/** Failable version of {@link #map(Function)}:
		 * If the transformation to the actual output
		 * item fails, then the thrown Exception will
		 * be trapped, and wrapped into a {@link RuntimeException},
		 * which will be thrown instead.
		 * @param pMapper The mapping function, which
		 * will be invoked to transform the output
		 * item.
		 * If the tranformation fails, then the thrown
		 * Exception will be trapped, and wrapped into a
		 * {@link RuntimeException}, which will be
		 * thrown instead.
		 * @return A new stream, which extends the
		 * current stream by adding the requested
		 * transformation.
		 */
		public <R> PStream<I,R> map(@NonNull FailableFunction<O,R,?> pMapper) {
			@SuppressWarnings("null")
			final @NonNull Function<O,R> mapper = Functions.asFunction(pMapper);
			return map(mapper);
		}
		
		/** Extends the stream by adding a new output
		 * branch. The created output branch will
		 * be terminated immediately by invoking
		 * the given collector. The main branch
		 * may be extended further by following
		 * invocations of
		 * {@link #filter(Predicate)},
		 * {@link #map(Function)}, or
		 * {@link #collect(Consumer)}.
		 * @param pCollector The collector,
		 * which will be invoked to receive
		 * output items.
		 * @return This stream, with the requested
		 * extension applied.
		 */
		public PStream<I,O> collect(Consumer<O> pCollector) {
			final PLink<O,O> link = new PLink<O,O>(){
				@Override
				public void accept(O pItem) {
					if (nextLink != null) {
						pushUp(pItem);
					}
					pCollector.accept(pItem);
				}
			};
			lastLink.nextLink = link;
			lastLink = link;
			return this;
		}

		/** Failable version of {@link #collect(Consumer)}:
		 * If the given consumer fails, throwing an Exception,
		 * then the thrown Exception will
		 * be trapped, and wrapped into a {@link RuntimeException},
		 * which will be thrown instead.
		 * @param pCollector The collector, which will
		 *   be invoked for any output item. If the collector
		 *   fails, throwing an Exception, then the thrown Exception will
		 *   be trapped, and wrapped into a {@link RuntimeException},
		 *   which will be thrown instead. 
		 * @return This stream object, with the requested
		 *   extension applied.
		 */
		public PStream<I,O> collect(@NonNull FailableConsumer<O,?> pCollector) {
			@SuppressWarnings("null")
			final @NonNull Consumer<O> collector = Functions.asConsumer(pCollector);
			return collect(collector);
		}

		/** Extends the stream by adding a new output
		 * branch. The created output branch will
		 * be terminated immediately by adding the
		 * item to the given collection.
		 * In other words: This is equivalent to
		 * <pre>
		 *   collect(pCollection::add)
		 * </pre>
		 * @param pCollector A collection, which acts as an
		 * output collector. The collections {@link Collection#add(Object)}
		 * method will be invoked for every output item.
		 * @return This stream object, with the requested
		 *   extension applied.
		 */
		public PStream<I,O> collect(@NonNull Collection<O> pCollector) {
			final @NonNull Consumer<O> collector = pCollector::add;
			return collect(collector);
		}
		
		/** Creates a new stream with the input type
		 * {@code &lt;E&gt;}.
		 * @param <E> The created streams input type.
		 *   (Also the output type, as long as the stream
		 *   isn't extended by invoking any of the
		 *   methods {@link #filter(Predicate)},
		 *   {@link #map(Function)},
		 *   or {@link #collect(Consumer)}. 
		 * @return The created stream.
		 */
		public static <E> PStream<E,E> of() {
			final PLink<E,E> link = new PLink<E,E>(){
				@Override
				public void accept(E pItem) {
					pushUp(pItem);
				}
			};
			final PStream<E,E> stream = new PStream<>(link, link);
			stream.lastLink = link;
			return stream;
		}
	}

	/** A {@link PLink link} is a single chain in the
	 * chain, that makes up a {@link PStream}.
	 * @param <I> The links input type.
	 * @param <O> The links output type, possibly the same
	 *   than the input type.
	 */
	public abstract static class PLink<I,O> implements Consumer<I> {
		/** The next link in the streams chain. The method {@link #pushUp(Object)}
		 * uses this to invoke {@link #accept(Object)} on the next link.
		 */
		protected PLink<O,?> nextLink;

		/** Called to push the item to the next link in the streams chain.
		 * 
		 * @param pItem The item, which is being pushed up in the stream.
		 */
		protected void pushUp(O pItem) {
			if (nextLink == null) {
				throw new IllegalStateException("The stream isn't terminated. Did you use a collect method?");
			}
			nextLink.accept(pItem);
		}
	}

}
