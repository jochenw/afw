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
	public interface Listener {}
	public interface Startable extends Listener {
		public void start() throws Exception;
	}
	public interface Terminable extends Listener {
		public void shutdown() throws Exception;
	}
	public interface TerminableListener extends Startable, Terminable {
	}

	public void addListener(Listener pListener);
	public void removeListener(Listener pListener);
	public void start();
	public void shutdown();
}
