package com.github.jochenw.afw.di.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

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

import com.github.jochenw.afw.di.api.Binder;
import com.github.jochenw.afw.di.api.ComponentFactoryBuilder;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.Module;
import com.github.jochenw.afw.di.api.Scopes;

public class ComponentFactoryTests {
	@SuppressWarnings("rawtypes")
	public static class CreateMapsObject {
		private @Inject @Named(value="hash") Map hashMap1;
		private @Inject @Named(value="hash") Map hashMap2;
		private @Inject @Named(value="linked") Map linkedMap1;
		private @Inject @Named(value="linked") Map linkedMap2;
		private @Inject @Named(value="empty") Map emptyMap1;
		private @Inject @Named(value="empty") Map emptyMap2;
		private @Inject Map map1;
		private @Inject Map map2;
	}

	@SuppressWarnings("unchecked")
	public static void testCreateMaps(Class<? extends AbstractComponentFactory> pType) {
		final Map<String,Object> hashMap = new HashMap<>();
		final Module module = (b) -> {
			b.bind(Map.class, "hash").toInstance(hashMap);
			b.bind(Map.class, "linked").to(LinkedHashMap.class);
			b.bind(Map.class).to(HashMap.class).in(Scopes.SINGLETON);
			b.bind(Map.class, "empty").toSupplier(() -> new Hashtable<>());
			b.bind(CreateMapsObject.class).in(Scopes.SINGLETON);
		};
		final IComponentFactory cf = new ComponentFactoryBuilder().module(module).type(pType).build();
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
		final CreateMapsObject cmo = cf.getInstance(CreateMapsObject.class);
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

	public static class TestParentObject {
		private @Inject IComponentFactory componentFactory;
	}

	public static void testParent(Class<? extends AbstractComponentFactory> pType) {
		final Module module = (b) -> {
			b.bind(TestParentObject.class);
		};
		final IComponentFactory parentCf =
				new ComponentFactoryBuilder()
				.type(pType)
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
				new ComponentFactoryBuilder()
				.type(pType)
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

	public static void testTck(Class<? extends AbstractComponentFactory> pType) {
		testTck(pType, false);
		testTck(pType, true);
	}

	private static void testTck(Class<? extends AbstractComponentFactory> pType, boolean pStaticInjection) {
		final Module module = new Module() {
			@Override
			public void configure(Binder pBinder) {
				pBinder.bind(Car.class).to(Convertible.class);
				pBinder.bind(Seat.class).annotatedWith(Drivers.class).to(DriversSeat.class);
				pBinder.bind(Engine.class).to(V8Engine.class);
				pBinder.bind(Cupholder.class);
				pBinder.bind(Tire.class);
				pBinder.bind(Tire.class, "spare").to(SpareTire.class);
				pBinder.bind(FuelTank.class);
				if (pStaticInjection) {
					pBinder.requestStaticInjection(Convertible.class, SpareTire.class);
				}
			}
		};
		final IComponentFactory cf = new ComponentFactoryBuilder().type(pType).module(module).build();
		Tck.testsFor(cf.requireInstance(Car.class), pStaticInjection, true);
	}
}
