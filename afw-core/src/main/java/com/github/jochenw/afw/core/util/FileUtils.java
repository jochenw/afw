/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.props.DefaultInterpolator;
import com.github.jochenw.afw.core.props.Interpolator;


/** Utility class for working with files, and directories.
 */
public class FileUtils {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	public FileUtils() {}

	/** <p>Constructs a path, which is guaranteed to be within the base directory {@code pBaseDir}.</p>
	 * <p>The use case is a web application, which is supposed to read a file within that directory.
	 * However, for security reasons, we must guarantee, that the result must not be outside.</p>
	 * @param pBaseDir The base directory, within the result is expected to live.
	 * @param pRelativePath The path of the result, relative to the base directory.
	 * @return Path of a file within the base directory.
	 * @throws IllegalArgumentException The parameter {@code pRelativePath} is an absolute file
	 *   name, and outside the base directory.
	 */
	public static Path resolve(Path pBaseDir, String pRelativePath) {
		final Path path = java.nio.file.Paths.get(pRelativePath);
		if (path.isAbsolute()) {
			if (isWithin(pBaseDir, path)) {
				return path;
			} else {
				throw new IllegalArgumentException("Invalid path: " + path
						+ " (Not within " + pBaseDir + ")");
			}
		} else {
			return pBaseDir.resolve(path);
		}
	}

	/**
	 * Replacement for {@link java.nio.file.Files#isDirectory(Path, java.nio.file.LinkOption...)}.
	 * This is here, so that the current class ({@link FileUtils}) can be imported without the need
	 * to qualify {@link java.nio.file.Files}, when using it.
	 * @param pPath The path object, which is being checked.
	 * @param pLinkOptions The link options, which are being considered.
	 * @return True, if {@code pPath} is a directory, considering the given link options,
	 *   otherwise false.
	 */
	public static boolean isDirectory(Path pPath, LinkOption... pLinkOptions) {
		return java.nio.file.Files.isDirectory(pPath, pLinkOptions);
	}

	/**
	 * Replacement for {@link java.nio.file.Files#isRegularFile(Path, java.nio.file.LinkOption...)}.
	 * This is here, so that the current class ({@link FileUtils}) can be imported without the need
	 * to qualify {@link java.nio.file.Files}, when using it.
	 * @param pPath The path object, which is being checked.
	 * @param pLinkOptions The link options, which are being considered.
	 * @return True, if {@code pPath} is a regular file, considering the given link options,
	 *   otherwise false.
	 */
	public static boolean isRegularFile(Path pPath, LinkOption... pLinkOptions) {
		return java.nio.file.Files.isRegularFile(pPath, pLinkOptions);
	}

	/** Copies the directory {@code pSource}, to the directory {@code pTrget}.
	 * including contents. Files in the target directory may be overwritten, but
	 * will not be deleted. (This is a copy operation, not a synchronization.)
	 * @param pSource The source directory.
	 * @param pTarget The target directory.
	 * @throws UncheckedIOException Copying failed.
	 */
	public static void copyDirectory(final Path pSource, final Path pTarget) {
		try {
			java.nio.file.Files.walk(pSource).forEach(a -> {
				final Path relativePath = pSource.relativize(a);
				final Path b = pTarget.resolve(relativePath);
				try {
					if (java.nio.file.Files.isDirectory(a)) {
						java.nio.file.Files.createDirectories(b);
					} else {
						java.nio.file.Files.copy(a, b, StandardCopyOption.REPLACE_EXISTING);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** Copies the directory {@code pSource}, to the directory {@code pTarget}.
	 * including contents. Files in the target directory may be overwritten, but
	 * will not be deleted. (This is a copy operation, not a synchronization.)
	 * @param pSource The source directory.
	 * @param pTarget The target directory.
	 * @param pMapper A mapping of property names to property values.
	 *   which may be contained, and replaced in the copied files.
	 * @throws UncheckedIOException Copying failed.
	 */
	public static void copyDirectory(final Path pSource, final Path pTarget,
			                         @Nullable Function<@NonNull String, @Nullable String> pMapper) {
		try {
			java.nio.file.Files.walk(pSource).forEach(a -> {
				final Path relativePath = pSource.relativize(a);
				final Path b = pTarget.resolve(relativePath);
				try {
					if (java.nio.file.Files.isDirectory(a)) {
						java.nio.file.Files.createDirectories(b);
					} else {
						if (pMapper == null) {
							try (InputStream in = Files.newInputStream(a);
								 OutputStream out = Files.newOutputStream(b)) {
								Streams.copy(in, out);
							}
						} else {
							final Interpolator interpolator = new DefaultInterpolator(pMapper);
							final StringWriter sw = new StringWriter();
							try (Reader reader = java.nio.file.Files.newBufferedReader(a, StandardCharsets.UTF_8)) {
								Streams.copy(reader, sw);
							}
							@SuppressWarnings("null")
							@NonNull String string = sw.toString();
							final String contents = interpolator.interpolate(string);
							try (Writer w = java.nio.file.Files.newBufferedWriter(b)) {
								w.write(contents);
							}
						}
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** Checks, whether a given file, or directory, is located within another directory.
	 * @param pDir The directory, which might contain a file, or another directory.
	 * @param pPath The file, or directory, which is being questioned.
	 * @return True, if the file, or directory {@code pPath} is located within the
	 *     directory {@code pDir}, otherwise false.
	 */
	public static boolean isWithin(Path pDir, Path pPath) {
		Path p = pPath;
		while (p != null) {
			if (p.equals(pDir)) {
				return true;
			} else {
				p = p.getParent();
			}
		}
		return false;
	}

	/**
	 * Removes the given directory completely, including contents.
	 * @param pDir The directory being removed.
	 * @throws UncheckedIOException Removal failed.
	 */
	public static void removeDirectory(Path pDir) {
		final FileVisitor<Path> sfv = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path pFile, BasicFileAttributes pAttrs) throws IOException {
				java.nio.file.Files.delete(pFile);
				return super.visitFile(pFile, pAttrs);
			}
	
			@Override
			public FileVisitResult postVisitDirectory(Path pDir, IOException pExc) throws IOException {
				java.nio.file.Files.delete(pDir);
				return super.postVisitDirectory(pDir, pExc);
			}
		};
		try {
			java.nio.file.Files.walkFileTree(pDir, sfv);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Checks, whether the given file's directory exists. If not, creates it.
	 * @param pFile The file, for which to create the directory.
	 * @throws NullPointerException The parameter {@code pFile} is null.
	 * @throws UncheckedIOException The directory could not be created.
	 */
	public static void createDirectoryFor(Path pFile) {
		final Path file = Objects.requireNonNull(pFile, "Path");
		final Path dir = file.getParent();
		if (dir != null) {
			try {
				Files.createDirectories(dir);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	/**
	 * Checks, whether the given file's directory exists. If not, creates it.
	 * @param pFile The file, for which to create the directory.
	 * @throws NullPointerException The parameter {@code pFile} is null.
	 * @throws UncheckedIOException The directory could not be created.
	 */
	public static void createDirectoryFor(File pFile) {
		createDirectoryFor(Objects.requireNonNull(pFile, "File").toPath());
	}
}
