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

    /**
     * Creates a new instance with the given configuration.
     * @param pCmdLine The command line to execute.
     */
    public Executor(String[] pCmdLine) {
        cmdLine = pCmdLine;
    }
}
