package com.github.jochenw.afw.jsgen.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.api.JSGFactory.NamedResource;


public class FileJavaSourceWriter extends AbstractSourceWriter {
	private final @Nonnull File javaSourceTargetDirectory, resourceFileDirectory;
	private boolean creatingTargetDirectoryForbidden;
	private boolean avoidingUpdates;

	public boolean isAvoidingUpdates() {
		return avoidingUpdates;
	}

	public void setAvoidingUpdates(boolean avoidingUpdates) {
		this.avoidingUpdates = avoidingUpdates;
	}

	public boolean isCreatingTargetDirectoryForbidden() {
		return creatingTargetDirectoryForbidden;
	}

	public void setCreatingTargetDirectoryForbidden(boolean creatingTargetDirectoryForbidden) {
		this.creatingTargetDirectoryForbidden = creatingTargetDirectoryForbidden;
	}

	public FileJavaSourceWriter(@Nonnull File pTargetDirectory) {
		this(Objects.requireNonNull(pTargetDirectory, "Target Directory"), Objects.requireNonNull(pTargetDirectory, "Target Directory"));
	}

	public FileJavaSourceWriter(@Nonnull File pJavaSourceTargetDirectory, @Nonnull File pResourceFileTargetDirectory) {
		javaSourceTargetDirectory = Objects.requireNonNull(pJavaSourceTargetDirectory, "Java Source Target Directory");
		resourceFileDirectory = Objects.requireNonNull(pResourceFileTargetDirectory, "Resource File Target Directory");
	}

	protected OutputStream open(@Nonnull NamedResource pResource) throws IOException {
		final File targetDir;
		if (pResource.isJavaSource()) {
			targetDir = javaSourceTargetDirectory;
		} else if (pResource.isResourceFile()) {
			targetDir = resourceFileDirectory;
		} else {
			throw new IllegalStateException("Resource is neither Java Source File, nor Resource File");
		}
		final File f = new File(targetDir, pResource.getName().getQName());
		final File dir = f.getParentFile();
		if (dir != null  &&  !dir.isDirectory()) {
			if (isCreatingTargetDirectoryForbidden()) {
				throw new IOException("Target directory does not exist: " + dir.getAbsolutePath());
			} else {
				if (!dir.mkdirs()) {
					throw new IOException("Unable to create target directory: " + dir.getAbsolutePath());
				}
			}
		}
		if (isAvoidingUpdates()) {
			return new ByteArrayOutputStream() {
				@Override
				public void close() throws IOException {
					FileJavaSourceWriter.this.write(f, toByteArray());
					super.close();
				}
			};
		} else {
			return new FileOutputStream(f);
		}
	}


	protected void write(File pFile, byte[] pBytes) throws IOException {
		if (isAvoidingUpdates()) {
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			baos.write(pBytes);
			final byte[] bytes = baos.toByteArray();
			boolean needWrite = false;
			if (pFile.isFile()) {
				if (isAvoidingUpdates()) {
					try (InputStream is = new FileInputStream(pFile);
						 BufferedInputStream bis = new BufferedInputStream(is)) {
						for (int i = 0;  i < bytes.length;  i++) {
							int b1 = bis.read();
							int b2 = bytes[i];
							if (b1 != b2) {
								needWrite = true;
								break;
							}
						}
						if (bis.read() != -1) {
							needWrite = true;
						}
					}
				} else {
					needWrite = true;
				}
			} else {
				needWrite = true;
			}
			if (needWrite) {
				try (OutputStream os = new FileOutputStream(pFile)) {
					os.write(pBytes);
				}
			}
		}
	}

}
