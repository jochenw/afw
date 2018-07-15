package com.github.jochenw.afw.core.components;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Reflection;

public class LifecycleController {
	public interface IListener {
		public void start();
		public void shutdown();
	}
	private static class ReflectionListener implements IListener {
		private final Object object;
		private final Method startMethod, shutdownMethod;

		ReflectionListener(Object pObject, Method pStartMethod, Method pShutdownMethod) {
			object = pObject;
			startMethod = pStartMethod;
			shutdownMethod = pShutdownMethod;
		}

		@Override
		public void start() {
			invoke(startMethod);
		}

		@Override
		public void shutdown() {
			invoke(shutdownMethod);
		}

		private void invoke(Method pMethod) {
			if (pMethod != null) {
				try {
					if (!pMethod.isAccessible()) {
						pMethod.setAccessible(true);
					}
					pMethod.invoke(object);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
		}
	}

	private final List<IListener> startableListeners = new ArrayList<>();
	private final List<IListener> startedListeners = new ArrayList<>();
	private enum State {
		WAITING, STARTED, TERMINATED
	}
	private State state = State.WAITING;

	public synchronized void addListener(Object pListener) {
		final IListener listener = asListener(pListener);
		switch (state) {
		case WAITING:
			startableListeners.add(listener);
			break;
		case STARTED:
			try {
				listener.start();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			startedListeners.add(listener);
			break;
		case TERMINATED:
			throw new IllegalStateException("Already shutting down");
		default:
			throw new IllegalStateException("Invalid state: " + state);
		}
	}

	public synchronized void start() {
		switch(state) {
		case WAITING:
			state = State.STARTED;
			while (!startableListeners.isEmpty()) {
				final IListener listener = startableListeners.remove(0);
				try {
					listener.start();
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
				startedListeners.add(listener);
			}
			break;
		case STARTED:
			throw new IllegalStateException("Already started");
		case TERMINATED:
			throw new IllegalStateException("Already shutting down");
		default:
			throw new IllegalStateException("Invalid state: " + state);
		}
	}

	public synchronized void shutdown() {
		switch(state) {
		case WAITING:
			throw new IllegalStateException("Not started");
		case STARTED:
			state = State.TERMINATED;
			Throwable th = null;
			for (int i = startedListeners.size()-1;  i >= 0;  i--) {
				final IListener listener = startedListeners.remove(i);
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
			break;
		case TERMINATED:
			throw new IllegalStateException("Already shutting down");
		default:
			throw new IllegalStateException("Invalid state: " + state);
		}
	}

	protected IListener asListener(Object pObject) {
		Objects.requireNonNull(pObject, "Listener");
		if (pObject instanceof IListener) {
			return (IListener) pObject;
		}
		Method postConstructMethod = Reflection.findPublicVoidMethodAnnotatedWith(pObject.getClass(), PostConstruct.class);
		Method preDestroyMethod = Reflection.findPublicVoidMethodAnnotatedWith(pObject.getClass(), PreDestroy.class);
		if (postConstructMethod == null  &&  preDestroyMethod == null) {
			throw new IllegalArgumentException("Instance of " + pObject.getClass().getName()
					+ " is neither implementing IListener, nor has it valid @PostConstruct, or @PreDestroy methods");
		}
		return new ReflectionListener(pObject, postConstructMethod, preDestroyMethod);
	}
	
}
