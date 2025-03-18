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


/**
 * The {@link ILifecycleController} is a registry for components, which take part in
 * the applications start/shutdown lifecycle. A component can be registered as a
 * {@link ILifecycleController.Listener}, in which case it will be started
 * automatically. If the component is even implementing {@link ILifecycleController.TerminableListener},
 * then it will also be stopped.
 */
public interface ILifecycleController {
	/**
	 * Interface of a component, which needs to be started.
	 */
	@FunctionalInterface
	public interface Listener {
		/**
		 * Called to start the component.
		 */
		void start();
	}
	/** Interface of a component, which can be stopped.
	 */
	@FunctionalInterface
	public interface Stoppable {
		/** Called to stop the component.
		 * 
		 */
		void stop();
	}
	/**
	 * Interface of a component, which needs to be started,
	 * and stopped.
	 */
	public interface TerminableListener extends Listener, Stoppable {
		/** Called to stop the component.
		 */
		void shutdown();
		/** Called to stop the component.
		 */
		default void stop() { shutdown(); }
		
	}
	/**
	 * Called to start the lifecycle controller, and all registered components,
	 * in the order of registration.
	 */
	void start();
	/**
	 * Called to stop the lifecycle controller, and all registered components,
	 * in reverse order of registration.
	 */
	void shutdown();
	/** Registers a new component. If the lifecycle controller is already started,
	 * then the component will be started immediately. Otherwise, the component
	 * will simply be registered, and started later, with the lifecycle controller.
	 * @param pListener The component, which is being registered.
	 */
	void addListener(Listener pListener);
	/** Removes a component, that has been registered previously. If the lifecycle
	 * controller is already started, then the component will be stopped.
	 * Otherwise, the component will never be stopped, or started.
	 * @param pListener The component, which is being removed.
	 */
	void removeListener(Listener pListener);
}
