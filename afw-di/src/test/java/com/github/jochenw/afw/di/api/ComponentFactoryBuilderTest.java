package com.github.jochenw.afw.di.api;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.di.api.IComponentFactory.IBinding;
import com.github.jochenw.afw.di.api.IComponentFactory.IConfiguration;
import com.github.jochenw.afw.di.impl.SimpleComponentFactory;


class ComponentFactoryBuilderTest {
	@SuppressWarnings("unused")
	public static class MappingSubject {
		private @jakarta.inject.Inject String jakartaInjectedField;
		private @javax.inject.Inject String javaxInjectedField;
		private @com.google.inject.Inject String googleInjectedField;
	}

	@Test
	void testBindings() {
		final SimpleComponentFactory cf = (SimpleComponentFactory) IComponentFactory.builder()
				.module((b) -> {
					
				})
				.build();
		assertNotNull(cf);
		final IConfiguration configuration = cf.getConfiguration();
		assertNotNull(configuration);
		assertNotNull(configuration.getBindings());
		assertSame(Scopes.SINGLETON, configuration.getDefaultScope());
		assertSame(DefaultAnnotationProvider.getInstance(), configuration.getAnnotationProvider());
		assertNull(configuration.getParent());
		assertFalse(configuration.getBindings().isEmpty());
		assertEquals(1, configuration.getBindings().size());
		final Key<Object> key = Key.of(IComponentFactory.class, "");
		final IBinding<Object> cfBinding = configuration.getBindings().get(key);
		assertNotNull(cfBinding);
		assertEquals(key, cfBinding.getKey());
		assertSame(Scopes.SINGLETON, cfBinding.getScope());
		// Reuesting the component factory binding should retrieve the component factory itself.
		assertSame(cf, cfBinding.apply(cf));
		// Repeating the request should retrieve the same instance. (Scope SINGLETON.)
		assertSame(cf, cfBinding.apply(cf)); 
	}

}
