/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Interface of  component for managing symbolic links.
 */
public interface ISymbolicLinksHandler {
	/**
	 * Creates a symbolic link {@code pLink}, which points to the directory {@code pTarget}.
	 * @param pTarget The actual directory, to which the symbolic link will point.
	 * @param pLink The symbolic link, which is being created.
	 * @throws IllegalArgumentException The target is not a directory.
	 */
	public void createDirectoryLink(@Nonnull Path pTarget, @Nonnull Path pLink);
	/**
	 * Creates a symbolic link {@code pLink}, which points to the file {@code pTarget}.
	 * @param pTarget The actual file, to which the symbolic link will point.
	 * @param pLink The symbolic link, which is being created.
	 * @throws UnsupportedOperationException The handler doesn't implement symbolic links for files.
	 * @throws IllegalArgumentException The target is not a file.
	 */
	public void createFileLink(@Nonnull Path pTarget, @Nonnull Path pLink) throws UnsupportedOperationException;
	/**
	 * Creates a symbolic link {@code pLink}, which points to the file, or directory, {@code pTarget}.
	 * @param pTarget The actual file, or directory, to which the symbolic link will point.
	 * @param pLink The symbolic link, which is being created.
	 * @throws UnsupportedOperationException The handler doesn't implement symbolic links for this type
	 *   of files.
	 * @throws IllegalArgumentException The target does not exist.
	 */
	public default void createLink(@Nonnull Path pTarget, @Nonnull Path pLink) throws UnsupportedOperationException {
		if (!Files.exists(pTarget)) {
			throw new IllegalArgumentException("The target does not exist: " + pTarget);
		}
		if (Files.isDirectory(pTarget)) {
			createDirectoryLink(pTarget, pLink);
		} else {
			createFileLink(pTarget, pLink);
		}
	}
	/**
	 * If the given path is a symbolic link, returns the target, to which the symbolic link
	 * refers. Otherwise, returns null.
	 * @param pPath The possible symbolic link, which is being checked.
	 * @return The target, to which the symbolic link refers, if the given path is a symbolic link.
	 *   Otherwise, returns null.
	 */
	public @Nullable Path checkLink(@Nonnull Path pPath);
	/** Deletes the given symbolic link.
	 * @param pPath The symbolic link, which is being deleted.
	 */
	public void removeLink(@Nonnull Path pPath);
}
