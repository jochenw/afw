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
package com.github.jochenw.afw.core.csv;

import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;


/**
 * A consumer, which is being notified about the end of events.
 * @param <T> The consumers input type.
 */
public class FinalizableConsumer<T> implements Consumer<T> {
	/** Creates a new instance.
	 */
	public FinalizableConsumer() {}

	/** Called for success.
	 */
	public void finished() {
	}

	/**  Called to report an error.
	 * @param pThrowable The error, which is being reported.
	 */
	public void error(@NonNull Throwable pThrowable) {
		final @NonNull Throwable th = Objects.requireNonNull(pThrowable);
		throw Exceptions.show(th);
	}

	@Override
	public void accept(T pValue) {
	}
}
