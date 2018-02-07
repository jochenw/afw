package com.github.jochenw.afw.rcm.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.github.jochenw.afw.rcm.plugins.schema.RcmPlugins;
import com.github.jochenw.afw.rcm.plugins.schema.RcmPlugins.RcmPlugin;
import com.github.jochenw.afw.rcm.util.ClassPathElementIterator;
import com.github.jochenw.afw.rcm.util.Exceptions;

public class XmlBasedPluginRepository extends AbstractPluginRepository {
	@Override
	protected void init(final Data pData) {
		try {
			final JAXBContext ctx = JAXBContext.newInstance(RcmPlugins.class);
			final Unmarshaller um = ctx.createUnmarshaller();
			final ClassPathElementIterator cpei = new ClassPathElementIterator() {
				@Override
				protected void iterateZipFile(File pZipFile, String pUri, URL pLocation) {
					loadPluginsFromZipFile(pData, um, pZipFile, pUri);
				}
				
				@Override
				protected void iterateDirectory(File pDirectory, String pUri, URL pLocation) {
					loadPluginsFromDirectory(pData, um, pDirectory, pUri);
				}
			};
			cpei.iterate(getComponentFactory().requireInstance(ClassLoader.class), "META-INF/rcm-plugins.xml", 2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected void loadPluginsFromZipFile(Data pData, Unmarshaller pUnmarshaller, File pZipFile, String pUri) {
		try (final ZipFile zipFile = new ZipFile(pZipFile)) {
			final ZipEntry ze = zipFile.getEntry(pUri);
			if (ze == null) {
				throw new IllegalStateException("Unable to locate entry " + pUri + " in ZIP file: " + pZipFile.getAbsolutePath()); 
			}
			try (InputStream is = zipFile.getInputStream(ze)) {
				loadPlugins(pData, pUnmarshaller, is);
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected void loadPluginsFromDirectory(Data pData, Unmarshaller pUnmarshaller, File pDirectory, String pUri) {
		final File pluginFile = new File(pDirectory, pUri);
		if (!pluginFile.isFile()  ||  !pluginFile.canRead()) {
			throw new IllegalStateException("Unable to locate plugin file " + pUri + " in directory " + pDirectory.getAbsolutePath());
		}
		try (InputStream is = new FileInputStream(pluginFile)) {
			loadPlugins(pData, pUnmarshaller, is);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected void loadPlugins(Data pData, Unmarshaller pUnmarshaller, InputStream pStream) throws JAXBException {
		final RcmPlugins plugins = (RcmPlugins) pUnmarshaller.unmarshal(pStream);
		for (RcmPlugin plugin : plugins.getRcmPlugin()) {
			final String className = plugin.getImplClass();
			pData.addPlugin(className);
		}
	}
}
