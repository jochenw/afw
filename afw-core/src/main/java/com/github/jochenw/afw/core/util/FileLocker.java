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

public class FileLocker {
	public static interface StreamAccessor {
		InputStream getInputStream();
		OutputStream getOutputStream();
	}
	public static void runLocked(File pFile, Consumer<StreamAccessor> pConsumer) {
		Function<StreamAccessor,Object> function = (sa) -> { pConsumer.accept(sa); return null; };
		callLocked(pFile, function);
	}
	public static <T> T callLocked(File pFile, Function<StreamAccessor,T> pConsumer) {
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
			final T t = pConsumer.apply(sa);
			return t;
		} catch (Throwable thr) {
			throw Exceptions.show(thr);
		}
	}
}
