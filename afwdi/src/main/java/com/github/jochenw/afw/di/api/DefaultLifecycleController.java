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
package com.github.jochenw.afw.di.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import com.github.jochenw.afw.di.impl.DiUtils;



/**
 * Default implementation of the {@link ILifecycleController}.
 */
public class DefaultLifecycleController implements ILifecycleController {
	/** State of the controller.
	 */
	public enum State {
		/** The initial state; ready for starting.
		 */
		WAITING,
		/** Started, ready for shutdown.
		 */
		STARTED,
		/** Shutted down.
		 */
		TERMINATED;
	}
	private static class WrappedListener {
		private final Listener listener;
		private State state = State.WAITING;
		public WrappedListener(Listener pListener) {
			listener = pListener;
		}
	}

	private State state = State.WAITING;
	private final List<WrappedListener> listeners = new ArrayList<>();

	
	@Override
	public void addListener(Listener pListener) {
		final Listener listener = Objects.requireNonNull(pListener, "Listener");
		synchronized(listeners) {
			final WrappedListener wrappedListener = new WrappedListener(listener);
			if (state == State.STARTED) {
				start(wrappedListener);
				listeners.add(wrappedListener);
			}
		}
	}

	private void start(final WrappedListener pWrappedListener) {
		try {
			final Listener listener = pWrappedListener.listener;
			if (listener instanceof Startable st) {
				st.start();
				pWrappedListener.state = State.STARTED;
			}
		} catch (Exception e) {
			throw DiUtils.show(e);
		}
	}

	@Override
	public void removeListener(Listener pListener) {
		final Listener listener = Objects.requireNonNull(pListener, "Listener");
		synchronized(listeners) {
			for (Iterator<WrappedListener> iter = listeners.iterator();  iter.hasNext();  ) {
				final WrappedListener wrappedListener = iter.next();
				final Listener lst = wrappedListener.listener;
				if (lst.equals(listener)) {
					iter.remove();
					shutdown(wrappedListener);
				}
			}
		}
	}

	private void shutdown(final WrappedListener pWrappedListener) {
		final Listener lst = pWrappedListener.listener;
		if (pWrappedListener.state == State.STARTED) {
			pWrappedListener.state = State.TERMINATED;
			if (lst instanceof Terminable trm) {
				try {
					trm.shutdown();
				} catch (Exception e) {
					throw DiUtils.show(e);
				}
			}
		}
	}

	@Override
	public void start() {
		synchronized(listeners) {
			if (state != State.WAITING) {
				throw new IllegalStateException("This controller has already been started.");
			}
			for (WrappedListener wl : listeners) {
				start(wl);
			}
			state = State.STARTED;
		}
	}

	@Override
	public void shutdown() {
		synchronized(listeners) {
			if (state == State.TERMINATED) {
				throw new IllegalStateException("This controller has already been shutted down.");
			}
			for (WrappedListener wl : listeners) {
				shutdown(wl);
			}
			state = State.TERMINATED;
		}
	}
}
