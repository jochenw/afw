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

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Utility class for supporting various kinds of actions.
 */
public class Actions {
	/** Executes all the given tasks in parallel, without any
	 * determined order, using a thread pool with the given size.
	 * @param pTasks The tasks, that are being executed.
	 * @param pNumberOfThreads Number of threads, that may be
	 *   executed in parallel. 
	 */
	public static void executeAll(int pNumberOfThreads, Runnable... pTasks) {
		final ExecutorService executor = Executors.newFixedThreadPool(pNumberOfThreads);
		@SuppressWarnings("unchecked")
		final CompletableFuture<Void>[] completableFutures = Objects.arrayOf(CompletableFuture.class, pTasks.length);
		for (int i = 0;  i < pTasks.length;  i++) {
			completableFutures[i] = CompletableFuture.runAsync(pTasks[i], executor);
		}
		try {
			CompletableFuture.allOf(completableFutures).get();
		} catch (ExecutionException e) {
			throw Exceptions.show(e.getCause());
		} catch (InterruptedException e) {
			throw Exceptions.show(e);
		}
	}

	/** Executes all the given tasks in parallel, without any
	 * determined order, using a thread pool with the given size.
	 * @param pTasks The tasks, that are being executed.
	 * @param pNumberOfThreads Number of threads, that may be
	 *   executed in parallel. 
	 */
	public static void executeAll(int pNumberOfThreads, Collection<Runnable> pTasks) {
		final Runnable[] runnables = pTasks.toArray(new Runnable[pTasks.size()]);
		executeAll(pNumberOfThreads, runnables);
	}
}
