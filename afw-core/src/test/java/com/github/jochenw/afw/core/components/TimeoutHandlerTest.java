/**
 * 
 */
package com.github.jochenw.afw.core.components;

import static org.junit.Assert.*;

import java.util.function.Consumer;

import org.junit.Test;

import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableRunnable;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.MutableBoolean;

/**
 * @author jwi
 *
 */
public class TimeoutHandlerTest {
	/** Test for a task, which finishes regularly, and on time. We assume, that the
	 * success handler is being called, with no exception, and the value true.
	 */
	@Test
	public void testSuccess() {
		final MutableBoolean success = new MutableBoolean();
		final FailableRunnable<?> action = () -> { Thread.sleep(5); };
		final FailableBiConsumer<Boolean,Throwable,?> successHandler = (b, t) -> {
        	assertNull(t);
        	assertTrue(b.booleanValue());
        	success.setValue(b.booleanValue());
        };
		FailableRunnable<?> expirationHandler = () -> success.setValue(false);
		Consumer<Throwable> exceptionHandler = (t) -> t.printStackTrace();
		new TimeoutHandler().runWithTimeout(action,
				                            5000,
				                            successHandler,
				                            expirationHandler,
				                            exceptionHandler);
		assertTrue(success.getValue());
	}

	/** Test for a task, which finishes with an exception. We assume, that the
	 * success handler is being called, passing the same exception.
	 */
	@Test
	public void testFinishedWithException() {
		final IllegalStateException e = new IllegalStateException();
		final Holder<Throwable> error = new Holder<>();
		final FailableRunnable<?> action = () -> { Thread.sleep(5); throw e; };
		final FailableBiConsumer<Boolean, Throwable, ?> successHandler = (b, t) -> {
			assertFalse(b.booleanValue());
			assertNotNull(t);
			error.set(t);
		};
		new TimeoutHandler().runWithTimeout(action,
				                            5000,
				                            successHandler,
				                            () -> error.set(null),
				                            (t) -> t.printStackTrace());
		assertSame(e, error.get());
	}

	/** Test for a task, which doesn't finish in time. We assume, that the
	 * expiration handler is being called.
	 */
	@Test
	public void testExpired() {
		final MutableBoolean success = new MutableBoolean();
		final FailableRunnable<?> action = () -> { Thread.sleep(50000); };
		final FailableBiConsumer<Boolean, Throwable, ?> successHandler = (b, t) -> { success.setValue(false); };
		new TimeoutHandler().runWithTimeout(action,
                5000,
                successHandler,
                () -> success.setValue(true),
                (t) -> t.printStackTrace());
		assertTrue(success.getValue());
	}

}
