package com.github.jochenw.afw.di.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


class GuiceComponentFactoryTest {
	@BeforeAll
	static void initLogging() {
		final Path loggingPropertiesFile = Paths.get("src/test/resources/logging.properties");
		assertTrue(Files.isRegularFile(loggingPropertiesFile));
		System.setProperty("java.util.logging.config.file", loggingPropertiesFile.toString());
	}
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
