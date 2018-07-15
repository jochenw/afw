package com.github.jochenw.afw.core.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
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
