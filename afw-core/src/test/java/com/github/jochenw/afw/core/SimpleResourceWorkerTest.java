package com.github.jochenw.afw.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.github.jochenw.afw.core.SimpleResourceWorker;
import com.github.jochenw.afw.core.SimpleResourceWorker.ResRunnable;
import com.github.jochenw.afw.core.SimpleResourceWorker.SimpleResourceTracker;

import org.junit.Assert;

public class SimpleResourceWorkerTest {
	private static final class SimpleResource implements AutoCloseable {
		private Long closingTime;

		public void close() {
			closingTime = Long.valueOf(System.currentTimeMillis());
		}
		public boolean isClosed() {
			return closingTime != null;
		}
		public long getClosingTime() {
			return closingTime.longValue();
		}
	}

	@Test
	public void test() {
		final List<SimpleResource> resources = new ArrayList<>();
		for (int i = 0;  i < 10;  i++) {
			resources.add(new SimpleResource());
		}
		for (SimpleResource sr : resources) {
			Assert.assertFalse(sr.isClosed());
		}
		final List<SimpleResource> trackedResources = new ArrayList<>();
		final Random random = new Random(System.currentTimeMillis());
		new SimpleResourceWorker().run(new ResRunnable(){
			@Override
			public void run(SimpleResourceTracker pTracker) {
				while (!resources.isEmpty()) {
					final int index = random.nextInt(resources.size());
					final SimpleResource sr = resources.remove(index);
					pTracker.track(sr);
					trackedResources.add(sr);
				}
			}
		});
		Assert.assertTrue(resources.isEmpty());
		Assert.assertEquals(10, trackedResources.size());
		for (int i = 0;  i < 10;  i++) {
			final SimpleResource sr = trackedResources.get(i);
			Assert.assertTrue(sr.isClosed());
			if (i > 0) {
				Assert.assertTrue(sr.getClosingTime() <= trackedResources.get(i-1).getClosingTime());
			}
		}
	}

}
