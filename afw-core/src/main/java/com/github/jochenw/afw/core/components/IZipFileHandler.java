/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/** Interface of a component for handling zip files.
 */
public interface IZipFileHandler {
	/**
	 * Creates a zip file by adding the files from a directory. The entries in the created zip
	 * file are guaaranteed to have paths, which are relative to the source directory.
	 * @param pSourceDir The source directory where to look for files (recursively) being added
	 *    to the zip file. 
	 * @param pZipFile The zip file being created.
	 * @param pBaseDirIncludedInPath Whether the base directories name should be included in the zip file entries. If
	 *    so, the base directories name will be present in <em>all</em> entries.
	 */
	public void createZipFile(Path pSourceDir, Path pZipFile, boolean pBaseDirIncludedInPath);
	/** Extracts a zip file to a given directory. Assumes, that all entries in the zip file
	 * have relative paths.
	 * @param pTargetDir The directory, where to extract files to.
	 * @param pZipFile The zip file being extracted. It is assumed, that this file exists, and
	 *   is a valid zip file.
	 * @throws IllegalStateException An entry in the zip file was found, which has an absolute
	 *   path.
	 */
	public void extractZipFile(Path pTargetDir, Path pZipFile) throws IllegalStateException;
	/** Extracts a single entry from the given zip file.
	 * @param pZipFile The zip file being read. It is assumed, that this file exists, and
	 *   is a valid zip file.
	 * @param pUri The entry, which is being opened.
	 * @return An InputStream, which allows to read the entry.
	 * @throws IOException The operation failed.
	 */
	public InputStream openEntry(Path pZipFile, String pUri) throws IOException;
}
