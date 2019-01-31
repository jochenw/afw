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

public class SimpleResourceWorker {
	public abstract static class ResRunnable {
		public abstract void run(SimpleResourceTracker pTracker);
	}
	public abstract static class ResCallable<T> {
		public abstract T call(SimpleResourceTracker pTracker);
	}
	public class SimpleResourceTracker {
		private final List<Object> resources = new ArrayList<>();

		public void track(Object pResource) {
			assertTrackable(pResource);
			resources.add(pResource);
		}

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

	protected void assertTrackable(Object pResource) {
		Objects.requireNonNull(pResource, "Resource");
		if (!(pResource instanceof AutoCloseable)
			&&  !(pResource instanceof HttpURLConnection)) {
			throw new IllegalStateException("Invalid resource: " + pResource.getClass().getName());
		}
	}

	protected void closeResource(Object pResource, boolean pCommit) throws Throwable {
		if (pResource instanceof AutoCloseable) {
			((AutoCloseable) pResource).close();
		} else if (pResource instanceof HttpURLConnection) {
			((HttpURLConnection) pResource).disconnect();
		} else {
			throw new IllegalStateException("Invalid resource: " + pResource.getClass().getName());
		}
	}


	protected SimpleResourceTracker newTracker() {
		return new SimpleResourceTracker();
	}
	
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
