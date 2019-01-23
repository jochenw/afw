package com.github.jochenw.afw.core.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.ILifefycleController.Listener;
import com.github.jochenw.afw.core.ILifefycleController.TerminableListener;

public class DefaultLifecycleControllerTest {
	private static class Startable implements TerminableListener {
		private Long startTime, shutdownTimee;

		@Override @PostConstruct
		public void start() {
			startTime = Long.valueOf(System.currentTimeMillis());
		}

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
