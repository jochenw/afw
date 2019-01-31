/**
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
package com.github.jochenw.afw.core.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AbstractFileFinder {
	public static class DetectedFile {
		private final String path;
		private final File file;

		public DetectedFile(String pPath, File pFile) {
			path = pPath;
			file = pFile;
		}

		public String getPath() {
			return path;
		}

		public File getFile() {
			return file;
		}
	}

	protected enum FileVisitResult {
		SKIP_SUBTREE,
		SKIP_SIBLINGS,
		ADD,
		CONTINUE,
		TERMINATE
	}
	
	public List<DetectedFile> findFiles(File pBaseDir) {
		final List<DetectedFile> files = new ArrayList<>();
		findFiles(pBaseDir, files, "", 0);
		return files;
	}

	protected FileVisitResult fileFound(File pFile, String pPath, int pLevel)  {
		return FileVisitResult.ADD;
	}
	
	protected FileVisitResult preDirectory(File pDir, String pPath, int pLevel) {
		return FileVisitResult.CONTINUE;
	}

	protected FileVisitResult postDirectory(File pDir, String pPath, int pLevel) {
		return FileVisitResult.CONTINUE;
	}
	
	protected FileVisitResult findFiles(File pDir, List<DetectedFile> pFiles, String pPath, int pLevel) {
		if (!pDir.isDirectory()) {
			throw new IllegalStateException("Not a directory: " + pDir.getAbsolutePath());
		}
		final String path;
		if (pLevel == 0) {
			path = pPath;
		} else {
			path = pPath + "/" + pDir.getName();
		}
		final FileVisitResult preResult = preDirectory(pDir, path, pLevel);
		final boolean findChildren;
		switch (preResult) {
			case SKIP_SIBLINGS:
				findChildren = false;
				break;
			case SKIP_SUBTREE:
				findChildren = false;
				break;
			case CONTINUE:
				findChildren = true;
				break;
			case TERMINATE:
				return FileVisitResult.TERMINATE;
			default:
				throw new IllegalStateException("Invalid result for preDirectory: "
						+ preResult + " (Expected SKIP_SIBLINGS|SKIP_SUBTREE|CONTINUE|TERMINATE");
		}
		if (findChildren) {
			for (File f : pDir.listFiles()) {
				final boolean goOn;
				final String p = path + "/" + f.getName();
				final String method;
				final FileVisitResult fileResult;
				if (f.isFile()) {
					method = "fileFound";
					fileResult = fileFound(f, p, pLevel+1);
				} else {
					method = "findFiles";
					fileResult = findFiles(f, pFiles, p, pLevel+1);
				}
				switch (fileResult) {
				  case SKIP_SIBLINGS:
					  goOn = false;
					  break;
				  case SKIP_SUBTREE:
					  goOn = false;
					  break;
				  case CONTINUE:
					  goOn = true;
					  break;
				  case ADD:
					  pFiles.add(new DetectedFile(p, f));
					  goOn = true;
					  break;
				  case TERMINATE:
					  return FileVisitResult.TERMINATE;
					default:
						throw new IllegalStateException("Invalid result for " + method + ": "
								+ fileResult + " (Expected SKIP_SIBLINGS|SKIP_SUBTREE|CONTINUE|TERMINATE");
					  
				}
				if (!goOn) {
					break;
				}
			}
		}
		return postDirectory(pDir, path, pLevel);
	}
}
