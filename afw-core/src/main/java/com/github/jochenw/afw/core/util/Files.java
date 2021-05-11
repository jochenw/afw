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

import java.io.UncheckedIOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.function.Function;


/** Utility class for working with files, and directories.
 * @deprecated Use {@link FileUtils}.
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
		FileUtils.copyDirectory(pSource, pTarget);
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
	public static void copyDirectory(final Path pSource, final Path pTarget, Function<String,String> pMapper) {
		FileUtils.copyDirectory(pSource, pTarget, pMapper);
	}

	/**
	 * Removes the given directory completely, including contents.
	 * @param pDir The directory being removed.
	 * @throws UncheckedIOException Removal failed.
	 */
	public static void removeDirectory(Path pDir) {
		FileUtils.removeDirectory(pDir);
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
		return FileUtils.resolve(pBaseDir, pRelativePath);
	}

	/** Checks, whether a given file, or directory, is located within another directory.
	 * @param pDir The directory, which might contain a file, or another directory.
	 * @param pPath The file, or directory, which is being questioned.
	 * @return True, if the file, or directory {@code pPath} is located within the
	 *     directory {@code pDir}, otherwise false.
	 */
	public static boolean isWithin(Path pDir, Path pPath) {
		return FileUtils.isWithin(pDir, pPath);
	}

	/**
	 * Replacement for {@link java.nio.file.Files#isDirectory(Path, java.nio.file.LinkOption...)}.
	 * This is here, so that the current class ({@link Files}) can be imported without the need
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
	 * This is here, so that the current class ({@link Files}) can be imported without the need
	 * to qualify {@link java.nio.file.Files}, when using it.
	 * @param pPath The path object, which is being checked.
	 * @param pLinkOptions The link options, which are being considered.
	 * @return True, if {@code pPath} is a regular file, considering the given link options,
	 *   otherwise false.
	 */
	public static boolean isRegularFile(Path pPath, LinkOption... pLinkOptions) {
		return java.nio.file.Files.isRegularFile(pPath, pLinkOptions);
	}
}
