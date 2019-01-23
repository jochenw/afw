package com.github.jochenw.afw.core.inject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Assert;

import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Binder;
import com.github.jochenw.afw.core.inject.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.inject.Types.Type;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;

import junit.framework.Test;

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


public class InjectTests {
	public static class Component {
		@Inject
		private IComponentFactory componentFactory;
		@Inject
		private List<Component> list;
		private Map<String,Object> map1;
		private Map<String,Object> map2;
		@LogInject
		private ILog log;
		@LogInject(id="com.github.jochenw.afw.core.components.SomeComponent")
		private ILog log2;
		@LogInject
		private ILog log3;
	
		public @Inject void init(@Named(value="1") Map<String,Object> pMap1, Map<String,Object> pMap2) {
			map1 = pMap1;
			map2 = pMap2;
		}
	}

	public static void testComponentFactory(ComponentFactoryBuilder pComponentFactoryBuilder) {
		final Map<String,Object> map1 = new HashMap<>();
		final Map<String,Object> map2 = new HashMap<>();
		final ILogFactory logFactory = new SimpleLogFactory();
		pComponentFactoryBuilder.module(new Module() {
			@Override
			public void configure(Binder pBinder) {
				final Type<List<InjectTests.Component>> componentListType = new Type<List<InjectTests.Component>>() {};
				pBinder.bind(componentListType).toClass(ArrayList.class);
				pBinder.bind(InjectTests.Component.class).in(Scopes.SINGLETON);
				Type<Map<String,Object>> mapType = new Type<Map<String,Object>>(){};
				pBinder.bind(mapType, "1").toInstance(map1);
				final Supplier<Map<String,Object>> supplier = () -> { return map2; };
				pBinder.bind(mapType).toSupplier(supplier);
				pBinder.bind(ILogFactory.class).toInstance(logFactory);
			}
		});
		final IComponentFactory cf = pComponentFactoryBuilder.build();
		Assert.assertNotNull(cf);
		final InjectTests.Component component = cf.requireInstance(InjectTests.Component.class);
		Assert.assertNotNull(cf);
		Assert.assertSame(component, cf.getInstance(InjectTests.Component.class));
		Assert.assertNotNull(cf.requireInstance(IComponentFactory.class));
		Assert.assertSame(cf, cf.getInstance(IComponentFactory.class));
		Assert.assertSame(cf, component.componentFactory);
		Assert.assertNotNull(component.list);
		Assert.assertTrue(component.list instanceof ArrayList);
		Assert.assertSame(map1, component.map1);
		Assert.assertSame(map2, component.map2);
		Assert.assertNotNull(component.log);
		Assert.assertEquals(InjectTests.Component.class.getName(), component.log.getId());
		Assert.assertNotNull(component.log2);
		Assert.assertEquals("com.github.jochenw.afw.core.components.SomeComponent", component.log2.getId());
		Assert.assertNotNull(component.log3);
		Assert.assertEquals(InjectTests.Component.class.getName(), component.log3.getId());
		Assert.assertNotSame(component.log3, component.log);
	}

	public static Test testTckCompliance(ComponentFactoryBuilder<?> pComponentFactoryBuilder) {
		final Module module = new Module() {
			@Override
			public void configure(Binder pBinder) {
                pBinder.bind(Car.class).to(Convertible.class);
                pBinder.bind(Engine.class).to(V8Engine.class);
                pBinder.bind(Cupholder.class);
                pBinder.bind(Tire.class);
                pBinder.bind(FuelTank.class);
                pBinder.requestStaticInjection(Convertible.class, SpareTire.class);
				pBinder.bind(Tire.class, "spare").to(SpareTire.class);
                pBinder.bind(Seat.class).annotatedWith(Drivers.class).to(DriversSeat.class);
            }
		};
		final IComponentFactory cf = pComponentFactoryBuilder.module(module).build();
		return Tck.testsFor(cf.requireInstance(Car.class), true, true);
	}
}
