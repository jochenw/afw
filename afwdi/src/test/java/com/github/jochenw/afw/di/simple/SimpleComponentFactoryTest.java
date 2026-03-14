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

}
