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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;


/** Default implementation of {@link IPluginRegistry}.
 */
public class DefaultPluginRegistry implements IPluginRegistry {
	/** Key of a registered extension point.
	 */
	public static class Key {
		private final @NonNull Class<? extends Object> type;
		private final @NonNull String id;
		/** Creates a new instance with the given type, and id.
		 * @param pType The extension points type.
		 * @param pId The extension points type.
		 */
		public Key(@NonNull Class<? extends Object> pType, @NonNull String pId) {
			type = pType;
			id = pId;
		}
		@Override
		public int hashCode() {
			return 31 * (31 * 1 + id.hashCode()) + type.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Key other = (Key) obj;
			return id.equals(other.id)  &&  type.equals(other.type);
		}
		/** Returns the extension points type.
		 * @return The extension points type.
		 */
		public Class<? extends Object> getType() {
			return type;
		}
		/** Returns the extension points id.
		 * @return The extension points id.
		 */
		public String getId() {
			return id;
		}
	}
	/** Default implementation of an {@link IPluginRegistry.IExtensionPoint extension point}.
	 * @param <O> The extension points plugin type.
	 */
	public static class DefaultExtensionPoint<O extends Object> implements IPluginRegistry.IExtensionPoint<O> {
		private final List<O> plugins = new ArrayList<O>();
		private List<O> unmodifiablePlugins = Collections.emptyList();

		@Override
		public @NonNull List<O> getPlugins() {
			synchronized(plugins) {
				if (unmodifiablePlugins == null) {
					@SuppressWarnings("null")
					final @NonNull List<O> plugins = Collections.unmodifiableList(this.plugins);
					unmodifiablePlugins = plugins;
					return plugins;
				} else {
					@SuppressWarnings("null")
					final @NonNull List<O> plugins = unmodifiablePlugins;
					return plugins;
				}
			}
		}

		@Override
		public void addPlugin(O pPlugin) {
			synchronized(plugins) {
				plugins.add(pPlugin);
				unmodifiablePlugins = Collections.unmodifiableList(plugins);
			}
		}

	}

	private final Map<Key,DefaultExtensionPoint<Object>> extensionPoints = new HashMap<>();

	@Override
	public <O> void addExtensionPoint(@NonNull Class<O> pType, @NonNull String pId) {
		final Key key = new Key(pType, pId);
		synchronized(extensionPoints) {
			if (extensionPoints.containsKey(key)) {
				throw new IllegalStateException("Extension point already exists: type=" + pType.getName() + ", id=" + pId);
			} else {
				extensionPoints.put(key, new DefaultExtensionPoint<Object>());
			}
		}
	}

	@Override
	public <O> IExtensionPoint<O> getExtensionPoint(@NonNull Class<O> pType, @NonNull String pId) {
		@SuppressWarnings("unchecked")
		final IExtensionPoint<O> ep = (IExtensionPoint<O>) extensionPoints.get(new Key(pType, pId));
		return ep;
	}

}
