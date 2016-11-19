package com.github.jochenw.afw.lc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.SimpleResourceWorker;
import com.github.jochenw.afw.core.SimpleResourceWorker.ResRunnable;
import com.github.jochenw.afw.core.SimpleResourceWorker.SimpleResourceTracker;


public class JdbcResourceWorkerTest {
	private static final class JdbcResource implements AutoCloseable {
		private Long closingTime;
		private Long connectionTime;

		public void connected() {
			connectionTime = Long.valueOf(System.currentTimeMillis());
		}
		public void close() {
			closingTime = Long.valueOf(System.currentTimeMillis());
		}
		public boolean isClosed() {
			return closingTime != null;
		}
		public boolean isConnected() {
			return connectionTime != null;
		}
		public long getClosingTime() {
			return closingTime.longValue();
		}
		public long getConnectionTime() {
			return connectionTime.longValue();
		}
	}

	@Test
	public void test() {
		final List<JdbcResource> resources = new ArrayList<>();
		for (int i = 0;  i < 10;  i++) {
			resources.add(new JdbcResource());
		}
		for (JdbcResource sr : resources) {
			Assert.assertFalse(sr.isClosed());
		}
		final List<JdbcResource> trackedResources = new ArrayList<>();
		final Random random = new Random(System.currentTimeMillis());
		new SimpleResourceWorker().run(new ResRunnable(){
			@Override
			public void run(SimpleResourceTracker pTracker) {
				while (!resources.isEmpty()) {
					final int index = random.nextInt(resources.size());
					final JdbcResource sr = resources.remove(index);
					pTracker.track(sr);
					trackedResources.add(sr);
				}
			}
		});
		Assert.assertTrue(resources.isEmpty());
		Assert.assertEquals(10, trackedResources.size());
		for (int i = 0;  i < 10;  i++) {
			final JdbcResource sr = trackedResources.get(i);
			Assert.assertTrue(sr.isClosed());
			if (i > 0) {
				Assert.assertTrue(sr.getClosingTime() <= trackedResources.get(i-1).getClosingTime());
			}
		}
	}
}
