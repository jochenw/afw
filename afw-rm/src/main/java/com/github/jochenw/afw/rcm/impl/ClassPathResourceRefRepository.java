package com.github.jochenw.afw.rcm.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.github.jochenw.afw.rcm.api.Resource;
import com.github.jochenw.afw.rcm.api.RmLogger;
import com.github.jochenw.afw.rcm.api.RmResourceRef;
import com.github.jochenw.afw.rcm.api.RmResourceRefRepository;
import com.github.jochenw.afw.rcm.api.RmVersion;
import com.github.jochenw.afw.rcm.util.ClassPathClassFinder;
import com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.Annotation;
import com.github.jochenw.afw.rcm.util.ClassPathClassFinder.ClassInfo;
import com.github.jochenw.afw.rcm.util.ClassPathClassFinder.OtherResourceListener;
import com.github.jochenw.afw.rcm.util.Strings;


public class ClassPathResourceRefRepository implements RmResourceRefRepository {
	private final ClassLoader cl;
	private final String resourcePrefix;
	private boolean usingResourceAnnotation = true;

	public boolean isUsingResourceAnnotation() {
		return usingResourceAnnotation;
	}
	public void setUsingResourceAnnotation(boolean pUsingResourceAnnotation) {
		usingResourceAnnotation = pUsingResourceAnnotation;
	}

	public ClassPathResourceRefRepository(ClassLoader pCl, String pResourcePrefix) {
		cl = pCl;
		resourcePrefix = pResourcePrefix;
	}

	@Override
	public InputStream open(RmResourceRef pResource) throws IOException {
		final SimpleClassPathResourceRef cprr = (SimpleClassPathResourceRef) pResource;
		final File zipFile = cprr.getZipFile();
		if (zipFile == null) {
			final File file = cprr.getFile();
			return new FileInputStream(file);
		}
		final String uri = cprr.getUri();
		@SuppressWarnings("resource")
		final ZipFile zFile = new ZipFile(zipFile);
		final ZipEntry entry = zFile.getEntry(uri);
		return zFile.getInputStream(entry);
	}
	
	
	public ClassLoader getClassLoader() {
		return cl;
	}

	public String getResourcePrefix() {
		return resourcePrefix;
	}

	@Override
	public List<RmResourceRef> getResources(RmLogger pLogger) {
		final Predicate<String> filter = new Predicate<String>() {
			@Override
			public boolean test(String pUri) {
				return resourcePrefix == null  ||  pUri.startsWith(resourcePrefix);
			}
		};
		final List<RmResourceRef> allResources = new ArrayList<>();
		final ClassPathClassFinder<RmResourceRef> cpf = new ClassPathClassFinder<>();
		cpf.setUriFilter(filter);
		cpf.setClassFilter(newClassFilter(pLogger));
		cpf.setOtherResourceListener(new OtherResourceListener() {
			@Override
			public void zipFileResource(File pZipFile, String pUri, String pLocation) {
				allResources.add(new SimpleClassPathResourceRef(pZipFile, pUri, pLocation));
			}
			
			@Override
			public void fileResource(File pFile, String pUri) {
				allResources.add(new SimpleClassPathResourceRef(pFile, pUri));
			}
		});
		final List<RmResourceRef> classResources = cpf.findClasses(cl);
		allResources.addAll(classResources);
		return allResources;
	}

	protected Function<ClassInfo,RmResourceRef> newClassFilter(RmLogger pLogger) {
		return new Function<ClassInfo,RmResourceRef>(){
			@Override
			public RmResourceRef apply(ClassInfo pClassInfo) {
				String type;
				final RmVersion version;
				final Annotation annotation;
				if (isUsingResourceAnnotation()) {
					annotation = pClassInfo.getAnnotation(Resource.class);
					if (annotation == null) {
						return null;
					}
					type = (String) annotation.getValue("type");
					if (type == null  ||  type.length() == 0  ||  "class".equals(type)) {
						type = "class:" + pClassInfo.getType();
					}
					final String versionStr = (String) annotation.getValue("version");
					if (versionStr == null  ||  versionStr.length() == 0) {
						pLogger.warning("Class " + pClassInfo.getType() + " is annotated with @Resource, but has an empty version attribute. Ignoring this class.");
						return null;
					}
					try {
						version = RmVersion.of(versionStr);
					} catch (IllegalArgumentException e) {
						pLogger.warning("Class " + pClassInfo.getType() + " is annotated with @Resource, but has an invalid version attribute. Ignoring this class.");
						return null;
					}
				} else {
					annotation = null;
					version = null;
					type = null;
				}
				final String title = annotation == null ? null : (String) annotation.getValue("title");
				final String description = annotation == null ? null : (String) annotation.getValue("description");
				final String uri = pClassInfo.getUri();
				if (pClassInfo.getZipFile() == null) {
					return new ClassPathResourceRef(pClassInfo.getFile(), pClassInfo.getUri(), version, type, title, description);
				} else {
					return new ClassPathResourceRef(pClassInfo.getZipFile(), pClassInfo.getUri(), Strings.getZipEntryLocation(pClassInfo.getZipFile(),  uri), version, type, title, description);
				}
			}
		};
	}
}
