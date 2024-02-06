/*
 * Copyright 2023 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.di.impl.limited;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.di.impl.limited.LimitedComponentFactory.Key;
import com.github.jochenw.afw.di.impl.limited.LimitedComponentFactory.IComponent;
import com.github.jochenw.afw.di.impl.limited.LimitedComponentFactory.IModule;

/** Test suite for the {@link LimitedComponentFactory}.
 */
public class LimitedComponentFactoryTest {
	/** Example of a component, that needs initialization.
	 */
	public static class Initializable implements LimitedComponentFactory.IComponent {
		private LimitedComponentFactory componentFactory;
		private Map<String,Object> map1, hash, linked;

		
		@Override
		public void init(LimitedComponentFactory pComponentFactory) {
			assertNotNull(pComponentFactory);
			componentFactory = pComponentFactory;
			@SuppressWarnings("unchecked")
			final Map<String,Object> mp1 = (Map<String,Object>) componentFactory.requireInstance(Map.class, "map1");
			map1 = mp1;
			@SuppressWarnings("unchecked")
			final Map<String,Object> hsh = (Map<String,Object>) componentFactory.requireInstance(Map.class, "hash");
			hash = hsh;
			@SuppressWarnings("unchecked")
			final Map<String,Object> lnked = (Map<String,Object>) componentFactory.requireInstance(Map.class, "linked");
			linked = lnked;
		}
	}
	/** A class, which contains a reference to another instance. We use this to
	 * implement, and test a circular dependency.
	 */
	private static class Reference implements LimitedComponentFactory.IComponent {
		private final String name;
		private Reference ref;
		/** Creates a new instance, which references an instance with the
		 * given name.
		 * @param pName Name of the referenced instance.
		 */
		public Reference(String pName) {
			name = pName;
		}
		@Override
		public void init(LimitedComponentFactory pComponentFactory) {
			ref = pComponentFactory.getInstance(Reference.class, name);
		}
	}

	/** Tests the basic functions
	 */
	@Test
	public void testBasicFunction() {
		@SuppressWarnings("null")
		final @NonNull Integer answer = Integer.valueOf(42);
		final Map<String,Object> map1 = new HashMap<>();
		IModule module = (b) -> {
			b.bind(Map.class, "map1", map1);
			b.bind(Map.class, "hash", HashMap.class);
			b.bind(Map.class, "linked", LinkedHashMap.class);
			b.bind(String.class, "jdbc.user", "myUser");
			final Supplier<@NonNull String> supplier = () -> "myPassword";
			b.bindToSupplier(String.class, "jdbc.password", supplier);
			b.bind(Initializable.class);
			b.bind(List.class, ArrayList.class);
			b.bind(Integer.class, answer);
			try {
				b.bind(Reference.class);
				fail("Expected Exception");
			} catch (IllegalArgumentException e) {
				assertEquals("The implementation class "
						+ Reference.class.getName()
						+ " doesn't have a public no-args constructor.",
						e.getMessage());
			}
		};
		final LimitedComponentFactory lcf =
				LimitedComponentFactory.of(module);
		final Consumer<LimitedComponentFactory> validator = (cf) -> {
			assertNotNull(cf);
			assertSame(cf.getInstance(Map.class, "map1"), map1);
			assertSame(cf.requireInstance(Map.class, "map1"), map1);
			assertNull(cf.getInstance(Map.class));
			assertNull(cf.getInstance(Map.class, ""));
			assertSame(answer, cf.requireInstance(Integer.class));
			try {
				cf.requireInstance(Map.class);
				fail("Expected Exception");
			} catch (NoSuchElementException e) {
				assertEquals("No binding has been registered"
						     + " with type=java.util.Map, and name=", e.getMessage());
			}
			try {
				cf.requireInstance(Map.class, "");
				fail("Expected Exception");
			} catch (NoSuchElementException e) {
				assertEquals("No binding has been registered"
					     + " with type=java.util.Map, and name=", e.getMessage());
			}
			final @NonNull Initializable initializable = cf.requireInstance(Initializable.class);
			assertSame(map1, initializable.map1);
			assertNotNull(initializable.hash);
			assertNotNull(initializable.linked);
			assertTrue(initializable.hash instanceof HashMap);
			assertTrue(initializable.linked instanceof LinkedHashMap);
			assertSame(initializable.hash, cf.getInstance(Map.class, "hash"));
			assertSame(initializable.linked, cf.getInstance(Map.class, "linked"));
			assertSame(cf, cf.requireInstance(LimitedComponentFactory.class));
		};
		validator.accept(lcf);
		validator.accept(LimitedComponentFactory.of(module, (b)-> {}));
	}

	/** Tests a circular dependency.
	 */
	@Test
	public void testCircularDependency() {
		final LimitedComponentFactory lcf = LimitedComponentFactory.of((b) -> {
			b.bind(Reference.class, "a", new Reference("b"));
			final Supplier<@NonNull Reference> supplier = () -> new Reference("c");
			b.bindToSupplier(Reference.class, "b", supplier);
			b.bindToSupplier(Reference.class, supplier);
			b.bind(Reference.class, "c", new Reference("a"));
		});
		assertNotNull(lcf);
		final Reference a = lcf.requireInstance(Reference.class, "a");
		final Reference b = lcf.requireInstance(Reference.class, "b");
		final Reference c = lcf.requireInstance(Reference.class, "c");
		assertSame(b, a.ref);
		assertSame(c, b.ref);
		assertSame(a, c.ref);
	}

	/** Tests module extension.
	 */
	@Test
	public void testModuleExtension() {
		@SuppressWarnings("null")
		final @NonNull Integer answer = Integer.valueOf(42);
		final IModule module1 = (b) -> {
			b.bind(Number.class, "42", answer);
		};
		final IModule module2 = module1.extend((b) -> {
			@SuppressWarnings("null")
			final @NonNull Boolean booleanTrue = Boolean.TRUE;
			b.bind(Boolean.class, "true", booleanTrue);
		});
		assertNotSame(module1, module2);
		assertSame(module1, module1.extend(null));
		final LimitedComponentFactory lcf1 = LimitedComponentFactory.of(module1);
		final LimitedComponentFactory lcf2 = LimitedComponentFactory.of(module2);
		assertNotNull(lcf1);
		assertNotNull(lcf2);
		assertNotSame(lcf1, lcf2);
		assertSame(answer, lcf1.getInstance(Number.class, "42"));
		assertSame(answer, lcf2.getInstance(Number.class, "42"));
		assertNull(lcf1.getInstance(Boolean.class, "true"));
		assertSame(Boolean.TRUE, lcf2.getInstance(Boolean.class, "true"));
	}

	/** Test for the {@link LimitedComponentFactory.Key} class.
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testKeys() {
		final Key key1 = new Key(Map.class, "a");
		assertSame(Map.class, key1.getType());
		assertEquals("a", key1.getName());
		final Key key2 = new Key(Map.class, "a" + "");
		assertEquals(key1, key2);
		assertEquals(key1.hashCode(), key2.hashCode());
		final Key key3 = new Key(Map.class, "b");
		assertNotEquals(key1, key3);
		final Key key4 = new Key(HashMap.class, "a");
		assertNotEquals(key1, key4);
		assertFalse(key1.equals(null));
		final Key key5 = new Key(Map.class, "a") {};
		assertFalse(key1.equals(key5));
		assertFalse(key1.equals(Boolean.TRUE));
		assertFalse(key1.equals(null));
	}

	@SuppressWarnings("null")
	private static <O> @NonNull O fakeNonNull() {
		final O nullO = (O) null;
		final @NonNull O o = (@NonNull O) nullO;
		return o;
	}

	/** Test for null values.
	 */
	@Test
	public void testNullValues() {
		final LimitedComponentFactory lcf = LimitedComponentFactory.of((b) -> {
			@SuppressWarnings("rawtypes")
			final Supplier<@NonNull Map> supplier = () -> fakeNonNull();
			b.bindToSupplier(Map.class, supplier);
		});
		try {
			lcf.getInstance(Map.class);
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertEquals("Binding returned a null value: "
						 + "type=" + Map.class.getName()
						 + ", name=", e.getMessage());
		}
	}

	private static class FailingComponent implements IComponent {
		private final Throwable th;
		public FailingComponent(Throwable pTh) {
			th = pTh;
		}
		@Override
		public void init(LimitedComponentFactory pComponentFactory) throws Throwable {
			if (th != null) {
				throw th;
			}
		}
		
	}
	/** Test for a failure in the
	 * {@link LimitedComponentFactory.IComponent#init(LimitedComponentFactory)}
	 * method.
	 */
	@Test
	public void testInitFailure() {
		final RuntimeException rte = new RuntimeException();
		final OutOfMemoryError oome = new OutOfMemoryError();
		final IOException ioe = new IOException();
		final LimitedComponentFactory lcf = LimitedComponentFactory.of((b) -> {
			b.bind(FailingComponent.class, new FailingComponent(null));
			b.bind(FailingComponent.class, "rte", new FailingComponent(rte));
			b.bind(FailingComponent.class, "oome", new FailingComponent(oome));
			b.bind(FailingComponent.class, "ioe", new FailingComponent(ioe));
		});
		assertNotNull(lcf.requireInstance(FailingComponent.class));
		try {
			lcf.requireInstance(FailingComponent.class, "rte");
			fail("Expected Exception");
		} catch (RuntimeException e) {
			assertSame(rte, e);
		}
		try {
			lcf.requireInstance(FailingComponent.class, "oome");
			fail("Expected Exception");
		} catch (OutOfMemoryError e) {
			assertSame(oome, e);
		}
		try {
			lcf.requireInstance(FailingComponent.class, "ioe");
			fail("Expected Exception");
		} catch (UndeclaredThrowableException e) {
			assertNotNull(e.getCause());
			assertSame(ioe, e.getCause());
		}
	}
	/** Test for a failure in the
	 * instance supplier.
	 */
	@Test
	public void testSupplierFailure() {
		final RuntimeException rte = new RuntimeException();
		final OutOfMemoryError oome = new OutOfMemoryError();
		final LimitedComponentFactory lcf = LimitedComponentFactory.of((b) -> {
			b.bindToSupplier(Object.class, () -> new Object());
			b.bindToSupplier(Object.class, "rte", (Supplier<@NonNull Object>) () -> { throw rte; });
			b.bindToSupplier(Object.class, "oome", (Supplier<@NonNull Object>) () -> { throw oome; });
		});
		assertNotNull(lcf.requireInstance(Object.class));
		try {
			lcf.requireInstance(Object.class, "rte");
			fail("Expected Exception");
		} catch (RuntimeException e) {
			assertSame(rte, e);
		}
		try {
			lcf.requireInstance(Object.class, "oome");
			fail("Expected Exception");
		} catch (OutOfMemoryError e) {
			assertSame(oome, e);
		}
	}

}
