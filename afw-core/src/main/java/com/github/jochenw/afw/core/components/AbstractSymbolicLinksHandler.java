/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/** Abstract base implementation of {@link ISymbolicLinksHandler}.
 */
public abstract class AbstractSymbolicLinksHandler implements ISymbolicLinksHandler {
	protected void assertDirectory(Path pTarget) {
		final Path target = Objects.requireNonNull(pTarget, "The target must not be null.");
		if (!Files.isDirectory(target)) {
			throw new IllegalArgumentException("The target does not exist, or is not a directory: " + pTarget);
		}
	}

	protected void assertFile(Path pTarget) {
		final Path target = Objects.requireNonNull(pTarget, "The target must not be null.");
		if (!Files.exists(target)  ||  Files.isDirectory(target)) {
			throw new IllegalArgumentException("The target does not exist, or is not a file: " + pTarget);
		}
	}

	protected abstract void createSymbolicDirLink(@Nonnull Path pTarget, @Nonnull Path pLink);
	protected abstract void createSymbolicFileLink(@Nonnull Path pTarget, @Nonnull Path pLink);
	protected abstract @Nullable Path checkSymbolicLink(@Nonnull Path pPath);
	protected abstract void removeSymbolicLink(@Nonnull Path pPath);

	@Override
	public void createDirectoryLink(@Nonnull Path pTarget, @Nonnull Path pLink) {
		assertDirectory(pTarget);
		createSymbolicDirLink(pTarget, pLink);
	}

	@Override
	public void createFileLink(Path pTarget, Path pLink) throws UnsupportedOperationException {
		assertFile(pTarget);
		createSymbolicFileLink(pTarget, pLink);
	}

	@Override
	public Path checkLink(Path pPath) {
		final Path path = Objects.requireNonNull(pPath, "The path must not be null.");
		return checkSymbolicLink(path);
	}

	@Override
	public void removeLink(Path pPath) {
		final Path path = Objects.requireNonNull(pPath, "The path must not be null.");
		removeSymbolicLink(path);
	}

}
