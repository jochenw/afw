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
package com.github.jochenw.afw.core;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.jochenw.afw.core.util.Exceptions;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.annotation.Nonnull;


/** This class is responsible for collecting resources, that must
 * be released later on. Typically a {@link SimpleResourceWorker}
 * is instantiated at the beginning of a transaction, and
 * released (together with the collected resources) at the end
 * of the transaction.
 */
public class SimpleResourceWorker {
	/**
	 * A runnable object, which may be invoked, adding
	 * resources to the {@link SimpleResourceWorker
	 * resource workers} collection set.
	 */
	@FunctionalInterface
	public interface ResRunnable {
		/**
		 * Called to perform an action, that produces no
		 * result. The {@link SimpleResourceTracker tracker}
		 * may be used to collect resources.
		 * @param pTracker The tracker accepts resources,
		 *   that have been collected. This resources will
		 *   later on be released, together with the
		 *   controlling {@link SimpleResourceWorker}.
		 */
		public abstract void run(SimpleResourceTracker pTracker);
	}
	/**
	 * A callable object, which may be invoked, adding
	 * resources to the {@link SimpleResourceWorker
	 * resource workers} collection set, and producing
	 * a result object.
	 * @param <T> The result objects type.
	 */
	@FunctionalInterface
	public interface ResCallable<T> {
		/**
		 * Called to perform an action, that produces no
		 * result. The {@link SimpleResourceTracker tracker}
		 * may be used to collect resources.
		 * @param pTracker The tracker accepts resources,
		 *   that have been collected. This resources will
		 *   later on be released, together with the
		 *   controlling {@link SimpleResourceWorker}.
		 * @return The result object, which has been
		 *   produced.
		 */
		public abstract T call(SimpleResourceTracker pTracker);
	}
	/** The {@link SimpleResourceTracker tracker} acts as a
	 * delegate of the {@link SimpleResourceWorker}, accepting
	 * collected resources on behalf of the latter.
	 */
	public class SimpleResourceTracker {
		private final List<Object> resources = new ArrayList<>();


		/**
		 * Called to collect a resource, which may be released
		 * later on.
		 * @param pResource The resource, which is being collected.
		 * @see SimpleResourceWorker#assertTrackable(Object)
		 * @see SimpleResourceWorker#closeResource(Object, boolean)
		 */
		public void track(Object pResource) {
			assertTrackable(pResource);
			resources.add(pResource);
		}

		/**
		 * Called to release the resources, that have been collected.
		 * Release is done in reverse order of collection.
		 * @throws RuntimeException An error occurred while
		 *   releasing a resource. If multiple errors are occurring,
		 *   then the first is thrown (assuming that later problems
		 *   may be triggered by the first).
		 */
		public void close() {
			Throwable th = null;
			for (int i = resources.size()-1;  i >= 0;  i--) {
				final Object resource = resources.remove(i);
				try {
					closeResource(resource, th == null);
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
			if (th != null) {
				throw Exceptions.show(th);
			}
		}
	}

        /** Called from {@link SimpleResourceTracker#track(java.lang.Object)} to
         * assert, that the given object can be tracked. This is the
         * case, if, and only if, {@link #closeResource(java.lang.Object, boolean)}
         * can close it. The default implementation returns true, if the object
         * is an instance of {@link AutoCloseable}, or an instance of
         * {@link HttpURLConnection}. The former includes, for example,
         * instances of {@link Connection}, {@link Statement}, or
         * {@link ResultSet}.
         * @param pResource The object, which is being tracked.
         */
	protected void assertTrackable(Object pResource) {
		final @Nonnull Object resource = Objects.requireNonNull(pResource, "Resource");
		if (!(resource instanceof AutoCloseable)
			&&  !(resource instanceof HttpURLConnection)) {
			throw new IllegalStateException("Invalid resource: "
                                + resource.getClass().getName());
		}
	}

	/** Called to close a resource.
	 * @param pResource The resouce, that is being closed.
	 * @param pCommit True, if a commit may be done. Otherwise, a rollback is assumed.
	 * @throws Throwable Closing the resource has failed.
	 */
	protected void closeResource(Object pResource, boolean pCommit) throws Throwable {
		final @Nonnull Object resource = Objects.requireNonNull(pResource, "Resource");
		if (resource instanceof AutoCloseable) {
			((AutoCloseable) resource).close();
		} else if (pResource instanceof HttpURLConnection) {
			((HttpURLConnection) resource).disconnect();
		} else {
			throw new IllegalStateException("Invalid resource: " + resource.getClass().getName());
		}
	}


	/** Creates a new resource tracker.
	 * @return The created resource tracker.
	 */
	protected SimpleResourceTracker newTracker() {
		return new SimpleResourceTracker();
	}

	/**
	 * Called to execute a piece of code, that allocates resources, that
	 * are to be released automatically, when the {@link ResRunnable runnable}
	 * is finished.
	 * @param pRunnable The piece of code, that is being executed.
	 */
	public void run(ResRunnable pRunnable) {
		Objects.requireNonNull(pRunnable, "Runnable");
		SimpleResourceTracker tracker = newTracker();
		Throwable th = null;
		try {
			pRunnable.run(tracker);
			tracker.close();
			tracker = null;
		} catch (Throwable t) {
			th = null;
		} finally {
			if (tracker != null) {
				try {
					tracker.close();
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
		}
		if (th != null) {
			throw Exceptions.show(th);
		}
	}
	
	/**
	 * Called to execute a piece of code, that allocates resources, producing
	 * a result object. The allocated resources are to be released automatically,
	 * when the {@link ResCallable callable} is finished
	 * @param pCallable The piece of code, that is being executed.
	 * @param <T> The result objects type.
	 * @return The result object, that has been produced by the
	 *   {@link ResCallable callable}.
	 */
	public <T> T call(ResCallable<T> pCallable) {
		Objects.requireNonNull(pCallable, "Callable");
		SimpleResourceTracker tracker = newTracker();
		Throwable th = null;
		T res = null;
		try {
			res = pCallable.call(tracker);
			tracker.close();
			tracker = null;
		} catch (Throwable t) {
			th = null;
		} finally {
			if (tracker != null) {
				try {
					tracker.close();
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
		}
		if (th != null) {
			throw Exceptions.show(th);
		}
		return res;
	}
}
