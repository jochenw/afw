package com.github.jochenw.afw.rm.impl;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.github.jochenw.afw.rm.api.RmVersion;
import com.github.jochenw.afw.rm.util.Objects;


public class XmlFileResourceRegistry extends AbstractInstalledResourceRegistry {
	public static final String NS = "http://namespaces.github.com/jochenw/afw/rm/XmlFileResourceRegistry/1.0.0";
	private final Path path;
	private final String tenant;

	public XmlFileResourceRegistry(File pFile) {
		this(pFile, null);
	}

	public XmlFileResourceRegistry(File pFile, String pTenant) {
		this (pFile.toPath(), pTenant);
	}

	public XmlFileResourceRegistry(Path pPath) {
		this(pPath, null);
	}

	public XmlFileResourceRegistry(Path pPath, String pTenant) {
		Objects.requireNonNull(pPath, "Path");
		path = pPath;
		tenant = pTenant;
	}

	@Override
	protected List<InstalledResource> readInstalledResources() {
		try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
			 InputStream istream = Channels.newInputStream(fileChannel);
			PushbackInputStream pistream = new PushbackInputStream(istream)) {
			// Replace pistream with mystream, so that pistream won't be closed when calling parse(InputStream) below.
			final FilterInputStream mystream = new FilterInputStream(pistream) {
				@Override
				public void close() throws IOException {
					// Do nothing.
				}
			};
			FileLock lock = fileChannel.lock(0, Long.MAX_VALUE, true);
			final int firstByte = pistream.read();
			final List<InstalledResource> list;
			if (firstByte == -1) {
				// File is empty, return an empty list.
				list = Collections.emptyList();
			} else {
				pistream.unread(firstByte);
				list = parse(mystream);  // Note, this will close the stream.
			}
			lock.close();
			return list;
		} catch (NoSuchFileException e) {
			return Collections.emptyList();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (SAXException|ParserConfigurationException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	protected List<InstalledResource> parse(InputStream pStream) throws SAXException, IOException, ParserConfigurationException {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
		final Document doc = dbf.newDocumentBuilder().parse(pStream);
		final Element root = doc.getDocumentElement();
		final List<InstalledResource> list = new ArrayList<>();
		if (!NS.equals(root.getNamespaceURI())  ||  !"xml-resource-registry".equals(root.getLocalName())) {
			throw new SAXException("Expected root element xml-resource-registry, got " + root.getTagName());
		}
		for (Node node = root.getFirstChild();  node != null;  node = node.getNextSibling()) {
			if (Node.ELEMENT_NODE == node.getNodeType()) {
				final Element e = (Element) node;
				if (!NS.equals(e.getNamespaceURI())  ||  !"resource".equals(e.getLocalName())) {
					throw new SAXException("Expected element xml-resource-registry/resource " + NS + ", got " +
				                           "xml-resource-registry/" + e.getTagName());
				}
				final String resTenant = e.getAttribute("tenant");
				if (tenant == null) {
					if (resTenant != null  &&  resTenant.length() > 0) {
						continue;
					}
				} else {
					if (!tenant.equals(resTenant)) {
						continue;
					}
				}
				final String type = e.getAttribute("type");
				final String versionStr = e.getAttribute("version");
				final String description = e.getAttribute("description");
				final String title = e.getAttribute("title");
				if (type == null  ||  type.length() == 0) {
					throw new SAXException("Missing, or empty, attribute: resource/@title");
				}
				if (versionStr == null  ||  versionStr.length() == 0) {
					throw new SAXException("Missing, or empty, attribute: resource/@version");
				}
				final RmVersion version;
				try {
					version = RmVersion.of(versionStr);
				} catch (IllegalArgumentException ex) {
					throw new SAXException("Invalid value for attribue resource/@version: " + versionStr);
				}
				if (title == null) {
					throw new SAXException("Missing attribute: resource/@title");
				}
				list.add(new InstalledResource(versionStr, version, type, title, description));
			}
		}
		return list;
	}
}
