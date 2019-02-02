package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;

import javax.annotation.Nonnull;


public class DirectoryScanner {
	public interface Context {
		/** Returns the base directory, which is currently being scanned.
		 */
		@Nonnull Path getBaseDir();
		BasicFileAttributes getAttrs();
		/** Returns the path of a file, which has been detected within the
		 * base directory.
		 */
		@Nonnull Path getFile();
		/** Returns the relative path of the file, within the base directory.
		 * The path is in a normalized form, using "/" as the separator, as
		 * is the case for a Unix file system.
		 */
		@Nonnull String getUri();
	}
	public interface Listener {
		/** This method is being invoked for every file, which the directory
		 * scanner has detected.
		 * @param pContext The context, which provides information on the detected
		 * file.
		 */
		void accept(Context pContext);
	}
	/** Extension of {@link DirectoryScanner.Listener}, which is being invoked
	 * for files, <em>and</em> directories. Use {@link Context#getAttrs()} to
	 * distinguish between files, and directories.
	 */
	public interface DirListener {
	}

	private static class ContextImpl implements Context {
		private @Nonnull Path baseDir;
		private @Nonnull Path file;
		private @Nonnull String uri;
		private @Nonnull BasicFileAttributes attrs;

		@Override
		public Path getBaseDir() {
			return baseDir;
		}

		@Override
		public Path getFile() {
			return file;
		}

		@Override
		public String getUri() {
			return uri;
		}

		@Override
		public BasicFileAttributes getAttrs() {
			return attrs;
		}
	}

	public void scan(@Nonnull Path pBaseDir, IMatcher[] pIncludes, IMatcher[] pExcludes, Listener pListener) {
		if (!Files.isDirectory(pBaseDir)) {
			throw new IllegalArgumentException("Directory not found, or otherwise unreadable: " + pBaseDir);
		}
		final Predicate<String> matcher = newMatcher(pIncludes, pExcludes);
		final ContextImpl ctx = new ContextImpl();
		ctx.baseDir = pBaseDir;
		final FileVisitor<Path> fv = new SimpleFileVisitor<Path>() {
			final boolean dirListener = (pListener instanceof DirListener);
			int level;
			final StringBuilder sb = new StringBuilder();

			@Override
			public FileVisitResult preVisitDirectory(Path pDir, BasicFileAttributes pAttrs) throws IOException {
				final int l = level++;
				switch (l) {
				case 0:
					// Do nothing. This is the base directory, and we do not want to include that in the URI.
					break;
				case 1:
					// Top level of the URI. Do not include the separator ('/') in the URI.
					sb.append(pDir.getFileName().toString());
					break;
				default:
					// Lower level of the URI. Include a separator.
					sb.append('/');
					sb.append(pDir.getFileName().toString());
					break;
				}
				if (dirListener) {
					ctx.file = pDir;
					ctx.uri = sb.toString();
					ctx.attrs = pAttrs;
					pListener.accept(ctx);
				}
				return super.preVisitDirectory(pDir, pAttrs);
			}

			@Override
			public FileVisitResult visitFile(Path pFile, BasicFileAttributes pAttrs) throws IOException {
				final int len = sb.length();
				sb.append('/');
				sb.append(pFile.getFileName().toString());
				ctx.file = pFile;
				ctx.uri = sb.toString();
				ctx.attrs = pAttrs;
				if (matcher.test(ctx.uri)) {
					pListener.accept(ctx);
				}
				sb.setLength(len);
				return super.visitFile(pFile, pAttrs);
			}

			@Override
			public FileVisitResult postVisitDirectory(Path pDir, IOException pExc) throws IOException {
				final int l = --level;
				switch (l) {
				case 0:
					// Do nothing. This is the base directory, and we didn't include that in the URI.
					break;
				case 1:
					// Top level of the URI. Do not remove the separator from the URI.
					sb.setLength(0);
					break;
				default:
					// Lower level of the URI. Include a separator.
					final int offset = sb.lastIndexOf("/");
					if (offset == -1) {
						throw new IllegalStateException("Separator not found in the URI: " + sb);
					}
					sb.setLength(offset);
					break;
				}
				return super.postVisitDirectory(pDir, pExc);
			}
		};
		try {
			Files.walkFileTree(pBaseDir, fv);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected Predicate<String> newMatcher(IMatcher[] pIncludes, IMatcher[] pExcludes) {
		if (isTrivial(pIncludes)) {
			if (isTrivial(pExcludes)) {
				return com.github.jochenw.afw.core.util.Predicates.alwaysTrue();
			} else {
				return newPredicate(pExcludes).negate();
			}
		} else {
			if (isTrivial(pExcludes)) {
				return newPredicate(pIncludes);
			} else {
				final Predicate<String> i = newPredicate(pIncludes);
				final Predicate<String> e = newPredicate(pExcludes);
				return (s) -> i.test(s)  &&  !e.test(s);
			}
		}
	}

	protected boolean isTrivial(IMatcher[] pMatchers) {
		if (pMatchers == null) {
			return true;
		}
		if (pMatchers.length == 0) {
			return true;
		}
		for (IMatcher m : pMatchers) {
			if (!m.isMatchingAll()) {
				return false;
			}
		}
		return true;
	}

	protected Predicate<String> newPredicate(IMatcher[] pMatchers) {
		return (s) -> {
			for (IMatcher m : pMatchers) {
				if (m.matches(s)) {
					return true;
				}
			}
			return false;
		};
	}
}
