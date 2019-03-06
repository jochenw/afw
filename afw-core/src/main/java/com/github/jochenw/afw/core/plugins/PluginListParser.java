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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.jochenw.afw.core.plugins.DependencyResolver.CircularDependencyException;
import com.github.jochenw.afw.core.plugins.DependencyResolver.DuplicateNodeIdException;
import com.github.jochenw.afw.core.plugins.DependencyResolver.Node;
import com.github.jochenw.afw.core.plugins.DependencyResolver.UnknownNodeIdException;
import com.github.jochenw.afw.core.plugins.IPluginRegistry.AbstractInitializer;
import com.github.jochenw.afw.core.plugins.IPluginRegistry.Initializer;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Sax;

public class PluginListParser {
	public static class PluginListHandler extends Sax.AbstractContentHandler {
		public static final String NS = "http://namespaces.github.com/jochenw/afw/core/plugins/list/1.0.0";
		private final List<IPluginRegistry.Initializer> plugins = new ArrayList<>();
		private final ClassLoader classLoader;

		public PluginListHandler(ClassLoader pClassLoader) {
			classLoader = pClassLoader;
		}
		
		@Override
		public void startDocument() throws SAXException {
			plugins.clear();
		}

		@Override
		public void endDocument() throws SAXException {
			if (plugins.isEmpty()) {
				throw error("Expected at least one plugin definition");
			}
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			// Ignore this
		}

		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
			// Ignore this
		}

		@Override
		public void startElement(String pUri, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
			if (!NS.equals(pUri)) {
				throw error("Invalid namespace: Expected " + NS + ", got " + pUri);
			}
			switch (super.incLevel()) {
			  case 1:
				  if (!"pluginList".equals(pLocalName)) {
					  throw error("Expected local name pluginList at level 1, got " + pLocalName);
				  }
				  break;
			  case 2:
				  if (!"plugin".equals(pLocalName)) {
					  throw error("Expected local name plugin at level 2, got " + pLocalName);
				  }
				  String id = pAttrs.getValue(XMLConstants.NULL_NS_URI, "id");
				  if (id == null  ||  id.trim().length() == 0) {
					  throw error("Expected attribute is missing, or empty: pluginList/plugin/@id");
				  } else {
					  id = id.trim();
					  for (int i = 0;  i < id.length();  i++) {
						  if (Character.isWhitespace(id.charAt(i))) {
							  throw error("Invalid value for pluginList/plugin/@id (Contains whitespace character):" + id);
						  }
					  }
				  }
				  final List<String> dependsOnList = new ArrayList<>();
				  final String dependsOn = pAttrs.getValue(XMLConstants.NULL_NS_URI, "dependsOn");
				  if (dependsOn != null  &&  dependsOn.trim().length() > 0) {
					  for (StringTokenizer st = new StringTokenizer(dependsOn, " ");  st.hasMoreTokens();  ) {
						  final String s = st.nextToken().trim();
						  dependsOnList.add(s);
					  }
				  }
				  final String className = pAttrs.getValue(XMLConstants.NULL_NS_URI, "class");
				  if (id == null  ||  id.trim().length() == 0) {
					  throw error("Expected attribute is missing, or empty: pluginListplugin/@class");
				  }
				  plugins.add(newPlugin(id, dependsOnList, className));
				  break;
			  case 3:
				  throw error("Unexpected element at level 3: " + pQName);
			}
		}

		protected Initializer newPlugin(String pId, List<String> pDependsOnList, String pClassName) throws SAXException {
			final Class<?> cl;
			try {
				cl = classLoader.loadClass(pClassName);
			} catch (Throwable t) {
				throw error("Unable to load plugin class " + pClassName
						+ ": " + t.getClass().getName() + ", " + t.getMessage(), t);
			}
			final Object o;
			try {
				o = cl.getDeclaredConstructor().newInstance();
			} catch (Throwable t) {
				throw error("Unable to instantiate plugin class " + cl.getName()
						+ ": " + t.getClass().getName() + ", " + t.getMessage(), t);
			}
			final AbstractInitializer initializer;
			if (o instanceof AbstractInitializer) {
				initializer = (AbstractInitializer) o;
			} else if (o instanceof Initializer) {
				initializer = new AbstractInitializer() {
					@Override
					public void accept(IPluginRegistry pRegistry) {
						((Initializer) o).accept(pRegistry);
					}
				};
			} else {
				final Method m;
				try {
					m = o.getClass().getMethod("accept", IPluginRegistry.class);
					final int modifiers = m.getModifiers();
					if (Modifier.isAbstract(modifiers)) {
						throw new IllegalStateException("The accept method is abstract.");
					}
					if (Modifier.isStatic(modifiers)) {
						throw new IllegalStateException("The accept method is static.");
					}
					initializer = new AbstractInitializer() {
						@Override
						public void accept(IPluginRegistry pRegistry) {
							try {
								m.invoke(o, pRegistry);
							} catch (Throwable t) {
								throw Exceptions.show(t);
							}
						}
					};
				} catch (Throwable t) {
					throw error("Invalid plugin class " + o.getClass().getName()
							+ ", expected public method accept(IPluginRegistry) not found: "
							+ t.getClass().getName() + ", " + t.getMessage(), t);
				}
			}
			initializer.setId(pId);
			initializer.setDependsOn(pDependsOnList);
			return initializer;
		}

		@Override
		public void endElement(String pUri, String pLocalName, String pQName) throws SAXException {
			if (!NS.equals(pUri)) {
				throw error("Invalid namespace: Expected " + NS + ", got " + pUri);
			}
			switch (super.decLevel()) {
			  case 1:
				  if (!"pluginList".equals(pLocalName)) {
					  throw error("Expected local name pluginList at level 1, got " + pLocalName);
				  }
				  break;
			  case 2:
				  if (!"plugin".equals(pLocalName)) {
					  throw error("Expected local name plugin at level 2, got " + pLocalName);
				  }
				  break;
			  case 3:
				  throw error("Unexpected element at level 3: " + pQName);
			}
		}

		@Override
		public void characters(char[] pChars, int pOffset, int pLength) throws SAXException {
			for (int i = 0;  i < pLength;  i++) {
				if (!Character.isWhitespace(pChars[pOffset+i])) {
					if (getLevel() == 0) {
						throw error("Unexpected non-whitespace character outside of root element.");
					} else {
						throw error("Unexpected non-whitespace character within element at level " + getLevel());
					}
				}
			}
			super.characters(pChars, pOffset, pLength);
		}

		@Override
		public void ignorableWhitespace(char[] pChars, int pOffset, int pLength) throws SAXException {
			super.ignorableWhitespace(pChars, pOffset, pLength);
		}
	}
	public static List<IPluginRegistry.Initializer> parse(File pFile, ClassLoader pClassLoader) {
		final PluginListHandler plh = new PluginListHandler(pClassLoader);
		Sax.parse(pFile, plh);
		return plh.plugins;
	}
	public static List<IPluginRegistry.Initializer> parse(Path pFile, ClassLoader pClassLoader) {
		final PluginListHandler plh = new PluginListHandler(pClassLoader);
		Sax.parse(pFile, plh);
		return plh.plugins;
	}
	public static List<IPluginRegistry.Initializer> parse(InputSource pSource, ClassLoader pClassLoader) {
		final PluginListHandler plh = new PluginListHandler(pClassLoader);
		Sax.parse(pSource, plh);
		return plh.plugins;
	}
	public static List<IPluginRegistry.Initializer> parse(InputStream pStream, ClassLoader pClassLoader) {
		final PluginListHandler plh = new PluginListHandler(pClassLoader);
		Sax.parse(pStream, plh);
		return plh.plugins;
	}
	public static List<IPluginRegistry.Initializer> parseAndSort(File pFile, ClassLoader pClassLoader) {
		return sort(parse(pFile, pClassLoader));
	}
	public static List<IPluginRegistry.Initializer> parseAndSort(Path pFile, ClassLoader pClassLoader) {
		return sort(parse(pFile, pClassLoader));
	}
	public static List<IPluginRegistry.Initializer> parseAndSort(InputSource pSource, ClassLoader pClassLoader) {
		return sort(parse(pSource, pClassLoader));
	}
	public static List<IPluginRegistry.Initializer> parseAndSort(InputStream pStream, ClassLoader pClassLoader) {
		return sort(parse(pStream, pClassLoader));
	}
	public static List<Initializer> sort(List<Initializer> pList) {
		final List<Node<Initializer>> unsortedNodeList = pList.stream().map((Function<Initializer, Node<Initializer>>) (p) -> new Node<Initializer>(p.getId(), p.getDependsOn(), p)).collect(Collectors.toList());
		try {
			final List<Node<Initializer>> sortedNodeList = new DependencyResolver().resolve(unsortedNodeList);
			return sortedNodeList.stream().map((Function<Node<Initializer>, Initializer>) (n) -> n.getObject()).collect(Collectors.toList());
		} catch (DuplicateNodeIdException e) {
			final String id = e.getId();
			final Initializer initializer0 = (Initializer) e.getNode0().getObject();
			final Initializer initializer1 = (Initializer) e.getNode1().getObject();
			throw new IllegalStateException("Duplicate plugin id: " + id
					+ ", used by " + initializer0.getClass().getName()
					+ ", and " + initializer1.getClass().getName());
		} catch (UnknownNodeIdException e) {
			final String id = e.getId();
			final Initializer initializer = (Initializer) e.getNode().getObject();
			throw new IllegalStateException("Unknown plugin id: " + id
					+ ", referenced by plugin with id: " + initializer.getId());
		} catch (CircularDependencyException e) {
			final String ids = String.join(", ", e.getIds());
			throw new IllegalStateException("Invalid circular dependency for plugin ids: " + ids);
		}
	}
}
