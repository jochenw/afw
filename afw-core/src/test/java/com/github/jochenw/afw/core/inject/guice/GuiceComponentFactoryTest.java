package com.github.jochenw.afw.core.inject.guice;

import org.junit.Test;

import com.github.jochenw.afw.core.inject.InjectTests;



public class GuiceComponentFactoryTest {
	@Test
	public void testGuiceComponentFactoryBuilder() {
		InjectTests.testComponentFactory(new GuiceComponentFactoryBuilder());
	}
}
