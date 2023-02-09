package com.github.jochenw.afw.di.api;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.inject.Provider;

import org.junit.Test;

import com.github.jochenw.afw.di.api.Application.ComponentFactorySupplier;

/** Test for the {@link Application} class.
 */
public class ApplicationTest {
	/** Subclass of {@link Application}, which allows us to test, whether
	 * {@link Application#of(Class, Module)}, or
	 * {@link Application#of(Class, Supplier)}
	 * can be used to create instances of a subclass.
	 */
	public static class MyApplication extends Application {
		/** Creates a new instance with the given module supplier.
		 * @param pModuleSupplier The module supplier, which will be
		 *   used to create the {@link IComponentFactory}.
		 */
		public MyApplication(Supplier<Module> pModuleSupplier) {
			super(pModuleSupplier);
		}
		/** Creates a new instance with the given component factory.
		 * @param pComponentFactoryProvider The component factory provider.
		 */
		public MyApplication(ComponentFactorySupplier pComponentFactoryProvider) {
			super(pComponentFactoryProvider);
		}
	}
	/** Subclass of {@link Application} without a valid constructor.
	 * This allows us to test, whether {@link Application#of(Class, Module)}
	 * provides an appropriate error message. 
	 */
	public static class InvalidApplicationClass extends Application {
		protected InvalidApplicationClass(Supplier<Module> pModuleSupplier) {
			super(pModuleSupplier);
			throw new IllegalStateException("Not implemented");
		}

		protected InvalidApplicationClass(ComponentFactorySupplier pComponentFctoryProvider) {
			super(pComponentFctoryProvider);
			throw new IllegalStateException("Not implemented");
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
		final ComponentFactorySupplier provider = () -> {
			return new ComponentFactoryBuilder().module(module).build();
		};
		final Application appl = Application.of(provider);
		validate(appl, true);
		try {
			Application.of(InvalidApplicationClass.class, provider);
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Not implemented", e.getMessage());
		}
	}

	/** Test creating an application with a component factory.
	 */
	@Test
	public void testCreateWithComponentFactory() {
		final Module module = (b) -> {
			b.bind(Map.class).toClass(HashMap.class);
		};
		final IComponentFactory cf = new ComponentFactoryBuilder().module(module).build();
		final Application appl = Application.of(cf);
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
		try {
			Application.of(InvalidApplicationClass.class, module);
			fail("Expected Exception");
		} catch (IllegalStateException e) {
			assertEquals("Not implemented", e.getMessage());
		}
	}

	/** Test creating a subclass with a component factory provider.
	 */
	@Test
	public void testCreateSubclassWithComponentFactoryProvider() {
		final Module module = (b) -> {
			b.bind(Map.class).toClass(HashMap.class);
		};
		final ComponentFactorySupplier provider = () -> {
			return new ComponentFactoryBuilder().module(module).build();
		};
		final MyApplication appl = Application.of(MyApplication.class, provider);
		validate(appl, true);
	}

	public static class Startable {
		private boolean started, stopped;

		@PostConstruct
		public void start() { started = true; }
		public boolean isStarted() { return started; }
		@PostConstruct
		public void stop() { stopped = true; }
		public boolean isStopped() { return stopped; }
	}

	/** Test, whether the {@link ILifecycleController} is properly
	 * initialized.
	 */
	public void testLifecycle() {
		final Application app = Application.of((b) -> {
			b.bind(Startable.class).in(Scopes.SINGLETON);
		});
		final ILifecycleController lc = app.getComponentFactory().requireInstance(ILifecycleController.class);
		assertNotNull(lc);
		final Startable startable = app.getComponentFactory().requireInstance(Startable.class);
		assertTrue(startable.isStarted());
		assertFalse(startable.isStopped());
		lc.shutdown();
		assertTrue(startable.isStarted());
		assertTrue(startable.isStopped());
	}
}
