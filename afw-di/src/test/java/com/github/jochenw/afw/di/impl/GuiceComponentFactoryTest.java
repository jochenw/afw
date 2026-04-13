package com.github.jochenw.afw.di.impl;

import org.junit.jupiter.api.Test;


class GuiceComponentFactoryTest {
	@Test
	void testCreateJakartaMapBindings() {
		ComponentFactoryTests.testCreateJakartaMapBindings(GuiceComponentFactory.class);
	}
	@Test
	void testCreateJakartaMaps() {
		ComponentFactoryTests.testCreateJakartaMaps(GuiceComponentFactory.class);
	}
	@Test
	void testTck() {
		ComponentFactoryTests.testTck(GuiceComponentFactory.class);
	}
	@Test
	void testGenerics() {
		ComponentFactoryTests.testGenerics(GuiceComponentFactory.class);
	}
	@Test
	void testModuleExtension() {
		ComponentFactoryTests.testModuleExtension(GuiceComponentFactory.class); }
	@Test
	void testModuleOverrides() {
		ComponentFactoryTests.testModuleOverrides(GuiceComponentFactory.class);	}
	@Test
	void testCustomBindingProvider() {
		ComponentFactoryTests.testCustomBindingProvider(GuiceComponentFactory.class);
	}
	@Test
	void testParent() {
		ComponentFactoryTests.testParent(GuiceComponentFactory.class);
	}
}
