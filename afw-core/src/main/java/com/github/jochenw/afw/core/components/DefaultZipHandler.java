/**
 * 
 */
package com.github.jochenw.afw.core.components;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
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
	public void createZipFile(Path pSourceDir, Path pZipFile) {
		final Path zipFileDir = pZipFile.getParent();
		try {
			if (!Files.isDirectory(zipFileDir)) {
				throw new IOException("Unable to create zip file, because target directory doesn't exist: " + zipFileDir);
			}
			Files.deleteIfExists(pZipFile);
		} catch (IOException e) {
			throw Exceptions.show(e);
		}
		try (OutputStream os = Files.newOutputStream(pZipFile, StandardOpenOption.CREATE_NEW);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				ZipOutputStream zos = new ZipOutputStream(bos)) {
			Files.walkFileTree(pSourceDir, new AbstractFileVisitor() {
				@Override
				protected void visitFile(String pPath, Path pFile, BasicFileAttributes pAttrs) throws IOException {
					final ZipEntry ze = new ZipEntry(pPath);
					ze.setCreationTime(pAttrs.creationTime());
					ze.setLastModifiedTime(pAttrs.lastModifiedTime());
					ze.setLastAccessTime(pAttrs.lastAccessTime());
					zos.putNextEntry(ze);
					try (InputStream in = Files.newInputStream(pFile)) {
						Streams.copy(in, zos);
					}
					zos.closeEntry();
				}

				@Override
				protected void visitDirectory(String pPath, Path pDir, BasicFileAttributes pAttrs) throws IOException {
					// Do nothing.
				}
			});
		} catch (IOException e) {
			throw Exceptions.show(e);
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

}
