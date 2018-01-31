package com.github.jochenw.afw.rm;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.github.jochenw.afw.rm.impl.XmlFileResourceRegistry;

public class RmTest {
	@Test
	public void test1() throws Exception {
		final File schemaDir = new File("target/test1/schema");
		if (!schemaDir.isDirectory()  &&  !schemaDir.mkdirs()) {
			throw new IOException("Unable to create directory: " + schemaDir);
		}
		final RmBuilder rmBuilder = Rm.builder()
				.resourceRepository("com/github/jochenw/afw/rm/test1")
				.defaultResourceRefGuessers()
				.installedResourceRegistry(new XmlFileResourceRegistry(new File(schemaDir, "resource-registry.xml")));
		final Rm rm = rmBuilder.build();
		rm.run();
	}
}
