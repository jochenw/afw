/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.nio.file.Path;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Systems;


/** Default implementation of {@link ISymbolicLinksHandler}. Internally, this acts as a proxy
 * for an instance of {@link WindowsCmdSymbolicLinksHandler}, or {@link JavaNioSymbolicLinksHandler},
 * depending on the current operating system.
 */
public class DefaultSymbolicLinksHandler extends AbstractSymbolicLinksHandler {
	private final ISymbolicLinksHandler handler = Systems.isWindows() ?
			new WindowsCmdSymbolicLinksHandler() : new JavaNioSymbolicLinksHandler();

	@Override
	protected void createSymbolicDirLink(@NonNull Path pTarget, @NonNull Path pLink) {
		handler.createDirectoryLink(pTarget, pLink);
	}

	@Override
	protected void createSymbolicFileLink(@NonNull Path pTarget, @NonNull Path pLink) {
		handler.createFileLink(pTarget, pLink);
	}

	@Override
	protected Path checkSymbolicLink(@NonNull Path pPath) {
		return handler.checkLink(pPath);
	}

	@Override
	protected void removeSymbolicLink(@NonNull Path pPath) {
		handler.removeLink(pPath);
	}
}
