/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.github.jochenw.afw.core.SimpleResourceWorker;
import com.github.jochenw.afw.core.SimpleResourceWorker.ResCallable;
import com.github.jochenw.afw.core.SimpleResourceWorker.ResRunnable;
import com.github.jochenw.afw.core.SimpleResourceWorker.SimpleResourceTracker;

import org.junit.Assert;

public class SimpleResourceWorkerTest {
	private static final class SimpleResource implements AutoCloseable {
		private Long closingTime;

		@Override
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

	/**
	 * Test, that all tracked resources are being closed in the reversed
	 * order of registration.
	 */
	@Test
	public void testAllClosedInOrder() {
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

	private static class FailingResource implements AutoCloseable {
		private final Exception error;

		public FailingResource(Exception pError) {
			error = pError;
		}

		@Override
		public void close() throws Exception {
			if (error != null) {
				throw error;
			}
		}
	}

	/**
	 * Tests, whether the {@link SimpleResourceWorker} reports the first
	 * exception, which is thrown upon closing.
	 */
	public void testFirstExceptionVisible() {
		final SimpleResourceWorker srw = new SimpleResourceWorker();
		final RuntimeException rte = new RuntimeException("An error occcurred.");
		/** If a single exception occurs, we expect that exception.
		 */
		try {
			srw.call((tracker) -> {
				tracker.track(new FailingResource(rte));
				return Boolean.TRUE;
			});
			Assert.fail("Expected Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(rte, e);
		}
		/** If multiple exceptions occur, we expect the first exception.
		 */
		final RuntimeException other = new RuntimeException("Another error occurred.");
		try {
			srw.call((tracker) -> {
				tracker.track(new FailingResource(rte));
				tracker.track(new FailingResource(other));
				return Boolean.TRUE;
			});
			Assert.fail("Expected Exception");
		} catch (RuntimeException e) {
			Assert.assertSame(other, e);
		}
	}

	/** Test for {@link SimpleResourceWorker#assertTrackable(Object)}.
	 */
	public void testAssertTrackable() {
		final SimpleResourceWorker srw = new SimpleResourceWorker();
		try {
			srw.run((tracker) -> {
				tracker.track("Untrackable object");
			});
			Assert.fail("Expected Exception");
		} catch (IllegalStateException ise) {
			Assert.assertEquals("", ise.getMessage());
		}
	}

	/** Test for a custom {@link SimpleResourceWorker#assertTrackable(Object)}.
	 */
	public void testCustomAssertTrackable() {
		final SimpleResourceWorker srw = new SimpleResourceWorker() {

			@Override
			protected void assertTrackable(Object pResource) {
				if (pResource instanceof String) {
					// Do nothing, okay.
				} else {
					super.assertTrackable(pResource);
				}
			}

			@Override
			protected void closeResource(Object pResource, boolean pCommit) throws Throwable {
				if (pResource instanceof String) {
					// Do nothing, okay.
				} else {
					super.closeResource(pResource, pCommit);
				}
			}
			
		};
		srw.run((tracker) -> {
			tracker.track("Untrackable object");
		});
	}
}
