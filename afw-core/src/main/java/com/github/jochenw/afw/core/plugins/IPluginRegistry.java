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
import java.util.Objects;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;



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
 *   final List&lt;Initializer&gt; initializers = PluginListParser.parseAndSort(pluginUri, cl);
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
		@NonNull List<O> getPlugins();
		/** Iterates over the plugin list, invoking the given consumer for each plugin.
		 * Shortcut for
		 * <pre>
		 *   getPlugins().forEach(pConsumer);
		 * </pre>
		 * @param pConsumer The consumer to invoke for every plugin.
		 */
		public default void forEach(@NonNull Consumer<O> pConsumer) {
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
		/** Returns the initializers id.
		 * @return The initializers id.
		 */
		@NonNull String getId();
		/** Returns the list of initializer id's, that this one depends on.
		 * The requirement is, that the initializers with those id's have
		 * already been executed.
		 * @return The initializers id.
		 */
		@NonNull List<String> getDependsOn();
	}
	/**
	 * Abstract base implementation of {@link Initializer}.
	 */
	public static abstract class AbstractInitializer implements Initializer {
		/** Creates a new instance.
		 */
		protected AbstractInitializer() {}

		private String id;
		private List<String> dependsOn;

		@Override
		public @NonNull String getId() {
			return Objects.requireNonNull(id);
		}
		/**
		 * Sets the initializers id.
		 * @param pId The initializers id.
		 */
		public void setId(String pId) {
			id = pId;
		}
		@Override
		public @NonNull List<String> getDependsOn() {
			return Objects.requireNonNull(dependsOn);
		}
		/**
		 * Sets the id's of the initializers, that this one depends on.
		 * @param pDependsOn List of the initializer id's, that this one depends on.
		 */
		public void setDependsOn(List<String> pDependsOn) {
			dependsOn = pDependsOn;
		}
	}

	/** Adds an extension point to the plugin registry.
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @param pId The extension points id.
	 */
	<O extends Object> void addExtensionPoint(@NonNull Class<O> pType, @NonNull String pId);
	/** Adds an extension point to the plugin registry, using the default id "".
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 */
	public default <O extends Object> void addExtensionPoint(@NonNull Class<O> pType) {
		addExtensionPoint(pType, "");
	}
	/** Returns an extension point from the plugin registry.
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @param pId The extension points id.
	 * @return The requested extension point, or null, if no such extension point has been registered.
	 */
	@Nullable <O extends Object> IExtensionPoint<O> getExtensionPoint(@NonNull Class<O> pType, @NonNull String pId);
	/** Returns an extension point with the default plugin id "" from the plugin registry.
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @return The requested extension point, or null, if no such extension point has been registered.
	 */
	public default @Nullable <O extends Object> IExtensionPoint<O> getExtensionPoint(@NonNull Class<O> pType) {
		return getExtensionPoint(pType, "");
	}
	/** Returns an extension point from the plugin registry.
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @param pId The extension points id.
	 * @return The requested extension point. Never null.
	 * @throws NoSuchElementException No such extension point has been registered.
	 */
	public default @NonNull <O extends Object> IExtensionPoint<O> requireExtensionPoint(@NonNull Class<O> pType, @NonNull String pId)
		throws NoSuchElementException {
		final IExtensionPoint<O> ep = getExtensionPoint(pType, pId);
		if (ep == null) {
			throw new NoSuchElementException("No such extension point registered: type=" + pType.getName() + ", id=" + pId);
		}
		return ep;
	}
	/** Returns an extension point with the default plugin id "" from the plugin registry.
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @return The requested extension point. Never null.
	 * @throws NoSuchElementException No such extension point has been registered.
	 */
	public default @NonNull <O extends Object> IExtensionPoint<O> requireExtensionPoint(@NonNull Class<O> pType) {
		final IExtensionPoint<O> ep = getExtensionPoint(pType);
		if (ep == null) {
			throw new NoSuchElementException("No such extension point registered: type=" + pType.getName());
		}
		return ep;
	}
	/**
	 * Invokes the given consumer on any plugin, that has been registered in an extension point.
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @param pId The extension points id.
	 * @param pConsumer The consumer, which is being invoked.
	 * @throws NoSuchElementException No such extension point has been registered.
	 */
	public default <O extends Object> void forEach(@NonNull Class<O> pType, @NonNull String pId, @NonNull Consumer<O> pConsumer)
		throws NoSuchElementException {
		requireExtensionPoint(pType, pId).forEach(pConsumer);
	}
	/**
	 * Invokes the given consumer on any plugin, that has been registered in an extension point
	 * with the default plugin id "".
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @param pConsumer The consumer, which is being invoked.
	 * @throws NoSuchElementException No such extension point has been registered.
	 */
	public default <O extends Object> void forEach(@NonNull Class<O> pType, @NonNull Consumer<O> pConsumer)
		throws NoSuchElementException {
		requireExtensionPoint(pType).forEach(pConsumer);
	}
	/**
	 * Returns a list of all the plugins, that have been registered in an extension point.
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @param pId The extension points id.
	 * @return The plugins, that have been registered on the given extension point.
	 * @throws NoSuchElementException No such extension point has been registered.
	 */
	public default <O extends Object> List<O> getPlugins(@NonNull Class<O> pType, @NonNull String pId) {
		return requireExtensionPoint(pType, pId).getPlugins();
	}
	/**
	 * Returns a list of all the plugins, that have been registered in an extension point
	 * with the default plugin id "".
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @return The plugins, that have been registered on the given extension point.
	 * @throws NoSuchElementException No such extension point has been registered.
	 */
	public default <O extends Object> List<O> getPlugins(@NonNull Class<O> pType) {
		return requireExtensionPoint(pType).getPlugins();
	}
	/**
	 * Adds a new plugin to an extension point.
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @param pId The extension points id.
	 * @param pPlugin The plugin, that is being added.
	 * @throws NoSuchElementException No such extension point has been registered.
	 */
	public default <O extends Object> void addPlugin(@NonNull Class<O> pType, @NonNull String pId, @NonNull O pPlugin) {
		requireExtensionPoint(pType, pId).addPlugin(pPlugin);
	}
	/**
	 * Adds a new plugin to an extension point with the default plugin id.
	 * @param <O> The extension points type.
	 * @param pType The extension points type.
	 * @param pPlugin The plugin, that is being added.
	 * @throws NoSuchElementException No such extension point has been registered.
	 */
	public default <O extends Object> void addPlugin(@NonNull Class<O> pType, @NonNull O pPlugin) {
		requireExtensionPoint(pType).addPlugin(pPlugin);
	}
}
