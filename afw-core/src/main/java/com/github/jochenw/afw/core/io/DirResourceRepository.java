package com.github.jochenw.afw.core.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.Consumer;


/** Implementation of {@link IResourceRepository}, which scans a base directory
 * for files, reporting them as resources.
 */
public class DirResourceRepository implements IResourceRepository {
	private static class ImmutablePathResource implements IResource {
		private final String namespace;
		private final String uri;
		private final Path path;
		public ImmutablePathResource(String pNamespace, String pUri, Path pPath) {
			namespace = pNamespace;
			uri = pUri;
			path = pPath;
		}
		@Override
		public String getNamespace() {
			return namespace;
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
		
	private static class PathResource implements IResource {
		private String namespace;
		private String uri;
		private Path path;
		@Override
		public String getNamespace() {
			return namespace;
		}
		@Override
		public String getUri() {
			return uri;
		}
		@Override
		public IResource makeImmutable() {
			return new ImmutablePathResource(namespace, uri, path);
		}
	}
	private final Path baseDir;

	/**
	 * Creates a new instance with the given base directory.
	 * @param pBaseDir The base directory.
	 * @throws NullPointerException The base directory is null.
	 * @throws IllegalArgumentException The base directory doesn't exist, or is no directory.
	 */
	public DirResourceRepository(Path pBaseDir) {
		final Path dir = Objects.requireNonNull(pBaseDir, "Base directory");
		if (!Files.isDirectory(dir)) {
			throw new IllegalArgumentException("Base directory doesn't exist, or is not a directory: " + dir);
		}
		baseDir = dir;
	}

	/**
	 * Creates a new instance with the given base directory.
	 * @param pBaseDir The base directory.
	 * @throws NullPointerException The base directory is null.
	 * @throws IllegalArgumentException The base directory doesn't exist, or is no directory.
	 */
	public DirResourceRepository(File pBaseDir) {
		final File dir = Objects.requireNonNull(pBaseDir, "Base directory");
		if (!dir.isDirectory()) {
			throw new IllegalArgumentException("Base directory doesn't exist, or is not a directory: " + dir);
		}
		baseDir = dir.toPath();
	}

	@Override
	public void list(Consumer<IResource> pConsumer) {
		final PathResource resource = new PathResource();
		final StringBuilder namespace = new StringBuilder();
		final StringBuilder uri = new StringBuilder();
		uri.append(baseDir.toString());
		final FileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
			private int level = 0;
			@Override
			public FileVisitResult preVisitDirectory(Path pDir, BasicFileAttributes pAttrs) throws IOException {
				if (level++ > 0) {
					if (namespace.length() > 0) {
						namespace.append('/');
					}
					final String fileName = pDir.getFileName().toString();
					namespace.append(fileName);
					if (uri.length() > 0) {
						uri.append('/');
					}
					uri.append(fileName);
				}
				return super.preVisitDirectory(pDir, pAttrs);
			}

			@Override
			public FileVisitResult visitFile(Path pFile, BasicFileAttributes pAttrs) throws IOException {
				final int len = uri.length();
				if (len > 0) {
					uri.append('/');
				}
				uri.append(pFile.getFileName().toString());
				resource.uri = uri.toString();
				uri.setLength(len);
				resource.path = pFile;
				resource.namespace = namespace.toString();
				pConsumer.accept(resource);
				return super.visitFile(pFile, pAttrs);
			}

			@Override
			public FileVisitResult postVisitDirectory(Path pDir, IOException pException) throws IOException {
				if (--level > 0) {
					final int nsOffset = namespace.lastIndexOf("/");
					if (nsOffset == -1) {
						namespace.setLength(0);
					} else {
						namespace.setLength(nsOffset);
					}
					final int uriOffset = uri.lastIndexOf("/");
					if (uriOffset == -1) {
						uri.setLength(0);
					} else {
						uri.setLength(uriOffset);
					}
				}
				return super.postVisitDirectory(pDir, pException);
			}
		};
		try {
			Files.walkFileTree(baseDir, fileVisitor);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public InputStream open(IResource pResource) throws IOException {
		final Path path;
		if (pResource instanceof ImmutablePathResource) {
			path = ((ImmutablePathResource) pResource).path;
		} else if (pResource instanceof PathResource) {
			path = ((PathResource) pResource).path;
		} else {
			throw new IllegalArgumentException("Invalid resource type: " + pResource.getClass().getName());
		}
		return Files.newInputStream(path);
	}

}
