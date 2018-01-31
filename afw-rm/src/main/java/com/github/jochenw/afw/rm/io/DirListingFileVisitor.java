package com.github.jochenw.afw.rm.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class DirListingFileVisitor extends SimpleFileVisitor<Path> {
	final StringBuilder sb = new StringBuilder();
	private int level = 0;

	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		if (level++ > 0) {
			if (sb.length() > 0) {
				sb.append('/');
			}
			sb.append(dir.getFileName().toString());
		}
		return super.preVisitDirectory(dir, attrs);
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes pAttrs) throws IOException {
		final int len = sb.length();
		if (sb.length() > 0) {
			sb.append('/');
		}
		sb.append(file.getFileName().toString());
		if (pAttrs.isRegularFile()) {
			noteFile(sb.toString(), file);
		}
		sb.setLength(len);
		return super.visitFile(file, pAttrs);
	}

	protected void noteFile(String pUri, Path pPath) {
	}
	
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		// TODO Auto-generated method stub
		return super.visitFileFailed(file, exc);
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
	    final int offset = sb.lastIndexOf("/");
	    if (offset > 0) {
	    	sb.setLength(offset-1);
	    }
	    return super.postVisitDirectory(dir, exc);
	}
}