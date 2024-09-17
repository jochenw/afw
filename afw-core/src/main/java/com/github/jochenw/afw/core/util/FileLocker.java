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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.function.Consumer;
import java.util.function.Function;

/** A component, which allows to perform actions on files, that are locked on the
 * operating system level.
 */
public class FileLocker {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	public FileLocker() {}

	/**
	 * Interface of an object, that can be used to access the locked file's contents. 
	 */
	public static interface StreamAccessor {
		/**
		 * Returns an {@link InputStream}, that can be used to read the locked file's
		 * contents.
		 * @return An {@link InputStream}, that can be used to read the locked file's
		 * contents.
		 */
		InputStream getInputStream();
		/**
		 * Returns an {@link OutputStream}, that can be used to write the locked file's
		 * contents.
		 * @return An {@link OutputStream}, that can be used to write the locked file's
		 * contents.
		 */
		OutputStream getOutputStream();
	}
	/**
	 * Locks the given file, and invokes the given {@link Consumer consumer}, keeping the
	 * lock, until the consumer is finished.
	 * @param pFile The file, that is being locked.
	 * @param pConsumer The consumer, that is being invoked, while the file is locked.
	 */
	public static void runLocked(File pFile, Consumer<StreamAccessor> pConsumer) {
		Function<StreamAccessor,Object> function = (sa) -> { pConsumer.accept(sa); return null; };
		callLocked(pFile, function);
	}
	/**
	 * Locks the given file, and invokes the given {@link Function function}, keeping the
	 * lock, until the function is finished. Returns the functions result.
	 * @param pFile The file, that is being locked.
	 * @param pFunction The consumer, that is being invoked, while the file is locked.
	 * @param <T> The result type, both the given functions, and this method's.
	 * @return The result of invoking the given function.
	 */
	public static <T> T callLocked(File pFile, Function<StreamAccessor,T> pFunction) {
		try (final RandomAccessFile raf = new RandomAccessFile(pFile, "rw");
				final FileChannel channel = raf	.getChannel();
				final FileLock lock = channel.lock()) {
			final StreamAccessor sa = new StreamAccessor() {
				@Override
				public OutputStream getOutputStream() {
					return Channels.newOutputStream(channel);
				}

				@Override
				public InputStream getInputStream() {
					return Channels.newInputStream(channel);
				}
			};
			final T t = pFunction.apply(sa);
			return t;
		} catch (Throwable thr) {
			throw Exceptions.show(thr);
		}
	}
}
