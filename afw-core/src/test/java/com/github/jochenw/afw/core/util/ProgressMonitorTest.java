package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;


/** Test suite for the {@link ProgressMonitor}.
 */
public class ProgressMonitorTest {
	private static class Listener1 implements Functions.FailableConsumer<String,RuntimeException> {
		private List<Object> reports = new ArrayList<>();
		private int count;

		
		@Override
		public void accept(String pStatus) throws RuntimeException {
			reports.add(Integer.valueOf(++count));
			reports.add(pStatus);
		}
	}
	private static class Listener2 implements Functions.FailableBiConsumer<ProgressMonitor,String,RuntimeException> {
		private List<Object> reports = new ArrayList<>();
		private int count;
		private final long total;

		/** Creates a new instance with the given expected total.
		 * @param pTotal The expected total.
		 */
		public Listener2(long pTotal) {
			total = pTotal;
		}

		@Override
		public void accept(ProgressMonitor pReporter, String pStatus) throws RuntimeException {
			long expectedCnt = ++count * pReporter.getInterval();
			final long expectedCount;
			if (total == -1) {
				expectedCount = expectedCnt;
			} else {
				if (total < expectedCnt) {
					expectedCount = total;
				} else {
					expectedCount = expectedCnt;
				}
			}
			final long actualCount = pReporter.getCount();
			if (expectedCount != actualCount) {
				// Trigger an error using JUnit's error reporting
				assertEquals(Long.valueOf(expectedCount), Long.valueOf(actualCount));
			}
			reports.add(Integer.valueOf(count));
			reports.add(pStatus);
		}
	}

	/** Test case for {@link ProgressMonitor#ProgressMonitor(long, Functions.FailableConsumer)}.
	 */
	@Test
	public void testProgressMonitorLongConsumer() {
		final Listener1 listener = new Listener1();
		final ProgressMonitor pm = new ProgressMonitor(5, listener);
		for (int i = 0;  i < 9;  i++) {
			pm.inc();
		}
		pm.inc();
		final List<Object> reports = listener.reports;
		assertEquals(4, reports.size());
		assertEquals(Integer.valueOf(1), reports.get(0));
		assertEquals("5", reports.get(1));
		assertEquals(Integer.valueOf(2), reports.get(2));
		assertEquals("10", reports.get(3));
	}

	/** Test case for {@link ProgressMonitor#ProgressMonitor(long, Functions.FailableConsumer)}.
	 */
	@Test
	public void testProgressMonitorLongLongConsumer() {
		final Listener1 listener = new Listener1();
		final ProgressMonitor pm = new ProgressMonitor(10, 3, listener);
		for (int i = 0;  i < 9;  i++) {
			pm.inc();
		}
		pm.inc();
		final List<Object> reports = listener.reports;
		assertEquals(8, reports.size());
		assertEquals(Integer.valueOf(1), reports.get(0));
		assertEquals("3/10 30.00%: ", reports.get(1));
		assertEquals(Integer.valueOf(2), reports.get(2));
		assertEquals("6/10 60.00%: ", reports.get(3));
		assertEquals(Integer.valueOf(3), reports.get(4));
		assertEquals("9/10 90.00%: ", reports.get(5));
		assertEquals(Integer.valueOf(4), reports.get(6));
		assertEquals("10/10 100.00%: ", reports.get(7));
	}

	/** Test case for {@link ProgressMonitor#ProgressMonitor(long, Functions.FailableBiConsumer)}.
	 */
	@Test
	public void testProgressMonitorLongBiConsumer() {
		final Listener2 listener = new Listener2(-1);
		final ProgressMonitor pm = new ProgressMonitor(5, listener);
		for (int i = 0;  i < 9;  i++) {
			pm.inc();
		}
		pm.inc();
		final List<Object> reports = listener.reports;
		assertEquals(4, reports.size());
		assertEquals(Integer.valueOf(1), reports.get(0));
		assertEquals("5", reports.get(1));
		assertEquals(Integer.valueOf(2), reports.get(2));
		assertEquals("10", reports.get(3));
	}

	/** Test case for {@link ProgressMonitor#ProgressMonitor(long, Functions.FailableBiConsumer)}.
	 */
	@Test
	public void testProgressMonitorLongLongBiConsumer() {
		final Listener2 listener = new Listener2(10);
		final ProgressMonitor pm = new ProgressMonitor(10, 3, listener);
		for (int i = 0;  i < 9;  i++) {
			pm.inc();
		}
		pm.inc();
		final List<Object> reports = listener.reports;
		assertEquals(8, reports.size());
		assertEquals(Integer.valueOf(1), reports.get(0));
		assertEquals("3/10 30.00%: ", reports.get(1));
		assertEquals(Integer.valueOf(2), reports.get(2));
		assertEquals("6/10 60.00%: ", reports.get(3));
		assertEquals(Integer.valueOf(3), reports.get(4));
		assertEquals("9/10 90.00%: ", reports.get(5));
		assertEquals(Integer.valueOf(4), reports.get(6));
		assertEquals("10/10 100.00%: ", reports.get(7));
	}

}
