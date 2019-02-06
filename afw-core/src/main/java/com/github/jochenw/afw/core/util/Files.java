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
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class Files {
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
}
