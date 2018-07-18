package com.github.jochenw.afw.lc;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.junit.Test;

import com.github.jochenw.afw.core.components.LifecycleController;

import org.junit.Assert;

public class LifecycleControllerTest {
	public static class Startable {
		private Long startTime, closeTime;

		@PostConstruct
		public void start() {
			if (startTime != null) {
				throw new IllegalStateException("Already started");
			}
			startTime = Long.valueOf(System.currentTimeMillis());
		}
		@PreDestroy
		public void close() {
			if (closeTime != null) {
				throw new IllegalStateException("Already closed");
			}
			closeTime = Long.valueOf(System.currentTimeMillis());
		}
		public boolean isStarted() {
			return startTime != null;
		}
		public long getStartTime() {
			return startTime.longValue();
		}
		public boolean isClosed() {
			return closeTime != null;
		}
		public long getCloseTime() {
			return closeTime.longValue();
		}
	}

	@Test
	public void test() {
		final List<Startable> startables = new ArrayList<>();
		for (int i = 0;  i < 10;  i++) {
			startables.add(new Startable());
		}
		final LifecycleController lc = new LifecycleController();
		for (int i = 0;  i < 7;  i++) {
			lc.addListener(startables.get(i));
		}
		for (int i = 0;  i < 10;  i++) {
			final Startable startable = startables.get(i);
			Assert.assertFalse(startable.isStarted());
			Assert.assertFalse(startable.isClosed());
		}
		lc.start();
		for (int i = 0;  i < 10;  i++) {
			final Startable startable = startables.get(i);
			Assert.assertEquals(i < 7, startable.isStarted());
			Assert.assertFalse(startable.isClosed());
		}
		for (int i = 7;  i < 10; i++) {
			final Startable startable = startables.get(i);
			lc.addListener(startable);
			Assert.assertTrue(startable.isStarted());
			Assert.assertFalse(startable.isClosed());
		}
		for (int i = 0;  i < 10;  i++) {
			final Startable startable = startables.get(i);
			Assert.assertTrue(startable.isStarted());
			Assert.assertFalse(startable.isClosed());
			if (i > 0) {
				Assert.assertTrue(startable.getStartTime() >= startables.get(i-1).getStartTime());
			}
		}
		lc.shutdown();
		for (int i = 9;  i >= 0;  i--) {
			final Startable startable = startables.get(i);
			Assert.assertTrue(startable.isStarted());
			Assert.assertTrue(startable.isClosed());
			if (i > 0) {
				Assert.assertTrue(startable.getStartTime() <= startables.get(i-1).getStartTime());
			}
		}
	}
}
