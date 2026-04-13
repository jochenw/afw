package com.github.jochenw.afw.di.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

import javax.inject.Inject;

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

import com.github.jochenw.afw.di.api.ComponentFactoryBuilder;
import com.github.jochenw.afw.di.api.DefaultLifecycleController;
import com.github.jochenw.afw.di.api.IBindingProvider;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactory.IBinding;
import com.github.jochenw.afw.di.api.ILifecycleController;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.afw.di.api.IModule.LinkableBindingBuilder;
import com.github.jochenw.afw.di.api.LogInjectBindingProvider.LoggerFactory;
import com.github.jochenw.afw.di.api.PropInjectBindingProvider.PropertyFactory;
import com.github.jochenw.afw.di.api.Key;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.api.LogInjectBindingProvider;
import com.github.jochenw.afw.di.api.PropInject;
import com.github.jochenw.afw.di.api.PropInjectBindingProvider;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.afw.di.api.Types;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


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
	public static void testCreateJavaxMaps(Class<IComponentFactory> pType) {
		final Map<String,Object> hashMap = new HashMap<>();
		final IModule module = (b) -> {
			b.bind(Map.class, "hash").toInstance(hashMap);
			b.bind(Map.class, "linked").toClass(LinkedHashMap.class);
			b.bind(Map.class).toClass(HashMap.class).in(Scopes.SINGLETON);
			b.bind(Map.class, "empty").toSupplier(() -> new Hashtable<>());
			b.bind(CreateJavaxMapsObject.class).in(Scopes.SINGLETON);
			b.bind(SpareTire.class).in(Scopes.SINGLETON);
		};
		final Supplier<IComponentFactory> supplier = newSupplier(pType);
		final IComponentFactory cf = IComponentFactory.builder(supplier).javax()
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

	private static Supplier<IComponentFactory> newSupplier(Class<? extends IComponentFactory> pType) {
		return () -> {
			try {
				return pType.getConstructor().newInstance();
			} catch (Exception e) {
				throw DiUtils.show(e);
			}
		};
	}

	/** A test class, which is being instantiated by the component factory,
	 * with various fields properly filled.
	 */
	@SuppressWarnings("rawtypes")
	public static class CreateJakartaMapsObject {
		@SuppressWarnings("javadoc")
		public @jakarta.inject.Inject @jakarta.inject.Named(value="hash") Map hashMap1;
		@SuppressWarnings("javadoc")
		public @jakarta.inject.Inject @jakarta.inject.Named(value="hash") Map hashMap2;
		@SuppressWarnings("javadoc")
		public @jakarta.inject.Inject @jakarta.inject.Named(value="linked") Map linkedMap1;
		@SuppressWarnings("javadoc")
		public @jakarta.inject.Inject @jakarta.inject.Named(value="linked") Map linkedMap2;
		@SuppressWarnings("javadoc")
		public @jakarta.inject.Inject @jakarta.inject.Named(value="empty") Map emptyMap1;
		@SuppressWarnings("javadoc")
		public @jakarta.inject.Inject @jakarta.inject.Named(value="empty") Map emptyMap2;
		@SuppressWarnings("javadoc")
		public @jakarta.inject.Inject Map map1;
		@SuppressWarnings("javadoc")
		public @jakarta.inject.Inject Map map2;
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

	private static ComponentFactoryBuilder<? extends IComponentFactory> builder(
			Class<? extends AbstractComponentFactory> pType, final IModule module) {
		final Supplier<IComponentFactory> supplier = newSupplier(pType);
		return IComponentFactory.builder(supplier).jakarta()
				.defaultScope(Scopes.NO_SCOPE)
				.module(module);
	}
	/** A test method, which tests proper instantiation, and injection of a
	 * {@link CreateJavaxMapsObject}.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testCreateJakartaMaps(Class<? extends IComponentFactory> pType) {
		final Map<String,Object> hashMap = new HashMap<>();
		final IModule module = createJakartaMapsModule(hashMap);
		final Supplier<IComponentFactory> supplier = newSupplier(pType);
		final IComponentFactory cf = IComponentFactory.builder(supplier).jakarta()
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
	public static void testParent(Class<? extends IComponentFactory> pType) {
		final IModule module = (b) -> {
			b.bind(TestParentObject.class);
		};
		final Supplier<IComponentFactory> supplier = newSupplier(pType);
		final IComponentFactory parentCf =
				IComponentFactory.builder(supplier)
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
				IComponentFactory.builder(supplier)
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

	private static void testTck(Class<? extends IComponentFactory> pType, boolean pStaticInjection) {
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
		final Supplier<IComponentFactory> supplier = newSupplier(pType);
		final IComponentFactory cf = IComponentFactory.builder(supplier).jakarta().module(module).build();
		Tck.testsFor(cf.requireInstance(Car.class), pStaticInjection, true);
	}

	/** A method for testing, whether a module can override a previous modules bindings.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testModuleOverrides(Class<? extends IComponentFactory> pType) {
		final Object overwrittenInstance = new Object();
		final Object overwritingInstance = new Object();
		final IModule overwrittenModule = (b) -> {
			b.bind(Object.class).toInstance(overwrittenInstance);
		};
		final IModule overwritingModule = (b) -> {
			b.bind(Object.class).toInstance(overwritingInstance);
		};
		final Supplier<IComponentFactory> supplier = newSupplier(pType);
		final IComponentFactory cf1 = IComponentFactory.builder(supplier)
				.module(overwrittenModule).build();
		final IComponentFactory cf2 = IComponentFactory.builder(supplier)
				.module(overwrittenModule)
				.module(overwritingModule).build();
		assertSame(overwrittenInstance, cf1.requireInstance(Object.class));
		assertSame(overwritingInstance, cf2.requireInstance(Object.class));
		final IComponentFactory cf3 = IComponentFactory.builder(supplier)
				.parent(cf1).module(overwritingModule).build();
		assertSame(overwrittenInstance, cf1.requireInstance(Object.class));
		assertSame(overwritingInstance, cf3.requireInstance(Object.class));
		assertSame(cf1, cf1.requireInstance(IComponentFactory.class));
		assertSame(cf2, cf2.requireInstance(IComponentFactory.class));
		assertSame(cf3, cf3.requireInstance(IComponentFactory.class));
	}

	/** Test class for dynamic binding, aka using a custom {@link IBindingProvider}.
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

	/** A method for testing, whether the {@link IBindingProvider} works,
	 * as expected.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testCustomBindingProvider(Class<? extends IComponentFactory> pType) {
		final Properties properties = new Properties();
		properties.put("myProperty", "myPropertyValue");
		properties.put(DynmicallyBindableComponent.class.getName() + ".otherProperty", "otherPropertyValue");
		// Check the state of a DynamicallyBindableComponent, that isn't properly initialized.
		final DynmicallyBindableComponent dbc0 = new DynmicallyBindableComponent();
		assertFalse(dbc0.isStarted());
		assertFalse(dbc0.isStopped());
		assertNull(dbc0.getMyProperty());
		assertNull(dbc0.getOtherProperty());
		assertNull(dbc0.getMyProperty());
		assertNull(dbc0.getMyLogger());
		assertNull(dbc0.getOtherLogger());
		final IModule module = (b) -> {
			b.bind(DynmicallyBindableComponent.class).in(Scopes.SINGLETON);
			b.bind(ILifecycleController.class).toClass(DefaultLifecycleController.class).in(Scopes.SINGLETON);
			final PropertyFactory propertyFactory = new PropertyFactory() {
				@Override
				public Object apply(IComponentFactory pComponentFactory, Type pPropertyType, String pPropertyId,
						String pDefaultValue, boolean pNullable) {
					String value = properties.getProperty(pPropertyId);
					if (value == null  ||  value == PropInject.NO_DEFAULT) {
						value = pDefaultValue;
					}
					if (pPropertyType == String.class  ||  pPropertyType.equals(String.class)) {
						return value;
					} else {
						throw new IllegalStateException("Invalid property type: " + pPropertyType);
					}
				}
			};
			final PropInjectBindingProvider<String> propInjectBindingProvider = new PropInjectBindingProvider<>(String.class, propertyFactory);
			final LoggerFactory<Logger> loggerFactory = new LoggerFactory<Logger>() {
				@Override
				public Logger apply(IComponentFactory pComponentFactory, String pLoggerId, String pMName) {
					if (pMName != null  &&  pMName.length() > 0) {
						return Logger.getLogger(pLoggerId + ":" + pMName);
					}
					return Logger.getLogger(pLoggerId);
				}
			};
			final LogInjectBindingProvider<Logger> logInjectBindingProvider = new LogInjectBindingProvider<>(Logger.class, loggerFactory);
			b.bind(PropInjectBindingProvider.class).toInstance(propInjectBindingProvider);
			b.bind(LogInjectBindingProvider.class).toInstance(logInjectBindingProvider);
			b.addFinalizer((cf) -> {
				cf.requireInstance(ILifecycleController.class).start();
			});
		};
		final Supplier<IComponentFactory> supplier = newSupplier(pType);
		final IComponentFactory cf1 = IComponentFactory.builder(supplier)
				.bindingProvider(new DefaultBindingProvider<Logger,String[]>())
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

	/** Test for {@link IModule#extend(IModule)}.
	 * @param pType Type of the component factory, that is being tested.
	 */
	public static void testModuleExtension(Class<? extends IComponentFactory> pType) {
		final Map<String,Object> hashMap = new HashMap<>();
		final IModule module0 = (b) -> {
			b.bind(Map.class).toInstance(hashMap);
		};
		final IModule module1 = (b) -> {
			b.bind(Map.class, "hash").toInstance(hashMap);
		};
		assertSame(module0, module0.extend((IModule) null));
		final IModule[] moduleArray = new IModule[0];
		assertSame(module0, module0.extend(moduleArray));
		final List<IModule> moduleList = Collections.emptyList();
		assertSame(module0, module0.extend(moduleList));
		final Supplier<IComponentFactory> supplier = newSupplier(pType);
		final IComponentFactory cf0 = IComponentFactory.builder(supplier).module(module0)
				.build();
		final Consumer<@NonNull IModule> validator = (m) -> {
			final IComponentFactory cf = IComponentFactory.builder(supplier).module(m).build();
			assertSame(hashMap, cf0.requireInstance(Map.class));
			assertSame(hashMap, cf.requireInstance(Map.class));
			assertNull(cf0.getInstance(Map.class, "hash"));
			assertSame(hashMap, cf.requireInstance(Map.class, "hash"));
		};
		validator.accept(module0.extend(module1));
		validator.accept(module0.extend(new @Nullable IModule @Nullable [] {module1}));
		validator.accept(module0.extend(Arrays.asList(module1)));
	}

	/** Test for {@link LinkableBindingBuilder#toFunction(Function)}.
	 * @param pComponentFactoryType Type of the component factory, whivh is being tested.
	 */
	public static void testBindToFunction(Class<? extends AbstractComponentFactory> pComponentFactoryType) {
		final String mappedValue = "Mapped Value";
		final IModule module = (b) -> {
			b.bind(String.class).toInstance(mappedValue);
			final Function<IComponentFactory,StringBuilder> sbCreator = (cf) -> {
				final String value = cf.requireInstance(String.class);
				return new StringBuilder(value);
			};
			b.bind(StringBuilder.class).toFunction(sbCreator);
		};
		final Supplier<IComponentFactory> supplier = newSupplier(pComponentFactoryType);
		final IComponentFactory cf = IComponentFactory.builder(supplier).module(module).build();
		final StringBuilder sb = cf.requireInstance(StringBuilder.class);
		assertNotNull(sb);
		assertEquals(mappedValue, sb.toString());
	}

	/** Test injecting a generic object.
	 * @param pComponentFactoryType Type of the component factory, whivh is being tested.
	 */
	public static void testGenerics(Class<? extends AbstractComponentFactory> pComponentFactoryType) {
		testGenerics(pComponentFactoryType, Types.TYPE_LIST_STRING);
	}

	private static void testGenerics(Class<? extends AbstractComponentFactory> pComponentFactoryType, Types.Type<List<String>> pListType) {
		final List<String> list = new ArrayList<>();
		final Supplier<IComponentFactory> supplier = newSupplier(pComponentFactoryType);
		final IComponentFactory cf = IComponentFactory.builder(supplier).jakarta().module((b) -> {
			b.bind(pListType).toInstance(list);
			b.bind(ListWrapper.class);
		}).build();
		final ListWrapper listWrapper = cf.requireInstance(ListWrapper.class);
		assertNotNull(listWrapper);
		assertSame(list, listWrapper.getList());
	}

	/** Test class for {@link #testGenerics}.
	 */
	public static class ListWrapper {
		private @jakarta.inject.Inject List<String> list;
		/** Returns the wrapped list.
		 * @return The wrapped list.
		 * 
		 */
		public List<String> getList() { return list; }
	}

}
