/**
 * 
 */
package com.github.jochenw.afw.core.log.app;

import java.util.Objects;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.function.Functions.FailableCallable;
import com.github.jochenw.afw.core.function.Functions.FailableRunnable;
import com.github.jochenw.afw.core.util.Locks;
import com.github.jochenw.afw.core.util.Locks.Lockable;


/** Abstract base implementation of {@link IAppLog}.
 */
public abstract class AbstractAppLog implements IAppLog {
	private @NonNull Level level;
	
	private final @NonNull Lockable lockable = Locks.newLockable();

	/** Creates a new instance with the given log level.
	 * @param pLevel The app loggers logging level.
	 */
	protected AbstractAppLog(@NonNull Level pLevel) {
		level = pLevel;
	}

	/** Calls the given {@link FailableCallable callable} to compute a result object, while
	 *   holding a shared lock.
	 * @param <O> Type of the result object.
	 * @param pCallable The {@link FailableCallable callable}, that is computing the result object.
	 * @return The computed result object, that has been obtained by invoking the
	 *   {@link FailableCallable callable}.
	 */
	protected <O> O callReadLocked(FailableCallable<O,?> pCallable) {
		return getLockable().callReadLocked(pCallable);
	}

	/** Calls the given {@link FailableCallable callable} to compute a result object, while
	 *   holding an exclusive lock.
	 * @param <O> Type of the result object.
	 * @param pCallable The {@link FailableCallable callable}, that is computing the result object.
	 * @return The computed result object, that has been obtained by invoking the
	 *   {@link FailableCallable callable}.
	 */
	protected <O> O callWriteLocked(FailableCallable<O,?> pCallable) {
		return getLockable().callWriteLocked(pCallable);
	}

	/** Calls the given {@link FailableRunnable runnable} to execute an action, while holding a shared
	 * lock.
	 * @param pRunnable The {@link FailableRunnable runnable}, that is perform,ing the
	 *   action.
	 */
	protected void runReadLocked(FailableRunnable<?> pRunnable) {
		getLockable().runReadLocked(pRunnable);
	}

	/** Calls the given {@link FailableRunnable runnable} to execute an action, while holding an
	 *  exclusive lock.
	 * @param pRunnable The {@link FailableRunnable runnable}, that is perform,ing the
	 *   action.
	 */
	protected void runWriteLocked(FailableRunnable<?> pRunnable) {
		getLockable().runWriteLocked(pRunnable);
	}

	/** Returns the {@link Lockable lock}, that is used to obtain shared, or exclusive locks.
	 * @return The object, that provides shared, or exclusive locks.
	 */
	protected Lockable getLockable() {
		return lockable;
	}
	
	@Override
	public @NonNull Level getLevel() {
		return callReadLocked(() -> level);
	}

	@Override
	public void setLevel(@NonNull Level pLevel) {
		final Level lvl = Objects.requireNonNull(pLevel);
		runWriteLocked(() -> level = lvl);
	}

	@Override
	public boolean isEnabled(@NonNull Level pLevel) {
		if (pLevel == null) {
			return false;
		} else {
			final FailableCallable<Boolean,?> callable = () -> {
				return Boolean.valueOf(isEnabledLocked(pLevel));
			};
			return callReadLocked(callable).booleanValue();
		}
	}

	/** Same as {@link #isEnabled(com.github.jochenw.afw.core.log.app.IAppLog.Level)},
	 * except that this method, assumes, that the caller already holds an exclusive, or shared lock.
	 * @param pLevel The logging level, that is being tested.
	 * @return True, if the given logging level is enabled.
	 */
	protected boolean isEnabledLocked(@NonNull Level pLevel) {
		if (pLevel == null) {
			return false;
		} else {
			return pLevel.ordinal() >= level.ordinal();
		}
	}
}
