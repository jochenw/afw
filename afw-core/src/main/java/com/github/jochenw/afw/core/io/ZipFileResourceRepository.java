package com.github.jochenw.afw.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import com.github.jochenw.afw.core.io.IResourceRepository;
import com.github.jochenw.afw.core.io.IResourceRepository.IResource;


/**
 * Implementation of {@link IResourceRepository}, which reads a zip file, and
 * reports the files in the zip file as resources.
 */
public class ZipFileResourceRepository implements IResourceRepository {
	/** Implementation of {@link IResource}, which represents a file entry in a zip file.
	 */
	public static class ZipFileResource implements IResource {
		private final Path zipFile;
		private final String entry;
		private final String nameSpace;
		private final String uri;
		/** Creates a new instance.
		 * @param pZipFile The zip file, from which to read the resource.
		 * @param pNamespace The resources namespace.
		 * @param pUri The resources URI.
		 */
		public ZipFileResource(Path pZipFile, String pEntry, String pNamespace, String pUri) {
			zipFile = pZipFile;
			entry = pEntry;
			nameSpace = pNamespace;
			uri = pUri;
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
			final String zipUri = "jar:" + zipFile.toUri().toURL().toExternalForm();
			for (;;) {
				final ZipEntry ze = zin.getNextEntry();
				if (ze == null) {
					break;
				}
				if (ze.isDirectory()) {
					continue;
				}
				final String nameSpace = asNamespace(ze.getName());
				final String uri = zipUri + "!" + ze.getName();
				pConsumer.accept(new ZipFileResource(zipFile, ze.getName(), nameSpace, uri));
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	protected String asNamespace(String pName) {
		final String name = pName.replace('\\', '/');
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
			final ZipFile zf = new ZipFile(zfr.zipFile.toFile());
			final ZipEntry ze = zf.getEntry(zfr.entry);
			return zf.getInputStream(ze);
		} else {
			throw new IllegalArgumentException("Invalid resource type: " + resource.getClass().getName());
		}
	}
}
