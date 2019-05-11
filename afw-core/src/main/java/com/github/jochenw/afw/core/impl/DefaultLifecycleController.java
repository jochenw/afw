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
package com.github.jochenw.afw.core.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.jochenw.afw.core.ILifecycleController;
import com.github.jochenw.afw.core.util.Exceptions;


public class DefaultLifecycleController implements ILifecycleController {
	private List<Listener> startableListeners = new ArrayList<>();
	private List<Listener> startedListeners = new ArrayList<>();
	private int state;

	@Override
	public synchronized void start() {
		switch(state) {
			case 0:
			{
				final Iterator<Listener> iter = startableListeners.iterator();
				while (iter.hasNext()) {
					final Listener listener = iter.next();
					try {
						listener.start();
					} catch (Throwable t) {
						throw Exceptions.show(t);
					}
					iter.remove();
					startedListeners.add(listener);
				}
				state = 1;
			}
			break;
			case 1:
				throw new IllegalStateException("Already started");
			case 2:
				throw new IllegalStateException("Already shutting down");
		}
	}
	

	@Override
	public synchronized void shutdown() {
		switch(state) {
		  case 0:
			  throw new IllegalStateException("Not yet started.");
		  case 1:
			  Throwable th = null;
			  for (int i = startedListeners.size()-1;  i >= 0;  i--) {
				  final Listener listener = startedListeners.remove(i);
				  try {
				      terminate(listener);
				  } catch (Throwable t) {
					  if (th == null) {
						  th = t;
					  }
				  }
			  }
			  if (th != null) {
				  throw Exceptions.show(th);
			  }
			  if (!startedListeners.isEmpty()) {
					throw new IllegalStateException("Expected all listeners to be stopped.");
			  }
			  state = 2;
			  break;
		  case 2:
			  throw new IllegalStateException("Already shutting down");
		}
	}

	@Override
	public synchronized void addListener(Listener pListener) {
		switch(state) {
		case 0:
			startableListeners.add(pListener);
			break;
		case 1:
			Listener listener = pListener;
			try {
				listener.start();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			startedListeners.add(listener);
			break;
		case 2:
			throw new IllegalStateException("Already shutting down");
		}
	}

	@Override
	public void removeListener(Listener pListener) {
		switch(state) {
		case 0:
			startableListeners.remove(pListener);
			break;
		case 1:
			boolean started = startedListeners.remove(pListener);
			startableListeners.remove(pListener);
			if (started) {
				terminate(pListener);
			}
			break;
		case 2:
			throw new IllegalStateException("Already shutting down");
		}
	}

	private void terminate(Listener pListener) {
		if (pListener instanceof TerminableListener) {
			((TerminableListener) pListener).shutdown();
		}
	}
}
