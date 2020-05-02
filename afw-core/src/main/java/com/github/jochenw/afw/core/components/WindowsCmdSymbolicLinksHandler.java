/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Executor;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Processes;
import com.github.jochenw.afw.core.util.Streams;

/** Alternative implementation of {@link ISymbolicLinksHandler}, which is using
 * the builtin facilities of "cmd.exe". Obviously, this won't work on other
 * operating systems, but Windows.
 */
public class WindowsCmdSymbolicLinksHandler extends AbstractSymbolicLinksHandler {
	private final Executor executor = new Executor();

	@Override
	protected void createSymbolicDirLink(Path pTarget, Path pLink) {
		final Path dir = Objects.notNull(pLink.getParent(), () -> Paths.get("."));
		final String[] command = { "cmd", "/c", "mklink", "/j", "/d", pLink.getFileName().toString(),
				                   pTarget.toAbsolutePath().toString() };
		executor.run(dir, command, null, null, null, null);
	}

	@Override
	protected void createSymbolicFileLink(Path pTarget, Path pLink) {
		final String[] command = { "cmd", "/c", "mkdir", pLink.toString(),
                pTarget.toString() };
		executor.run(null, command, null, null);
	}

	@Override
	protected Path checkSymbolicLink(Path pPath) {
		final Path dir = Objects.notNull(pPath.getParent(), () -> Paths.get("."));
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final Consumer<InputStream> outConsumer = (in) -> { Streams.copy(in, baos); };
		executor.run(dir, new String[] {"cmd", "/c", "dir", "." }, null,
				     outConsumer, null, null);
		final String output = baos.toString();
		final Pattern pat = Pattern.compile("\\s+\\Q" + pPath.getFileName()
		                                    + "\\E\\s+\\[(.*)\\]\\s*$");
		try (StringReader sr = new StringReader(output);
			 BufferedReader br = new BufferedReader(sr)) {
			for (;;) {
				final String line = br.readLine();
				if (line == null) {
					return null;
				} else {
					final Matcher m = pat.matcher(line);
					if (m.find()) {
						return Paths.get(m.group(1));
					}
				}
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
