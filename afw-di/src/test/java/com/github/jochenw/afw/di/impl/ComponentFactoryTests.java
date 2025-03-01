package com.github.jochenw.afw.di.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.di.api.Binder;
import com.github.jochenw.afw.di.api.ComponentFactoryBuilder;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.ILifecycleController;
import com.github.jochenw.afw.di.api.IOnTheFlyBinder;
import com.github.jochenw.afw.di.api.LinkableBindingBuilder;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.api.Module;
import com.github.jochenw.afw.di.api.PropInject;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.api.Types;


/** A helper class for testing implementations of {@link IComponentFactory}.
 */
public class ComponentFactoryTests {
	/** A test class, which is being instantiated by the component factory,
	 * with various fields properly filled.
	 */
	@SuppressWarnings("rawtypes")
	public static class CreateJavaxMapsObject {
		private @Inject @Named(value="hash") Map hashMap1;
		private @Inject @Named(value="hash") Map hashMap2;
		private @Inject @Named(value="linked") Map linkedMap1;
		private @Inject @Named(value="linked") Map linkedMap2;
		private @Inject @Named(value="empty") Map emptyMap1;
		private @Inject @Named(value="empty") Map emptyMap2;
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
		final Module module = (b) -> {
			b.bind(Map.class, "hash").toInstance(hashMap);
			b.bind(Map.class, "linked").to(LinkedHashMap.class);
			b.bind(Map.class).to(HashMap.class).in(Scopes.SINGLETON);
			b.bind(Map.class, "empty").toSupplier(() -> new Hashtable<>());
			b.bind(CreateJavaxMapsObject.class).in(Scopes.SINGLETON);
			b.bind(SpareTire.class).in(Scopes.SINGLETON);
		};
		final IComponentFactory cf = IComponentFactory.builder().javax()
				.module(module).type(pType).build();
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
		final @NonNull CreateJavaxMapsObject cmo = cf.requireInstance(CreateJavaxMapsObject.class);
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
		private @jakarta.inject.Inject @Named(value="hash") Map hashMap1;
		private @jakarta.inject.Inject @Named(value="hash") Map hashMap2;
		private @jakarta.inject.Inject @Named(value="linked") Map linkedMap1;
		private @jakarta.inject.Inject @Named(value="linked") Map linkedMap2;
		private @jakarta.inject.Inject @Named(value="empty") Map emptyMap1;
		private @jakarta.inject.Inject @Named(value="empty") Map emptyMap2;
		private @jakarta.inject.Inject Map map1;
		private @jakarta.inject.Inject Map map2;
	}

	/** A test method, which tests proper instantiation, and injection of a
	 * {@link CreateJavaxMapsObject}.
	 * @param pType Type of the component factory, that is being tested.
	 */
	@SuppressWarnings("unchecked")
	public static void testCreateJakartaMaps(Class<? extends AbstractComponentFactory> pType) {
		final Map<String,Object> hashMap = new HashMap<>();
		final Module module = (b) -> {
			b.bind(Map.class, "hash").toInstance(hashMap);
			b.bind(Map.class, "linked").to(LinkedHashMap.class);
			b.bind(Map.class).to(HashMap.class).in(Scopes.SINGLETON);
			b.bind(Map.class, "empty").toSupplier(() -> new Hashtable<>());
			b.bind(CreateJakartaMapsObject.class).in(Scopes.SINGLETON);
			b.bind(SpareTire.class).in(Scopes.SINGLETON);
		};
		final IComponentFactory cf = IComponentFactory.builder().javax()
				.module(module).type(pType).build();
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
		final Module module = new Module() {
			@Override
			public void configure(@NonNull Binder pBinder) {
				pBinder.bind(Car.class).to(Convertible.class);
				pBinder.bind(Seat.class).in(Scopes.SINGLETON);
				pBinder.bind(Seat.class).annotatedWith(Drivers.class).to(DriversSeat.class);
				pBinder.bind(Engine.class).to(V8Engine.class);
				pBinder.bind(Cupholder.class);
				pBinder.bind(Tire.class);
				pBinder.bind(Tire.class, "spare").to(SpareTire.class);
				pBinder.bind(SpareTire.class);
				pBinder.bind(FuelTank.class);
				if (pStaticInjection) {
					pBinder.requestStaticInjection(Convertible.class, Tire.class, SpareTire.class);
				}
			}
		};
		final IComponentFactory cf = new ComponentFactoryBuilder().type(pType).module(module).build();
		Tck.testsFor(cf.requireInstance(Car.class), pStaticInjection, true);
	}

	/** A method for testing, whether a module can override a previous modules bindings.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testModuleOverrides(Class<? extends AbstractComponentFactory> pType) {
		final Object overwrittenInstance = new Object();
		final Object overwritingInstance = new Object();
		final Module overwrittenModule = (b) -> {
			b.bind(Object.class).toInstance(overwrittenInstance);
		};
		final Module overwritingModule = (b) -> {
			b.bind(Object.class).toInstance(overwritingInstance);
		};
		final IComponentFactory cf1 = new ComponentFactoryBuilder().type(pType)
				.module(overwrittenModule).build();
		final IComponentFactory cf2 = new ComponentFactoryBuilder().type(pType)
				.module(overwrittenModule)
				.module(overwritingModule).build();
		assertSame(overwrittenInstance, cf1.requireInstance(Object.class));
		assertSame(overwritingInstance, cf2.requireInstance(Object.class));
	}

	/** Test class for dynamic binding, aka using the {@link IOnTheFlyBinder}.
	 */
	public static class DynmicallyBindableComponent {
		private boolean started;
		private boolean stopped;
		private @PropInject(id="myProperty") String myProperty;
		private @PropInject String otherProperty;
		private @LogInject(id="myLogger") Logger myLogger;
		private @LogInject() Logger otherLogger;

		/** Called to start the component.
		 */
		@PostConstruct
		public void start() {
			started = true;
		}

		/** Called to stop the component.
		 */
		@PreDestroy
		public void shutdown() {
			stopped = true;
		}

		/** Returns, whether the component has been started.
		 * @return True, if {@link #start()} has been invoked.
		 *   Otherwise false.
		 */
		public boolean isStopped() {
			return stopped;
		}

		/** Returns, whether the component has been stopped.
		 * @return True, if {@link #shutdown()} has been invoked.
		 *   Otherwise false.
		 */
		public boolean isStarted() {
			return started;
		}

		/** Returns the value of the "myProperty" attribute, which has
		 * been configured by the component factory.
		 * @return The value of the "myProperty" attribute.
		 */
		public String getMyProperty() { return myProperty; }
		/** Returns the value of the "otherProperty" attribute, which has
		 * been configured by the component factory.
		 * @return The value of the "otherProperty" attribute.
		 */
		public String getOtherProperty() { return otherProperty; }
		/** Returns the value of the "myLogger" attribute, which has
		 * been configured by the component factory.
		 * @return The value of the "myLogger" attribute.
		 */
		public Logger getMyLogger() { return myLogger; }
		/** Returns the value of the "otherLogger" attribute, which has
		 * been configured by the component factory.
		 * @return The value of the "otherLogger" attribute.
		 */
		public Logger getOtherLogger() { return otherLogger; }
	}

	/** A method for testing, whether the {@link IOnTheFlyBinder} works,
	 * as expected.
	 * @param pComponentFactoryType Type of the component factory, that is being tested.
	 */
	public static void testOnTheFlyBinder(Class<? extends AbstractComponentFactory> pComponentFactoryType) {
		// Check the state of a DynamicallyBindableComponent, that isn't properly initialized.
		final DynmicallyBindableComponent dbc0 = new DynmicallyBindableComponent();
		assertFalse(dbc0.isStarted());
		assertFalse(dbc0.isStopped());
		assertNull(dbc0.getMyProperty());
		assertNull(dbc0.getOtherProperty());
		assertNull(dbc0.getMyProperty());
		assertNull(dbc0.getMyLogger());
		assertNull(dbc0.getOtherLogger());
		final Module module = (b) -> {
			b.bind(DynmicallyBindableComponent.class).in(Scopes.SINGLETON);
			b.bind(ILifecycleController.class).to(DefaultLifecycleController.class).in(Scopes.SINGLETON);
			b.addFinalizer((cf) -> {
				cf.requireInstance(ILifecycleController.class).start();
			});
		};
		final IComponentFactory cf1 = new ComponentFactoryBuilder().type(pComponentFactoryType)
				.onTheFlyBinder(new DefaultOnTheFlyBinder(){
					@Override
					protected Object getLogger(IComponentFactory pFactory, Class<?> pType, String pId) {
						return Logger.getLogger(pId);
					}

					@Override
					protected Object getProperty(@NonNull IComponentFactory pFactory, @NonNull Class<?> pType, @NonNull String pId,
							                     @NonNull String pDefaultValue, boolean pNullable) {
						assertSame(String.class, pType);
						switch(pId) {
						  case "myProperty": return "myPropertyValue";
						  case "com.github.jochenw.afw.di.impl.ComponentFactoryTests.DynmicallyBindableComponent.otherProperty":
							  return "otherPropertyValue";
						  default: throw new IllegalStateException("Invalid property key: " + pId);
						}
					}
				})
				.module(module).build();
		// Retrieve another DynamicallyBindableComponent, that is properly initialized.
		final DynmicallyBindableComponent dbc1 = cf1.requireInstance(DynmicallyBindableComponent.class);
		assertTrue(dbc1.isStarted());
		assertFalse(dbc1.isStopped());
		assertEquals("myPropertyValue", dbc1.getMyProperty());
		assertEquals("otherPropertyValue", dbc1.getOtherProperty());
		assertEquals("myLogger", dbc1.getMyLogger().getName());
		assertEquals("com.github.jochenw.afw.di.impl.ComponentFactoryTests.DynmicallyBindableComponent", dbc1.getOtherLogger().getName());
		cf1.requireInstance(ILifecycleController.class).shutdown();
		assertTrue(dbc1.isStopped());
		assertFalse(dbc0.isStarted());
		assertFalse(dbc0.isStopped());
	}

	/** Test for {@link Module#extend(Module)}.
	 * @param pComponentFactoryType Type of the component factory, that is being tested.
	 */
	public static void testModuleExtension(Class<? extends AbstractComponentFactory> pComponentFactoryType) {
		final Map<String,Object> hashMap = new HashMap<>();
		final Module module0 = (b) -> {
			b.bind(Map.class).toInstance(hashMap);
		};
		final Module module1 = (b) -> {
			b.bind(Map.class, "hash").toInstance(hashMap);
		};
		assertSame(module0, module0.extend((Module) null));
		assertSame(module0, module0.extend((Module[]) null));
		assertSame(module0, module0.extend((Iterable<Module>) null));
		final IComponentFactory cf0 = IComponentFactory.builder().module(module0)
				.type(pComponentFactoryType).build();
		final Consumer<@NonNull Module> validator = (m) -> {
			final IComponentFactory cf = IComponentFactory.builder().type(pComponentFactoryType).module(m).build();
			assertSame(hashMap, cf0.requireInstance(Map.class));
			assertSame(hashMap, cf.requireInstance(Map.class));
			assertNull(cf0.getInstance(Map.class, "hash"));
			assertSame(hashMap, cf.requireInstance(Map.class, "hash"));
		};
		validator.accept(module0.extend(module1));
		validator.accept(module0.extend(new @Nullable Module @Nullable [] {module1}));
		validator.accept(module0.extend(Arrays.asList(module1)));
	}

	/** Test for {@link LinkableBindingBuilder#to(Function)}.
	 * @param pComponentFactoryType Type of the component factory, whivh is being tested.
	 */
	public static void testBindToFunction(Class<? extends AbstractComponentFactory> pComponentFactoryType) {
		final Map<String,Object> hashMap = new HashMap<>();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		final Map[] array = (Map<String,Object>[]) Array.newInstance(Map.class, 1);
		@SuppressWarnings("null")
		final Module module = (b) -> {
			b.bind(Map.class).toInstance(hashMap);
			@SuppressWarnings("rawtypes")
			final Function<IComponentFactory,Map[]> arrayCreator = (cf) -> {
				@SuppressWarnings("unchecked")
				Map<String,Object> map = (Map<String,Object>) cf.requireInstance(Map.class);
				array[0] = map;
				@SuppressWarnings("rawtypes")
				final Map[] arr = array;
				return arr;
			};
			b.bind(Map[].class).to(arrayCreator);
		};
		final IComponentFactory cf = IComponentFactory.builder().type(pComponentFactoryType).module(module).build();
		assertNull(array[0]);
		@SuppressWarnings("unchecked")
		final Map<String,Object>[] arr = cf.requireInstance(Map[].class);
		assertNotNull(arr);
		assertSame(hashMap, arr[0]);
		assertSame(array, arr);
		assertNotNull(array[0]);
	}

	/** Test injecting a generic object.
	 * @param pComponentFactoryType Type of the component factory, whivh is being tested.
	 */
	public static void testGenerics(Class<? extends AbstractComponentFactory> pComponentFactoryType) {
		final List<String> list = new ArrayList<>();
		final IComponentFactory cf = IComponentFactory.builder().jakarta().type(pComponentFactoryType).module((b) -> {
			b.bind(Types.of()).toInstance(list);
			b.bind(ListWrapper.class);
		}).build();
		final ListWrapper listWrapper = cf.requireInstance(ListWrapper.class);
		assertNotNull(listWrapper);
		assertSame(list, listWrapper.getList());
	}

	/** Test class for {@link #testGenerics}.
	 */
	private static class ListWrapper {
		private @jakarta.inject.Inject List<String> list;
		public List<String> getList() { return list; }
	}
}
