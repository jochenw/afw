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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.MutableInteger;
import com.github.jochenw.afw.core.util.Objects;

/** An {@link Executor} is an object, which has the ability to run an external
 * command, with parameters. The {@link Executor} can provide input for the
 * external command. It can also record the external commands standard
 * output, or error output. Basically, it is a convenient, and safe wrapper
 * for a {@link Process}.
 * Executors are created by using {@link Executor#builder()}.
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
    private final FailableConsumer<InputStream,?> stdOutHandler, stdErrHandler;
    private final FailableConsumer<String[],?> cmdLineConsumer;
    private final Map<String,String> environment;

    /**
     * Creates a new instance with the given configuration.
     * @param pCmdLine The command line to execute.
     * @param pDirectory The directory, where to launch the external process. May be
     *   null, in which case the directory is inherited from the current process.
     * @param pStdOutHandler The handler for the external processes stdout stream.
     * @param pStdErrHandler The handler for the external processes stderr stream.
     * @param pCmdLineConsumer A listener, which is being notified with the command
     *   line, that is being executed.
     * @param pEnvironment A map with additional environment variables.
     * @throws NullPointerException Either of the parameters is null.
     */
    public Executor(String[] pCmdLine, Path pDirectory, FailableConsumer<InputStream,?> pStdOutHandler,
    		        FailableConsumer<InputStream,?> pStdErrHandler,
    		        FailableConsumer<String[],?> pCmdLineConsumer,
    		        Map<String,String> pEnvironment) {
        cmdLine = Objects.requireAllNonNull(pCmdLine, "CmdLineArg");
        directory = pDirectory;
        stdOutHandler = Objects.requireNonNull(pStdOutHandler, "StdOutHandler");
        stdErrHandler = Objects.requireNonNull(pStdErrHandler, "StdErrHandler");
        cmdLineConsumer = pCmdLineConsumer;
        environment = pEnvironment;
    }

    /** Performs the actual launch, waits for completion, and returns the result code.
     * @return The external processes exit code.
     */
    public int run() {
    	if (cmdLineConsumer != null) {
    		try {
    			cmdLineConsumer.accept(cmdLine);
    		} catch (Throwable t) {
    			throw Exceptions.show(t);
    		}
    	}
    	final String[] env;
    	if (environment == null  ||  environment.isEmpty()) {
    		env = null;
    	} else {
    		final List<String> list = new ArrayList<>();
    		environment.forEach((k,v) -> list.add(k + "=" + v));
    		env = list.toArray(new String[list.size()]);
    	}
    	Process process;
		try {
			process = Runtime.getRuntime().exec(cmdLine, env, directory == null ? null : directory.toFile());
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
