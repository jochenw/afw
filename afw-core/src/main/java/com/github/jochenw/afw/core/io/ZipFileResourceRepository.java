package com.github.jochenw.afw.core.io;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.github.jochenw.afw.core.io.IResourceRepository.IResource;
import com.github.jochenw.afw.core.util.Exceptions;


/**
 * Implementation of {@link IResourceRepository}, which reads a zip file, and
 * reports the files in the zip file as resources.
 */
public class ZipFileResourceRepository implements IResourceRepository {
	/** Implementation of {@link IResource}, which represents a file entry in a zip file.
	 */
	public static class ZipFileResource implements IResource {
		private final Path zipFile;
		private final String nameSpace;
		private final String uri;
		private final String entry;
		/** Creates a new instance.
		 * @param pZipFile The zip file, from which to read the resource.
		 * @param pNamespace The resources namespace.
		 * @param pEntry The zip files entry.
		 * @param pUri The resources URI.
		 */
		public ZipFileResource(Path pZipFile, String pEntry, String pNamespace, String pUri) {
			zipFile = pZipFile;
			nameSpace = pNamespace;
			uri = pUri;
			entry = pEntry;
		}
		@Override
		public String getNamespace() {
			return nameSpace;
		}
		@Override
		public String getUri() {
			return uri;
		}
		@Override
		public IResource makeImmutable() {
			return this;
		}
	}
	private final Path zipFile;

	/**
	 * Creates a new instance with the given zip file.
	 * @param pZipFile The zip file to read.
	 */
	public ZipFileResourceRepository(Path pZipFile) {
		zipFile = Objects.requireNonNull(pZipFile, "Zip file");
	}

	/**
	 * Creates a new instance with the given zip file.
	 * @param pZipFile The zip file to read.
	 */
	public ZipFileResourceRepository(File pZipFile) {
		zipFile = Objects.requireNonNull(pZipFile, "Zip file").toPath();
	}

	@Override
	public void list(Consumer<IResource> pConsumer) {
		try (InputStream in = Files.newInputStream(zipFile);
			 ZipInputStream zin = new ZipInputStream(in)) {
			for (;;) {
				final ZipEntry ze = zin.getNextEntry();
				if (ze == null) {
					break;
				}
				if (ze.isDirectory()) {
					continue;
				}
				final String nameSpace = asNamespace(ze.getName());
				final String uri = asUri(ze.getName());
				pConsumer.accept(new ZipFileResource(zipFile, ze.getName(), nameSpace, uri));
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected String asUri(String pName) {
		final String uri = pName.replace('\\', '/');
		if (uri.startsWith("./")) {
			return uri.substring(2);
		} else if (uri.startsWith("/")) {
			return uri.substring(1);
		} else {
			return uri;
		}
	}

	protected String asNamespace(String pName) {
		final String name = asUri(pName);
		final int offset = name.lastIndexOf('/');
		if (offset == -1) {
			return "";
		} else {
			return name.substring(0, offset);
		}
	}

	@Override
	public InputStream open(IResource pResource) throws IOException {
		final IResource resource = Objects.requireNonNull(pResource, "Resource");
		if (resource instanceof ZipFileResource) {
			final ZipFileResource zfr = (ZipFileResource) resource;
			@SuppressWarnings("resource")
			ZipFile zf = null;
			try {
				zf = new ZipFile(zfr.zipFile.toFile());
				final ZipFile zfFinal = zf;
				final ZipEntry ze = zfFinal.getEntry(zfr.entry);
				final InputStream in = zfFinal.getInputStream(ze);
				final FilterInputStream result = new FilterInputStream(in) {
					@Override
					public void close() throws IOException {
						super.close();
						zfFinal.close();
					}
				};
				return result;
			} catch (Throwable t) {
				if (zf != null) {
					try { zf.close(); } catch (IOException e) { /* Ignore this, and throw the cause. */ }
				}
				throw Exceptions.show(t);
			}
		} else {
			throw new IllegalArgumentException("Invalid resource type: " + resource.getClass().getName());
		}
	}

	/**
	 * Opens the given resource. Assumes, that the resource is valid, as testable
	 * by {@link #isValidResource(com.github.jochenw.afw.core.io.IResourceRepository.IResource)}.
	 * @param pResource The resource being opened.
	 * @return An opened {@link InputStream}, which allows reading the resource.
	 * @throws IllegalArgumentException The resource isn't valid.
	 * @throws UncheckedIOException The resource is valid, but opening the resource failed anyways.
	 */
	public static InputStream openResource(IResource pResource) {
		try {
			final IResource resource = Objects.requireNonNull(pResource, "Resource");
			if (resource instanceof ZipFileResource) {
				final ZipFileResource zfr = (ZipFileResource) resource;
				@SuppressWarnings("resource")
				ZipFile zf = new ZipFile(zfr.zipFile.toFile());
				try {
					final ZipEntry ze = zf.getEntry(zfr.getUri());
					return new FilterInputStream(zf.getInputStream(ze)) {
						@Override
						public void close() throws IOException {
							super.close();
							zf.close();
						}
					};
				} catch (Throwable t) {
					if (zf != null) {
						try { zf.close(); } catch (Throwable th) { /* Ignore this, throw the cause. */ }
					}
					throw Exceptions.show(t);
				}
			} else {
				throw new IllegalArgumentException("Invalid resource type: " + resource.getClass().getName());
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** Returns, whether this resource instance can be opened by either
	 * {@link #open(com.github.jochenw.afw.core.io.IResourceRepository.IResource)}, or
	 * {@link #openResource(com.github.jochenw.afw.core.io.IResourceRepository.IResource)}.
	 * @param pResource The resource being tested.
	 * @return True, if the above methods can open the resource. Otherwise false.
	 */
	public static boolean isValidResource(IResource pResource) {
		return Objects.requireNonNull(pResource, "Resource") instanceof ZipFileResource;
	}
}
