package com.github.jochenw.afw.core.inject.simple;

import org.junit.Test;

import com.github.jochenw.afw.core.inject.InjectTests;



public class SimpleComponentFactoryTest {
	@Test
	public void testSimpleComponentFactory() {
		InjectTests.testComponentFactory(new SimpleComponentFactoryBuilder());
	}
}
