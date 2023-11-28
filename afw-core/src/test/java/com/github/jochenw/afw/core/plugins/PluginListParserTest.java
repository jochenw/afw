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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.github.jochenw.afw.core.plugins.IPluginRegistry.AbstractInitializer;
import com.github.jochenw.afw.core.plugins.IPluginRegistry.IExtensionPoint;
import com.github.jochenw.afw.core.plugins.IPluginRegistry.Initializer;


/** Test for the {@link PluginListParser}.
 */
public class PluginListParserTest {
	private static final String XML_EMPTY_LIST =
			"<pluginList xmlns='" + PluginListParser.PluginListHandler.NS_100 + "'>\n"
			+ "</pluginList>";

	/**
	 * Test parsing an empty plugin list.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testEmptyPluginList() throws Exception {
		final Schema schema = newSchema();
		final String xmlEmptyList100 = XML_EMPTY_LIST;
		final String xmlEmptyList101 = XML_EMPTY_LIST.replace(PluginListParser.PluginListHandler.NS_100, PluginListParser.PluginListHandler.NS_100);
		try {
			schema.newValidator().validate(newSource(xmlEmptyList100));
			Assert.fail("Expected Schema validation failure");
		} catch (SAXException se) {
			// Okay, nothing to do
		}
		try {
			schema.newValidator().validate(newSource(xmlEmptyList101));
			Assert.fail("Expected Schema validation failure");
		} catch (SAXException se) {
			// Okay, nothing to do
		}
		try {
			PluginListParser.parse(newInputSource(xmlEmptyList100), Thread.currentThread().getContextClassLoader());
		} catch (UndeclaredThrowableException se) {
			final Throwable cause = se.getCause();
			Assert.assertNotNull(cause);
			Assert.assertTrue(cause instanceof SAXParseException);
			final SAXParseException spe = (SAXParseException) cause;
			Assert.assertEquals("Expected at least one plugin definition", spe.getMessage());
		}
		try {
			PluginListParser.parse(newInputSource(xmlEmptyList100), Thread.currentThread().getContextClassLoader());
		} catch (UndeclaredThrowableException se) {
			final Throwable cause = se.getCause();
			Assert.assertNotNull(cause);
			Assert.assertTrue(cause instanceof SAXParseException);
			final SAXParseException spe = (SAXParseException) cause;
			Assert.assertEquals("Expected at least one plugin definition", spe.getMessage());
		}
	}

	private Schema newSchema(String pVersion) throws SAXException, IOException {
		final URL url = getClass().getResource("plugin-list-" + pVersion + ".xsd");
		Assert.assertNotNull(url);
		final Schema schema;
		try (InputStream in = url.openStream()) {
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schema = schemaFactory.newSchema(newSource(in));
		}
		return schema;
	}
	private Schema newSchema() throws SAXException, IOException {
		return newSchema("100");
	}

	/**
	 * A test plugin.
	 */
	public static class MyPlugin extends AbstractInitializer {
		@Override
		public void accept(IPluginRegistry pRegistry) {
			pRegistry.addExtensionPoint(Map.class);
		}
	}

	/** Another test plugin.
	 */
	public static class APlugin extends AbstractInitializer {
		@Override
		public void accept(IPluginRegistry pRegistry) {
			@SuppressWarnings("rawtypes")
			final Map map = new HashMap<>();
			pRegistry.addPlugin(Map.class, map);
			pRegistry.addExtensionPoint(List.class);
		}
	}
	/** Yet another test plugin.
	 */
	public static class BPlugin extends AbstractInitializer {
		@Override
		public void accept(IPluginRegistry pRegistry) {
			@SuppressWarnings("rawtypes")
			final Map map = Collections.emptyMap();
			pRegistry.addPlugin(Map.class, map);
		}
	}

	private static final String XML_SINGLE_PLUGIN =
			"<pluginList xmlns='" + PluginListParser.PluginListHandler.NS_100 + "'>\n"
			+ "  <plugin id='MyPlugin' class='" + MyPlugin.class.getName() + "'/>"
			+ "</pluginList>";

	/** Test case for parsing a file with a single plugin.
	 * @throws Exception The test failed.
	 */
	public void testSinglePlugin() throws Exception {
		final String xmlSinglePlugin100 = XML_SINGLE_PLUGIN;
		final String xmlSinglePlugin101 = XML_SINGLE_PLUGIN.replace(PluginListParser.PluginListHandler.NS_100, PluginListParser.PluginListHandler.NS_101);
		final Schema schema = newSchema("100");
		System.out.println("Plugin list:");
		System.out.println(xmlSinglePlugin100);
		{
			schema.newValidator().validate(newSource(xmlSinglePlugin100));
			final List<IPluginRegistry.Initializer> plugins = PluginListParser.parse(newInputSource(XML_SINGLE_PLUGIN), Thread.currentThread().getContextClassLoader());
			Assert.assertEquals(1, plugins.size());
			assertPlugin(plugins, 0, MyPlugin.class, "MyPlugin");
		}
		{
			newSchema("101").newValidator().validate(newSource(xmlSinglePlugin101));
			final List<IPluginRegistry.Initializer> plugins = PluginListParser.parse(newInputSource(XML_SINGLE_PLUGIN), Thread.currentThread().getContextClassLoader());
			Assert.assertEquals(1, plugins.size());
			assertPlugin(plugins, 0, MyPlugin.class, "MyPlugin");
		}
		
	}

	private static final String XML_PLUGIN_DEPENDENCIES =
			"<pluginList xmlns='" + PluginListParser.PluginListHandler.NS_100 + "'>\n"
			+ "  <plugin id='APlugin' dependsOn='MyPlugin' class='" + APlugin.class.getName() + "'/>\n"
			+ "  <plugin id='BPlugin' dependsOn='APlugin MyPlugin' class='" + BPlugin.class.getName() + "'/>\n"
			+ "  <plugin id='MyPlugin' class='" + MyPlugin.class.getName() + "'/>"
			+ "</pluginList>";

	/** Test case for parsing a file with several plugins, and dependencies.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testPluginDependencies() throws Exception {
		final String xmlPluginDependencies100 = XML_PLUGIN_DEPENDENCIES;
		final String xmlPluginDependencies101 = xmlPluginDependencies100.replace(PluginListParser.PluginListHandler.NS_100, PluginListParser.PluginListHandler.NS_101);
		final BiConsumer<String,String> tester = (s, t) -> {
			try {
				newSchema(t).newValidator().validate(newSource(s));
			} catch (SAXException e) {
				throw new UndeclaredThrowableException(e);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			final List<IPluginRegistry.Initializer> plugins = PluginListParser.parse(newInputSource(s), Thread.currentThread().getContextClassLoader());
			Assert.assertEquals(3, plugins.size());
			assertPlugin(plugins, 0, APlugin.class, "APlugin", "MyPlugin");
			assertPlugin(plugins, 1, BPlugin.class, "BPlugin", "APlugin", "MyPlugin");
			assertPlugin(plugins, 2, MyPlugin.class, "MyPlugin");
			final List<IPluginRegistry.Initializer> sortedPlugins = PluginListParser.parseAndSort(newInputSource(s), Thread.currentThread().getContextClassLoader());
			Assert.assertEquals(3, sortedPlugins.size());
			assertPlugin(sortedPlugins, 0, MyPlugin.class, "MyPlugin");
			assertPlugin(sortedPlugins, 1, APlugin.class, "APlugin", "MyPlugin");
			assertPlugin(sortedPlugins, 2, BPlugin.class, "BPlugin", "APlugin", "MyPlugin");

			final IPluginRegistry registry = new DefaultPluginRegistry();
			sortedPlugins.forEach((p) -> 
			{ p.accept(registry); });
			@SuppressWarnings("rawtypes")
			final IExtensionPoint<Map> epMap = (IExtensionPoint<Map>) registry.getExtensionPoint(Map.class);
			Assert.assertNotNull(epMap);
			@SuppressWarnings("rawtypes")
			final IExtensionPoint<List> epList = (IExtensionPoint<List>) registry.getExtensionPoint(List.class);
			Assert.assertNotNull(epList);
			Assert.assertNull(registry.getExtensionPoint(Collection.class));
			@SuppressWarnings("rawtypes")
			List<Map> mapPlugins = epMap.getPlugins();
			Assert.assertEquals(2, mapPlugins.size());
			Assert.assertTrue(mapPlugins.get(0) != null  &&  mapPlugins.get(0) instanceof HashMap);
			Assert.assertTrue(mapPlugins.get(1) != null  &&  mapPlugins.get(1).getClass().getName().equals("java.util.Collections$EmptyMap"));
		};
		tester.accept(xmlPluginDependencies100, "100");
		tester.accept(xmlPluginDependencies101, "101");
	}

	/** Asserts, that the the given list of initializers contains a particular element at the
	 * given index.
	 * @param pList The list of initializers, that is being tested.
	 * @param pIndex The index of the element in the list, that is being tested.
	 * @param pType The expected type of the element.
	 * @param pId The expected id of the element.
	 * @param pDependsOn The expected dependencies of the element.
	 */
	protected static void assertPlugin(List<Initializer> pList, int pIndex, Class<?> pType, String pId, String... pDependsOn) {
		final Initializer initializer = pList.get(pIndex);
		Assert.assertNotNull(initializer);
		Assert.assertTrue(initializer instanceof AbstractInitializer);
		if (AbstractInitializer.class.isAssignableFrom(pType)) {
			Assert.assertTrue(initializer.getClass().getName(), pType.isInstance(initializer));
		} else {
			Assert.assertFalse(pType.isInstance(initializer));
		}
		Assert.assertEquals(pId, initializer.getId());
		final List<String> dependsOn = initializer.getDependsOn();
		if (pDependsOn == null  ||  pDependsOn.length == 0) {
			Assert.assertTrue(dependsOn == null  ||  dependsOn.size() == 0);
		} else {
			Assert.assertEquals(pDependsOn.length, dependsOn.size());
		}
	}

	/** Creates a {@link Source}, that produces the given input.
	 * @param pIn The created sources input.
	 * @return The created source.
	 */
	protected Source newSource(InputStream pIn) {
		return new StreamSource(pIn);
	}

	/** Creates a {@link InputSource}, that produces the given input.
	 * @param pIn The created sources input.
	 * @return The created source.
	 */
	protected InputSource newInputSource(InputStream pIn) {
		return new InputSource(pIn);
	}

	/** Creates a {@link Source}, that produces the given input.
	 * @param pXml The created sources input.
	 * @return The created source.
	 */
	protected Source newSource(String pXml) {
		return new StreamSource(new StringReader(pXml));
	}

	/** Creates an {@link InputSource}, that produces the given input.
	 * @param pXml The created sources input.
	 * @return The created source.
	 */
	protected InputSource newInputSource(String pXml) {
		return new InputSource(new StringReader(pXml));
	}
}
