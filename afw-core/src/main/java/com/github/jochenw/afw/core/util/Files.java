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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;


/** Utility class for working with files, and directories.
 */
public class Files {
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
}
