package com.github.jochenw.afw.bootstrap.archive;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.github.jochenw.afw.bootstrap.io.IoStreamSupplier;

public class ZipArchiveHandler implements IArchiveHandler {
	@Override
	public void read(IoStreamSupplier pStreamSupplier, String pUri, ArchiveEntryConsumer pEntryConsumer) {
		try (InputStream is = pStreamSupplier.get();
			 ZipInputStream zis = new ZipInputStream(is)) {
			for (;;) {
				final ZipEntry ze = zis.getNextEntry();
				if (ze == null) {
					break;
				} else {
					pEntryConsumer.accept(ze.getName(), () -> {
						return new InputStream() {
							@Override
							public int read() throws IOException {
								return zis.read();
							}

							@Override
							public int read(byte[] pBuffer) throws IOException {
								return zis.read(pBuffer);
							}

							@Override
							public int read(byte[] pBuffer, int pOff, int pLen) throws IOException {
								return zis.read(pBuffer, pOff, pLen);
							}

							@Override
							public void close() throws IOException {
								zis.closeEntry();
							}
						};
					});
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public boolean isHandling(Path pPath) {
		return pPath.getFileName().toString().endsWith(".zip");
	}
}
