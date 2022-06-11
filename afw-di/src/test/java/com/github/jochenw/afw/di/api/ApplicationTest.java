package com.github.jochenw.afw.di.api;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Test;

public class ApplicationTest {
	public static class MyApplication extends Application {
		public MyApplication(Supplier<Module> pModuleSupplier) {
			super(pModuleSupplier);
		}
	}

	/** Test creating an application.
	 */
	@Test
	public void testCreate() {
		final Module module = (b) -> {
			b.bind(Map.class).toClass(HashMap.class);
		};
		final Application appl = Application.of(module);
		assertNotNull(appl);
		final IComponentFactory cf = appl.getComponentFactory();
		assertNotNull(cf);
		assertSame(appl, cf.requireInstance(Application.class));
		@SuppressWarnings("unchecked")
		final Map<String,Object> map1 = cf.requireInstance(Map.class);
		@SuppressWarnings("unchecked")
		final Map<String,Object> map2 = cf.requireInstance(Map.class);
		assertTrue(map1 instanceof HashMap);
		assertTrue(map2 instanceof HashMap);
		assertNotSame(map1, map2);
	}

	/** Test creating a subclass.
	 */
	@Test
	public void testCreateSubclass() {
		final Module module = (b) -> {
			b.bind(Map.class).toClass(HashMap.class);
		};
		final MyApplication appl = Application.of(MyApplication.class, module);
		assertNotNull(appl);
		final IComponentFactory cf = appl.getComponentFactory();
		assertNotNull(cf);
		assertSame(appl, cf.requireInstance(Application.class));
		@SuppressWarnings("unchecked")
		final Map<String,Object> map1 = cf.requireInstance(Map.class);
		@SuppressWarnings("unchecked")
		final Map<String,Object> map2 = cf.requireInstance(Map.class);
		assertTrue(map1 instanceof HashMap);
		assertTrue(map2 instanceof HashMap);
		assertNotSame(map1, map2);
	}

}
