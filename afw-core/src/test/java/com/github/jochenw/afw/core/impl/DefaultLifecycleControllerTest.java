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
package com.github.jochenw.afw.core.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.ILifecycleController.TerminableListener;

/** Test for the {@link DefaultLifecycleController}.
 */
public class DefaultLifecycleControllerTest {
	private static class Startable implements TerminableListener {
		private Long startTime, shutdownTimee;

		@Override
		@PostConstruct
		public void start() {
			startTime = Long.valueOf(System.currentTimeMillis());
		}

		@Override
		@PreDestroy
		public void shutdown() {
			shutdownTimee = Long.valueOf(System.currentTimeMillis());
		}

		public boolean isStarted() {
			return startTime != null;
		}

		public boolean isShuttingdown() {
			return shutdownTimee != null;
		}

		public Long getStartTime() {
			return startTime;
		}

		public Long getShutdownTime() {
			return shutdownTimee;
		}
	}

	/** Test case for the {@link DefaultLifecycleController}.
	 */
	@Test
	public void testDefaultLifecycleController() {
		final List<Startable> startables = new ArrayList<>(10);
		for (int i = 0;  i < 10;  i++) {
			startables.add(new Startable());
		}
		final DefaultLifecycleController dlc = new DefaultLifecycleController();
		for (int i = 0;   i < 7;  i++) {
			dlc.addListener(startables.get(i));
		}
		for (Startable startable : startables) {
			Assert.assertFalse(startable.isStarted());
			Assert.assertFalse(startable.isShuttingdown());
		}
		dlc.start();
		for (int i = 0;  i < 7;  i++) {
			Assert.assertTrue(startables.get(i).isStarted());
		}
		for (int i = 7;  i < 10;  i++) {
			final Startable startable = startables.get(i);
			dlc.addListener(startable);
			Assert.assertTrue(startable.isStarted());
		}
		for (int i = 0;  i < 10;  i++) {
			final Startable startable = startables.get(i);
			Assert.assertTrue("Not started: " + i, startable.isStarted());
			if (i > 0) {
				Assert.assertTrue(startable.getStartTime().longValue() >= startables.get(i-1).getStartTime().longValue());
			}
			Assert.assertFalse(startable.isShuttingdown());
		}
		dlc.shutdown();
		for (int i = 0;  i < 10;  i++) {
			final Startable startable = startables.get(i);
			Assert.assertTrue(startable.isStarted());
			Assert.assertTrue(startable.isShuttingdown());
			if (i > 0) {
				Assert.assertTrue(startable.getStartTime().longValue() >= startables.get(i-1).getStartTime().longValue());
				Assert.assertTrue(startables.get(i-1).getShutdownTime().longValue() >= startable.getShutdownTime().longValue());
			}
		}
	}
}
