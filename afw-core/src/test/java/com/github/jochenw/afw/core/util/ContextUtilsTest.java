/**
 * 
 */
package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.util.ContextUtils.IContextProvider;


/**
 * 
 */
public class ContextUtilsTest {
	private static class SampleCtx implements AutoCloseable {
		private boolean closed;
		private boolean used;

		@Override
		public void close() throws Exception {
			closed = true;
		}

		public boolean isClosed() { return closed; }
		public boolean isUsed() { return used; }
		public void setUsed() { used = true; }
	}

	/**
	 * Test case 1 for {@link ContextUtils#of(Functions.FailableSupplier)},
	 * and {@link IContextProvider#run(ContextUtils.IRunnable)}.
	 */
	@Test
	public void testOfSupplier1() {
		@SuppressWarnings("resource")
		final SampleCtx ctx = new SampleCtx();
		final IContextProvider<SampleCtx> cp = ContextUtils.of(() -> ctx);
		assertFalse(ctx.isClosed());
		assertFalse(ctx.isUsed());
		cp.run((c) -> {
			c.setUsed();
		});
		assertTrue(ctx.isClosed());
		assertTrue(ctx.isUsed());
	}

	/**
	 * Test case 2 for {@link ContextUtils#of(Functions.FailableSupplier)},
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}.
	 */
	@Test
	public void testOfSupplier2() {
		@SuppressWarnings("resource")
		final SampleCtx ctx = new SampleCtx();
		final IContextProvider<SampleCtx> cp = ContextUtils.of(() -> ctx);
		assertFalse(ctx.isClosed());
		assertFalse(ctx.isUsed());
		final Boolean b = cp.call((c) -> {
			c.setUsed();
			return Boolean.TRUE;
		});
		assertTrue(ctx.isClosed());
		assertTrue(ctx.isUsed());
		assertSame(Boolean.TRUE, b);
	}

	/**
	 * Test case 3 for {@link ContextUtils#of(Functions.FailableSupplier)}.
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}.
	 */
	@Test
	public void testOfSupplier3() {
		@SuppressWarnings("resource")
		final SampleCtx ctx = new SampleCtx();
		final IContextProvider<SampleCtx> cp = ContextUtils.of(() -> ctx);
		assertFalse(ctx.isClosed());
		assertFalse(ctx.isUsed());
		final Boolean b = cp.call((c) -> {
			c.setUsed();
			return Boolean.FALSE;
		});
		assertTrue(ctx.isClosed());
		assertTrue(ctx.isUsed());
		assertSame(Boolean.FALSE, b);
	}

	/**
	 * Test case 4 for {@link ContextUtils#of(Functions.FailableSupplier)}.
	 * and {@link IContextProvider#run(ContextUtils.IRunnable)}, with failure.
	 */
	@Test
	public void testOfSupplier4() {
		@SuppressWarnings("resource")
		final SampleCtx ctx = new SampleCtx();
		final IContextProvider<SampleCtx> cp = ContextUtils.of(() -> ctx);
		assertFalse(ctx.isClosed());
		assertFalse(ctx.isUsed());
		final RuntimeException rte = new RuntimeException();
		try {
			cp.run((c) -> {
				throw rte;
			});
			fail("Expected Exception");
		} catch (RuntimeException e) {
			assertSame(rte, e);
		}
		assertTrue(ctx.isClosed());
		assertFalse(ctx.isUsed());
	}

	/**
	 * Test case 5 for {@link ContextUtils#of(Functions.FailableSupplier)}.
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}, with failure.
	 */
	@Test
	public void testOfSupplier5() {
		@SuppressWarnings("resource")
		final SampleCtx ctx = new SampleCtx();
		final IContextProvider<SampleCtx> cp = ContextUtils.of(() -> ctx);
		assertFalse(ctx.isClosed());
		assertFalse(ctx.isUsed());
		final RuntimeException rte = new RuntimeException();
		try {
			cp.run((c) -> { throw rte; });
			fail("Expected Exception");
		} catch (RuntimeException e) {
			assertSame(rte, e);
		}
		assertTrue(ctx.isClosed());
		assertFalse(ctx.isUsed());
	}

	/**
	 * Test case 6 for {@link ContextUtils#of(Functions.FailableSupplier)}.
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}, with failing
	 * terminator.
	 */
	@Test
	public void testOfSupplier6() {
		final NullPointerException npe = new NullPointerException();
		@SuppressWarnings("resource")
		final SampleCtx ctx = new SampleCtx() {
			@Override
			public void close() throws Exception {
				throw npe;
			}
		};
		final IContextProvider<SampleCtx> cp = ContextUtils.of(() -> ctx);
		assertFalse(ctx.isClosed());
		assertFalse(ctx.isUsed());
		try {
			cp.run((c) -> { c.setUsed(); });
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertSame(npe, e);
		}
		assertFalse(ctx.isClosed());
		assertTrue(ctx.isUsed());
	}

	/**
	 * Test case 7 for {@link ContextUtils#of(Functions.FailableSupplier)}.
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}, with failing
	 * terminator.
	 */
	@Test
	public void testOfSupplier7() {
		final NullPointerException npe = new NullPointerException();
		final RuntimeException rte = new RuntimeException();
		@SuppressWarnings("resource")
		final SampleCtx ctx = new SampleCtx() {
			@Override
			public void close() throws Exception {
				throw npe;
			}
		};
		final IContextProvider<SampleCtx> cp = ContextUtils.of(() -> ctx);
		assertFalse(ctx.isClosed());
		assertFalse(ctx.isUsed());
		try {
			cp.run((c) -> { c.setUsed(); throw rte; });
			fail("Expected Exception");
		} catch (RuntimeException e) {
			assertSame(rte, e);
		}
		assertFalse(ctx.isClosed());
		assertTrue(ctx.isUsed());
	}

	private static final Functions.FailableConsumer<Map<String,Object>,?> MAP_TERMINATOR =
			(map) -> { map.put("closed", Boolean.TRUE); };

	/**
	 * Test case 1 for {@link ContextUtils#of(Functions.FailableSupplier, Functions.FailableConsumer)},
	 * and {@link IContextProvider#run(ContextUtils.IRunnable)}.
	 */
	@Test
	public void testOfSupplierConsumer1() {
		final @NonNull Map<String,Object> map = new HashMap<>();
		final IContextProvider<@NonNull Map<String,Object>> cp = ContextUtils.of(() -> map, MAP_TERMINATOR);
		assertNull(map.get("closed"));
		assertNull(map.get("used"));
		cp.run((c) -> {
			map.put("used", "true");
		});
		assertEquals("true", map.get("used"));
		assertSame(Boolean.TRUE, map.get("closed"));
	}

	/**
	 * Test case 2 for {@link ContextUtils#of(Functions.FailableSupplier, Functions.FailableConsumer)},
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}.
	 */
	@Test
	public void testOfSupplierConsumer2() {
		final @NonNull Map<String,Object> map = new HashMap<>();
		final IContextProvider<@NonNull Map<String,Object>> cp = ContextUtils.of(() -> map, MAP_TERMINATOR);
		assertNull(map.get("closed"));
		assertNull(map.get("used"));
		final Boolean b = cp.call((mp) -> {
			mp.put("used", "true");
			return Boolean.TRUE;
		});
		assertEquals("true", map.get("used"));
		assertSame(Boolean.TRUE, map.get("closed"));
		assertSame(Boolean.TRUE, b);
	}

	/**
	 * Test case 3 for {@link ContextUtils#of(Functions.FailableSupplier, Functions.FailableConsumer)},
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}.
	 */
	@Test
	public void testOfSupplierConsumer3() {
		final @NonNull Map<String,Object> map = new HashMap<>();
		final IContextProvider<@NonNull Map<String,Object>> cp = ContextUtils.of(() -> map, MAP_TERMINATOR);
		assertNull(map.get("closed"));
		assertNull(map.get("used"));
		final Boolean b = cp.call((mp) -> {
			mp.put("used", "true");
			return Boolean.FALSE;
		});
		assertEquals("true", map.get("used"));
		assertSame(Boolean.TRUE, map.get("closed"));
		assertSame(Boolean.FALSE, b);
	}

	/**
	 * Test case 4 for {@link ContextUtils#of(Functions.FailableSupplier, Functions.FailableConsumer)},
	 * and {@link IContextProvider#run(ContextUtils.IRunnable)}.
	 */
	@Test
	public void testOfSupplierConsumer4() {
		final @NonNull Map<String,Object> map = new HashMap<>();
		final IContextProvider<@NonNull Map<String,Object>> cp = ContextUtils.of(() -> map, MAP_TERMINATOR);
		assertNull(map.get("closed"));
		assertNull(map.get("used"));
		final RuntimeException rte = new RuntimeException();
		try {
			cp.run((c) -> {
				throw rte;
			});
			fail("Expected Exception");
		} catch (RuntimeException e) {
			assertSame(rte, e);
		}
		assertNull(map.get("used"));
		assertSame(Boolean.TRUE, map.get("closed"));
	}

	/**
	 * Test case 5 for {@link ContextUtils#of(Functions.FailableSupplier, Functions.FailableConsumer)},
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}.
	 */
	@Test
	public void testOfSupplierConsumer5() {
		final @NonNull Map<String,Object> map = new HashMap<>();
		final IContextProvider<@NonNull Map<String,Object>> cp = ContextUtils.of(() -> map, MAP_TERMINATOR);
		assertNull(map.get("closed"));
		assertNull(map.get("used"));
		final IOException ioe = new IOException();
		try {
			cp.call((mp) -> {
				throw ioe;
			});
			fail("Expected Exception");
		} catch (UncheckedIOException e) {
			assertNotNull(e.getCause());
			assertSame(ioe, e.getCause());
		}
		assertNull(map.get("used"));
		assertSame(Boolean.TRUE, map.get("closed"));
	}

	/**
	 * Test case 6 for
	 * {@link ContextUtils#of(Functions.FailableSupplier, Functions.FailableConsumer)},
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}, with failing
	 * terminator.
	 */
	@Test
	public void testOfSupplierConsumer6() {
		final NullPointerException npe = new NullPointerException();
		final @NonNull Map<String,Object> map = new HashMap<>();
		final IContextProvider<@NonNull Map<String,Object>> cp = ContextUtils.of(() -> map,
				(c) -> { map.put("closed", Boolean.TRUE); throw npe; });
		assertNull(map.get("closed"));
		assertNull(map.get("used"));
		try {
			cp.run((c) -> { map.put("used", "true"); });
			fail("Expected Exception");
		} catch (NullPointerException e) {
			assertSame(npe, e);
		}
		assertSame(Boolean.TRUE, map.get("closed"));
		assertEquals("true", map.get("used"));
	}

	/**
	 * Test case 7 for
	 * {@link ContextUtils#of(Functions.FailableSupplier, Functions.FailableConsumer)},
	 * and {@link IContextProvider#call(ContextUtils.ICallable)}, with failing
	 * terminator.
	 */
	@Test
	public void testOfSupplierConsumer7() {
		final NullPointerException npe = new NullPointerException();
		final @NonNull Map<String,Object> map = new HashMap<>();
		final IContextProvider<@NonNull Map<String,Object>> cp = ContextUtils.of(() -> map,
				(c) -> { map.put("closed", Boolean.TRUE); throw npe; });
		final RuntimeException rte = new RuntimeException();
		assertNull(map.get("closed"));
		assertNull(map.get("used"));
		try {
			cp.run((c) -> { map.put("used", "true"); throw rte; });
			fail("Expected Exception");
		} catch (RuntimeException e) {
			assertSame(rte, e);
		}
		assertSame(Boolean.TRUE, map.get("closed"));
		assertEquals("true", map.get("used"));
	}

}
