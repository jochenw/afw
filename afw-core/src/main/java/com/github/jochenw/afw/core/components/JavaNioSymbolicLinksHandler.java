/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.nio.file.Files;
import java.nio.file.Path;

import com.github.jochenw.afw.core.util.Exceptions;

/** Simple implementation of the {@link ISymbolicLinksHandler}, based on the
 * {@link Files} class. As of Java 8, this is not supposed to be usable on
 * Windows.
 */
public class JavaNioSymbolicLinksHandler implements ISymbolicLinksHandler {
	@Override
	public void createDirectoryLink(Path pTarget, Path pLink) {
		try {
			Files.createSymbolicLink(pLink, pTarget);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public void createFileLink(Path pTarget, Path pLink) throws UnsupportedOperationException {
		try {
			Files.createSymbolicLink(pLink, pTarget);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public Path checkLink(Path pPath) {
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
	public void removeLink(Path pPath) {
		try {
			Files.delete(pPath);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

}
