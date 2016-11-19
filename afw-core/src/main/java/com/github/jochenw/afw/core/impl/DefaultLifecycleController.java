package com.github.jochenw.afw.core.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.github.jochenw.afw.core.ILifefycleController;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.core.ILifecycleListener;

public class DefaultLifecycleController implements ILifefycleController {
	public static class ListenerProxy implements ILifecycleListener {
		private final Object object;
		private final Method starter, stopper;

		ListenerProxy(Object pObject, Method pStarter, Method pStopper) {
			object = pObject;
			starter = pStarter;
			stopper = pStopper;
		}
		@Override
		public void start() {
			invoke(starter);
		}
		@Override
		public void shutdown() {
			invoke(stopper);
		}
		private void invoke(Method pMethod) {
			if (pMethod != null) {
				try {
					pMethod.invoke(object);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
		}
	}
	private List<ILifecycleListener> startableListeners = new ArrayList<>();
	private List<ILifecycleListener> startedListeners = new ArrayList<>();
	private int state;

	@Override
	public synchronized void start() {
		switch(state) {
		  case 0:
			final Iterator<ILifecycleListener> iter = startableListeners.iterator();
			while (iter.hasNext()) {
				final ILifecycleListener listener = iter.next();
				try {
					listener.start();
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
				iter.remove();
				startedListeners.add(listener);
			}
			if (!startableListeners.isEmpty()) {
				throw new IllegalStateException("Expected all listeners to be started.");
			}
			state = 1;
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
				  final ILifecycleListener listener = startedListeners.remove(i);
				  try {
					  listener.shutdown();
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
	public synchronized void addListener(Object pListener) {
		switch(state) {
		case 0:
			startableListeners.add(asListener(pListener));
			break;
		case 1:
			ILifecycleListener listener = asListener(pListener);
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
	public void removeListener(Object pListener) {
		switch(state) {
		case 0:
			remove(startableListeners, pListener);
			break;
		case 1:
			final ILifecycleListener listener = remove(startedListeners, pListener);
			if (listener == null) {
				remove(startableListeners, pListener);
			} else {
				try {
					listener.shutdown();
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
			break;
		case 2:
			throw new IllegalStateException("Already shutting down");
		}
	}

	private ILifecycleListener asListener(Object pObject) {
		if (pObject instanceof ILifecycleListener) {
			return (ILifecycleListener) pObject;
		}
		Method starter = Reflection.findPublicVoidMethodAnnotatedWith(pObject.getClass(), PostConstruct.class);
		Method stopper = Reflection.findPublicVoidMethodAnnotatedWith(pObject.getClass(), PreDestroy.class);
		if (starter == null  &&  stopper == null) {
			throw new IllegalArgumentException("The class " + pObject.getClass()
					+ " does neither implement ILifecycleListener, nor does it have public methods, annotated with @PostConstruct,"
					+ " or @PreDestrory");
		}
		return new ListenerProxy(pObject, starter, stopper);
	}

	private ILifecycleListener remove(List<ILifecycleListener> pListeners, Object pListener) {
		final Iterator<ILifecycleListener> iter = pListeners.iterator();
		while (iter.hasNext()) {
			final ILifecycleListener listener = iter.next();
			if (listener.equals(pListener)) {
				
			} else if (listener instanceof ListenerProxy  &&  ((ListenerProxy) listener).object.equals(pListener)) {
			} else {
				continue;
			}
			iter.remove();
			return listener;
		}
		return null;
	}
}
