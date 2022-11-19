/*
 * Copyright 2021 Jochen Wiedmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.MutableInteger;
import com.github.jochenw.afw.core.util.Objects;

/**
 *
 */
public class Executor {
	/**
	 * Creates a new {@link ExecutorBuilder builder}.
	 * @return A new, unconfigured {@link ExecutorBuilder builder}.
	 */
    public static ExecutorBuilder builder() {
        return new ExecutorBuilder();
    }

    private final String[] cmdLine;
    private final Path directory;
    private FailableConsumer<InputStream,?> stdOutHandler, stdErrHandler;

    /**
     * Creates a new instance with the given configuration.
     * @param pCmdLine The command line to execute.
     * @param pDirectory The directory, where to launch the external process. May be
     *   null, in which case the directory is inherited from the current process.
     * @param pStdOutHandler The handler for the external processes stdout stream.
     * @param pStdErrHandler The handler for the external processes stderr stream.
     * @throws NullPointerException Either of the parameters is null.
     */
    public Executor(String[] pCmdLine, Path pDirectory, FailableConsumer<InputStream,?> pStdOutHandler,
    		        FailableConsumer<InputStream,?> pStdErrHandler) {
        cmdLine = Objects.requireAllNonNull(pCmdLine, "CmdLineArg");
        directory = pDirectory;
        stdOutHandler = Objects.requireNonNull(pStdOutHandler, "StdOutHandler");
        stdErrHandler = Objects.requireNonNull(pStdErrHandler, "StdErrHandler");
    }

    /** Performs the actual launch, waits for completion, and returns the result code.
     * @return The external processes exit code.
     */
    public int run() {
    	Process process;
		try {
			process = Runtime.getRuntime().exec(cmdLine, null, directory == null ? null : directory.toFile());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
    	final CompletableFuture<Void> cfout = asCompletableFuture(process.getInputStream(), stdOutHandler);
    	final CompletableFuture<Void> cferr = asCompletableFuture(process.getErrorStream(), stdErrHandler);
    	final MutableInteger exitCode = new MutableInteger();
    	final CompletableFuture<Void>cfwait = CompletableFuture.runAsync(() -> {
    		final int status;
    		try {
    			process.getOutputStream().close();
				status = process.waitFor();
			} catch (InterruptedException e) {
				throw new UndeclaredThrowableException(e);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
    		exitCode.setValue(status);
    	});
    	final CompletableFuture<Void> cf = CompletableFuture.allOf(cfout, cferr, cfwait);
    	try {
			cf.get();
		} catch (InterruptedException|ExecutionException e) {
			throw new UndeclaredThrowableException(e);
		}
    	return exitCode.getValue();
    }

	private CompletableFuture<Void> asCompletableFuture(InputStream pInputStream,
			FailableConsumer<InputStream, ?> pStdOutHandler) {
		return CompletableFuture.runAsync(() -> {
			try {
				pStdOutHandler.accept(pInputStream);
			} catch (Throwable e) {
				throw Exceptions.show(e);
			}
		});
	}
}
