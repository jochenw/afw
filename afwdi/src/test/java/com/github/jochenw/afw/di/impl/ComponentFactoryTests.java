package com.github.jochenw.afw.di.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

import com.github.jochenw.afw.di.api.ComponentFactoryBuilder;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactory.IBinding;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.Scopes;


/** Implementation of various tests for the component factories.
 */
public class ComponentFactoryTests {
	/** A test class, which is being instantiated by the component factory,
	 * with various fields properly filled.
	 */
	@SuppressWarnings("rawtypes")
	public static class CreateJavaxMapsObject {
		private @javax.inject.Inject @javax.inject.Named(value="hash") Map hashMap1;
		private @javax.inject.Inject @javax.inject.Named(value="hash") Map hashMap2;
		private @javax.inject.Inject @javax.inject.Named(value="linked") Map linkedMap1;
		private @javax.inject.Inject @javax.inject.Named(value="linked") Map linkedMap2;
		private @javax.inject.Inject @javax.inject.Named(value="empty") Map emptyMap1;
		private @javax.inject.Inject @javax.inject.Named(value="empty") Map emptyMap2;
		private @Inject Map map1;
		private @Inject Map map2;
	}

	/** A test method, which tests proper instantiation, and injection of a
	 * {@link CreateJavaxMapsObject}.
	 * @param pType Type of the component factory, that is being tested.
	 */
	@SuppressWarnings("unchecked")
	public static void testCreateJavaxMaps(Class<? extends AbstractComponentFactory> pType) {
		final Map<String,Object> hashMap = new HashMap<>();
		final IModule module = (b) -> {
			b.bind(Map.class, "hash").toInstance(hashMap);
			b.bind(Map.class, "linked").toClass(LinkedHashMap.class);
			b.bind(Map.class).toClass(HashMap.class).in(Scopes.SINGLETON);
			b.bind(Map.class, "empty").toSupplier(() -> new Hashtable<>());
			b.bind(CreateJavaxMapsObject.class).in(Scopes.SINGLETON);
			b.bind(SpareTire.class).in(Scopes.SINGLETON);
		};
		final IComponentFactory cf = IComponentFactory.builder(pType).javax()
				.module(module).build();
		final Map<String,Object> hashMapCf1 = cf.getInstance(Map.class, "hash");
		assertNotNull(hashMapCf1);
		assertSame(hashMap, hashMapCf1);
		final Map<String,Object> hashMapCf2 = cf.getInstance(Map.class, "hash");
		assertSame(hashMap, hashMapCf2);
		final Map<String,Object> linkedMapCf1 = cf.getInstance(Map.class, "linked");
		assertNotNull(linkedMapCf1);
		final Map<String,Object> linkedMapCf2 = cf.getInstance(Map.class, "linked");
		assertNotSame(linkedMapCf1, linkedMapCf2);
		final Map<String,Object> mapCf1 = cf.getInstance(Map.class);
		assertNotNull(mapCf1);
		final Map<String,Object> mapCf2 = cf.getInstance(Map.class);
		assertSame(mapCf1, mapCf2);
		final Map<String,Object> emptyMapCf1 = cf.getInstance(Map.class, "empty");
		assertNotNull(emptyMapCf1);
		final Map<String,Object> emptyMapCf2 = cf.getInstance(Map.class, "empty");
		assertNotSame(emptyMapCf1, emptyMapCf2);
		final CreateJavaxMapsObject cmo = cf.requireInstance(CreateJavaxMapsObject.class);
		assertSame(hashMap, cmo.hashMap1);
		assertSame(hashMap, cmo.hashMap2);
		assertNotNull(cmo.linkedMap1);
		assertTrue(cmo.linkedMap1 instanceof LinkedHashMap);
		assertNotNull(cmo.emptyMap1);
		assertFalse(cmo.emptyMap1 instanceof HashMap);
		assertFalse(cmo.emptyMap2 instanceof LinkedHashMap);
		assertNotSame(cmo.emptyMap1, cmo.emptyMap2);
		assertNotNull(cmo.map1);
		assertTrue(cmo.map1 instanceof HashMap);
		assertSame(cmo.map1, cmo.map2);
	}

	/** A test class, which is being instantiated by the component factory,
	 * with various fields properly filled.
	 */
	@SuppressWarnings("rawtypes")
	public static class CreateJakartaMapsObject {
		private @jakarta.inject.Inject @jakarta.inject.Named(value="hash") Map hashMap1;
		private @jakarta.inject.Inject @jakarta.inject.Named(value="hash") Map hashMap2;
		private @jakarta.inject.Inject @jakarta.inject.Named(value="linked") Map linkedMap1;
		private @jakarta.inject.Inject @jakarta.inject.Named(value="linked") Map linkedMap2;
		private @jakarta.inject.Inject @jakarta.inject.Named(value="empty") Map emptyMap1;
		private @jakarta.inject.Inject @jakarta.inject.Named(value="empty") Map emptyMap2;
		private @jakarta.inject.Inject Map map1;
		private @jakarta.inject.Inject Map map2;
	}

	/** A test method, which tests the creation of bindings.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testCreateJakartaMapBindings(Class<? extends AbstractComponentFactory> pType) {
		final Map<String,Object> hashMap = new HashMap<>();
		final IModule module = createJakartaMapsModule(hashMap);
		final IComponentFactory cf = builder(pType, module).build();
		final Map<Key<Object>,IBinding<Object>> bindings = cf.getBindings();
		assertEquals(6, bindings.size());
		assertBinding(bindings, Key.of(IComponentFactory.class, ""), Scopes.SINGLETON);
		assertBinding(bindings, Key.of(Map.class, "hash"), Scopes.SINGLETON);
		assertBinding(bindings, Key.of(Map.class, "linked"), Scopes.NO_SCOPE);
		assertBinding(bindings, Key.of(Map.class, ""), Scopes.SINGLETON);
		assertBinding(bindings, Key.of(Map.class, "empty"), Scopes.NO_SCOPE);
		assertBinding(bindings, Key.of(CreateJakartaMapsObject.class, ""), Scopes.SINGLETON);
		
	}

	/** Asserts, that the given set of bindings contains an entry with the given key, and scope.
	 * @param pBindings The set of bindings, that are being tested.
	 * @param pKey Key of the binding, that is being tested.
	 * @param pScope The scope, that the requested binding is supposed to have.
	 */
	static void assertBinding(Map<Key<Object>,IBinding<Object>> pBindings, Key<?> pKey, Scopes.Scope pScope) {
		@SuppressWarnings("unchecked")
		final Key<Object> key = (Key<Object>) Objects.requireNonNull(pKey, "Key");
		final Scopes.Scope scope = Objects.requireNonNull(pScope, "Scope");
		final IBinding<Object> binding = pBindings.get(key);
		assertNotNull(binding);
		assertEquals(key, binding.getKey());
		assertEquals(scope.name(), binding.getScope().name());
	}

	private static ComponentFactoryBuilder<? extends AbstractComponentFactory> builder(
			Class<? extends AbstractComponentFactory> pType, final IModule module) {
		return IComponentFactory.builder(pType).jakarta()
				.defaultScope(Scopes.NO_SCOPE)
				.module(module);
	}
	/** A test method, which tests proper instantiation, and injection of a
	 * {@link CreateJavaxMapsObject}.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testCreateJakartaMaps(Class<? extends AbstractComponentFactory> pType) {
		final Map<String,Object> hashMap = new HashMap<>();
		final IModule module = createJakartaMapsModule(hashMap);
		final IComponentFactory cf = IComponentFactory.builder(pType).jakarta()
				.defaultScope(Scopes.NO_SCOPE)
				.module(module).build();
		final Map<String,Object> hashMapCf1 = cf.getInstance(Map.class, "hash");
		assertNotNull(hashMapCf1);
		assertSame(hashMap, hashMapCf1);
		final Map<String,Object> hashMapCf2 = cf.getInstance(Map.class, "hash");
		assertSame(hashMap, hashMapCf2);
		final Map<String,Object> linkedMapCf1 = cf.getInstance(Map.class, "linked");
		assertNotNull(linkedMapCf1);
		final Map<String,Object> linkedMapCf2 = cf.getInstance(Map.class, "linked");
		assertNotSame(linkedMapCf1, linkedMapCf2);
		final Map<String,Object> mapCf1 = cf.getInstance(Map.class);
		assertNotNull(mapCf1);
		final Map<String,Object> mapCf2 = cf.getInstance(Map.class);
		assertSame(mapCf1, mapCf2);
		final Map<String,Object> emptyMapCf1 = cf.getInstance(Map.class, "empty");
		assertNotNull(emptyMapCf1);
		final Map<String,Object> emptyMapCf2 = cf.getInstance(Map.class, "empty");
		assertNotSame(emptyMapCf1, emptyMapCf2);
		final CreateJakartaMapsObject cmo = cf.requireInstance(CreateJakartaMapsObject.class);
		assertSame(hashMap, cmo.hashMap1);
		assertSame(hashMap, cmo.hashMap2);
		assertNotNull(cmo.linkedMap1);
		assertTrue(cmo.linkedMap1 instanceof LinkedHashMap);
		assertNotNull(cmo.emptyMap1);
		assertFalse(cmo.emptyMap1 instanceof HashMap);
		assertFalse(cmo.emptyMap2 instanceof LinkedHashMap);
		assertNotSame(cmo.emptyMap1, cmo.emptyMap2);
		assertNotNull(cmo.map1);
		assertTrue(cmo.map1 instanceof HashMap);
		assertSame(cmo.map1, cmo.map2);
	}

	private static IModule createJakartaMapsModule(final Map<String, Object> hashMap) {
		final IModule module = (b) -> {
			b.bind(Map.class, "hash").toInstance(hashMap);
			b.bind(Map.class, "linked").toClass(LinkedHashMap.class);
			b.bind(Map.class).toClass(HashMap.class).in(Scopes.SINGLETON);
			b.bind(Map.class, "empty").toSupplier(() -> new Hashtable<>());
			b.bind(CreateJakartaMapsObject.class).in(Scopes.SINGLETON);
		};
		return module;
	}

	/** A test class, which is used to test proper injection of the component factory
	 * itself in a child component factory.
	 */
	public static class TestParentObject {
		private @Inject IComponentFactory componentFactory;
	}

	/** A test method, which tests proper instantiation, and injection of a
	 * {@link TestParentObject}.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testParent(Class<? extends AbstractComponentFactory> pType) {
		final IModule module = (b) -> {
			b.bind(TestParentObject.class);
		};
		final IComponentFactory parentCf =
				IComponentFactory.builder(pType)
				.module(module)
				.build();
		assertNotNull(parentCf);
		IComponentFactory parentInstance = parentCf.getInstance(IComponentFactory.class);
		assertNotNull(parentInstance);
		assertSame(parentCf, parentInstance);
		final TestParentObject tpo1 = parentCf.getInstance(TestParentObject.class);
		assertNotNull(tpo1);
		assertNotNull(tpo1.componentFactory);
		assertSame(tpo1.componentFactory, parentCf);
		final IComponentFactory cf =
				IComponentFactory.builder(pType)
				.module(module)
				.build();
		assertNotNull(cf);
		assertNotSame(parentCf, cf);
		IComponentFactory instance = cf.getInstance(IComponentFactory.class);
		assertSame(cf, instance);
		final TestParentObject tpo2 = cf.getInstance(TestParentObject.class);
		assertNotNull(tpo2);
		assertNotSame(tpo1, tpo2);
		assertNotNull(tpo2.componentFactory);
		assertSame(cf, tpo2.componentFactory);
	}

	/** A test method, which runs the Java Inject TCK on the component factory.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testTck(Class<? extends AbstractComponentFactory> pType) {
		testTck(pType, true);
	}

	private static void testTck(Class<? extends AbstractComponentFactory> pType, boolean pStaticInjection) {
		final IModule module = new IModule() {
			@Override
			public void configure(IBinder pBinder) {
				pBinder.bind(Car.class).toClass(Convertible.class);
				pBinder.bind(Seat.class).in(Scopes.SINGLETON);
				pBinder.bind(Seat.class).annotatedWith(Drivers.class).toClass(DriversSeat.class);
				pBinder.bind(Engine.class).toClass(V8Engine.class);
				pBinder.bind(Cupholder.class);
				pBinder.bind(Tire.class);
				pBinder.bind(Tire.class, "spare").toClass(SpareTire.class);
				pBinder.bind(SpareTire.class);
				pBinder.bind(FuelTank.class);
				if (pStaticInjection) {
					pBinder.staticInjection(Convertible.class, Tire.class, SpareTire.class);
				}
			}
		};
		final IComponentFactory cf = IComponentFactory.builder(pType).jakarta().module(module).build();
		Tck.testsFor(cf.requireInstance(Car.class), pStaticInjection, true);
	}

	/** A method for testing, whether a module can override a previous modules bindings.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testModuleOverrides(Class<? extends AbstractComponentFactory> pType) {
		final Object overwrittenInstance = new Object();
		final Object overwritingInstance = new Object();
		final IModule overwrittenModule = (b) -> {
			b.bind(Object.class).toInstance(overwrittenInstance);
		};
		final IModule overwritingModule = (b) -> {
			b.bind(Object.class).toInstance(overwritingInstance);
		};
		final IComponentFactory cf1 = IComponentFactory.builder(pType)
				.module(overwrittenModule).build();
		final IComponentFactory cf2 = IComponentFactory.builder(pType)
				.module(overwrittenModule)
				.module(overwritingModule).build();
		assertSame(overwrittenInstance, cf1.requireInstance(Object.class));
		assertSame(overwritingInstance, cf2.requireInstance(Object.class));
		final IComponentFactory cf3 = IComponentFactory.builder(pType)
				.parent(cf1).module(overwritingModule).build();
		assertSame(overwrittenInstance, cf1.requireInstance(Object.class));
		assertSame(overwritingInstance, cf3.requireInstance(Object.class));
		assertSame(cf1, cf1.requireInstance(IComponentFactory.class));
		assertSame(cf2, cf2.requireInstance(IComponentFactory.class));
		assertSame(cf3, cf3.requireInstance(IComponentFactory.class));
	}
}
