package com.github.jochenw.afw.rcm.io;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.rcm.impl.FileRmResourceRef;
import com.github.jochenw.afw.rcm.util.Objects;


public class DirectoryFileScanner {
	public List<FileRmResourceRef> scan(Path pDirectory) {
		Objects.requireNonNull(pDirectory, "Directory");
		final List<FileRmResourceRef> list = new ArrayList<>();
		final DirListingFileVisitor dlfv = new DirListingFileVisitor() {
			@Override
			protected void noteFile(String pUri, Path pPath) {
				final FileRmResourceRef res = new FileRmResourceRef(pPath, pUri);
				list.add(res);
			}
		};
		try {
			Files.walkFileTree(pDirectory, dlfv);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return list;
	}

	public List<FileRmResourceRef> scan(File pDirectory) {
		Objects.requireNonNull(pDirectory, "Directory");
		final List<FileRmResourceRef> list = new ArrayList<>();
		final StringBuilder path = new StringBuilder();
		scan(list, path, pDirectory);
		return list;
	}

	protected void scan(List<FileRmResourceRef> pList, StringBuilder pPath, File pDir) {
		final int len = pPath.length();
		for (File f : pDir.listFiles()) {
			if (pPath.length() > 0) {
				pPath.append('/');
			}
			pPath.append(f.getName());
			if (f.isFile()) {
				pList.add(new FileRmResourceRef(f, pPath.toString()));
			} else if (f.isDirectory()) {
				scan(pList, pPath, f);
			}
			pPath.setLength(len);
		}
		pPath.setLength(len);
	}
}
