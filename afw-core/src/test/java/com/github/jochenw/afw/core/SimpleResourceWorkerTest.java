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
