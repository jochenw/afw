/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableRunnable;
import com.github.jochenw.afw.core.util.Exceptions;


/**
 * The {@link TimeoutHandler} provides a clean, and comfortable way of implementing a task,
 * which is restrained by a timeout.
 */
public class TimeoutHandler {
	/** Creates a new instance.
	 */
	public TimeoutHandler() {}

	/** Called to execute the given {@link FailableRunnable task}. If the task finishes within
	 * the given amount of milliseconds, invokes the {@link FailableRunnable success handler}.
	 * If not, and the task expires, invokes the {@link FailableRunnable expiration handler}.
	 * If either of these handlers fail, the {@link Consumer exception handler} is being
	 * invoked.
	 * @param pTask The task, which is being executed. Note, that there is no specified way
	 *   to interrupt the task. So, if the timeout is triggered, the task may still be running
	 *   for an indefinite amount of time.
	 * @param pWaitMillis The timeout. The task is supposed to terminate within the given number
	 *   of milliseconds.
	 * @param pSuccessHandler A handler, which is being executed, if the task has finished
	 *   within the permitted time frame. The handler will receive a Boolean: TRUE = The task
	 *   has finished successfully, and the second argument is null, or FALSE = The task has
	 *   finished by throwing an exception, which is being passed as the second argument.
	 * @param pExpirationHandler A handler, which is being executed, if the timeout has been
	 *   triggered, and the task is possibly still ongoing. Ideally, the expiration handler
	 *   should implement a way to terminate the task.
	 * @param pExceptionHandler Either of the handlers has failed, and thrown an exception,
	 *   which is being passed as an argument to the exception handlers. <em>Note:</em> The
	 *   exception handler <em>must not</em> fail in the sense, that it must not throw an
	 *   exception. If it does, that exception is being silently ignored. 
	 */
	public void runWithTimeout(@NonNull FailableRunnable<?> pTask, long pWaitMillis,
			                   @Nullable FailableBiConsumer<Boolean,Throwable,?> pSuccessHandler,
			                   @Nullable FailableRunnable<?> pExpirationHandler,
			                   @Nullable Consumer<Throwable> pExceptionHandler) {
		final CompletableFuture<Object> completableFuture = CompletableFuture.supplyAsync(() -> {
			try {
				pTask.run();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			return null;
		});
		try {
			completableFuture.get(pWaitMillis, TimeUnit.MILLISECONDS);
			if (pSuccessHandler != null) {
				run(() -> pSuccessHandler.accept(Boolean.TRUE, null), pExceptionHandler);
			}
		} catch (TimeoutException e) {
			run(pExpirationHandler, pExceptionHandler);
		} catch (ExecutionException e) {
			if (pSuccessHandler != null) {
				Throwable t = e.getCause();
				if (t instanceof UndeclaredThrowableException) {
					t = t.getCause();
				} else if (t instanceof UncheckedIOException) {
					t = t.getCause();
				}
				final Throwable th = t;
				run(() -> pSuccessHandler.accept(Boolean.FALSE, th), pExceptionHandler);
			}
		} catch (InterruptedException e) {
			throw Exceptions.show(e);
		}
	}

	private void run(@Nullable FailableRunnable<?> pRunnable, @Nullable Consumer<Throwable> pExceptionHandler) {
		if (pRunnable != null) {
			try {
				pRunnable.run();
			} catch (Throwable t) {
				if (pExceptionHandler != null) {
					try {
						pExceptionHandler.accept(t);
					} catch (Throwable th) {
						/* Ignore this. The Exception handler is supposed to care for this,
						 * and not throw an exception.
						 */
					}
				}
			}
		}
	}
}
