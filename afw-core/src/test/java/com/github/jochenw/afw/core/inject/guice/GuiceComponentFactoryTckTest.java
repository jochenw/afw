package com.github.jochenw.afw.core.inject.guice;

import com.github.jochenw.afw.core.inject.InjectTests;

import junit.framework.Test;
import junit.framework.TestCase;

public class GuiceComponentFactoryTckTest extends TestCase {
	public static Test suite() {
		return InjectTests.testTckCompliance(new GuiceComponentFactoryBuilder());
	}
}
