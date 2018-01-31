package com.github.jochenw.afw.rm.impl;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.rm.api.InstalledResourceRegistry;
import com.github.jochenw.afw.rm.api.RmResourceInfo;
import com.github.jochenw.afw.rm.api.RmVersion;
import com.github.jochenw.afw.rm.impl.AbstractInstalledResourceRegistry.InstalledResource;


public class XmlFileResourceRegistryTest {
	@Test
	public void testRead() {
		final URL url = getClass().getResource("xml-resource-registry.xml");
		Assert.assertNotNull(url);
		Assert.assertEquals("file", url.getProtocol());
		final File file = new File(url.getFile());
		Assert.assertTrue(file.isFile());
		Assert.assertTrue(file.canRead());

		final XmlFileResourceRegistry xfrr0 = new XmlFileResourceRegistry(file);
		final List<InstalledResource> noTenantResources = xfrr0.readInstalledResources();
		Assert.assertNotNull(noTenantResources);
		Assert.assertEquals(1, noTenantResources.size());
		final InstalledResource ir0 = noTenantResources.get(0);
		Assert.assertEquals("0.0.3", ir0.getVersionStr());
		Assert.assertEquals("unknown", ir0.getType());
		Assert.assertEquals("Unknown Resource", ir0.getTitle());
		Assert.assertEquals("Unknown resource description", ir0.getDescription());
		assertInstalled(xfrr0, "0.0.3", "unknown", "Unknown Resource");
		assertNotInstalled(xfrr0, "0.0.2", "unknown", "Unknown Resource");
		assertNotInstalled(xfrr0, "0.0.3", "unKnown", "Unknown Resource");
		assertNotInstalled(xfrr0, "0.0.3", "unknown", "Unknown resource");
		assertNotInstalled(xfrr0, "0.0.2", "sql", "Some Resource");

		final XmlFileResourceRegistry xfrr1 = new XmlFileResourceRegistry(file, "FooApp");
		final List<InstalledResource> fooAppResources = xfrr1.readInstalledResources();
		Assert.assertEquals(2, fooAppResources.size());
		final InstalledResource ir1 = fooAppResources.get(0);
		Assert.assertEquals("0.0.2", ir1.getVersionStr());
		Assert.assertEquals("sql", ir1.getType());
		Assert.assertEquals("Some Resource", ir1.getTitle());
		Assert.assertEquals("Description of some resource", ir1.getDescription());
		final InstalledResource ir2 = fooAppResources.get(1);
		Assert.assertEquals("0.0.1", ir2.getVersionStr());
		Assert.assertEquals("sql", ir2.getType());
		Assert.assertEquals("Other Resource", ir2.getTitle());
		Assert.assertEquals("Description of some other resource", ir2.getDescription());

		final XmlFileResourceRegistry xfrr2 = new XmlFileResourceRegistry(file, "OtherApp");
		final List<InstalledResource> otherAppResources = xfrr2.readInstalledResources();
		Assert.assertEquals(1, otherAppResources.size());
		final InstalledResource ir3 = otherAppResources.get(0);
		Assert.assertEquals("0.0.1", ir3.getVersionStr());
		Assert.assertEquals("sql", ir3.getType());
		Assert.assertEquals("Another Resource", ir3.getTitle());
		Assert.assertEquals("Description of another resource", ir3.getDescription());
	}

	private void assertInstalled(InstalledResourceRegistry pIrr, String pVersionStr, String pType, String pTitle) {
		final RmResourceInfo rri = newRmResourceInfo(pVersionStr, pType, pTitle);
		Assert.assertTrue(pIrr.isInstalled(rri));
	}

	private void assertNotInstalled(InstalledResourceRegistry pIrr, String pVersionStr, String pType, String pTitle) {
		final RmResourceInfo rri = newRmResourceInfo(pVersionStr, pType, pTitle);
		Assert.assertFalse(pIrr.isInstalled(rri));
	}

	private RmResourceInfo newRmResourceInfo(String pVersionStr, String pType, String pTitle) {
		final RmVersion version = RmVersion.of(pVersionStr);
		return new RmResourceInfo() {
			@Override
			public RmVersion getVersion() {
				return version;
			}
			
			@Override
			public String getUri() {
				throw new IllegalStateException("Not implemented");
			}
			
			@Override
			public String getType() {
				return pType;
			}
			
			@Override
			public String getTitle() {
				return pTitle;
			}
			
			@Override
			public String getDescription() {
				throw new IllegalStateException("Not implemented");
			}
		};
	}


}
