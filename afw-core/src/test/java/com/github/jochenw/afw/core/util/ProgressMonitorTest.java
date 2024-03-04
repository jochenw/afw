package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

		@Override
		public void accept(ProgressMonitor pReporter, String pStatus) throws RuntimeException {
			reports.add(Long.valueOf(pReporter.getCount()));
			reports.add(Long.valueOf(pReporter.getTotal()));
			reports.add(Long.valueOf(pReporter.getInterval()));
			reports.add(pStatus);
		}
	}

	/** Test case for {@link ProgressMonitor#of(long, Functions.FailableConsumer)}.
	 */
	@Test
	public void testProgressMonitorLongConsumer() {
		final Listener1 listener = new Listener1();
		final ProgressMonitor pm = ProgressMonitor.of(5, listener);
		runTestProgressMonitorLongConsumer(listener, pm);
	}

	private void runTestProgressMonitorLongConsumer(final Listener1 listener, final ProgressMonitor pm) {
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

	/** Test case for {@link ProgressMonitor#of(long, Functions.FailableConsumer)}.
	 */
	@Test
	public void testProgressMonitorLongLongConsumer() {
		final Listener1 listener = new Listener1();
		final ProgressMonitor pm = ProgressMonitor.of(10, 3, listener);
		runTestProgressMonitorLongLongConsumer(listener, pm);
	}

	private void runTestProgressMonitorLongLongConsumer(final Listener1 pListener, final ProgressMonitor pMonitor) {
		for (int i = 0;  i < 9;  i++) {
			pMonitor.inc();
		}
		pMonitor.inc();
		final List<Object> reports = pListener.reports;
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

	/** Test case for {@link ProgressMonitor#of(long, Functions.FailableBiConsumer)}.
	 */
	@Test
	public void testProgressMonitorLongBiConsumer() {
		final Listener2 listener = new Listener2();
		final ProgressMonitor pm = ProgressMonitor.of(5, listener);
		runTestProgressMonitorLongBiConsumer(listener, pm);
	}

	private void runTestProgressMonitorLongBiConsumer(final Listener2 listener, final ProgressMonitor pm) {
		for (int i = 0;  i < 9;  i++) {
			pm.inc();
		}
		pm.inc();
		final List<Object> reports = listener.reports;
		final Consumer<List<Object>> validator = (list) -> {
			assertEquals(Long.valueOf(5), list.get(0));
			assertEquals(Long.valueOf(-1), list.get(1));
			assertEquals(Long.valueOf(5), list.get(2));
			assertEquals("5", list.get(3));
			assertEquals(Long.valueOf(10), list.get(4));
			assertEquals(Long.valueOf(-1), list.get(5));
			assertEquals(Long.valueOf(5), list.get(6));
			assertEquals("10", reports.get(7));
		};
		assertEquals(8, reports.size());
		validator.accept(reports);
		pm.finish();
		assertEquals(12, reports.size());
		validator.accept(reports);
		assertEquals(Long.valueOf(10), reports.get(8));
		assertEquals(Long.valueOf(10), reports.get(9));
		assertEquals(Long.valueOf(5), reports.get(10));
		assertEquals("10/10 100.00%: ", reports.get(11));
	}

	/** Test case for {@link ProgressMonitor#of(long, Functions.FailableBiConsumer)}.
	 */
	@Test
	public void testProgressMonitorLongLongBiConsumer() {
		final Listener2 listener = new Listener2();
		final ProgressMonitor pm = ProgressMonitor.of(10, 3, listener);
		runTestProgressMonitorLongLongBiConsumer(listener, pm);
		
	}

	private void runTestProgressMonitorLongLongBiConsumer(final Listener2 pListener, final ProgressMonitor pMonitor) {
		for (int i = 0;  i < 10;  i++) {
			pMonitor.inc();
		}
		final List<Object> reports = pListener.reports;
		assertEquals(16, reports.size());
		assertEquals(Long.valueOf(3), reports.get(0));
		assertEquals(Long.valueOf(10), reports.get(1));
		assertEquals(Long.valueOf(3), reports.get(2));
		assertEquals("3/10 30.00%: ", reports.get(3));
		assertEquals(Long.valueOf(6), reports.get(4));
		assertEquals(Long.valueOf(10), reports.get(5));
		assertEquals(Long.valueOf(3), reports.get(6));
		assertEquals("6/10 60.00%: ", reports.get(7));
		assertEquals(Long.valueOf(9), reports.get(8));
		assertEquals(Long.valueOf(10), reports.get(9));
		assertEquals(Long.valueOf(3), reports.get(10));
		assertEquals("9/10 90.00%: ", reports.get(11));
		assertEquals(Long.valueOf(10), reports.get(12));
		assertEquals(Long.valueOf(10), reports.get(13));
		assertEquals(Long.valueOf(3), reports.get(14));
		assertEquals("10/10 100.00%: ", reports.get(15));
	}

	/** Test case for {@link ProgressMonitor#synchrnzd()}.
	 */
	@Test
	public void testSynchronzd() {
		final Listener1 listener = new Listener1();
		final ProgressMonitor pm = ProgressMonitor.of(5, listener).synchrnzd();
		runTestProgressMonitorLongConsumer(listener, pm);

		final Listener1 listener2 = new Listener1();
		final ProgressMonitor pm2 = ProgressMonitor.of(10, 3, listener2).synchrnzd();
		runTestProgressMonitorLongLongConsumer(listener2, pm2);

		final Listener2 listener3 = new Listener2();
		final ProgressMonitor pm3 = ProgressMonitor.of(5, listener3);
		runTestProgressMonitorLongBiConsumer(listener3, pm3);

		final Listener2 listener4 = new Listener2();
		final ProgressMonitor pm4 = ProgressMonitor.of(10, 3, listener4);
		runTestProgressMonitorLongLongBiConsumer(listener4, pm4);

	}
}
