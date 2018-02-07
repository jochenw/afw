package com.github.jochenw.afw.rcm.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.github.jochenw.afw.rcm.Rcm;
import com.github.jochenw.afw.rcm.RcmBuilder;
import com.github.jochenw.afw.rcm.api.InstalledResourceRegistry;
import com.github.jochenw.afw.rcm.impl.XmlFileResourceRegistry;

public class Tests {
	public static InstalledResourceRegistry newResourceRegistry(Class<?> pTestClass) throws IOException {
		final File schemaFile = new File("target/junit-tests/" + pTestClass.getSimpleName() + "/resource-registry.xml");
		final File schemaDir = schemaFile.getParentFile();
		if (schemaDir != null  &&  !schemaDir.isDirectory()  &&  !schemaDir.mkdirs()) {
			throw new IOException("Unable to create schema directory: " + schemaDir.getAbsolutePath());
		}
		return new XmlFileResourceRegistry(schemaFile);
	}

	public static Rcm newRcm(Class<?> pTestClass) throws IOException {
		return newRcmBuilder(pTestClass).build();
	}

	public static RcmBuilder newRcmBuilder(Class<?> pTestClass) throws IOException {
		final InstalledResourceRegistry irr = newResourceRegistry(pTestClass);
		final Properties props = new Properties();
		return Rcm.builder().properties(props).installedResourceRegistry(irr);
	}
}
