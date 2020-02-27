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
package com.github.jochenw.afw.core.plugins;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;



/** Interface of a plugin registry, which is defined as a system for management of so-called
 * {@link IExtensionPoint extension points}, and associated plugin lists.
 * A plugin registry is created by so-called {@link Initializer initializer}. Typically, one
 * creates an empty plugin registry, and a list of initializers, which will then be invoked to
 * fill the plugin registry. Once the initializers are done, the plugin registry is ready for
 * use, and the application can start running, using the plugins from the registry.
 * The creation of the initializer list can be delegated to the so-called {@link PluginListParser}.
 * In summary, a typical plugin application could look like this:
 * <pre>
 *   //  Create a new, empty plugin registry:
 *   final IPluginRegistry pluginRegistry = new DefaultPluginRegistry();
 *   //  Jar files, which contribute to the plugin system are supposed to contain this file:
 *   final String pluginUri = "META-INF/plugins/my-plugin-list.xml";
 *   final ClassLoader cl = Thread.currentThread.getContextClassLoader();
 *   final List<Initializer> initializers = PluginListParser.parseAndSort(pluginUri, cl);
 *   for (Initializer init : initializers) {
 *       init.accept(pluginRegistry);
 *   }
 *   // Application start: Plugin registry is ready for use.
 *   ...
 * </pre>
 */
public interface IPluginRegistry {
	/** Interface of an extension point. An extension point is the declaration of a plugin
	 * list, which the declarator intends to use. In other words: You can only add a plugin
	 * to the plugin registry, if a suitable extension point has been declared in advance.
	 * @param <O> The plugin type (Type of the plugins in the list).
	 */
	public interface IExtensionPoint<O extends Object> {
		/**  Returns the list of plugins in the extension point. Initially, this list is empty.
		 * @return List of plugins in the extension point. Initially, this list is empty.
		 *  Never null.
		 */
		@Nonnull List<O> getPlugins();
		/** Iterates over the plugin list, invoking the given consumer for each plugin.
		 * Shortcut for
		 * <pre>
		 *   getPlugins().forEach(pConsumer);
		 * </pre>
		 * @param pConsumer The consumer to invoke for every plugin.
		 */
		public default void forEach(@Nonnull Consumer<O> pConsumer) {
			getPlugins().forEach(pConsumer);
		}
		/** Adds a new plugin to the list.
		 * @param pPlugin The the plugin being added.
		 */
		void addPlugin(O pPlugin);
	}
	/** Interface of a plugin registry initializer. The initializers will prepare the registry by
	 * declaring extension points, and adding plugins to extension points, which already have been
	 * declared.
	 * 
	 */
	public interface Initializer extends Consumer<IPluginRegistry> {
		String getId();
		List<String> getDependsOn();
	}
	public static abstract class AbstractInitializer implements Initializer {
		private String id;
		private List<String> dependsOn;

		@Override
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		@Override
		public List<String> getDependsOn() {
			return dependsOn;
		}
		public void setDependsOn(List<String> dependsOn) {
			this.dependsOn = dependsOn;
		}
	}

	<O extends Object> void addExtensionPoint(@Nonnull Class<O> pType, @Nonnull String pId);
	public default <O extends Object> void addExtensionPoint(@Nonnull Class<O> pType) {
		addExtensionPoint(pType, "");
	}
	@Nullable <O extends Object> IExtensionPoint<O> getExtensionPoint(@Nonnull Class<O> pType, @Nonnull String pId);
	public default @Nullable <O extends Object> IExtensionPoint<O> getExtensionPoint(@Nonnull Class<O> pType) {
		return getExtensionPoint(pType, "");
	}
	public default @Nonnull <O extends Object> IExtensionPoint<O> requireExtensionPoint(@Nonnull Class<O> pType, @Nonnull String pId)
		throws NoSuchElementException {
		final IExtensionPoint<O> ep = getExtensionPoint(pType, pId);
		if (ep == null) {
			throw new NoSuchElementException("No such extension point registered: type=" + pType.getName() + ", id=" + pId);
		}
		return ep;
	}
	public default @Nonnull <O extends Object> IExtensionPoint<O> requireExtensionPoint(@Nonnull Class<O> pType) {
		final IExtensionPoint<O> ep = getExtensionPoint(pType);
		if (ep == null) {
			throw new NoSuchElementException("No such extension point registered: type=" + pType.getName());
		}
		return ep;
	}
	public default <O extends Object> void forEach(@Nonnull Class<O> pType, @Nonnull String pId, @Nonnull Consumer<O> pConsumer) {
		requireExtensionPoint(pType, pId).forEach(pConsumer);
	}
	public default <O extends Object> void forEach(@Nonnull Class<O> pType, @Nonnull Consumer<O> pConsumer) {
		requireExtensionPoint(pType).forEach(pConsumer);
	}
	public default <O extends Object> List<O> getPlugins(Class<O> pType, String pId) {
		return requireExtensionPoint(pType, pId).getPlugins();
	}
	public default <O extends Object> List<O> getPlugins(Class<O> pType) {
		return requireExtensionPoint(pType).getPlugins();
	}
	public default <O extends Object> void addPlugin(Class<O> pType, String pId, O pPlugin) {
		requireExtensionPoint(pType, pId).addPlugin(pPlugin);
	}
	public default <O extends Object> void addPlugin(Class<O> pType, O pPlugin) {
		requireExtensionPoint(pType).addPlugin(pPlugin);
	}
	public default <O extends Object> List<O> requirePlugins(Class<O> pType) {
		return requireExtensionPoint(pType).getPlugins();
	}
	public default <O extends Object> List<O> requirePlugins(Class<O> pType, String pId) {
		return requireExtensionPoint(pType, pId).getPlugins();
	}
}
