package com.github.jochenw.afw.bootstrap.archive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.github.jochenw.afw.bootstrap.io.IoStreamSupplier;

public interface IArchiveHandler {
	public interface ArchiveEntryConsumer {
		public void accept(String pPath, IoStreamSupplier pStreamSupplier) throws IOException;
	}
	public void read(IoStreamSupplier pStreamSupplier, String pUri, ArchiveEntryConsumer pEntryConsumer);
	public default void read(Path pPath, ArchiveEntryConsumer pEntryConsumer) {
		read(() -> Files.newInputStream(pPath), pPath.toString(), pEntryConsumer);
	}
	public boolean isHandling(Path pPath);
	public static List<IArchiveHandler> getArchiveHandlers() {
		return Arrays.asList(new ZipArchiveHandler());
	}
}
