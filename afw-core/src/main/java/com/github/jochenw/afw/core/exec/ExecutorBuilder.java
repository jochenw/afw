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

import com.github.jochenw.afw.core.util.Objects;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class ExecutorBuilder {
    private final List<String> cmdLine = new ArrayList<>();

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
     */
    public ExecutorBuilder args(String... pArgs) {
        cmdLine.addAll(Arrays.asList(Objects.requireAllNonNull(pArgs, "Argument")));
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

    /**
     * Creates an {@link Executor} with the current configuration.
     * @return The created {@link Executor}.
     */
}
