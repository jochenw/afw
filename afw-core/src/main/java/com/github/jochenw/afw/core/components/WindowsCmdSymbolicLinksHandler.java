/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.nio.file.attribute.FileAttributeView;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Executor;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.core.util.Streams;

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
	private final Executor executor = new Executor();

	@Override
	protected void createSymbolicDirLink(Path pTarget, Path pLink) {
		final Path dir = Objects.notNull(pLink.getParent(), () -> Paths.get("."));
		final String[] command = { CMD_EXE, "/c", "mklink", "/j", "/d", pLink.getFileName().toString(),
				                   pTarget.toAbsolutePath().toString() };
		executor.run(dir, command, null, null, null, null);
	}

	@Override
	protected void createSymbolicFileLink(Path pTarget, Path pLink) {
		final String[] command = { CMD_EXE, "/c", "mkdir", pLink.toString(),
                pTarget.toString() };
		executor.run(null, command, null, null);
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
