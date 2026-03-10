package com.github.jochenw.afw.di.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

class ScopesTest {
	@Test
	void testInstances() {
		assertNotNull(Scopes.SINGLETON);
		assertEquals("SINGLETON", Scopes.SINGLETON.name());
		assertNotNull(Scopes.EAGER_SINGLETON);
		assertEquals("EAGEER_SINGLETON", Scopes.EAGER_SINGLETON.name());
		assertNotSame(Scopes.SINGLETON, Scopes.EAGER_SINGLETON);
	}
}
