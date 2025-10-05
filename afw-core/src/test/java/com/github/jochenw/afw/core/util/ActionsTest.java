package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions.FailableBiFunction;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.function.Functions.TriConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.util.Actions.ActionErrorException;
import com.github.jochenw.afw.core.util.Actions.ActionWarningException;
import com.github.jochenw.afw.core.util.Actions.Context;
import com.github.jochenw.afw.core.util.Actions.Status;
import com.github.jochenw.afw.core.util.Actions.Status.State;


/** Test suite for {@link Actions}.
 */
public class ActionsTest {
	/** Wrapper for a {@link Runnable}, which allows to
	 * determine, if the actual {@ink Runnable} has been
	 * executed.
	 */
	public static class Reportable implements Runnable {
		private final Runnable runnable;
		private boolean executed;
		/** Creates a new instance as a wrapper for the given
		 * {@link Runnable}
		 * @param pRunnable The actual {@link Runnable}, which
		 * is being executed for doing the real job.
		 */
		public Reportable(Runnable pRunnable) {
			runnable = pRunnable;
		}
		@Override
		public void run() {
			executed = true;
			runnable.run();
		}
	}
	/** Test case for {@link Actions#executeAll(int, Runnable...)}.
	 */
	@Test
	public void testExecuteAllArray() {
		final Map<String,Integer> sourceMap = new HashMap<>();
		final ConcurrentMap<String,Integer> targetMap = new ConcurrentHashMap<>();
		final String letters = "abcdefghijklmnopqrstuvwxyz";
		final Runnable[] runnables = new Runnable[letters.length()];
		for (int i = 0;  i < letters.length();  i++) {
			final char letter = letters.charAt(i);
			final String key = "" + letter;
			final Integer value = Integer.valueOf((int) letter);
			sourceMap.put(key, value);
			runnables[i] = new Reportable(() -> targetMap.put(key, sourceMap.get(key)));
		}
		Actions.executeAll(5, runnables);
		assertTrue(targetMap.size() == sourceMap.size());
		for (int i = 0;  i < runnables.length;  i++) {
			final Reportable reportable = (Reportable) runnables[i];
			assertTrue(reportable.executed);
		}
		for (Map.Entry<String,Integer> en: sourceMap.entrySet()) {
			assertEquals(en.getValue(), targetMap.get(en.getKey()));
		}
	}
	/** Test case for {@link Actions#executeAll(int, Collection)}.
	 */
	@Test
	public void testExecuteAllList() {
		final Map<String,Integer> sourceMap = new HashMap<>();
		final ConcurrentMap<String,Integer> targetMap = new ConcurrentHashMap<>();
		final String letters = "abcdefghijklmnopqrstuvwxyz";
		final Runnable[] runnables = new Runnable[letters.length()];
		for (int i = 0;  i < letters.length();  i++) {
			final char letter = letters.charAt(i);
			final String key = "" + letter;
			final Integer value = Integer.valueOf((int) letter);
			sourceMap.put(key, value);
			runnables[i] = new Reportable(() -> targetMap.put(key, sourceMap.get(key)));
		}
		Actions.executeAll(5, Arrays.asList(runnables));
		assertTrue(targetMap.size() == sourceMap.size());
		for (int i = 0;  i < runnables.length;  i++) {
			final Reportable reportable = (Reportable) runnables[i];
			assertTrue(reportable.executed);
		}
		for (Map.Entry<String,Integer> en: sourceMap.entrySet()) {
			assertEquals(en.getValue(), targetMap.get(en.getKey()));
		}
	}

	/** Test case for {@link Actions#call(com.github.jochenw.afw.core.function.Functions.FailableFunction)}
	 * with success.
	 */
	@Test
	public void testCallObjectContextSuccess() {
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			final FailableBiFunction<Context<Integer>, Object, Integer, ?> action = (ac, c) -> {
				assertSame(context1, c);
				return Integer.valueOf(index);
			};
			final Integer ri = Actions.call(context1, action);
			assertSame(Integer.valueOf(i), ri);
			final String rs = Actions.call(context2, (ac, c) -> {
				assertSame(context2, c);
				return String.valueOf(index);
			});
			assertEquals(String.valueOf(i), rs);
		}
	}

	/** Test case for {@link Actions#call(Object, com.github.jochenw.afw.core.function.Functions.FailableBiFunction)}
	 * with success.
	 */
	@Test
	public void testCallObjectSuccess() {
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			final FailableFunction<Context<Integer>, Integer, ?> action = (ac) -> {
				return Integer.valueOf(index);
			};
			final Integer ri = Actions.call(action);
			assertSame(Integer.valueOf(i), ri);
			final String rs = Actions.call((ac) -> {
				return String.valueOf(index);
			});
			assertEquals(String.valueOf(i), rs);
		}
	}

	/** Test case for {@link Actions#call(Object, com.github.jochenw.afw.core.function.Functions.FailableBiFunction)}
	 * with warning.
	 */
	@Test
	public void testCallObjectContextWarning() {
		final TriConsumer<Object,Integer,ActionWarningException> validator = (r,i,e) -> {
			assertSame(ActionWarningException.class, e.getClass());
			assertNull(e.getCause());
			assertEquals(r, e.getResult());
			assertEquals("WARN" + i, e.getErrorCode());
			assertEquals("Warning Nr. " + i, e.getErrorMsg());
			assertEquals("Details for warning " + i, e.getErrorDetails());
			assertEquals(e.getErrorCode() + ": " + e.getErrorMsg(), e.getMessage());
			assertSame(e.getErrorCode(), e.getWarningCode());
			assertSame(e.getErrorMsg(), e.getWarningMsg());
			assertSame(e.getErrorDetails(), e.getWarningDetails());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			final FailableBiFunction<Context<Integer>, Object, Integer, ?> action = (ac, c) -> {
				assertSame(context1, c);
				throw ac.warning(Integer.valueOf(index), "WARN" + index, "Warning Nr. " + index,
						   "Details for warning " + index);
			};
			try {
				Actions.call(context1, action);
				fail("Expected Exception");
			} catch (ActionWarningException awe) {
				final Integer in = Integer.valueOf(i);
				validator.accept(in, in, awe);
			}
			try {
				Actions.call(context2, (ac, c) -> {
					assertSame(context2, c);
					throw ac.warning(String.valueOf(index), "WARN" + index, "Warning Nr. " + index,
							   "Details for warning " + index);
				});
				fail("Expected Exception");
			} catch (ActionWarningException awe) {
				final Integer in = Integer.valueOf(i);
				validator.accept(in.toString(), in, awe);
			}
		}
	}


	/** Test case for {@link Actions#call(Object, com.github.jochenw.afw.core.function.Functions.FailableBiFunction)}
	 * with error.
	 */
	@Test
	public void testCallObjectContextError() {
		final BiConsumer<Integer,ActionErrorException> validator = (i,e) -> {
			assertSame(ActionErrorException.class, e.getClass());
			assertNull(e.getCause());
			assertEquals("ERR" + i, e.getErrorCode());
			assertEquals("Error Nr. " + i, e.getErrorMsg());
			assertEquals("Details for error " + i, e.getErrorDetails());
			assertEquals(e.getErrorCode() + ": " + e.getErrorMsg(), e.getMessage());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			final FailableBiFunction<Context<Integer>, Object, Integer, ?> action = (ac, c) -> {
				assertSame(context1, c);
				throw ac.error("ERR" + index, "Error Nr. " + index, "Details for error " + index);
			};
			try {
				Actions.call(context1, action);
				fail("Expected Exception");
			} catch (ActionErrorException aee) {
				final Integer in = Integer.valueOf(i);
				validator.accept(in, aee);
			}
			try {
				Actions.call(context2, (ac, c) -> {
					assertSame(context2, c);
					throw ac.error("ERR" + index, "Error Nr. " + index, "Details for error " + index);
				});
				fail("Expected Exception");
			} catch (ActionErrorException aee) {
				final Integer in = Integer.valueOf(i);
				validator.accept(in, aee);
			}
		}
	}

	/** Test case for {@link Actions#call(Object, com.github.jochenw.afw.core.function.Functions.FailableBiFunction)}
	 * with failure00.
	 */
	@Test
	public void testCallObjectContextFailure() {
		final BiConsumer<Throwable,Throwable> validator = (te,ta) -> {
			assertSame(te, ta);
			assertNull(ta.getCause());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		final IllegalArgumentException iae = new IllegalArgumentException();
		final OutOfMemoryError oome = new OutOfMemoryError();
		for (int i = 0;  i < 10;  i++) {
			final FailableBiFunction<Context<Integer>, Object, Integer, ?> action = (ac, c) -> {
				assertSame(context1, c);
				throw iae;
			};
			try {
				Actions.call(context1, action);
				fail("Expected Exception");
			} catch (RuntimeException e) {
				validator.accept(iae, e);
			}
			try {
				Actions.call(context2, (ac, c) -> {
					assertSame(context2, c);
					throw oome;
				});
				fail("Expected Exception");
			} catch (OutOfMemoryError e) {
				validator.accept(oome, e);
			}
		}
	}

	/** Test case for {@link Actions#perform(Object, com.github.jochenw.afw.core.function.Functions.FailableBiFunction)}
	 * with success.
	 */
	@Test
	public void testPerformObjectContextSuccess() {
		final BiConsumer<Object,Status<?>> validator = (r,s) -> {
			assertSame(State.success,s.getState());
			assertEquals(r, s.getResult());
			assertNull(s.getErrorCode());
			assertNull(s.getErrorMsg());
			assertNull(s.getErrorDetails());
			assertNull(s.getFailure());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			final FailableBiFunction<Context<Integer>, Object, Integer, ?> action = (ac, c) -> {
				assertSame(context1, c);
				return Integer.valueOf(index);
			};
			final Status<Integer> ri = Actions.perform(context1, action);
			validator.accept(Integer.valueOf(i), ri);
			final Status<String> rs = Actions.perform(context2, (ac, c) -> {
				assertSame(context2, c);
				return String.valueOf(index);
			});
			validator.accept(String.valueOf(i), rs);
		}
	}

	/** Test case for {@link Actions#perform(com.github.jochenw.afw.core.function.Functions.FailableFunction)}
	 * with success.
	 */
	@Test
	public void testPerformObjectSuccess() {
		final BiConsumer<Object,Status<?>> validator = (r,s) -> {
			assertSame(State.success,s.getState());
			assertEquals(r, s.getResult());
			assertNull(s.getErrorCode());
			assertNull(s.getErrorMsg());
			assertNull(s.getErrorDetails());
			assertNull(s.getFailure());
		};
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			final FailableFunction<Context<Integer>, Integer, ?> action = (ac) -> {
				return Integer.valueOf(index);
			};
			final Status<Integer> ri = Actions.perform(action);
			validator.accept(Integer.valueOf(i), ri);
			final Status<String> rs = Actions.perform((ac) -> {
				return String.valueOf(index);
			});
			validator.accept(String.valueOf(i), rs);
		}
	}

	/** Test case for {@link Actions#perform(Object, com.github.jochenw.afw.core.function.Functions.FailableBiFunction)}
	 * with warning.
	 */
	@Test
	public void testPerformObjectContextWarning() {
		final TriConsumer<Object,Integer,Status<?>> validator = (r,i,s) -> {
			assertSame(State.warning,s.getState());
			assertEquals(r, s.getResult());
			assertEquals("WARN" + i, s.getErrorCode());
			assertEquals("Warning Nr. " + i, s.getErrorMsg());
			assertEquals("Details for warning " + i, s.getErrorDetails());
			assertNotNull(s.getFailure());
			assertSame(ActionWarningException.class, s.getFailure().getClass());
			final ActionWarningException e = (ActionWarningException) s.getFailure();
			assertSame(s.getErrorCode(), e.getErrorCode());
			assertSame(s.getErrorMsg(), e.getErrorMsg());
			assertSame(s.getErrorDetails(), e.getErrorDetails());
			assertEquals(s.getErrorCode() + ": " + s.getErrorMsg(), e.getMessage());
			assertNull(e.getCause());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			final FailableBiFunction<Context<Integer>, Object, Integer, ?> action = (ac, c) -> {
				assertSame(context1, c);
				throw ac.warning(Integer.valueOf(index), "WARN" + index, "Warning Nr. " + index,
						   "Details for warning " + index);
			};
			final Integer in = Integer.valueOf(i);
			final Status<Integer> ri = Actions.perform(context1, action);
			validator.accept(in, in, ri);
			final Status<String> rs = Actions.perform(context2, (ac, c) -> {
				throw ac.warning(String.valueOf(index), "WARN" + index, "Warning Nr. " + index,
						   "Details for warning " + index);
			});
			validator.accept(in.toString(), in, rs);
		}
	}

	/** Test case for {@link Actions#perform(Object, com.github.jochenw.afw.core.function.Functions.FailableBiFunction)}
	 * with error.
	 */
	@Test
	public void testPerformObjectContextError() {
		final BiConsumer<Integer,Status<?>> validator = (i,s) -> {
			assertSame(State.error,s.getState());
			assertNull(s.getResult());
			assertEquals("ERR" + i, s.getErrorCode());
			assertEquals("Error Nr. " + i, s.getErrorMsg());
			assertEquals("Details for error " + i, s.getErrorDetails());
			assertNotNull(s.getFailure());
			assertSame(ActionErrorException.class, s.getFailure().getClass());
			final ActionErrorException e = (ActionErrorException) s.getFailure();
			assertSame(s.getErrorCode(), e.getErrorCode());
			assertSame(s.getErrorMsg(), e.getErrorMsg());
			assertSame(s.getErrorDetails(), e.getErrorDetails());
			assertEquals(s.getErrorCode() + ": " + s.getErrorMsg(), e.getMessage());
			assertNull(e.getCause());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			final FailableBiFunction<Context<Integer>, Object, Integer, ?> action = (ac, c) -> {
				assertSame(context1, c);
				throw ac.error("ERR" + index, "Error Nr. " + index, "Details for error " + index);
			};
			final Integer in = Integer.valueOf(i);
			final Status<Integer> ri = Actions.perform(context1, action);
			validator.accept(in, ri);
			final Status<String> rs = Actions.perform(context2, (ac, c) -> {
				assertSame(context2, c);
				throw ac.error("ERR" + index, "Error Nr. " + index, "Details for error " + index);
			});
			validator.accept(in, rs);
		}
	}

	/** Test case for {@link Actions#perform(Object, com.github.jochenw.afw.core.function.Functions.FailableBiFunction)}
	 * with failure.
	 */
	@Test
	public void testPerformObjectContextFailure() {
		final BiConsumer<Throwable,Status<?>> validator = (t,s) -> {
			assertSame(State.failure,s.getState());
			assertNull(s.getResult());
			assertNull(s.getErrorCode());
			assertNull(s.getErrorMsg());
			assertNull(s.getErrorDetails());
			assertSame(t, s.getFailure());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		final IllegalArgumentException iae = new IllegalArgumentException();
		final OutOfMemoryError oome = new OutOfMemoryError();
		for (int i = 0;  i < 10;  i++) {
			final FailableBiFunction<Context<Integer>, Object, Integer, ?> action = (ac, c) -> {
				assertSame(context1, c);
				throw iae;
			};
			final Status<Integer> ri = Actions.perform(context1, action);
			validator.accept(iae, ri);
			final Status<String> rs = Actions.perform(context2, (ac, c) -> {
				assertSame(context2, c);
				throw oome;
			});
			validator.accept(oome, rs);
		}
	}

	/** Test case for {@link Actions#run(Object, com.github.jochenw.afw.core.function.Functions.FailableBiConsumer)}
	 * with success.
	 */
	@Test
	public void testRunObjectContextSuccess() {
		final Holder<Object> result = Holder.of();
		final BiConsumer<Object,Status<?>> validator = (r,s) -> {
			assertSame(State.success,s.getState());
			assertEquals(r, result.get());
			assertNull(s.getResult());
			assertNull(s.getErrorCode());
			assertNull(s.getErrorMsg());
			assertNull(s.getErrorDetails());
			assertNull(s.getFailure());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			result.set(null);
			final FailableBiConsumer<Context<Void>, Object, ?> action = (ac, c) -> {
				assertSame(context1, c);
				result.set(Integer.valueOf(index));
			};
			final Status<Void> ri = Actions.run(context1, action);
			validator.accept(Integer.valueOf(i), ri);
			final Status<Void> rs = Actions.run(context2, (ac, c) -> {
				assertSame(context2, c);
				result.set(String.valueOf(index));
			});
			validator.accept(String.valueOf(i), rs);
		}
	}

	/** Test case for {@link Actions#run(com.github.jochenw.afw.core.function.Functions.FailableConsumer)}
	 * with success.
	 */
	@Test
	public void testRunObjectSuccess() {
		final Holder<Object> result = Holder.of();
		final BiConsumer<Object,Status<?>> validator = (r,s) -> {
			assertSame(State.success,s.getState());
			assertEquals(r, result.get());
			assertNull(s.getResult());
			assertNull(s.getErrorCode());
			assertNull(s.getErrorMsg());
			assertNull(s.getErrorDetails());
			assertNull(s.getFailure());
		};
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			result.set(null);
			final FailableConsumer<Context<Void>, ?> action = (ac) -> {
				result.set(Integer.valueOf(index));
			};
			final Status<Void> ri = Actions.run(action);
			validator.accept(Integer.valueOf(i), ri);
			final Status<Void> rs = Actions.run((ac) -> {
				result.set(String.valueOf(index));
			});
			validator.accept(String.valueOf(i), rs);
		}
	}

	/** Test case for {@link Actions#run(Object, com.github.jochenw.afw.core.function.Functions.FailableBiConsumer)}
	 * with warning.
	 */
	@Test
	public void testRunObjectContextWarning() {
		final Holder<Object> result = Holder.of();
		final BiConsumer<Integer,Status<?>> validator = (i,s) -> {
			assertSame(State.warning,s.getState());
			assertNull(s.getResult());
			assertEquals("WARN" + i, s.getErrorCode());
			assertEquals("Warning Nr. " + i, s.getErrorMsg());
			assertEquals("Details for warning " + i, s.getErrorDetails());
			assertNotNull(s.getFailure());
			assertSame(ActionWarningException.class, s.getFailure().getClass());
			final ActionWarningException e = (ActionWarningException) s.getFailure();
			assertSame(s.getErrorCode(), e.getErrorCode());
			assertSame(s.getErrorMsg(), e.getErrorMsg());
			assertSame(s.getErrorDetails(), e.getErrorDetails());
			assertEquals(s.getErrorCode() + ": " + s.getErrorMsg(), e.getMessage());
			assertNull(e.getCause());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			result.set(null);
			final FailableBiConsumer<Context<Void>, Object, ?> action = (ac, c) -> {
				assertSame(context1, c);
				throw ac.warning(null, "WARN" + index, "Warning Nr. " + index,
						   "Details for warning " + index);
			};
			final Status<Void> ri = Actions.run(context1, action);
			validator.accept(Integer.valueOf(i), ri);
			final Status<Void> rs = Actions.run(context2, (ac, c) -> {
				assertSame(context2, c);
				throw ac.warning(null, "WARN" + index, "Warning Nr. " + index,
						   "Details for warning " + index);
			});
			validator.accept(Integer.valueOf(i), rs);
		}
	}

	/** Test case for {@link Actions#run(Object, com.github.jochenw.afw.core.function.Functions.FailableBiConsumer)}
	 * with warning.
	 */
	@Test
	public void testRunObjectContextError() {
		final Holder<Object> result = Holder.of();
		final BiConsumer<Integer,Status<?>> validator = (i,s) -> {
			assertSame(State.error,s.getState());
			assertNull(s.getResult());
			assertEquals("ERR" + i, s.getErrorCode());
			assertEquals("Error Nr. " + i, s.getErrorMsg());
			assertEquals("Details for error " + i, s.getErrorDetails());
			assertNotNull(s.getFailure());
			assertSame(ActionErrorException.class, s.getFailure().getClass());
			final ActionErrorException e = (ActionErrorException) s.getFailure();
			assertSame(s.getErrorCode(), e.getErrorCode());
			assertSame(s.getErrorMsg(), e.getErrorMsg());
			assertSame(s.getErrorDetails(), e.getErrorDetails());
			assertEquals(s.getErrorCode() + ": " + s.getErrorMsg(), e.getMessage());
			assertNull(e.getCause());
		};
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			final int index = i;
			result.set(null);
			final FailableBiConsumer<Context<Void>, Object, ?> action = (ac, c) -> {
				assertSame(context1, c);
				throw ac.error("ERR" + index, "Error Nr. " + index, "Details for error " + index);
			};
			final Status<Void> ri = Actions.run(context1, action);
			validator.accept(Integer.valueOf(i), ri);
			final Status<Void> rs = Actions.run(context2, (ac, c) -> {
				assertSame(context2, c);
				throw ac.error("ERR" + index, "Error Nr. " + index, "Details for error " + index);
			});
			validator.accept(Integer.valueOf(i), rs);
		}
	}

	/** Test case for {@link Actions#run(Object, com.github.jochenw.afw.core.function.Functions.FailableBiConsumer)}
	 * with failure.
	 */
	@Test
	public void testRunObjectContextFailure() {
		final Holder<Object> result = Holder.of();
		final BiConsumer<Throwable,Status<?>> validator = (t,s) -> {
			assertSame(State.failure,s.getState());
			assertNull(s.getResult());
			assertNull(s.getErrorCode());
			assertNull(s.getErrorMsg());
			assertNull(s.getErrorDetails());
			assertSame(t, s.getFailure());
		};
		final IllegalArgumentException iae = new IllegalArgumentException();
		final OutOfMemoryError oome = new OutOfMemoryError();
		final Object context1 = new Object();
		final Long context2 = Long.valueOf(0);
		for (int i = 0;  i < 10;  i++) {
			result.set(null);
			final FailableBiConsumer<Context<Void>, Object, ?> action = (ac, c) -> {
				assertSame(context1, c);
				throw iae;
			};
			final Status<Void> ri = Actions.run(context1, action);
			validator.accept(iae, ri);
			final Status<Void> rs = Actions.run(context2, (ac, c) -> {
				assertSame(context2, c);
				throw oome;
			});
			validator.accept(oome, rs);
		}
	}
}
