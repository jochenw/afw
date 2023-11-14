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
	private long count, intervalCount;
	private boolean silent;
	private final long total, interval;
	private final FailableBiConsumer<ProgressMonitor,String,?> listener1;
	private final FailableConsumer<String,?> listener2;

	/** Creates a new instance with the given interval, a total of -1, and
	 * the given listener. In other words, this is equivalent to
	 * <pre>
	 *   new ProgressMonitor(-1, pInterval, pListener);
	 * </pre>
	 * @param pInterval The reporting interval. For example, if the interval is 1000, then
	 *   the progress monitor will report its status after a count of 1000, 2000, 3000, ...
	 * @param pListener The listener will be notified whenever the count has reached the
	 *   interval.
	 */
	public ProgressMonitor(long pInterval, FailableConsumer<String,?> pListener) {
		this(-1l, pInterval, pListener);
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
	 */
	public ProgressMonitor(long pInterval, FailableBiConsumer<ProgressMonitor,String,?> pListener) {
		this(-1l, pInterval, pListener);
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
	public ProgressMonitor(long pTotal, long pInterval, FailableConsumer<String,?> pListener) {
		total = pTotal;
		interval = pInterval;
		count = 0;
		intervalCount = 0;
		listener1 = null;
		listener2 = pListener;
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
	public ProgressMonitor(long pTotal, long pInterval, FailableBiConsumer<ProgressMonitor,String,?> pListener) {
		total = pTotal;
		interval = pInterval;
		count = 0;
		intervalCount = 0;
		listener1 = pListener;
		listener2 = null;
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
		if (!silent  &&  (listener1 != null  ||  listener2 != null)) {
			if ((total != -1  &&  count >= total)  ||  intervalCount >= interval) {
				try {
					if (total == -1) {
						notify(String.valueOf(count));
					} else {
						final StringBuilder sb = new StringBuilder();
						sb.append(count);
						sb.append("/");
						sb.append(total);
						sb.append(" ");
						final double percentage = ((double) count * 100.0d) / ((double) total);
						final BigDecimal bd = new BigDecimal(percentage);
						sb.append(bd.setScale(2, RoundingMode.HALF_UP));
						sb.append("%: ");
						notify(sb.toString());
					}
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
				if (total != -1  &&  count >= total) {
					silent = true;
				}
				if (intervalCount >= interval) {
					intervalCount = 0;
				}
			}
		}
	}

	/** Called to invoke the listener with the given status.
	 * @param pStatus The progress monitors status. If the total is -1, then this will
	 *   simply be the value of the counter. Otherwise, this will be like
	 *   "5/10 50.00%".
	 */
	protected void notify(String pStatus) {
		if (listener1 != null) {
			Functions.accept(listener1, this, pStatus);
		}
		if (listener2 != null) {
			Functions.accept(listener2, pStatus);
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
}
