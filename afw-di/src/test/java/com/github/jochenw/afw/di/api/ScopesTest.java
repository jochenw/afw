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
		assertEquals("EAGER_SINGLETON", Scopes.EAGER_SINGLETON.name());
		assertNotSame(Scopes.SINGLETON, Scopes.EAGER_SINGLETON);
		assertNotNull(Scopes.NO_SCOPE);
		assertEquals("NO_SCOPE", Scopes.NO_SCOPE.name());
		assertNotSame(Scopes.SINGLETON, Scopes.NO_SCOPE);
		assertNotSame(Scopes.EAGER_SINGLETON, Scopes.NO_SCOPE);
	}
}
