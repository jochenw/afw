/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.github.jochenw.afw.core.io.AbstractFileVisitor;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Streams;

/**
 * @author jwi
 *
 */
public class DefaultZipHandler implements IZipFileHandler {
	@Override
	public void createZipFile(Path pSourceDir, Path pZipFile, boolean pBaseDirIncludedInPath) {
		final Path zipFileDir = pZipFile.getParent();
		try {
			if (!Files.isDirectory(zipFileDir)) {
				throw new IOException("Unable to create zip file, because target directory doesn't exist: " + zipFileDir);
			}
			Files.deleteIfExists(pZipFile);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		OutputStream os = null;
		BufferedOutputStream bos = null;
		ZipOutputStream zos = null;
		Throwable th = null;
		try {
			os = Files.newOutputStream(pZipFile, StandardOpenOption.CREATE_NEW);
			bos = new BufferedOutputStream(os);
			zos = new ZipOutputStream(bos);
			final ZipOutputStream zout = zos;
			Files.walkFileTree(pSourceDir, new AbstractFileVisitor(pBaseDirIncludedInPath) {
				@Override
				protected void visitFile(String pPath, Path pFile, BasicFileAttributes pAttrs) throws IOException {
					final ZipEntry ze = new ZipEntry(pPath);
					ze.setCreationTime(pAttrs.creationTime());
					ze.setLastModifiedTime(pAttrs.lastModifiedTime());
					ze.setLastAccessTime(pAttrs.lastAccessTime());
					zout.putNextEntry(ze);
					try (InputStream in = Files.newInputStream(pFile)) {
						Streams.copy(in, zout);
					}
					zout.closeEntry();
				}
			});
			zos.close();
			zos = null;
			bos.close();
			bos = null;
			os.close();
			os = null;
		} catch (Throwable t) {
			th = t;
		} finally {
			if (zos != null) { try { zos.close(); } catch (Throwable t) { if (th == null) { th = t; } } }
			if (bos != null) { try { bos.close(); } catch (Throwable t) { if (th == null) { th = t; } } }
			if (os != null) { try { os.close(); } catch (Throwable t) { if (th == null) { th = t; } } }
		}
		if (th != null) {
			throw Exceptions.show(th);
		}
	}

	@Override
	public void extractZipFile(Path pTargetDir, Path pZipFile) throws IllegalStateException {
		try (InputStream is = Files.newInputStream(pZipFile);
			 BufferedInputStream bis = new BufferedInputStream(is);
			 ZipInputStream zis = new ZipInputStream(bis)) {
			ZipEntry ze = zis.getNextEntry();
			while (ze != null) {
				final Path p = Paths.get(ze.getName());
				if (p.isAbsolute()) {
					throw new IllegalStateException("Invalid zip entry: Expected relative path, got " + ze.getName());
				}
				final Path path = pTargetDir.resolve(p);
				final Path dir = path.getParent();
				Files.createDirectories(dir);
				try (OutputStream os = Files.newOutputStream(path)) {
					Streams.copy(zis, os);
				}
				zis.closeEntry();
				ze = zis.getNextEntry();
			}
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
	}

	@Override
	public InputStream openEntry(Path pZipFile, String pUri) throws IOException {
		final ZipFile zipFile = new ZipFile(pZipFile.toFile());
		final ZipEntry entry = zipFile.getEntry(pUri);
		if (entry == null) {
			throw new IOException("Zip entry " + pUri + " not found in file: " + pZipFile);
		}
		final InputStream in = zipFile.getInputStream(entry);
		return new FilterInputStream(in) {
			@Override
			public int read() throws IOException {
				return in.read();
			}

			@Override
			public int read(byte[] b) throws IOException {
				return in.read(b);
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				// TODO Auto-generated method stub
				return in.read(b, off, len);
			}

			@Override
			public void close() throws IOException {
				Throwable th = null;
				try {
					super.close();
				} catch (IOException e) {
					th = e;
				}
				try {
					zipFile.close();
				} catch (IOException e) {
					if (th == null) {
						th = e;
					}
				}
				if (th != null) {
					throw Exceptions.show(th);
				}
			}
		};
	}

}
