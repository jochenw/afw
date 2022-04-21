package com.github.jochenw.afw.di.impl.guice;

import org.junit.Test;

import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.ComponentFactoryTests;

public class GuiceComponentFactoryTest {
	private static final Class<? extends AbstractComponentFactory> COMPONENT_FACTORY_TYPE = GuiceComponentFactory.class;

	@Test
	public void testCreateMaps() {
		ComponentFactoryTests.testCreateMaps(COMPONENT_FACTORY_TYPE);
	}

	@Test
	public void testParent() {
		ComponentFactoryTests.testParent(COMPONENT_FACTORY_TYPE);
	}

	@Test
	public void testTck() {
		ComponentFactoryTests.testTck(COMPONENT_FACTORY_TYPE);
	}
}
