/**
 * 
 */
package com.github.jochenw.afw.core.log.app;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.util.Functions.FailableCallable;
import com.github.jochenw.afw.core.util.Functions.FailableRunnable;
import com.github.jochenw.afw.core.util.Locks;
import com.github.jochenw.afw.core.util.Locks.Lockable;


/** Abstract base implementation of {@link IAppLog}.
 */
public abstract class AbstractAppLog implements IAppLog {
	private @Nonnull Level level;
	
	private final @Nonnull Lockable lockable = Locks.newLockable();

	protected AbstractAppLog(@Nonnull Level pLevel) {
		level = pLevel;
	}

	protected <O> O callReadLocked(FailableCallable<O,?> pCallable) {
		return getLockable().callReadLocked(pCallable);
	}

	protected <O> O callWriteLocked(FailableCallable<O,?> pCallable) {
		return getLockable().callWriteLocked(pCallable);
	}

	protected void runReadLocked(FailableRunnable<?> pRunnable) {
		getLockable().runReadLocked(pRunnable);
	}

	protected void runWriteLocked(FailableRunnable<?> pRunnable) {
		getLockable().runWriteLocked(pRunnable);
	}

	protected Lockable getLockable() {
		return lockable;
	}
	
	@Override
	public @Nonnull Level getLevel() {
		return callReadLocked(() -> level);
	}

	@Override
	public void setLevel(@Nonnull Level pLevel) {
		final Level lvl = Objects.requireNonNull(pLevel);
		runWriteLocked(() -> level = lvl);
	}

	@Override
	public boolean isEnabled(@Nonnull Level pLevel) {
		if (pLevel == null) {
			return false;
		} else {
			final FailableCallable<Boolean,?> callable = () -> {
				final Level lvl = level;
				return pLevel.ordinal() >= lvl.ordinal();
			};
			return callReadLocked(callable);
		}
	}
}
