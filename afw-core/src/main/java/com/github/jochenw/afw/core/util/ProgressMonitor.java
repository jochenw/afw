package com.github.jochenw.afw.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;


/** A progress monitor is used to provide information about how much of a measurable
 * action has been done.
 */
public class ProgressMonitor {
	private long count, intervalCount, total;
	private boolean silent;
	private long interval;
	private final FailableBiConsumer<ProgressMonitor,String,?> listener;

	/** Creates a new instance with the given interval, a total of -1, and
	 * the given listener. In other words, this is equivalent to
	 * <pre>
	 *   new ProgressMonitor(-1, pInterval, pListener);f
	 * </pre>
	 * @param pInterval The reporting interval. For example, if the interval is 1000, then
	 *   the progress monitor will report its status after a count of 1000, 2000, 3000, ...
	 * @param pListener The listener will be notified whenever the count has reached the
	 *   interval.
	 * @return The created instance.
	 */
	public static ProgressMonitor of(long pInterval, FailableConsumer<String,?> pListener) {
		return of(-1l, pInterval, pListener);
	}

	/** Creates a new instance with the given interval, a total of -1, and
	 * the given listener. In other words, this is equivalent to
	 * <pre>
	 *   new ProgressMonitor(-1, pInterval, pListener);
	 * </pre>
	 * @param pInterval The reporting interval. For example, if the interval is 1000, then
	 *   the progress monitor will report its status after a count of 1000, 2000, 3000, ...
	 * @param pListener The listener will be notified whenever the count has reached the
	 *   interval.
	 * @return The created instance.
	 */
	public static ProgressMonitor of(long pInterval, FailableBiConsumer<ProgressMonitor,String,?> pListener) {
		return of(-1, pInterval, pListener);
	}

	/** Creates a new instance with the given interval, a total of -1, and
	 * the given listener. In other words, this is equivalent to
	 * <pre>
	 *   new ProgressMonitor(-1, pInterval, pListener);
	 * </pre>
	 * @param pTotal The expected total number of calls to {@link #inc()}.
	 * @param pInterval The reporting interval. For example, if the interval is 1000, then
	 *   the progress monitor will report its status after a count of 1000, 2000, 3000, ...
	 * @param pListener The listener will be notified whenever the count has reached the
	 *   interval.
	 * @return The created instance.
	 */
	public static ProgressMonitor of (long pTotal, long pInterval, FailableConsumer<String,?> pListener) {
		return new ProgressMonitor(pTotal, pInterval, (p,s) -> pListener.accept(s));
	}

	/** Creates a new instance with the given interval, a total of -1, and
	 * the given listener. In other words, this is equivalent to
	 * <pre>
	 *   new ProgressMonitor(-1, pInterval, pListener);
	 * </pre>
	 * @param pTotal The expected total number of calls to {@link #inc()}.
	 * @param pInterval The reporting interval. For example, if the interval is 1000, then
	 *   the progress monitor will report its status after a count of 1000, 2000, 3000, ...
	 * @param pListener The listener will be notified whenever the count has reached the
	 *   interval.
	 */
	protected ProgressMonitor(long pTotal, long pInterval, FailableBiConsumer<ProgressMonitor,String,?> pListener) {
		total = pTotal;
		interval = pInterval;
		count = 0;
		intervalCount = 0;
		listener = pListener;
		silent = pListener == null;
	}

	/** Creates a new instance with the given interval, a total of -1, and
	 * the given listener. In other words, this is equivalent to
	 * <pre>
	 *   new ProgressMonitor(-1, pInterval, pListener);
	 * </pre>
	 * @param pTotal The expected total number of calls to {@link #inc()}.
	 * @param pInterval The reporting interval. For example, if the interval is 1000, then
	 *   the progress monitor will report its status after a count of 1000, 2000, 3000, ...
	 * @param pListener The listener will be notified whenever the count has reached the
	 *   interval.
	 * @return The created instance.
	 */
	public static ProgressMonitor of(long pTotal, long pInterval, FailableBiConsumer<ProgressMonitor,String,?> pListener) {
		return new ProgressMonitor(pTotal, pInterval, pListener);
	}

	/** Increments the counter by 1. Triggers a status report
	 * (an invocation of the listener), whenever the interval,
	 * or the total are reached.
	 */
	public void inc() {
		inc(1);
	}

	/** Increments the counter by the given number. Triggers a status report
	 * (an invocation of the listener), whenever the interval, or the
	 * total are reached.
	 * @param pNumber The counters increment
	 */
	public void inc(int pNumber) {
		count += pNumber;
		intervalCount += pNumber;
		if (!silent) {
			if (total == -1) {
				if (intervalCount >= interval) {
					report(count, total);
					intervalCount = 0;
				}
			} else {
				if (count >= total) {
					finish();
				} else if (intervalCount >= interval) {
					report(count, total);
					intervalCount = 0;
				}
			}
		}
	}

	private void report(long pCount, long total) {
		if (!silent) {
			try {
				if (total == -1) {
					notify(String.valueOf(pCount));
				} else {
					final StringBuilder sb = new StringBuilder();
					sb.append(pCount);
					sb.append("/");
					sb.append(total);
					sb.append(" ");
					final double percentage = ((double) pCount * 100.0d) / ((double) total);
					final BigDecimal bd = new BigDecimal(percentage);
					sb.append(bd.setScale(2, RoundingMode.HALF_UP));
					sb.append("%: ");
					notify(sb.toString());
				}
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
	}

	/** Called to invoke the listener with the given status.
	 * @param pStatus The progress monitors status. If the total is -1, then this will
	 *   simply be the value of the counter. Otherwise, this will be like
	 *   "5/10 50.00%".
	 */
	protected void notify(String pStatus) {
		if (listener != null) {
			Functions.accept(listener, this, pStatus);
		}
	}

	/** Returns the interval.
	 * @return The interval.
	 */
	public long getInterval() { return interval; }
	/** Returns the count.
	 * @return The count.
	 */
	public long getCount() { return count; }
	/** Returns the total.
	 * @return The total.
	 */
	public long getTotal() { return total; }

	/** Called to indicate, that the progress monitor should report successful execution,
	 * and is no longer being used.
	 */
	public void finish() {
		if (total == -1) {
			total = count;
			report(count, count);
		} else {
			report(total, total);
		}
		silent = true;
	}

	/** Creates a synchronized version of this {@link ProgressMonitor},
	 * with the count reset to 0.
	 * @return A synchronized version of this {@link ProgressMonitor},
	 * with the count reset to 0.
	 */
	public ProgressMonitor synchrnzd() {
		return new ProgressMonitor(getTotal(), getInterval(), listener) {
			@Override
			public synchronized void inc() {
				super.inc();
			}

			@Override
			public synchronized void inc(int pNumber) {
				// TODO Auto-generated method stub
				super.inc(pNumber);
			}

			@Override
			public synchronized long getInterval() {
				// TODO Auto-generated method stub
				return super.getInterval();
			}

			@Override
			public synchronized long getCount() {
				// TODO Auto-generated method stub
				return super.getCount();
			}

			@Override
			public synchronized long getTotal() {
				return super.getTotal();
			}

			@Override
			public synchronized void finish() {
				super.finish();
			}
		};
	}
}
