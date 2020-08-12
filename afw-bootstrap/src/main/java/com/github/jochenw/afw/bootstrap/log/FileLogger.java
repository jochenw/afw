package com.github.jochenw.afw.bootstrap.log;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


public class FileLogger extends Logger {
	private final Path file;
	private boolean open;
	private OutputStream out;

	public FileLogger(Path pLogFile, Level pLogLevel) {
		super(pLogLevel);
		file = pLogFile;
	}

	@Override
	protected OutputStream getOutputStream() {
		if (!open) {
			synchronized (this) {
				if (!open) {
					try {
						out = Files.newOutputStream(file, StandardOpenOption.APPEND);
					} catch (IOException e) {
						throw new UndeclaredThrowableException(e);
					}
					open = true;
				}
			}
		}
		return out;
	}

	@Override
	public void close() {
		if (open) {
			synchronized (this) {
				if (open) {
					try {
						out.close();
					} catch (IOException e) {
						throw new UndeclaredThrowableException(e);
					}
					open = false;
				}
			}
		}
	}

}
