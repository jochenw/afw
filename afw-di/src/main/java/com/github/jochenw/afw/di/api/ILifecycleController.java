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
package com.github.jochenw.afw.di.api;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * The {@link ILifecycleController} is a registry for components, which take part in
 * the applications start/shutdown lifecycle. A component can be registered as a
 * {@link ILifecycleController.Listener}, in which case it will be started
 * automatically. If the component is even implementing {@link ILifecycleController.TerminableListener},
 * then it will also be stopped.
 * 
 * The easiest way to make use of the controller is by using @PostConstruct
 * and/or @PreDestroy annotations as a marker for the objects startup,
 * or shutdown methods.
 * @see PostConstruct
 * @see PreDestroy
 */
public interface ILifecycleController {
	/** Interface of an object, which can be attached to the
	 * controller for startup and/or shutdown. In general, a
	 * class would never implement the {@code Listener}
	 * interface directly. Instead, one would implement
	 * either {@link ILifecycleController.Startable},
	 * {@link ILifecycleController.Terminable}, or
	 * {@link ILifecycleController.TerminableListener}.
	 * 
	 * Even more, instead of implementing either of these
	 * interfaces, it is sufficient to use @PostConstruct,
	 * and/or @PreDestroy annotations on suitable methods,
	 * in which case the component factory will silently
	 * create a {@code Listener}, that invokes these
	 * methods internally.
	 * @see ILifecycleController.Startable
	 * @see ILifecycleController.Terminable
	 * @see ILifecycleController.TerminableListener
	 * @see PostConstruct
	 * @see PreDestroy
	 */
	public interface Listener {}

	/** Interface of a listener, which can be started by invoking
	 * it's {@link #start()} method. If the listener supports
	 * shutting down as well, then you should implement
	 * {@link ILifecycleController.TerminableListener} instead.
	 * @see ILifecycleController.Listener
	 * @see ILifecycleController.Terminable
	 * @see ILifecycleController.TerminableListener
	 */
	public interface Startable extends Listener {
		/** Called to start the object. This will
		 * be done, after the component factory
		 * has initialized the object, but before
		 * the component factory considers the
		 * object to be usable.
		 * @throws Exception Starting the object
		 *   has failed.
		 */
		public void start() throws Exception;
	}
	/** Interface of a listener, which can be shutted down
	 * it's {@link #shutdown()} method. If the listener supports
	 * starting as well, then you should implement
	 * {@link ILifecycleController.TerminableListener} instead.
	 * @see ILifecycleController.Listener
	 * @see ILifecycleController.Startable
	 * @see ILifecycleController.TerminableListener
	 */
	public interface Terminable extends Listener {
		/** Called to shutdown an object, which
		 * has been previously started.
		 * @throws Exception Shutting down the
		 *   object has failed.
		 */
		public void shutdown() throws Exception;
	}
	/** Interface of a listener which can be both started, and
	 * shutted down.
	 */
	public interface TerminableListener extends Startable, Terminable {
	}

	/** Called to add a listener to the controller.
	 * 
	 *  If the controller has not yet been started, then nothing will happen immediately.
	 *  However, if the controller's {@link #start()} method is being invoked later on,
	 *  and the listener is an instance of {@link Startable}, or
	 *  {@link TerminableListener}, then the controller will start the listener at that
	 *  point.
	 *  
	 *  If the controller is already started, and the listener is an instance of 
	 *  {@link Startable}, or {@link TerminableListener}, then the controller will
	 *  start the listener immediately.
	 *
	 *  When the controller's method {@link #shutdown()} is being invoked later on,
	 *  and the listener is an instance of {@link TerminableListener}, that has been
	 *  started, or the listener is an instance of {@link Terminable}, then the
	 *  listener's shutdown method will be invoked.
	 *
	 * @param pListener The listener, which is being added.
	 */
	public void addListener(Listener pListener);
	/** Called to remove a listener from the controller. After removing the
	 * listener, the controller will act, as if it were shutting down with
	 * regards to this listener:
	 * 
	 * If the listener is an instance of {@link TerminableListener}, that has been
	 * started, or the listener is an instance of {@link Terminable}, then the
	 * listener's shutdown method will be invoked.
     *
	 * @param pListener The listener, which is being removed.
	 */
	public void removeListener(Listener pListener);
	/** Starts the controller. This will effectively start all listeners, that
	 * are currently added, and can be started.
	 */
	public void start();
	/** Performs a shutdown on the controller. This will effectively shutdown all
	 * listeners, that are currently added, and can be stopped.
	 */
	public void shutdown();
}
