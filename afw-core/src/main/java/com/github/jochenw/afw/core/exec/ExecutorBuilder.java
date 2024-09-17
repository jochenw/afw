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

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.FileUtils;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Streams;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ExecutorBuilder {
	/** Creates a new instance.
	 */
	public ExecutorBuilder() {}

	private final List<String> cmdLine = new ArrayList<>();
	private final Map<String,String> environment = new HashMap<>();
    private ProcessOutputHandler stdOutHandler, stdErrHandler;
    private Path directory;
    private FailableConsumer<String[],?> cmdLineListener;

    /** Sets the executable, that is being executed.
     * As a side-effect, clears the argument list.
     * @param pCmd The executable, that is being executed.
     * @throws NullPointerException The executable is null.
     * @return This builder.
     * @see #exec(File)
     * @see #exec(Path)
     */
    public ExecutorBuilder exec(String pCmd) {
        cmdLine.clear();
        cmdLine.add(Objects.requireNonNull(pCmd, "Cmd"));
        return this;
    }

    /** Sets the executable, that is being executed.
     * As a side-effect, clears the argument list.
     * @param pCmd The executable, that is being executed.
     * @throws NullPointerException The executable is null.
     * @return This builder.
     * @see #exec(String)
     * @see #exec(Path)
     */
    public ExecutorBuilder exec(Path pCmd) {
        return exec(Objects.requireNonNull(pCmd, "Cmd").toString());
    }

    /** Sets the executable, that is being executed.
     * As a side-effect, clears the argument list.
     * @param pCmd The executable, that is being executed.
     * @throws NullPointerException The executable is null.
     * @return This builder.
     * @see #exec(String)
     * @see #exec(Path)
     */
    public ExecutorBuilder exec(File pCmd) {
        return exec(Objects.requireNonNull(pCmd, "Cmd").getPath());
    }

    /** Adds arguments to the command line.
     * @param pArgs The arguments, that are being added to the command line.
     * @throws NullPointerException Either of the arguments is null.
     * @return This builder.
     * @see #arg(Path)
     * @see #arg(File)
     * @see #arg(String)
     * @see #args(Iterable)
     */
    public ExecutorBuilder args(String... pArgs) {
        cmdLine.addAll(Arrays.asList(Objects.requireAllNonNull(pArgs, "Arguments")));
        return this;
    }

    /** Adds arguments to the command line.
     * @param pArgs The arguments, that are being added to the command line.
     * @throws NullPointerException Either of the arguments is null.
     * @return This builder.
     * @see #arg(Path)
     * @see #arg(File)
     * @see #arg(String)
     * @see #args(String[])
     */
    public ExecutorBuilder args(Iterable<String> pArgs) {
    	final Iterable<String> args = Objects.requireAllNonNull(pArgs, "Arguments");
    	args.forEach(cmdLine::add);
        return this;
    }

    /** Adds an argument to the command line.
     * @param pArg The argument, that's being added to the command line.
     * @throws NullPointerException The argument is null.
     * @return This builder.
     * @see #arg(Path)
     * @see #arg(File)
     * @see #args(String...)
i     */
    public ExecutorBuilder arg(String pArg) {
        cmdLine.add(Objects.requireNonNull(pArg, "Argument"));
        return this;
    }

    /** Adds an argument to the command line.
     * @param pArg The argument, that's being added to the command line.
     * @throws NullPointerException The argument is null.
     * @return This builder.
     * @see #arg(String)
     * @see #arg(File)
     * @see #args(String...)
     */
    public ExecutorBuilder arg(Path pArg) {
        return arg(Objects.requireNonNull(pArg, "Argument").toString());
    }

    /** Adds an argument to the command line.
     * @param pArg The argument, that's being added to the command line.
     * @throws NullPointerException The argument is null.
     * @return This builder.
     * @see #arg(String)
     * @see #arg(Path)
     * @see #args(String...)
     */
    public ExecutorBuilder arg(File pArg) {
        return arg(Objects.requireNonNull(pArg, "Argument").getPath());
    }

    /** Sets a listener, which is being notified about the actually
     * executed command line. The typical use case is for logging,
     * and other diagnostic purposes.
     * @param pCmdLine The command line, which is about to be executed.
     */
    /** Sets the directory, where the external process will be launched.
     * In other words: From the external processes perspective, this will
     * be the current directory.
     * @param pDirectory The directory, where the external process will be launched.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pDirectory} is null.
     * @throws IllegalArgumentException The given directory does not exist, or is
     *   otherwise invalid.
     */
    public ExecutorBuilder directory(Path pDirectory) {
    	final Path dir = Objects.requireNonNull(pDirectory, "Directory");
    	if (!Files.isDirectory(dir)) {
    		throw new IllegalArgumentException("Invalid value for parameter directory:"
    				+ " Expected existing directory, got " + pDirectory);
    	}
    	directory = dir;
    	return this;
    }

    /** Sets the directory, where the external process will be launched.
     * In other words: From the external processes perspective, this will
     * be the current directory.
     * @param pDirectory The directory, where the external process will be launched.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pDirectory} is null.
     * @throws IllegalArgumentException The given directory does not exist, or is
     *   otherwise invalid.
     */
    public ExecutorBuilder directory(File pDirectory) {
    	final File dir = Objects.requireNonNull(pDirectory, "Directory");
    	return directory(dir.toPath());
    }

    /** Sets the directory, where the external process will be launched.
     * In other words: From the external processes perspective, this will
     * be the current directory.
     * @param pDirectory The directory, where the external process will be launched.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pDirectory} is null.
     * @throws IllegalArgumentException The given directory does not exist, or is
     *   otherwise invalid.
     */
    public ExecutorBuilder directory(String pDirectory) {
    	final String dir = Objects.requireNonNull(pDirectory, "Directory");
    	return directory(Paths.get(dir));
    }

    /** Sets a listener, which is being notified, before the actual execution
     * starts. The use case is for logging, and other diagnostic purposes.
     * @param pCmdLineConsumer The listener, which is being notified about
     *   the command line before execution.
     * @return This builder.
     */
    public ExecutorBuilder cmdLineListener(FailableConsumer<String[],?> pCmdLineConsumer) {
    	cmdLineListener = pCmdLineConsumer;
    	return this;
    }

    /** Sets a listener, which is being notified, before the actual execution
     * starts. The use case is for logging, and other diagnostic purposes.
     * @return The command line listener, if present, or null.
     */
    FailableConsumer<String[],?> getCmdLineListener() {
    	return cmdLineListener;
    }
 
    /** Returns the directory, where the external process will be launched.
     * In other words: From the external processes perspective, this will
     * be the current directory.
     * @return The directory, where the external process will be launched.
     */
    public Path getDirectory() {
    	if (directory == null) {
    		return Paths.get(".");
    	} else {
    		return directory;
    	}
    }

    /** Specifies an environment variable, that the executed process should have.
     * @param pVar Name of the environment variable.
     * @param pValue Value of the environment variable.
     * @return This builder.
     */
    public ExecutorBuilder envVar(String pVar, String pValue) {
    	environment.put(pVar, pValue);
    	return this;
    }

    /**
     * Sets the {@code stream handler}, which is being invoked to process the
     * external processes stdout byte stream.
     * @param pStdOutHandler The {@code stream handler}, which is being invoked to process the
     * external processes stdout byte stream.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pStdOutHandler} is null.
     * @see #stdOut(File)
     * @see #stdOut(Path)
     * @see #stdOut(OutputStream)
     */
    public ExecutorBuilder stdOutHandler(ProcessOutputHandler pStdOutHandler) {
    	stdOutHandler = Objects.requireNonNull(pStdOutHandler, "StdOutHandler");
    	return this;
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stdout byte stream. The created handler will write to the
     * {@link OutputStream}, that is returned by the given {@link FailableSupplier}.
     * @param pSupplier The {@link FailableSupplier supplier}, which returns an
     * {@code output stream}, to which the external processes output stream will be written.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pSupplier} is null.
     * @see #stdOut(File)
     * @see #stdOut(Path)
     * @see #stdOut(OutputStream)
     */
    public ExecutorBuilder stdOut(FailableSupplier<OutputStream,?> pSupplier) {
    	final FailableSupplier<OutputStream,?> supplier = Objects.requireNonNull(pSupplier, "Supplier");
    	final ProcessOutputHandler stdOutHandler = (in) -> {
    		final OutputStream out = Objects.requireNonNull(supplier.get(), "OutputStream");
    		Streams.copy(in, out);
    		out.flush();
    	};
    	return stdOutHandler(stdOutHandler);
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stdout byte stream. The created handler will write to the given
     * {@link OutputStream}.
     * @param pOutputStream The {@code output stream}, to which the external processes
     *   output stream will be written.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pOutputStream} is null.
     * @see #stdOut(File)
     * @see #stdOut(Path)
     * @see #stdErr(OutputStream)
     */
    public ExecutorBuilder stdOut(OutputStream pOutputStream) {
		final OutputStream out = Objects.requireNonNull(pOutputStream, "OutputStream");
		return stdOut((FailableSupplier<OutputStream, ?>) () -> out);
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stdout byte stream. The created handler will write to the given
     * {@link Path file}.
     * @param pOutputFile The {@code output file}, to which the external processes
     *   output stream will be written.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pOutputFile} is null.
     * @see #stdOut(File)
     * @see #stdOut(OutputStream)
     * @see #stdOut(Path)
     */
    public ExecutorBuilder stdOut(Path pOutputFile) {
    	final Path outputFile = Objects.requireNonNull(pOutputFile, "OutputFile");
    	final FailableSupplier<OutputStream,?> supplier = () -> {
    		FileUtils.createDirectoryFor(outputFile);
    		final OutputStream out = Files.newOutputStream(outputFile);
    		return new BufferedOutputStream(out);
    	};
    	return stdOut(supplier);
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stdout byte stream. The created handler will write to the given
     * {@link File file}.
     * @param pOutputFile The {@code output file}, to which the external processes
     *   output stream will be written.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pOutputFile} is null.
     * @see #stdOut(OutputStream)
     * @see #stdOut(Path)
     * @see #stdErr(File)
     */
    public ExecutorBuilder stdOut(File pOutputFile) {
    	return stdOut(Objects.requireNonNull(pOutputFile, "File").toPath());
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stdout byte stream. The created handler will invoke the given
     * {@link FailableConsumer consumer} with the received output.
     * @param pConsumer The {@link FailableConsumer consumer}, to which the external processes
     *   output stream will be sent.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pConsumer} is null.
     */
    public ExecutorBuilder stdOut(FailableConsumer<byte[],?> pConsumer) {
    	final FailableConsumer<byte[],?> consumer = Objects.requireNonNull(pConsumer, "Consumer");
    	final ProcessOutputHandler stdOutConsumer = (in) -> {
    		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		Streams.copy(in, baos);
    		consumer.accept(baos.toByteArray());
    	};
    	return stdOutHandler(stdOutConsumer);
    }

    /**
     * Sets the {@code stream handler}, which is being invoked to process the
     * external processes stderr byte stream.
     * @param pStdErrHandler The {@code stream handler}, which is being invoked to process the
     * external processes stderr byte stream.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pStdOutHandler} is null.
     * @see #stdErr(File)
     * @see #stdErr(Path)
     * @see #stdErr(OutputStream)
     */
    public ExecutorBuilder stdErrHandler(ProcessOutputHandler pStdErrHandler) {
    	stdErrHandler = Objects.requireNonNull(pStdErrHandler, "StdErrHandler");
    	return this;
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stderr byte stream. The created handler will write to the
     * {@link OutputStream}, that is returned by the given {@link FailableSupplier}.
     * @param pSupplier The {@link FailableSupplier supplier}, which returns an
     * {@code output stream}, to which the external processes error stream will be written.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pSupplier} is null.
     * @see #stdErr(File)
     * @see #stdErr(Path)
     * @see #stdErr(OutputStream)
     */
    public ExecutorBuilder stdErr(FailableSupplier<OutputStream,?> pSupplier) {
    	final FailableSupplier<OutputStream,?> supplier = Objects.requireNonNull(pSupplier, "Supplier");
    	final ProcessOutputHandler stdErrHandler = (in) -> {
    		final OutputStream out = Objects.requireNonNull(supplier.get(), "OutputStream");
    		Streams.copy(in, out);
    		out.flush();
    	};
    	return stdErrHandler(stdErrHandler);
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stderr byte stream. The created handler will write to the
     * {@link Writer}, that is returned by the given {@link FailableSupplier},
     * using the given {@link Charset character set} for conversion of bytes into
     * characters.
     * @param pSupplier The {@link FailableSupplier supplier}, which returns an
     * {@code output stream}, to which the external processes error stream will be written.
     * @param pCharset The {@link Charset character set}, which is being used for
     * conversion of bytes into characters. Defaults to {@link StandardCharsets#UTF_8}.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pSupplier} is null.
     * @see #stdErr(File)
     * @see #stdErr(Path)
     * @see #stdErr(OutputStream)
     */
    public ExecutorBuilder stdErr(FailableSupplier<Writer,?> pSupplier, Charset pCharset) {
    	final FailableSupplier<Writer,?> supplier = Objects.requireNonNull(pSupplier, "Supplier");
    	final Charset cs = Objects.notNull(pCharset, Streams.UTF_8);
    	final ProcessOutputHandler stdErrHandler = (in) -> {
    		final Writer w = Objects.requireNonNull(supplier.get(), "Writer");
    		Streams.copy(in, w, cs);
    		w.flush();
    	};
    	return stdErrHandler(stdErrHandler);
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stderr byte stream. The created handler will write to the given
     * {@link OutputStream}.
     * @param pOutputStream The {@code output stream}, to which the external processes
     *   error stream will be written.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pOutputStream} is null.
     * @see #stdErr(File)
     * @see #stdErr(Path)
     * @see #stdOut(OutputStream)
     */
    public ExecutorBuilder stdErr(OutputStream pOutputStream) {
		final OutputStream out = Objects.requireNonNull(pOutputStream, "OutputStream");
		return stdErr((FailableSupplier<OutputStream, ?>) () -> out);
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stderr byte stream. The created handler will write to the given
     * {@link Writer}, using the given character set for conversion of bytes to characters.
     * @param pWriter The {@link Writer writer}, to which the external processes
     *   error stream will be written.
     * @param pCharset The {@link Charset}, which will be used for conversion of bytes
     *   into characters. Defaults to {@link StandardCharsets#UTF_8}.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pOutputStream} is null.
     * @see #stdErr(File)
     * @see #stdErr(Path)
     */
    public ExecutorBuilder stdErr(Writer pWriter, Charset pCharset) {
		final Writer w = Objects.requireNonNull(pWriter, "Writer");
		return stdErr((FailableSupplier<Writer, ?>) () -> w, pCharset);
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stderr byte stream. The created handler will write to the given
     * {@link Path file}.
     * @param pOutputFile The {@code output file}, to which the external processes
     *   output stream will be written.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pOutputFile} is null.
     * @see #stdErr(File)
     * @see #stdErr(OutputStream)
     * @see #stdOut(Path)
     */
    public ExecutorBuilder stdErr(Path pOutputFile) {
    	final Path outputFile = Objects.requireNonNull(pOutputFile, "OutputFile");
    	final FailableSupplier<OutputStream,?> supplier = () -> {
    		FileUtils.createDirectoryFor(outputFile);
    		final OutputStream out = Files.newOutputStream(outputFile);
    		return new BufferedOutputStream(out);
    	};
    	return stdErr(supplier);
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stderr byte stream. The created handler will write to the given
     * {@link File file}.
     * @param pOutputFile The {@code output file}, to which the external processes
     *   error stream will be written.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pOutputFile} is null.
     * @see #stdErr(OutputStream)
     * @see #stdErr(Path)
     * @see #stdOut(File)
     */
    public ExecutorBuilder stdErr(File pOutputFile) {
    	return stdErr(Objects.requireNonNull(pOutputFile, "File").toPath());
    }

    /**
     * Creates, and sets a {@code stream handler}, which is being invoked to process the
     * external processes stderr byte stream. The created handler will invoke the given
     * {@link FailableConsumer consumer} with the received output.
     * @param pConsumer The {@link FailableConsumer consumer}, to which the external processes
     *   error stream will be sent.
     * @return This builder.
     * @throws NullPointerException The parameter {@code pConsumer} is null.
     */
    public ExecutorBuilder stdErr(FailableConsumer<byte[],?> pConsumer) {
    	final FailableConsumer<byte[],?> consumer = Objects.requireNonNull(pConsumer, "Consumer");
    	final ProcessOutputHandler stdOutConsumer = (in) -> {
    		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		Streams.copy(in, baos);
    		consumer.accept(baos.toByteArray());
    	};
    	return stdErrHandler(stdOutConsumer);
    }

    /**
     * Creates an {@link Executor} with the current configuration.
     * @return The created {@link Executor}.
     * @throws IllegalStateException The {@link Executor executor's} configuration is incomplete.
     */
    public Executor build() {
    	if (cmdLine.isEmpty()) {
    		throw new IllegalStateException("No executable has been set. Did you invoke either of the exec() methods?");
    	}
    	final FailableConsumer<InputStream,?> outHandler = Objects.notNull(stdOutHandler, () -> {
    		return (in) -> Streams.readAndDiscard(in);
    	});
    	final FailableConsumer<InputStream,?> errHandler = Objects.notNull(stdErrHandler, () -> {
    		return (in) -> Streams.readAndDiscard(in);
    	});
    	return new Executor(cmdLine.toArray(new String[cmdLine.size()]), directory, outHandler, errHandler,
    			            cmdLineListener, environment);
    }

    /** Returns the command line, that is being executed.
     * @return The command line, that is being executed.
     */
    public List<String> getCmdLine() {
    	return cmdLine;
    }
}

