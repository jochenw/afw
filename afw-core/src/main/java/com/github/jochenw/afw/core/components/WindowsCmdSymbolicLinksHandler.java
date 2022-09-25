/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.exec.Executor;
import com.github.jochenw.afw.core.exec.ExecutorBuilder;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;


/** Alternative implementation of {@link ISymbolicLinksHandler}, which is using
 * the builtin facilities of "cmd.exe". Obviously, this won't work on other
 * operating systems, but Windows.
 */
public class WindowsCmdSymbolicLinksHandler extends AbstractSymbolicLinksHandler {
	/** Name of "cmd.exe". We need this, because we are, in fact, launching cmd.exe,
	 * to execute internal commands like mklink, that don't exist as separate .exe
	 * files.
	 */
	public static final String CMD_EXE = "cmd.exe";

	protected void run(Path pDir, String[] pCommand) {
		final ExecutorBuilder eb = Executor.builder();
		if (pDir != null) {
			eb.directory(pDir);
		}
		eb.exec(CMD_EXE);
		eb.arg("/c");
		eb.args(pCommand);
	}

	@Override
	protected void createSymbolicDirLink(@Nonnull Path pTarget, @Nonnull Path pLink) {
		final @Nonnull Path target = Objects.requireNonNull(pTarget, "Target");
		final @Nonnull Path link = Objects.requireNonNull(pLink, "Link");
		final @Nonnull Path fileName = Objects.requireNonNull(link.getFileName(), "File name"); 
		final Path dir = Objects.notNull(pLink.getParent(), () -> Paths.get("."));
		final String[] command = { "mklink", "/j", "/d", fileName.toString(),
				                   target.toAbsolutePath().toString() };
		run(dir, command);
	}

	@Override
	protected void createSymbolicFileLink(Path pTarget, Path pLink) {
		final String[] command = { "mkdir", pLink.toString(),
                pTarget.toString() };
		run(null, command);
	}

	@Override
	protected Path checkSymbolicLink(Path pPath) {
		try {
			if (Files.exists(pPath, LinkOption.NOFOLLOW_LINKS)) {
				final BasicFileAttributes bfa = Files.readAttributes(pPath, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
				if (!bfa.isSymbolicLink()  &&  bfa.isOther()) {
					return pPath.toRealPath();
				} else {
					return null;
				}
			} else {
				return null;
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	protected void removeSymbolicLink(Path pPath) {
		try {
			Files.delete(pPath);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
