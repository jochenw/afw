package com.github.jochenw.afw.core.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
import com.google.common.collect.ImmutableMap;

public class PluginListParserTest {
	private static final String XML_EMPTY_LIST =
			"<pluginList xmlns='" + PluginListParser.PluginListHandler.NS + "'>\n"
			+ "</pluginList>";
	@Test
	public void testEmptyPluginList() throws Exception {
		final Schema schema = newSchema();
		try {
			schema.newValidator().validate(newSource(XML_EMPTY_LIST));
			Assert.fail("Expected Schema validation failure");
		} catch (SAXException se) {
			// Okay, nothing to do
		}
		try {
			PluginListParser.parse(newInputSource(XML_EMPTY_LIST), Thread.currentThread().getContextClassLoader());
		} catch (UndeclaredThrowableException se) {
			final Throwable cause = se.getCause();
			Assert.assertNotNull(cause);
			Assert.assertTrue(cause instanceof SAXParseException);
			final SAXParseException spe = (SAXParseException) cause;
			Assert.assertEquals("Expected at least one plugin definition", spe.getMessage());
		}
	}

	private Schema newSchema() throws SAXException, IOException {
		final URL url = getClass().getResource("plugin-list.xsd");
		Assert.assertNotNull(url);
		final Schema schema;
		try (InputStream in = url.openStream()) {
			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schema = schemaFactory.newSchema(newSource(in));
		}
		return schema;
	}

	public static class MyPlugin extends AbstractInitializer {
		@Override
		public void accept(IPluginRegistry pRegistry) {
			pRegistry.addExtensionPoint(Map.class);
		}
	}
	public static class APlugin implements Consumer<IPluginRegistry> {
		@Override
		public void accept(IPluginRegistry pRegistry) {
			@SuppressWarnings("rawtypes")
			final Map map = new HashMap<>();
			pRegistry.addPlugin(Map.class, map);
			pRegistry.addExtensionPoint(List.class);
		}
	}
	public static class BPlugin implements Initializer {
		@Override
		public void accept(IPluginRegistry pRegistry) {
			@SuppressWarnings("rawtypes")
			final Map map = Collections.emptyMap();
			pRegistry.addPlugin(Map.class, map);
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public List<String> getDependsOn() {
			return null;
		}
	}

	private static final String XML_SINGLE_PLUGIN =
			"<pluginList xmlns='" + PluginListParser.PluginListHandler.NS + "'>\n"
			+ "  <plugin id='MyPlugin' class='" + MyPlugin.class.getName() + "'/>"
			+ "</pluginList>";

	@Test
	public void testSinglePlugin() throws Exception {
		newSchema().newValidator().validate(newSource(XML_SINGLE_PLUGIN));
		final List<IPluginRegistry.Initializer> plugins = PluginListParser.parse(newInputSource(XML_SINGLE_PLUGIN), Thread.currentThread().getContextClassLoader());
		Assert.assertEquals(1, plugins.size());
		assertPlugin(plugins, 0, MyPlugin.class);
	}

	private static final String XML_PLUGIN_DEPENDENCIES =
			"<pluginList xmlns='" + PluginListParser.PluginListHandler.NS + "'>\n"
			+ "  <plugin id='APlugin' dependsOn='MyPlugin' class='" + APlugin.class.getName() + "'/>\n"
			+ "  <plugin id='BPlugin' dependsOn='APlugin MyPlugin' class='" + BPlugin.class.getName() + "'/>\n"
			+ "  <plugin id='MyPlugin' class='" + MyPlugin.class.getName() + "'/>"
			+ "</pluginList>";

	@Test
	public void testPluginDependencies() throws Exception {
		newSchema().newValidator().validate(newSource(XML_PLUGIN_DEPENDENCIES));
		final List<IPluginRegistry.Initializer> plugins = PluginListParser.parse(newInputSource(XML_PLUGIN_DEPENDENCIES), Thread.currentThread().getContextClassLoader());
		Assert.assertEquals(3, plugins.size());
		assertPlugin(plugins, 0, APlugin.class);
		assertPlugin(plugins, 1, BPlugin.class);
		assertPlugin(plugins, 2, MyPlugin.class);
		final List<IPluginRegistry.Initializer> sortedPlugins = PluginListParser.parseAndSort(newInputSource(XML_PLUGIN_DEPENDENCIES), Thread.currentThread().getContextClassLoader());
		Assert.assertEquals(3, sortedPlugins.size());
		assertPlugin(sortedPlugins, 2, APlugin.class);
		assertPlugin(sortedPlugins, 1, BPlugin.class);
		assertPlugin(sortedPlugins, 0, MyPlugin.class);

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
	}

	protected static void assertPlugin(List<Initializer> pList, int pIndex, Class<?> pType) {
		final Initializer initializer = pList.get(pIndex);
		Assert.assertNotNull(initializer);
		Assert.assertTrue(initializer instanceof AbstractInitializer);
		if (AbstractInitializer.class.isAssignableFrom(pType)) {
			Assert.assertTrue(pType.isInstance(initializer));
		} else {
			Assert.assertFalse(pType.isInstance(initializer));
		}
	}
	
	protected Source newSource(InputStream pIn) {
		return new StreamSource(pIn);
	}

	protected InputSource newInputSource(InputStream pIn) {
		return new InputSource(pIn);
	}

	protected Source newSource(String pXml) {
		return new StreamSource(new StringReader(pXml));
	}

	protected InputSource newInputSource(String pXml) {
		return new InputSource(new StringReader(pXml));
	}
}
