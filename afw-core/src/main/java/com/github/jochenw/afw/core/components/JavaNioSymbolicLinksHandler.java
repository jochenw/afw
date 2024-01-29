/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Exceptions;

/** Simple implementation of the {@link ISymbolicLinksHandler}, based on the
 * {@link Files} class. As of Java 8, this is not supposed to be usable on
 * Windows.
 */
public class JavaNioSymbolicLinksHandler implements ISymbolicLinksHandler {
	@Override
	public void createDirectoryLink(@NonNull Path pTarget, @NonNull Path pLink) {
		try {
			Files.createSymbolicLink(pLink, pTarget.toAbsolutePath());
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public void createFileLink(@NonNull Path pTarget, @NonNull Path pLink) throws UnsupportedOperationException {
		try {
			Files.createSymbolicLink(pLink, pTarget.toAbsolutePath());
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public Path checkLink(@NonNull Path pPath) {
		try {
			if (Files.isSymbolicLink(pPath)) {
				return Files.readSymbolicLink(pPath);
			} else {
				return null;
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public void removeLink(@NonNull Path pPath) {
		try {
			Files.delete(pPath);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

}
