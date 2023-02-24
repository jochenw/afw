package com.github.jochenw.afw.di.api;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
		/** Creates a new instance. The created instance will invoke
		 * the given {@link Supplier module supplier} to acquire a
		 * {@link IComponentFactory component factory}.
		 * @param pModuleSupplier The {@link Supplier module supplier},
		 *   which is being invoked to acquire a
		 *   {@link IComponentFactory component factory}.
		 */
		protected InvalidApplicationClass(Supplier<Module> pModuleSupplier) {
			super(pModuleSupplier);
			throw new IllegalStateException("Not implemented");
		}

		/** Creates a new instance. The created instance will invoke
		 * the given {@link ComponentFactorySupplier component factory
		 * provider} to acquire a {@link IComponentFactory component factory}.
		 * @param pComponentFactoryProvider The {@link ComponentFactorySupplier
		 * component factory provider}, which is being invoked to acquire a
		 *   {@link IComponentFactory component factory}.
		 */
		protected InvalidApplicationClass(ComponentFactorySupplier pComponentFactoryProvider) {
			super(pComponentFactoryProvider);
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

	/** Tests, whether the given application's component factory
	 * works as expected.
	 * @param pApplication The application, that is being tested.
	 * @param pNoApplication False, if the test includes a check,
	 *   that the component factory has a binding for the
	 *   {@link Application} itself. Otherwise true.
	 */
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

	/** A test class for testing {@code @PostConstruct},
	 *   and {@code PreDestroy}.
	 */
	public static class Startable {
		private boolean started, stopped;

		/** Called to start the instance.
		 */
		@PostConstruct
		public void start() { started = true; }
		/** Returns, whether the instance has been started.
		 * @return True, if {@link #start()} has been invoked.
		 */
		public boolean isStarted() { return started; }
		/** Called to stop the instance.
		 */
		@PreDestroy
		public void stop() { stopped = true; }
		/** Returns, whether the instance has been stopped.
		 * @return True, if {@link #stop()} has been invoked.
		 */
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
