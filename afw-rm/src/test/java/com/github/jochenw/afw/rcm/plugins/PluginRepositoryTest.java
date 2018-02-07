package com.github.jochenw.afw.rcm.plugins;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rcm.api.ComponentFactory;
import com.github.jochenw.afw.rcm.api.RmPluginRepository;
import com.github.jochenw.afw.rcm.api.RmResourcePlugin;
import com.github.jochenw.afw.rcm.api.RmResourceRefGuesser;
import com.github.jochenw.afw.rcm.api.RmTargetLifecyclePlugin;
import com.github.jochenw.afw.rcm.util.Tests;

public class PluginRepositoryTest {
	private static ComponentFactory componentFactory;
	private static AnnotationBasedPluginRepository annotationBasedRepo;
	private static XmlBasedPluginRepository xmlBasedRepo;

	protected static ComponentFactory getComponentFactory() throws IOException {
		if (componentFactory == null) {
			componentFactory = Tests.newRcmBuilder(PluginRepositoryTest.class)
					.bindClass(AnnotationBasedPluginRepository.class)
					.bindClass(XmlBasedPluginRepository.class)
					.build().getComponentFactory();
		}
		return componentFactory;
	}
	
	protected static AnnotationBasedPluginRepository getAnnotationRepo() throws IOException {
		if (annotationBasedRepo == null) {
			annotationBasedRepo = getComponentFactory().requireInstance(AnnotationBasedPluginRepository.class);
		}
		return annotationBasedRepo;
	}

	protected static XmlBasedPluginRepository  getXmlRepo() throws IOException {
		if (xmlBasedRepo == null) {
			xmlBasedRepo = getComponentFactory().requireInstance(XmlBasedPluginRepository.class);
		}
		return xmlBasedRepo;
	}
	
	@Test
	public void testResourcePluginsWithAnnotations() throws Exception {
		validateResourcePlugins(getAnnotationRepo());
	}

	@Test
	public void testResourcePluginsWithAnno() throws Exception {
		validateResourcePlugins(getXmlRepo());
	}

	protected void validateResourcePlugins(RmPluginRepository pRepo) {
		final List<RmResourcePlugin> resourcePlugins = pRepo.getResourcePlugins();
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
	public void testTargetLifecyclePluginsWithAnnotations() throws Exception {
		validateTargetLifecyclePlugins(getAnnotationRepo());
	}

	@Test
	public void testTargetLifecyclePluginsWithXml() throws Exception {
		validateTargetLifecyclePlugins(getXmlRepo());
	}

	protected void validateTargetLifecyclePlugins(RmPluginRepository pRepo) {
		final List<RmTargetLifecyclePlugin> lifecyclePlugins = pRepo.getTargetLifecyclePlugins();
		assertNotNull(lifecyclePlugins);
		assertEquals(1, lifecyclePlugins.size());
		assertPluginOf(lifecyclePlugins, JdbcTargetLifecyclePlugin.class);
	}
	
	@Test
	public void testResourceRefGuesserPluginsWithAnnotations() throws Exception {
		validateResourceRefGuesserPlugins(getAnnotationRepo());
	}

	@Test
	public void testResourceRefGuesserPluginsWithXml() throws Exception {
		validateResourceRefGuesserPlugins(getXmlRepo());
	}

    protected void validateResourceRefGuesserPlugins(RmPluginRepository pRepo) {
		final List<RmResourceRefGuesser> resourceRefGuesserPlugins = pRepo.getResourceRefGuessers();
		assertNotNull(resourceRefGuesserPlugins);
		assertEquals(1, resourceRefGuesserPlugins.size());
		assertPluginOf(resourceRefGuesserPlugins, AnnotationScanningResourceRefGuesser.class);
    }
}
