package com.github.jochenw.afw.di.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.di.simple.SimpleComponentFactory;

import jakarta.inject.Inject;

class ComponentFactoryBuilderTest {
	public static class MappingSubject {
		private @jakarta.inject.Inject String jakartaInjectedField;
		private @javax.inject.Inject String javaxInjectedField;
		private @com.google.inject.Inject String googleInjectedField;
	}

	@Test
	void testBindings() {
		final SimpleComponentFactory cf = IComponentFactory.builder()
				.module((b) -> {
					
				})
				.build();
	}

}
