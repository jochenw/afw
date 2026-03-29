/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;


/** Abstract base implementation of {@link ISymbolicLinksHandler}.
 */
public abstract class AbstractSymbolicLinksHandler implements ISymbolicLinksHandler {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	protected AbstractSymbolicLinksHandler() {}

	/** Called to assert, that the given path is actually a directory.
     * @param pTarget The path, which is being checked.
     * @throws IllegalArgumentException The path doesn't refer to a
     *   directory.
     */
	protected void assertDirectory(Path pTarget) {
		final Path target = Objects.requireNonNull(pTarget, "The target must not be null.");
		if (!Files.isDirectory(target)) {
			throw new IllegalArgumentException("The target does not exist, or is not a directory: " + pTarget);
		}
	}

        /** Called to assert, that the given path is actually a file.
         * @param pTarget The path, which is being checked.
         * @throws IllegalArgumentException The path doesn't refer to a
         *   file.
         */
	protected void assertFile(Path pTarget) {
		final Path target = Objects.requireNonNull(pTarget, "The target must not be null.");
		if (!Files.exists(target)  ||  Files.isDirectory(target)) {
			throw new IllegalArgumentException("The target does not exist, or is not a file: " + pTarget);
		}
	}

        /** Creates a symbolic directory link.
         * @param pTarget The directory, to which the link is supposed to
         *   refer.
         * @param pLink The path of the link, that is being created.
         */
        protected abstract void createSymbolicDirLink(@NonNull Path pTarget, @NonNull Path pLink);
        /** Creates a symbolic file link.
         * @param pTarget The file, to which the link is supposed to
         *   refer.
         * @param pLink The path of the link, that is being created.
         */
	protected abstract void createSymbolicFileLink(@NonNull Path pTarget, @NonNull Path pLink);
        /** Checks, whether the given object is a symbolic link.
         * @param pPath The path, which is being checked.
         * @return If the object is a symbolic link: Path of the object, to which the link refers.
         *   Otherwise null.
         */
	protected abstract @Nullable Path checkSymbolicLink(@NonNull Path pPath);
        /** Removes a symbolic link.
         * @param pPath Path of the symbolic link, that is being removed.
         */
	protected abstract void removeSymbolicLink(@NonNull Path pPath);

        /** Creates a directory link.
         * @param pTarget Path of the directory, to which the created
         *   link should refer to.
         * @param pLink Path of the link, that is being created.
         */
	@Override
	public void createDirectoryLink(@NonNull Path pTarget, @NonNull Path pLink) {
		assertDirectory(pTarget);
		createSymbolicDirLink(pTarget, pLink);
	}

        /** Creates a file link.
         * @param pTarget Path of the file, to which the created
         *   link should refer to.
         * @param pLink Path of the link, that is being created.
         */
	@Override
	public void createFileLink(@NonNull Path pTarget, @NonNull Path pLink) throws UnsupportedOperationException {
		assertFile(pTarget);
		createSymbolicFileLink(pTarget, pLink);
	}

        
	@Override
	public Path checkLink(@NonNull Path pPath) {
		final @NonNull Path path = Objects.requireNonNull(pPath, "The path must not be null.");
		return checkSymbolicLink(path);
	}

	@Override
	public void removeLink(@NonNull Path pPath) {
		final Path path = Objects.requireNonNull(pPath, "The path must not be null.");
		removeSymbolicLink(path);
	}

}
