/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.exec.Executor;
import com.github.jochenw.afw.core.exec.ExecutorBuilder;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;


/** Alternative implementation of {@link ISymbolicLinksHandler}, which is using
 * the builtin facilities of "cmd.exe". Obviously, this won't work on other
 * operating systems, but Windows.
 */
public class WindowsCmdSymbolicLinksHandler extends AbstractSymbolicLinksHandler {
	/** Creates a new instance.
	 */
	public WindowsCmdSymbolicLinksHandler() {}

	/** Name of "cmd.exe". We need this, because we are, in fact, launching cmd.exe,
	 * to execute internal commands like mklink, that don't exist as separate .exe
	 * files.
	 */
	public static final String CMD_EXE = "cmd.exe";

	/** Executes a {@link #CMD_EXE "cmd /c"} invocation with the given arguments.
	 * @param pCommand The command line, that is being executed.
	 * @param pDir The directory, where to execute the command.
	 */
	protected void run(Path pDir, String[] pCommand) {
		final ExecutorBuilder eb = Executor.builder();
		if (pDir != null) {
			eb.directory(pDir);
		}
		eb.exec(CMD_EXE);
		eb.arg("/c");
		eb.args(pCommand);
		eb.build().run();
	}

	@Override
	protected void createSymbolicDirLink(@NonNull Path pTarget, @NonNull Path pLink) {
		final @NonNull Path target = Objects.requireNonNull(pTarget, "Target");
		final @NonNull Path link = Objects.requireNonNull(pLink, "Link");
		final @NonNull Path fileName = Objects.requireNonNull(link.getFileName(), "File name"); 
		final Path dir = Objects.notNull(pLink.getParent(), () -> Paths.get("."));
		final String[] command = { "mklink", "/j", "/d", fileName.toString(),
				                   target.toAbsolutePath().toString() };
		run(dir, command);
	}

	@Override
	protected void createSymbolicFileLink(@NonNull Path pTarget, @NonNull Path pLink) {
		final String[] command = { "mkdir", pLink.toString(),
                pTarget.toString() };
		run(null, command);
	}

	@Override
	protected Path checkSymbolicLink(@NonNull Path pPath) {
		try {
			if (Files.exists(pPath)) {
				@SuppressWarnings("null")
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
	protected void removeSymbolicLink(@NonNull Path pPath) {
		try {
			Files.delete(pPath);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
