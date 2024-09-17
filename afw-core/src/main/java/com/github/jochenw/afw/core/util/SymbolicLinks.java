/**
 * 
 */
package com.github.jochenw.afw.core.util;

import java.nio.file.Path;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.components.DefaultSymbolicLinksHandler;
import com.github.jochenw.afw.core.components.ISymbolicLinksHandler;

/** Utility class for working with symbolic links.
 */
public class SymbolicLinks {
	/** Creates a new instance. Private constructor, because
	 * all methods are static.
	 */
	private SymbolicLinks() {}

	private static final ISymbolicLinksHandler handler = new DefaultSymbolicLinksHandler();

	/**
	 * Creates a symbolic link {@code pLink}, which points to the directory {@code pTarget}.
	 * @param pTarget The actual directory, to which the symbolic link will point.
	 * @param pLink The symbolic link, which is being created.
	 * @throws IllegalArgumentException The target is not a directory.
	 */
	public static void createDirectoryLink(@NonNull Path pTarget, @NonNull Path pLink) {
		handler.createDirectoryLink(pTarget, pLink);
	}

	/**
	 * Creates a symbolic link {@code pLink}, which points to the file {@code pTarget}.
	 * @param pTarget The actual file, to which the symbolic link will point.
	 * @param pLink The symbolic link, which is being created.
	 * @throws UnsupportedOperationException The handler doesn't implement symbolic links for files.
	 * @throws IllegalArgumentException The target is not a file.
	 */
	public static void createFileLink(@NonNull Path pTarget, @NonNull Path pLink) throws UnsupportedOperationException {
		handler.createFileLink(pTarget, pLink);
	}

	/**
	 * Creates a symbolic link {@code pLink}, which points to the file, or directory, {@code pTarget}.
	 * @param pTarget The actual file, or directory, to which the symbolic link will point.
	 * @param pLink The symbolic link, which is being created.
	 * @throws UnsupportedOperationException The handler doesn't implement symbolic links for this type
	 *   of files.
	 * @throws IllegalArgumentException The target does not exist.
	 */
	public static void createLink(@NonNull Path pTarget, @NonNull Path pLink) throws UnsupportedOperationException {
		handler.createLink(pTarget, pLink);
	}

	/**
	 * If the given path is a symbolic link, returns the target, to which the symbolic link
	 * refers. Otherwise, returns null.
	 * @param pPath The possible symbolic link, which is being checked.
	 * @return The target, to which the symbolic link refers, if the given path is a symbolic link.
	 *   Otherwise, returns null.
	 */
	public static @Nullable Path checkLink(@NonNull Path pPath) {
		return handler.checkLink(pPath);
	}

	/** Deletes the given symbolic link.
	 * @param pPath The symbolic link, which is being deleted.
	 */
	public static void removeLink(@NonNull Path pPath) {
		handler.removeLink(pPath);
	}

}
