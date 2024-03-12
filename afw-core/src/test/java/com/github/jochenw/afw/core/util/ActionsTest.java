package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.Test;

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

}
