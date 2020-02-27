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
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.xml.XMLConstants;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.github.jochenw.afw.core.components.DefaultInstantiator;
import com.github.jochenw.afw.core.components.IInstantiator;
import com.github.jochenw.afw.core.plugins.DependencyResolver.CircularDependencyException;
import com.github.jochenw.afw.core.plugins.DependencyResolver.DuplicateNodeIdException;
import com.github.jochenw.afw.core.plugins.DependencyResolver.Node;
import com.github.jochenw.afw.core.plugins.DependencyResolver.UnknownNodeIdException;
import com.github.jochenw.afw.core.plugins.IPluginRegistry.AbstractInitializer;
import com.github.jochenw.afw.core.plugins.IPluginRegistry.Initializer;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Sax;


/** A parser for plugin lists.
 */
public class PluginListParser {
	/** Interface of an initializer specification, as read by the parser.
	 */
	public interface IInitializerSpec {
		/**
		 * Returns the initializers class name.
		 * @return The initializers class name.
		 */
		public @Nonnull String getClassName();
		/**
		 * Returns the initializers id.
		 * @return The initializers id.
		 */
		public @Nonnull String getId();
		/**
		 * Returns the initializers dependency list. The dependency list is defined as a set of id's.
		 * The initializer expects, that the given initializers have been executed first.
		 * @return The initializers dependency list. The dependency list is defined as a set of id's.
		 * The initializer expects, that the given initializers have been executed first.
		 * The list may be empty, but bot null.
		 */
		public @Nonnull List<String> getDependsOn();
		/**
		 * Returns the set of bean properties, which are supposed to be set on the initializer.
		 * @return the set of bean properties, which are supposed to be set on the initializer.
		 */
		public @Nonnull default Map<String,String> getProperties() { return null; }
	}
	/** Interface of a listener for initializer specifications.
	 * The {@link PluginListHandler} requires such a listener to report what it reads.
	 */
	public interface Listener {
		/**
		 * Called by the {@link PluginListHandler}, whenever a new initializer specification has been read.
		 * @param pInitializerSpec The new initializer spec. The listener will typically use this to
		 *   construct a suitable object, and add that object to a list.
		 */
		public void accept(IInitializerSpec pInitializerSpec);
	}
	/** The actual SAX parser for plugin lists, which is internally used by the {@link PluginListParser}.
	 */
	public static class PluginListHandler extends Sax.AbstractContentHandler {
		/** The name space of an initializer list, version 1.0.0.
		 * @see #NS_101
		 */
		public static final String NS_100 = "http://namespaces.github.com/jochenw/afw/core/plugins/list/1.0.0";
		/** The name space of an initializer list, version 1.0.1.
		 * @see #NS_100
		 */
		public static final String NS_101 = "http://namespaces.github.com/jochenw/afw/core/plugins/list/1.0.1";
		private final ClassLoader classLoader;
		private final Listener listener;

		/** Creates a new instance, which uses the given class loader to instantiate objects.
		 * @param pClassLoader The class loader being used to instantiate objects.
		 * @param pListener The listener, which will receive the initializer specifications, that the parser reads.
		 */
		public PluginListHandler(ClassLoader pClassLoader, Listener pListener) {
			classLoader = pClassLoader;
			listener = pListener;
		}
		
		private String id, className;
		private List<String> dependsOnList;
		private Map<String,String> properties;
		private String propertyName, propertyValue;
		private StringBuilder propertyValueSb = new StringBuilder();
		
		@Override
		public void startElement(String pUri, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
			assertNamespace(pUri);
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
				  id = className = null;
				  dependsOnList = null;
				  properties = null;
				  id = pAttrs.getValue(XMLConstants.NULL_NS_URI, "id");
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
				  dependsOnList = new ArrayList<>();
				  final String dependsOn = pAttrs.getValue(XMLConstants.NULL_NS_URI, "dependsOn");
				  if (dependsOn != null  &&  dependsOn.trim().length() > 0) {
					  for (StringTokenizer st = new StringTokenizer(dependsOn, " ");  st.hasMoreTokens();  ) {
						  final String s = st.nextToken().trim();
						  dependsOnList.add(s);
					  }
				  }
				  className = pAttrs.getValue(XMLConstants.NULL_NS_URI, "class");
				  if (className == null  ||  className.trim().length() == 0) {
					  throw error("Expected attribute is missing, or empty: pluginListplugin/@class");
				  }
				  break;
			  case 3:
				  if (NS_100.equals(pUri)) {
					  throw error("Unexpected element at level 3: " + pQName);
				  }
				  if (!"property".equals(pLocalName)) {
					  throw error("Expected local name property at level 3, got " + pLocalName);
				  }
				  propertyName = pAttrs.getValue("name");
				  propertyValue = pAttrs.getValue("value");
				  if (propertyValue == null) {
					  propertyValueSb = new StringBuilder();
				  }
				  break;
			  case 4:
				  throw error("Unexpected element at level 4: " + pQName);
			}
		}

		private void assertNamespace(String pUri) throws SAXParseException {
			if (!NS_100.equals(pUri)  &&  !NS_101.equals(pUri)) {
				throw error("Invalid namespace: Expected " + NS_100 + ", or " + NS_101 + ", got " + pUri);
			}
		}

		@Override
		public void endElement(String pUri, String pLocalName, String pQName) throws SAXException {
			assertNamespace(pUri);
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
				  final IInitializerSpec spec = new IInitializerSpec() {
					  @Override
					  public String getId() {
						  return id;
					  }

					  @Override
					  public List<String> getDependsOn() {
						  return dependsOnList;
					  }

					  @Override
					  public String getClassName() {
						  return className;
					  }

					  @Override
					  public Map<String, String> getProperties() {
						  return properties;
					  }
				  };
				  listener.accept(spec);
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

		@Override
		public void startDocument() throws SAXException {
			// Does nothing
		}

		@Override
		public void endDocument() throws SAXException {
			// Does nothing
		}
	}
	/** Reads an initializer list from the given input file, initializes the initializers, as specified by
	 * the initializer list, returns the unsorted list.
	 * @param pFile The input file to read.
	 * @param pClassLoader The class loader, which has been used to search for resource files,
	 *    and will be used to instantiate initializers.
	 * @return The unsorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parse(File pFile, ClassLoader pClassLoader) {
		final List<IPluginRegistry.Initializer> list = new ArrayList<>();
		final PluginListHandler plh = new PluginListHandler(pClassLoader, newListener(pClassLoader, (pi) -> list.add(pi)));
		Sax.parse(pFile, plh);
		return list;
	}
	/**
	 * Creates a new {@link Listener listener}. The created listener will
	 * construct a new initializer, using the given class loader, and pass
	 * the created object to the given consumer.
	 * @param pClassLoader The class loader to use for constructing objects.
	 * @param pConsumer The consumer, which will receive the created objects.
	 * @return A new {@link Listener listener}. The created listener will
	 * construct a new initializer, using the given class loader, and pass
	 * the created object to the given consumer.
	 */
	public static Listener newListener(ClassLoader pClassLoader, Consumer<IPluginRegistry.Initializer> pConsumer) {
		final IInstantiator instantiator = new DefaultInstantiator();
		return new Listener() {
			@Override
			public void accept(IInitializerSpec pInitializerSpec) {
				final IPluginRegistry.Initializer initializer = instantiator.newInstance(pClassLoader, pInitializerSpec.getClassName(), pInitializerSpec.getProperties());
				pConsumer.accept(initializer);
				if (initializer instanceof AbstractInitializer) {
					AbstractInitializer abstractInitializer = (AbstractInitializer) initializer;
					abstractInitializer.setId(pInitializerSpec.getId());
					abstractInitializer.setDependsOn(pInitializerSpec.getDependsOn());
				} else {
					throw new IllegalStateException("The initializer class "
							                        + initializer.getClass().getName() + " doesn't extend "
							                        + AbstractInitializer.class.getName());
				}
			}
		};
	}
	/** Reads an initializer list from the given input file, initializes the initializers, as specified by
	 * the initializer list, returns the unsorted list.
	 * @param pFile The input file to read.
	 * @param pClassLoader The class loader, which has been used to search for resource files,
	 *    and will be used to instantiate initializers.
	 * @return The unsorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parse(Path pFile, ClassLoader pClassLoader) {
		final List<IPluginRegistry.Initializer> list = new ArrayList<>();
		final PluginListHandler plh = new PluginListHandler(pClassLoader, newListener(pClassLoader, (pi) -> list.add(pi)));
		Sax.parse(pFile, plh);
		return list;
	}
	/** Reads an initializer list from the given input source, initializes the initializers, as specified by
	 * the initializer list, returns the unsorted list.
	 * @param pSource The input stream to read.
	 * @param pClassLoader The class loader, which has been used to search for resource files,
	 *    and will be used to instantiate initializers.
	 * @return The unsorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parse(InputSource pSource, ClassLoader pClassLoader) {
		final List<IPluginRegistry.Initializer> list = new ArrayList<>();
		final PluginListHandler plh = new PluginListHandler(pClassLoader, newListener(pClassLoader, (pi) -> list.add(pi)));
		Sax.parse(pSource, plh);
		return list;
	}
	/** Reads an initializer list from the given input stream, initializes the initializers, as specified by
	 * the initializer list, returns the unsorted list.
	 * @param pStream The input stream to read.
	 * @param pClassLoader The class loader, which has been used to search for resource files,
	 *    and will be used to instantiate initializers.
	 * @return The unsorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parse(InputStream pStream, ClassLoader pClassLoader) {
		final List<IPluginRegistry.Initializer> list = new ArrayList<>();
		final PluginListHandler plh = new PluginListHandler(pClassLoader, newListener(pClassLoader, (pi) -> list.add(pi)));
		Sax.parse(pStream, plh);
		return list;
	}
	/** Reads an initializer list from the given input file, initializes the initializers, as specified by
	 * the initializer list, sorts and returns the list.
	 * @param pFile The input file to read.
	 * @param pClassLoader The class loader, which has been used to search for resource files,
	 *    and will be used to instantiate initializers.
	 * @return The sorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parseAndSort(File pFile, ClassLoader pClassLoader) {
		return sort(parse(pFile, pClassLoader));
	}
	/** Reads an initializer list from the given input file, initializes the initializers, as specified by
	 * the initializer list, sorts and returns the list.
	 * @param pFile The input file to read.
	 * @param pClassLoader The class loader, which has been used to search for resource files,
	 *    and will be used to instantiate initializers.
	 * @return The sorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parseAndSort(Path pFile, ClassLoader pClassLoader) {
		return sort(parse(pFile, pClassLoader));
	}
	/** Reads an initializer list from the given input source, initializes the initializers, as specified by
	 * the initializer list, sorts and returns the list.
	 * @param pSource The input source to read.
	 * @param pClassLoader The class loader, which has been used to search for resource files,
	 *    and will be used to instantiate initializers.
	 * @return The sorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parseAndSort(InputSource pSource, ClassLoader pClassLoader) {
		return sort(parse(pSource, pClassLoader));
	}
	/** Reads an initializer list from the given input stream, initializes the initializers, as specified by
	 * the initializer list, sorts and returns the list.
	 * @param pStream The input stream to read.
	 * @param pClassLoader The class loader, which has been used to search for resource files,
	 *    and will be used to instantiate initializers.
	 * @return The sorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parseAndSort(InputStream pStream, ClassLoader pClassLoader) {
		return sort(parse(pStream, pClassLoader));
	}
	/** Reads the resource files, as given by the given list of {@link URL urls}, and
	 * returns a list of all the initializers, as defined in the XML files, so detected,
	 * and returns the list.
	 * @param pUrls A list of URL's, typically obtained by invoking
	 *   {@link ClassLoader#getResources(String)} on the given class loader.
	 * @param pClassLoader The class loader, which has been used to search for resource files,
	 *    and will be used to instantiate initializers.
	 * @return The sorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parseAndSort(List<URL> pUrls, ClassLoader pClassLoader) {
		final List<IPluginRegistry.Initializer> initializers = new ArrayList<>();
		for (URL url : pUrls) {
			try (InputStream in = url.openStream()) {
				final InputSource isource = new InputSource(in);
				isource.setSystemId(url.toExternalForm());
				initializers.addAll(parse(isource, pClassLoader));
			} catch (IOException e) {
				throw Exceptions.newUncheckedIOException(e);
			}
		}
		return sort(initializers);
	}
	/** Reads the resource files, as given by the uri, and returns a list of all the initializers,
	 * as defined in the XML files, so detected, and returns the list.
	 * @param pUri A URI, which is being used to search for plugin list definitions.
	 *    The URI will be used by invoking {@link ClassLoader#getResources(String)} on the given
	 *    class loader.
	 * @param pClassLoader The class loader, which is being used to search for resource files,
	 *    and to instantiate initializers.
	 * @return The sorted initializer list.
	 */
	public static List<IPluginRegistry.Initializer> parseAndSort(String pUri, ClassLoader pClassLoader) {
		final List<URL> urls = new ArrayList<>();
		try {
			for (final Enumeration<?> en = pClassLoader.getResources(pUri);  en.hasMoreElements();  ) {
				final URL url = (URL) en.nextElement();
				urls.add(url);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return parseAndSort(urls, pClassLoader);
	}

	/** Called for sorting the given list of initializers, according to
	 * {@link Initializer#getId() initializer id}, and {@link Initializer#getDependsOn() dependencies}.
	 * @param pList The list being sorted. This list will be left unchanged.
	 * @return The sorted list. A new list will be created.
	 */
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
