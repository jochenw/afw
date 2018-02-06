package com.github.jochenw.afw.rcm.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.github.jochenw.afw.rcm.impl.ClassPathResourceRef;
import com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.Annotation;

public class ClassPathClassFinder<T> {
	public static class ClassInfo {
		private final String type;
		private final List<Annotation> annotations;
		private final File zipFile;
		private final String uri;
		private final File file;

		public ClassInfo(com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.ClassInfo pCl, File pZipFile, String pUri) {
			type = pCl.getClassName();
			annotations = pCl.getAnnotations();
			zipFile = pZipFile;
			uri = pUri;
			file = null;
		}

		public ClassInfo(com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.ClassInfo pCl, String pUri, File pFile) {
			type = pCl.getClassName();
			annotations = pCl.getAnnotations();
			zipFile = null;
			uri = pUri;
			file = pFile;
		}

		public Annotation getAnnotation(Class<? extends java.lang.annotation.Annotation> pAnnotationType) {
			for (Annotation ann : annotations) {
				if (pAnnotationType.getName().equals(ann.getType())) {
					return ann;
				}
			}
			return null;
		}

		public String getType() {
			return type;
		}

		public String getUri() {
			return uri;
		}

		public File getZipFile() {
			return zipFile;
		}

		public File getFile() {
			return file;
		}
		
		public String getLocation() {
			if (zipFile == null) {
				return file.getAbsolutePath();
			} else {
				return "zip:" + zipFile.getPath() + "!" + uri;
			}
		}

	}
	public static interface OtherResourceListener {
		void fileResource(File pFile, String pUri);
		void zipFileResource(File pZipFile, String pUri, String pLocation);
	}

	public static InputStream open(ClassInfo pClassInfo) throws IOException {
		final InputStream is;
		if (pClassInfo.file == null) {
			@SuppressWarnings("resource")
			final ZipFile zipFile = new ZipFile(pClassInfo.zipFile);
			final ZipEntry ze = zipFile.getEntry(pClassInfo.uri);
			is = zipFile.getInputStream(ze);
		} else {
			is = new FileInputStream(pClassInfo.file);
		}
		return is;
	}

	public void setOtherResourceListener(OtherResourceListener pListener) {
		otherResourceListener = pListener;
	}

	private static final AsmClassInfoScanner classScanner = new AsmClassInfoScanner();
	private Predicate<String> uriFilter;
	private Function<ClassInfo,T> classFilter;
	private OtherResourceListener otherResourceListener;

	public List<T> findClasses(ClassLoader pCl) {
		final List<T> classes = new ArrayList<>();
		Enumeration<URL> en;
		try {
			en = pCl.getResources("META-INF/MANIFEST.MF");
			while (en.hasMoreElements()) {
				final URL url = en.nextElement();
				findClasses(classes, url);
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		return classes;
	}

	public Predicate<String> getUriFilter() {
		return uriFilter;
	}

	public void setUriFilter(Predicate<String> uriFilter) {
		this.uriFilter = uriFilter;
	}

	public Function<ClassInfo,T> getClassFilter() {
		return classFilter;
	}

	public void setClassFilter(Function<ClassInfo,T> pClassFilter) {
		this.classFilter = pClassFilter;
	}
	
	private void findClasses(List<T> pList, URL pUrl) throws IOException {
		String protocol = pUrl.getProtocol();
		if ("file".equals(protocol)) {
			final File manifestFile = new File(pUrl.getFile());
			final File metaInfDir = manifestFile.getParentFile();
			if (metaInfDir == null  || !metaInfDir.isDirectory()) {
				throw new IllegalStateException("Unable to locate parent directory for file: " + manifestFile);
			}
			final File classPathDir = metaInfDir.getParentFile();
			if (classPathDir == null  ||  !classPathDir.isDirectory()) {
				throw new IllegalStateException("Unable to locate parent directory for directory: " + metaInfDir);
			}
			final StringBuilder sb = new StringBuilder();
			findClassesInDir(pList, classPathDir, sb);
		} else if ("zip".equals(protocol)  ||  "jar".equals(protocol)) {
			final String url = pUrl.toExternalForm();
			String prefix = protocol + ":file:/";
			if (!url.startsWith(prefix)) {
				throw new IllegalStateException("Unable to parse URL: " + pUrl);
			}
			final String path = url.substring(prefix.length());
			final int offset = path.indexOf('!');
			if (offset == -1) {
				throw new IllegalStateException("Unable to parse file path from URL: " + pUrl);
			}
			final String file = path.substring(0, offset);
			final File zipFile = new File(file);
			if (!zipFile.isFile()) {
				throw new IllegalStateException("File " + zipFile + " does not exist for URL: " + pUrl);
			}
			try (InputStream is = new FileInputStream(zipFile);
				 BufferedInputStream bis = new BufferedInputStream(is);
				 final ZipInputStream zis = new ZipInputStream(bis)) {
				for (;;) {
					ZipEntry ze = zis.getNextEntry();
					if (ze == null) {
						break;
					} else if (ze.isDirectory()) {
						continue;
					}
					final String name = ze.getName();
					if (isAcceptedUri(name)) {
						if (name.endsWith(".class")) {
							final com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.ClassInfo clInfo = classScanner.getClassInfo(zis);
							final ClassInfo classInfo = new ClassInfo(clInfo, zipFile, name);
							zis.closeEntry();
							final T t = classFilter.apply(classInfo);
							if (t != null) {
								pList.add(t);
							}
						} else {
							if (otherResourceListener != null) {
								otherResourceListener.zipFileResource(zipFile, name, Strings.getZipEntryLocation(zipFile, name));
							}
						}
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			throw new IllegalStateException("Invalid protocol: " + protocol);
		}
	}

	protected boolean isAcceptedUri(String pUri) {
		return uriFilter == null  ||  uriFilter.test(pUri);
	}

	protected T isAcceptedClass(ClassInfo pClass) {
		return classFilter.apply(pClass);
	}

	
	protected void findClassesInDir(List<T> pList, File pDir, StringBuilder pPath) throws IOException {
		final File[] files = pDir.listFiles();
		final int len = pPath.length();
		for (File f : files) {
			if (len > 0) {
				pPath.append('/');
			}
			pPath.append(f.getName());
			if (f.isDirectory()) {
				findClassesInDir(pList, f, pPath);
			} else if (f.isFile()) {
				final String uri = pPath.toString();
				if (isAcceptedUri(uri)) {
					final String name = f.getName();
					if (name.endsWith(".class")) {
						try (InputStream istream = new FileInputStream(f)) {
							com.github.jochenw.afw.rcm.util.AsmClassInfoScanner.ClassInfo clInfo = classScanner.getClassInfo(istream);
							final ClassInfo classInfo = new ClassInfo(clInfo, pPath.toString(), f);
							final T t = classFilter.apply(classInfo);
							if (t != null) {
								pList.add(t);
							}
						}
					} else {
						if (otherResourceListener != null) {
							otherResourceListener.fileResource(f, uri);
						}
					}
				}
			}
			pPath.setLength(len);
		}
	}

	public static List<ClassInfo> findClassesAnnotatedWith(Class<? extends java.lang.annotation.Annotation> pAnnotationType) {
		final ClassPathClassFinder<ClassInfo> finder = new ClassPathClassFinder<ClassInfo>();
		final Function<ClassInfo,ClassInfo> filter = new Function<ClassInfo,ClassInfo>() {
			@Override
			public ClassInfo apply(ClassInfo pClassInfo) {
				if (pClassInfo.getAnnotation(pAnnotationType) != null) {
					return pClassInfo;
				} else {
					return null;
				}
			}
		};
		finder.setClassFilter(filter);
		return finder.findClasses(Thread.currentThread().getContextClassLoader());
	}
}
