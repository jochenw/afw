/**
 * 
 */
package com.github.jochenw.afw.core.components;

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
	 */
	public void createZipFile(Path pSourceDir, Path pZipFile);
	/** Extracts a zip file to a given directory. Assumes, that all entries in the zip file
	 * have relative paths.
	 * @param pTargetDir The directory, where to extract files to.
	 * @param pZipFile The zip file being extracted.
	 * @throws IllegalStateException An entry in the zip file was found, which has an absolute
	 *   path.
	 */
	public void extractZipFile(Path pTargetDir, Path pZipFile) throws IllegalStateException;
}
