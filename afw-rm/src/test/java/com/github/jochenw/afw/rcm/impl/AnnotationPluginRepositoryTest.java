package com.github.jochenw.afw.rcm.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rcm.api.RmPluginRepository;
import com.github.jochenw.afw.rcm.api.RmResourcePlugin;
import com.github.jochenw.afw.rcm.api.RmResourceRefGuesser;
import com.github.jochenw.afw.rcm.api.RmTargetLifecyclePlugin;
import com.github.jochenw.afw.rcm.impl.AnnotationBasedPluginRepository;
import com.github.jochenw.afw.rcm.impl.ClassExecutionPlugin;
import com.github.jochenw.afw.rcm.impl.JdbcResourcePlugin;
import com.github.jochenw.afw.rcm.util.Tests;

public class AnnotationPluginRepositoryTest {
	private static AnnotationBasedPluginRepository repo;

	protected static AnnotationBasedPluginRepository getRepo() throws IOException {
		if (repo == null) {
			repo = (AnnotationBasedPluginRepository) Tests.newRcm(AnnotationPluginRepositoryTest.class).getComponentFactory().requireInstance(RmPluginRepository.class);
		}
		return repo;
	}

	@Test
	public void testResourcePlugins() throws Exception {
		final List<RmResourcePlugin> resourcePlugins = getRepo().getResourcePlugins();
		assertNotNull(resourcePlugins);
		assertEquals(2, resourcePlugins.size());
		assertPluginOf(resourcePlugins, JdbcResourcePlugin.class);
		assertPluginOf(resourcePlugins, ClassExecutionPlugin.class);
	}

	protected <O> void assertPluginOf(List<O> pPlugins, Class<? extends O> pClass) {
		for (O plugin : pPlugins) {
			if (pClass.isAssignableFrom(plugin.getClass())) {
				return;
			}
		}
		Assert.fail("No plugin of type " + pClass.getName() + " found.");
	}

	@Test
	public void testTargetLifecyclePlugins() throws Exception {
		final List<RmTargetLifecyclePlugin> lifecyclePlugins = getRepo().getTargetLifecyclePlugins();
		assertNotNull(lifecyclePlugins);
		assertEquals(1, lifecyclePlugins.size());
		assertPluginOf(lifecyclePlugins, JdbcTargetLifecyclePlugin.class);
	}

	@Test
	public void testResourceRefGuesserPlugins() throws Exception {
		final List<RmResourceRefGuesser> resourceRefGuesserPlugins = getRepo().getResourceRefGuessers();
		assertNotNull(resourceRefGuesserPlugins);
		assertEquals(1, resourceRefGuesserPlugins.size());
		assertPluginOf(resourceRefGuesserPlugins, AnnotationScanningResourceRefGuesser.class);
	}
}
