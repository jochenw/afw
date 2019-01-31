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
package com.github.jochenw.afw.core.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;


public class DefaultPluginRegistry implements IPluginRegistry {
	public static class Key {
		private final @Nonnull Class<? extends Object> type;
		private final @Nonnull String name;
		public Key(@Nonnull Class<? extends Object> pType, String pName) {
			type = pType;
			name = pName;
		}
		@Override
		public int hashCode() {
			return 31 * (31 * 1 + name.hashCode()) + type.hashCode();
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
			return name.equals(other.name)  &&  type.equals(other.type);
		}
		public Class<? extends Object> getType() {
			return type;
		}
		public String getName() {
			return name;
		}
	}
	public static class DefaultExtensionPoint<O extends Object> implements IPluginRegistry.IExtensionPoint<O> {
		private final List<O> plugins = new ArrayList<O>();
		private List<O> unmodifiablePlugins = Collections.emptyList();

		@Override
		public List<O> getPlugins() {
			synchronized(plugins) {
				if (unmodifiablePlugins == null) {
					unmodifiablePlugins = Collections.unmodifiableList(plugins);
				}
				return unmodifiablePlugins;
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
	public <O> void addExtensionPoint(Class<O> pType, String pId) {
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
	public <O> IExtensionPoint<O> getExtensionPoint(Class<O> pType, String pId) {
		@SuppressWarnings("unchecked")
		final IExtensionPoint<O> ep = (IExtensionPoint<O>) extensionPoints.get(new Key(pType, pId));
		return ep;
	}

}
