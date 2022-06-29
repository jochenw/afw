package com.github.jochenw.afw.di.api;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Provider;

import org.junit.Test;

public class ApplicationTest {
	public static class MyApplication extends Application {
		public MyApplication(Supplier<Module> pModuleSupplier) {
			super(pModuleSupplier);
		}
		public MyApplication(Provider<IComponentFactory> pComponentFactoryProvider) {
			super(pComponentFactoryProvider);
		}
	}

	/** Test creating an application with a module supplier.
	 */
	@Test
	public void testCreateWithModuleSupplier() {
		final Module module = (b) -> {
			b.bind(Map.class).toClass(HashMap.class);
		};
		final Application appl = Application.of(module);
		validate(appl, false);
	}

	protected void validate(final Application pApplication, boolean pNoApplication) {
		assertNotNull(pApplication);
		final IComponentFactory cf = pApplication.getComponentFactory();
		assertNotNull(cf);
		if (!pNoApplication) {
			assertSame(pApplication, cf.requireInstance(Application.class));
		}
		@SuppressWarnings("unchecked")
		final Map<String,Object> map1 = cf.requireInstance(Map.class);
		@SuppressWarnings("unchecked")
		final Map<String,Object> map2 = cf.requireInstance(Map.class);
		assertTrue(map1 instanceof HashMap);
		assertTrue(map2 instanceof HashMap);
		assertNotSame(map1, map2);
	}

	/** Test creating an application with a component factory provider.
	 */
	@Test
	public void testCreateWithComponentFactoryProvider() {
		final Module module = (b) -> {
			b.bind(Map.class).toClass(HashMap.class);
		};
		final Provider<IComponentFactory> provider = () -> {
			return new ComponentFactoryBuilder().module(module).build();
		};
		final Application appl = Application.of(provider);
		validate(appl, true);
	}

	/** Test creating a subclass with a module supplier.
	 */
	@Test
	public void testCreateSubclassWithModuleSupplier() {
		final Module module = (b) -> {
			b.bind(Map.class).toClass(HashMap.class);
		};
		final MyApplication appl = Application.of(MyApplication.class, module);
		validate(appl, false);
	}

	/** Test creating a subclass with a component factory provider.
	 */
	@Test
	public void testCreateSubclassWithComponentFactoryProvider() {
		final Module module = (b) -> {
			b.bind(Map.class).toClass(HashMap.class);
		};
		final Provider<IComponentFactory> provider = () -> {
			return new ComponentFactoryBuilder().module(module).build();
		};
		final MyApplication appl = Application.of(MyApplication.class, provider);
		validate(appl, true);
	}

}
