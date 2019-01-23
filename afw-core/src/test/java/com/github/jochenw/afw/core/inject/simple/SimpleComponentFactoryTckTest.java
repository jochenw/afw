package com.github.jochenw.afw.core.inject.simple;

import com.github.jochenw.afw.core.inject.InjectTests;

import junit.framework.Test;
import junit.framework.TestCase;

public class SimpleComponentFactoryTckTest extends TestCase {
	public static Test suite() {
		return InjectTests.testTckCompliance(new SimpleComponentFactoryBuilder());
	}
}
