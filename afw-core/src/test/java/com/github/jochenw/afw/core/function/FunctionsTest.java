package com.github.jochenw.afw.core.function;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;

import org.jspecify.annotations.NonNull;
import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableBiFunction;
import com.github.jochenw.afw.core.function.Functions.FailableBiIntPredicate;
import com.github.jochenw.afw.core.function.Functions.FailableBiLongPredicate;
import com.github.jochenw.afw.core.function.Functions.FailableBiPredicate;
import com.github.jochenw.afw.core.function.Functions.FailableCallable;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.function.Functions.FailablePredicate;
import com.github.jochenw.afw.core.function.Functions.FailableRunnable;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Reflection;


/** Test for the {@link Functions} class.
 */
public class FunctionsTest {
	/** A special Exception class, for use in tests.
	 */
    public static class SomeException extends Exception {
        private static final long serialVersionUID = -4965704778119283411L;

        private Throwable t;

        SomeException(String pMsg) {
            super(pMsg);
        }

        /** Sets the throwable.
         * @param pThrowable The throwable, which is being thrown, if {@link #test()} is being invoked.
         */
        public void setThrowable(Throwable pThrowable) {
            t = pThrowable;
        }

        /** Tests, whether a throwable has been set. If so, throws it.
         * Otherwise, does nothing.
         * @throws Throwable A throwable, that has been configured in advance
         *   by invoking {@link #setThrowable(Throwable)}.
         */
        public void test() throws Throwable {
            if (t != null) {
                throw t;
            }
        }
    }
   
    /** A helper object for use in tests.
     */
    public static class Testable {
        private Throwable t;

        Testable(Throwable pTh) {
            t = pTh;
        }

        /** Sets the throwable.
         * @param pThrowable The throwable, which is being thrown, if {@link #test()} is being invoked.
         */
        public void setThrowable(Throwable pThrowable) {
            t = pThrowable;
        }

        /** Tests, whether a throwable has been set. If so, throws it.
         * Otherwise, does nothing.
         * @throws Throwable A throwable, that has been configured in advance
         *   by invoking {@link #setThrowable(Throwable)}.
         */
        public void test() throws Throwable {
            test(t);
        }

        /** Tests, whether the given throwable is null. If so, throws it.
         * Otherwise, does nothing.
         * @param pThrowable The throwable, which is being tested.
         * @throws Throwable The given {@link #setThrowable(Throwable)}.
         */
        public void test(Throwable pThrowable) throws Throwable {
            if (pThrowable != null) {
                throw pThrowable;
            }
        }

        /** Tests, whether a throwable has been configure by invoking {@link #setThrowable(Throwable)}.
         * If so, throws it. Otherwise, does nothing.
         * @throws Throwable The configured {@link #setThrowable(Throwable)}.
         * @return The value Zero (Integer.valueOf(0)).
         */
        public Integer testInt() throws Throwable {
            return testInt(t);
        }

        /** Tests, whether the given throwable is null. If so, throws it.
         * Otherwise, does nothing.
         * @param pThrowable The throwable, which is being tested.
         * @throws Throwable The given {@link #setThrowable(Throwable)}.
         * @return The value Zero (Integer.valueOf(0)).
         */
        public Integer testInt(Throwable pThrowable) throws Throwable {
            if (pThrowable != null) {
                throw pThrowable;
            }
            return 0;
        }
    }

    /** A helper object, which fails on every second instantiation.
     */
    public static class FailureOnOddInvocations {
        private static int invocation;
        FailureOnOddInvocations() throws SomeException {
            final int i = ++invocation;
            if (i % 2 == 1) {
                throw new SomeException("Odd Invocation: " + i);
            }
        }
    }

    /** A helper object, on which we can check, whether it has been closed.
     */
    public static class CloseableObject {
        private boolean closed;

        /** Called to test, whether the given {@link Throwable} is null.
         * If so, throws it. Otherwise, does nothing.
         * @param pTh The {@link Throwable}, which is being tested.
         * @throws Throwable The given {@link Throwable}.
         */
        public void run(Throwable pTh) throws Throwable {
            if (pTh != null) {
                throw pTh;
            }
        }

        /** Resets the object to it's initial state (not closed).
         */
        public void reset() {
            closed = false;
        }

        /** Changes the object's state to closed.
         */
        public void close() {
            closed = true;
        }

        /** Returns, whether the object is closed.
         * @return True, if the object is closed.
         */
        public boolean isClosed() {
            return closed;
        }
    }

    /** Tests, whether we can instantiate the Functions class.
     * @throws Exception The test failed.
     */
	@Test
    public void testCreate() throws Exception {
    	@SuppressWarnings("null")
		final @NonNull Constructor<Functions> constructor = Functions.class.getDeclaredConstructor();
    	Reflection.makeAcccessible(constructor);
    	final Functions functions = constructor.newInstance();
    	assertNotNull(functions);
    }

    /** Test case for {@link Functions#run(FailableRunnable)}.
     */
    @Test
    public void testRunnable() {
        FailureOnOddInvocations.invocation = 0;
        UndeclaredThrowableException e = assertThrows(UndeclaredThrowableException.class, () ->  Functions.run(FailureOnOddInvocations::new));
        final Throwable cause = e.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof SomeException);
        assertEquals("Odd Invocation: 1", cause.getMessage());

        // Even invocation, should not throw an exception
        Functions.run(FailureOnOddInvocations::new);
    }

    /** Test case for {@link Functions#asRunnable(FailableRunnable)}.
     */
    @Test
    public void testAsRunnable() {
        FailureOnOddInvocations.invocation = 0;
        Runnable runnable = Functions.asRunnable(() -> new FailureOnOddInvocations());
        UndeclaredThrowableException e = assertThrows(UndeclaredThrowableException.class, () ->  runnable.run());
        final Throwable cause = e.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof SomeException);
        assertEquals("Odd Invocation: 1", cause.getMessage());

        // Even invocation, should not throw an exception
        runnable.run();
    }

    /** Test case for {@link Functions#call(FailableCallable)}.
     */
    @Test
    public void testCallable() {
        FailureOnOddInvocations.invocation = 0;
        UndeclaredThrowableException e = assertThrows(UndeclaredThrowableException.class, () ->  Functions.run(FailureOnOddInvocations::new));
        final Throwable cause = e.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof SomeException);
        assertEquals("Odd Invocation: 1", cause.getMessage());
        final FailureOnOddInvocations instance = Functions.call(FailureOnOddInvocations::new);
        assertNotNull(instance);
    }

    /** Test case for {@link Functions#asCallable(FailableCallable)}.
     */
    @Test
    public void testAsCallable() {
        FailureOnOddInvocations.invocation = 0;
        final FailableCallable<FailureOnOddInvocations,SomeException> failableCallable = () -> { return new FailureOnOddInvocations(); };
        final Callable<FailureOnOddInvocations> callable = Functions.asCallable(failableCallable);
        UndeclaredThrowableException e = assertThrows(UndeclaredThrowableException.class, () ->  callable.call());
        final Throwable cause = e.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof SomeException);
        assertEquals("Odd Invocation: 1", cause.getMessage());
        final FailureOnOddInvocations instance;
        try {
        	instance = callable.call();
        } catch (Exception ex) {
        	throw Exceptions.show(ex);
        }
        assertNotNull(instance);
    }

    /** Test case for {@link Functions#accept(FailableConsumer, Object)}.
     */
    @Test
    public void testAcceptConsumer() {
        final IllegalStateException ise = new IllegalStateException();
        final Testable testable = new Testable(ise);
        Throwable e = assertThrows(IllegalStateException.class, () -> Functions.accept(Testable::test, testable));
        assertSame(ise, e);

        final Error error = new OutOfMemoryError();
        testable.setThrowable(error);
        e = assertThrows(OutOfMemoryError.class, () -> Functions.accept(Testable::test, testable));
        assertSame(error, e);

        final IOException ioe = new IOException("Unknown I/O error");
        testable.setThrowable(ioe);
        e = assertThrows(UncheckedIOException.class, () -> Functions.accept(Testable::test, testable));
        final Throwable t = e.getCause();
        assertNotNull(t);
        assertSame(ioe, t);

        testable.setThrowable(null);
        Functions.accept(Testable::test, testable);
    }

    /** Test case for {@link Functions#asConsumer(FailableConsumer)}.
     */
    @Test
    public void testAsConsumer() {
        final IllegalStateException ise = new IllegalStateException();
        final Testable testable = new Testable(ise);
        final Consumer<Testable> consumer = Functions.asConsumer((t) -> t.test()); 
        Throwable e = assertThrows(IllegalStateException.class, () -> consumer.accept(testable));
        assertSame(ise, e);

        final Error error = new OutOfMemoryError();
        testable.setThrowable(error);
        e = assertThrows(OutOfMemoryError.class, () -> consumer.accept(testable));
        assertSame(error, e);

        final IOException ioe = new IOException("Unknown I/O error");
        testable.setThrowable(ioe);
        e = assertThrows(UncheckedIOException.class, () -> consumer.accept(testable));
        final Throwable t = e.getCause();
        assertNotNull(t);
        assertSame(ioe, t);

        testable.setThrowable(null);
        Functions.accept(Testable::test, testable);
    }

    /** Test case for {@link Functions#accept(FailableBiConsumer, Object, Object)}.
     */
    @Test
    public void testAcceptBiConsumer() {
        final IllegalStateException ise = new IllegalStateException();
        final Testable testable = new Testable(null);
        Throwable e = assertThrows(IllegalStateException.class, () -> Functions.accept(Testable::test, testable, ise));
        assertSame(ise, e);

        final Error error = new OutOfMemoryError();
        e = assertThrows(OutOfMemoryError.class, () -> Functions.accept(Testable::test, testable, error));
        assertSame(error, e);

        final IOException ioe = new IOException("Unknown I/O error");
        testable.setThrowable(ioe);
        e = assertThrows(UncheckedIOException.class, () -> Functions.accept(Testable::test, testable, ioe));
        final Throwable t = e.getCause();
        assertNotNull(t);
        assertSame(ioe, t);

        testable.setThrowable(null);
        Functions.accept(Testable::test, testable, (Throwable) null);
    }

    /** Test case for {@link Functions#asBiConsumer(FailableBiConsumer)}.
     */
    @Test
    public void testAsBiConsumer() {
        final IllegalStateException ise = new IllegalStateException();
        final Testable testable = new Testable(null);
        final FailableBiConsumer<Testable, Throwable, Throwable> failableBiConsumer = (t, th) -> { t.setThrowable(th); t.test(); }; 
        final BiConsumer<Testable, Throwable> consumer = Functions.asBiConsumer(failableBiConsumer);
        Throwable e = assertThrows(IllegalStateException.class, () -> consumer.accept(testable, ise));
        assertSame(ise, e);

        final Error error = new OutOfMemoryError();
        e = assertThrows(OutOfMemoryError.class, () -> consumer.accept(testable, error));
        assertSame(error, e);

        final IOException ioe = new IOException("Unknown I/O error");
        testable.setThrowable(ioe);
        e = assertThrows(UncheckedIOException.class, () -> consumer.accept(testable,  ioe));
        final Throwable t = e.getCause();
        assertNotNull(t);
        assertSame(ioe, t);

        consumer.accept(testable, null);
    }

    /** Test case for {@link Functions#apply(FailableFunction, Object)}.
     */
    @Test
    public void testApplyFunction() {
        final IllegalStateException ise = new IllegalStateException();
        final Testable testable = new Testable(ise);
        Throwable e = assertThrows(IllegalStateException.class, () -> Functions.apply(Testable::testInt, testable));
        assertSame(ise, e);

        final Error error = new OutOfMemoryError();
        testable.setThrowable(error);
        e = assertThrows(OutOfMemoryError.class, () -> Functions.apply(Testable::testInt, testable));
        assertSame(error, e);

        final IOException ioe = new IOException("Unknown I/O error");
        testable.setThrowable(ioe);
        e = assertThrows(UncheckedIOException.class, () -> Functions.apply(Testable::testInt, testable));
        final Throwable t = e.getCause();
        assertNotNull(t);
        assertSame(ioe, t);

        testable.setThrowable(null);
        final Integer i = Functions.apply(Testable::testInt, testable);
        assertNotNull(i);
        assertEquals(0, i.intValue());
    }

    /** Test case for {@link Functions#asFunction(FailableFunction)}.
     */
    @Test
    public void testAsFunction() {
        final IllegalStateException ise = new IllegalStateException();
        final Testable testable = new Testable(ise);
        final FailableFunction<Throwable,Integer,Throwable> failableFunction = (th) -> {
        	testable.setThrowable(th);
        	return Integer.valueOf(testable.testInt());
        };
        final Function<Throwable,Integer> function = Functions.asFunction(failableFunction);
        Throwable e = assertThrows(IllegalStateException.class, () -> function.apply(ise));
        assertSame(ise, e);

        final Error error = new OutOfMemoryError();
        testable.setThrowable(error);
        e = assertThrows(OutOfMemoryError.class, () -> function.apply(error));
        assertSame(error, e);

        final IOException ioe = new IOException("Unknown I/O error");
        testable.setThrowable(ioe);
        e = assertThrows(UncheckedIOException.class, () -> function.apply(ioe));
        final Throwable t = e.getCause();
        assertNotNull(t);
        assertSame(ioe, t);

        assertEquals(0, function.apply(null).intValue());
    }

    /** Test case for {@link Functions#apply(FailableBiFunction, Object, Object)}.
     */
    @Test
    public void testApplyBiFunction() {
        final IllegalStateException ise = new IllegalStateException();
        final Testable testable = new Testable(null);
        Throwable e = assertThrows(IllegalStateException.class, () -> Functions.apply(Testable::testInt, testable, ise));
        assertSame(ise, e);

        final Error error = new OutOfMemoryError();
        e = assertThrows(OutOfMemoryError.class, () -> Functions.apply(Testable::testInt, testable, error));
        assertSame(error, e);

        final IOException ioe = new IOException("Unknown I/O error");
        e = assertThrows(UncheckedIOException.class, () -> Functions.apply(Testable::testInt, testable, ioe));
        final Throwable t = e.getCause();
        assertNotNull(t);
        assertSame(ioe, t);

        final Integer i = Functions.apply(Testable::testInt, testable, (Throwable) null);
        assertNotNull(i);
        assertEquals(0, i.intValue());
    }

    /**
     * Test case for {@link Functions#test(FailableBiIntPredicate, int, Object)}.
     * @throws Throwable The test failed.
     */
    @Test
    public void testTestBiIntFunction() throws Throwable {
    	final RuntimeException rte = new IllegalArgumentException("Odd number");
    	final Exception oddNumberException = new Exception("Odd number");
    	final FailableBiIntPredicate<Throwable,Throwable> biIntPredicate = (i, t) -> {
    		if (i %2 == 1 ) { throw t;
    		}  else {
    			return true;
    		}
    	};
    	Functions.test(biIntPredicate, 0, null);
    	try {
    		Functions.test(biIntPredicate, 1, rte);
    		Assert.fail("Expected Exception");
    	} catch (Exception e) {
    		Assert.assertSame(rte, e);
    	}
    	Functions.test(biIntPredicate, 2, rte);
    	try {
    		Functions.test(biIntPredicate, 3, oddNumberException);
    		Assert.fail("Expected Exception");
    	} catch (UndeclaredThrowableException ute) {
    		final Throwable th = ute.getCause();
    		Assert.assertSame(oddNumberException, th);
    	}
    }

    /**
     * Test case for {@link Functions#test(FailableBiLongPredicate, long, Object)}.
     * @throws Throwable The test failed.
     */
    @Test
    public void testTestBiLongFunction() throws Throwable {
    	final RuntimeException rte = new IllegalArgumentException("Odd number");
    	final Exception oddNumberException = new Exception("Odd number");
    	final FailableBiLongPredicate<Throwable,Throwable> biIntPredicate = (l, t) -> {
    		if (l %2 == 1 ) { throw t;
    		}  else {
    			return true;
    		}
    	};
    	Functions.test(biIntPredicate, 0l, null);
    	try {
    		Functions.test(biIntPredicate, 1l, rte);
    		Assert.fail("Expected Exception");
    	} catch (IllegalArgumentException e) {
    		Assert.assertSame(rte, e);
    	}
    	Functions.test(biIntPredicate, 2l, rte);
    	try {
    		Functions.test(biIntPredicate, 3l, oddNumberException);
    		Assert.fail("Expected Exception");
    	} catch (UndeclaredThrowableException ute) {
    		final Throwable th = ute.getCause();
    		Assert.assertSame(oddNumberException, th);
    	}
    }

    /** Test case for {@link Functions#asBiFunction(FailableBiFunction)}.
     */
    @Test
    public void testAsBiFunction() {
        final IllegalStateException ise = new IllegalStateException();
        final Testable testable = new Testable(ise);
        final FailableBiFunction<Testable,Throwable,Integer,Throwable> failableBiFunction = (t, th) -> {
        	t.setThrowable(th);
        	return Integer.valueOf(t.testInt());
        };
        final BiFunction<Testable,Throwable,Integer> biFunction = Functions.asBiFunction(failableBiFunction);
        Throwable e = assertThrows(IllegalStateException.class, () -> biFunction.apply(testable, ise));
        assertSame(ise, e);

        final Error error = new OutOfMemoryError();
        testable.setThrowable(error);
        e = assertThrows(OutOfMemoryError.class, () -> biFunction.apply(testable, error));
        assertSame(error, e);

        final IOException ioe = new IOException("Unknown I/O error");
        testable.setThrowable(ioe);
        e = assertThrows(UncheckedIOException.class, () -> biFunction.apply(testable, ioe));
        final Throwable t = e.getCause();
        assertNotNull(t);
        assertSame(ioe, t);

        assertEquals(0, biFunction.apply(testable, null).intValue());
    }

    /** Test case for {@link Functions#get(FailableSupplier)}.
     */
    @Test
    public void testGetFromSupplier() {
        FailureOnOddInvocations.invocation = 0;
        UndeclaredThrowableException e = assertThrows(UndeclaredThrowableException.class, () ->  Functions.run(FailureOnOddInvocations::new));
        final Throwable cause = e.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof SomeException);
        assertEquals("Odd Invocation: 1", cause.getMessage());
        final FailureOnOddInvocations instance = Functions.call(FailureOnOddInvocations::new);
        assertNotNull(instance);
    }

    /** Test case for {@link Functions#asSupplier(FailableSupplier)}.
     */
    @Test
    public void testAsSupplier() {
        FailureOnOddInvocations.invocation = 0;
        final FailableSupplier<FailureOnOddInvocations,Throwable> failableSupplier = () -> { return new FailureOnOddInvocations(); };
        final Supplier<FailureOnOddInvocations> supplier = Functions.asSupplier(failableSupplier);
        UndeclaredThrowableException e = assertThrows(UndeclaredThrowableException.class, () ->  supplier.get());
        final Throwable cause = e.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof SomeException);
        assertEquals("Odd Invocation: 1", cause.getMessage());
        final FailureOnOddInvocations instance = supplier.get();
        assertNotNull(instance);
    }

    /** Test case for {@link Functions#tryWithResources(FailableRunnable, FailableRunnable...)}.
     */
    @Test
    public void testTryWithResources() {
        final CloseableObject co = new CloseableObject();
        final FailableConsumer<Throwable, ? extends Throwable> consumer = co::run;
        final IllegalStateException ise = new IllegalStateException();
        final FailableRunnable<?> action = () -> consumer.accept(ise);
        Throwable e = assertThrows(IllegalStateException.class, () -> Functions.tryWithResources(action, co::close));
        assertSame(ise, e);

        assertTrue(co.isClosed());
        co.reset();
        final Error error = new OutOfMemoryError();
        final FailableRunnable<?> action2 = () -> consumer.accept(error);
        e = assertThrows(OutOfMemoryError.class, () -> Functions.tryWithResources(action2, co::close));
        assertSame(error, e);

        assertTrue(co.isClosed());
        co.reset();
        final IOException ioe = new IOException("Unknown I/O error");
        final FailableRunnable<?> action3 = () -> consumer.accept(ioe);
        UncheckedIOException uioe = assertThrows(UncheckedIOException.class, () ->  Functions.tryWithResources(action3, co::close));
        final IOException cause = uioe.getCause();
        assertSame(ioe, cause);

        assertTrue(co.isClosed());
        co.reset();
        Functions.tryWithResources(() -> consumer.accept(null), co::close);
        assertTrue(co.isClosed());
    }

    /** Asserts, that running the given {@link Functions.FailableRunnable}
     * throws the expected exception.
     * @param <T> The expected type of exception.
     * @param pType The expected type of exception.
     * @param pRunnable The code, that is being executed to produce the exception.
     * @return The actual exception, if it is of the expected type.
     * @throws RuntimeException The actual exception, of an unexpected type.
     */
    protected <T extends Throwable> T assertThrows(Class<T> pType, FailableRunnable<Throwable> pRunnable) {
    	try {
    		pRunnable.run();
    		throw new IllegalStateException("Expected exception");
    	} catch (Throwable t) {
    		if (pType.isAssignableFrom(t.getClass())) {
    			return pType.cast(t);
    		} else {
    			throw Exceptions.show(t);
    		}
    	}
    }

    /** Test case for the {@link FailablePredicate}.
     * @throws IOException The test failed.
     */
    @Test
    public void testTestPredicate() throws IOException {
    	final FailablePredicate<Integer, IOException> predicate = (i) -> { if (i.intValue() % 2 != 0) { throw new IOException("Expected even number, got " + i); } return true; };
    	// Successfull execution:
    	assertTrue(predicate.test(Integer.valueOf(0)));
    	// Failing execution.
   		try {
   			predicate.test(Integer.valueOf(1));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number, got 1", e.getMessage());
   		}
    }

    /** Test case for the {@link FailableBiIntPredicate}.
     * @throws IOException The test failed.
     */
    @Test
    public void testTestBiIntPredicate() throws IOException {
    	final FailableBiIntPredicate<Integer, IOException> predicate = (i, I) -> {
    		if (i % 2 != 0) { throw new IOException("Expected even number for parameter 1, got " + i); }
    		if (I.intValue() % 2 != 0) { throw new IOException("Expected even number for parameter 2, got " + I); }  	
    		return true;
    	};
    	// Successful execution:
    	assertTrue(predicate.test(0,Integer.valueOf(0)));
    	// Failing execution (parameter 1)
   		try {
   			predicate.test(1,Integer.valueOf(0));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number for parameter 1, got 1", e.getMessage());
   		}
    	// Failing execution (parameter 2)
   		try {
   			predicate.test(0,Integer.valueOf(1));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number for parameter 2, got 1", e.getMessage());
   		}
    	// Failing execution (both parameters)
   		try {
   			predicate.test(3,Integer.valueOf(1));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number for parameter 1, got 3", e.getMessage());
   		}
    }

    /** Test case for the {@link FailableBiIntPredicate}.
     * @throws IOException The test failed.
     */
    @Test
    public void testTestBiLongPredicate() throws IOException {
    	final FailableBiLongPredicate<Long, IOException> predicate = (l, L) -> {
    		if (l % 2 != 0) { throw new IOException("Expected even number for parameter 1, got " + l); }
    		if (L.longValue() % 2 != 0) { throw new IOException("Expected even number for parameter 2, got " + L); }  	
    		return true;
    	};
    	// Successful execution:
    	assertTrue(predicate.test(0l,Long.valueOf(0)));
    	// Failing execution (parameter 1)
   		try {
   			predicate.test(1l,Long.valueOf(0));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number for parameter 1, got 1", e.getMessage());
   		}
    	// Failing execution (parameter 2)
   		try {
   			predicate.test(0l,Long.valueOf(1));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number for parameter 2, got 1", e.getMessage());
   		}
    	// Failing execution (both parameters)
   		try {
   			predicate.test(3l,Long.valueOf(1));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number for parameter 1, got 3", e.getMessage());
   		}
    }

    /** Test case for the {@link FailableBiPredicate}.
     * @throws IOException The test failed.
     */
    @Test
    public void testTestBiPredicate() throws IOException {
    	final FailableBiPredicate<Integer,Long, IOException> predicate = (i, l) -> {
    		if (i.intValue() % 2 != 0) { throw new IOException("Expected even number for parameter 1, got " + i); }
    		if (l.longValue() % 2 != 0) { throw new IOException("Expected even number for parameter 2, got " + l); }  	
    		return true;
    	};
    	// Successful execution:
   		assertTrue(predicate.test(Integer.valueOf(0),Long.valueOf(0)));
    	// Failing execution (parameter 1)
   		try {
   			predicate.test(Integer.valueOf(1),Long.valueOf(0));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number for parameter 1, got 1", e.getMessage());
   		}
    	// Failing execution (parameter 2)
   		try {
   			predicate.test(Integer.valueOf(0),Long.valueOf(1));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number for parameter 2, got 1", e.getMessage());
   		}
    	// Failing execution (both parameters)
   		try {
   			predicate.test(Integer.valueOf(3),Long.valueOf(1));
   			fail("Expected IOException");
   		} catch (IOException e) {
   			assertEquals("Expected even number for parameter 1, got 3", e.getMessage());
   		}
    }
}
