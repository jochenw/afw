package com.github.jochenw.afw.di.simple;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.di.impl.ComponentFactoryTests;

class SimpleComponentFactoryTest {
	@Test
	void testCreateJakartaMapBindings() {
		ComponentFactoryTests.testCreateJakartaMapBindings(SimpleComponentFactory.class);
	}
	@Test
	void testCreateJakartaMaps() {
		ComponentFactoryTests.testCreateJakartaMaps(SimpleComponentFactory.class);
	}
	@Test
	void testTck() {
		ComponentFactoryTests.testTck(SimpleComponentFactory.class);
	}
	@Test
	void testGenerics() {
		ComponentFactoryTests.testGenerics(SimpleComponentFactory.class);
	}
	@Test
	void testModuleExtension() {
		ComponentFactoryTests.testModuleExtension(SimpleComponentFactory.class); }
	@Test
	void testModuleOverrides() {
		ComponentFactoryTests.testModuleOverrides(SimpleComponentFactory.class);	}
	@Test
	void testCustomBindingProvider() {
		ComponentFactoryTests.testCustomBindingProvider(SimpleComponentFactory.class);
	}
	@Test
	void testParent() {
		ComponentFactoryTests.testParent(SimpleComponentFactory.class);
	}
}
