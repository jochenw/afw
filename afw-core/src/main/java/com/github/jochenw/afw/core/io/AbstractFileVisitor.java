/*
 * Copyright 2020 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;


/** Abstract base class for creating instances of {@link FileVisitor}.
 */
public abstract class AbstractFileVisitor extends SimpleFileVisitor<Path>{
    final StringBuilder sb = new StringBuilder();
    int level = 0;
    private final boolean baseDirIncludedInPath;

    protected AbstractFileVisitor(boolean pBaseDirIncludedInPath) {
    	baseDirIncludedInPath = pBaseDirIncludedInPath;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path pDir, BasicFileAttributes pAttrs) throws IOException {
        /** Add the current directories name to the path.
         */
    	if (level > 0  ||  baseDirIncludedInPath) {
    		final String dirName = pDir.getFileName().toString();
    		if (sb.length() > 0) {
    			sb.append('/');
    		}
    		sb.append(dirName);
    	}
    	++level;
        try {
        	visitDirectory(sb.toString(), pDir, pAttrs);
        } catch (TerminationRequest tr) {
        	return FileVisitResult.SKIP_SUBTREE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path pFile, BasicFileAttributes pAttrs) throws IOException {
        final String fileName = pFile.getFileName().toString();
        final int length = sb.length();
        sb.append('/');
        sb.append(fileName);
        final String path = sb.toString();
        sb.setLength(length);
        visitFile(path, pFile, pAttrs);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path pDir, IOException pExc) throws IOException, TerminationRequest {
        /** Remove the current directories name to the path.
         */
        --level;
        if (level > 0  ||  baseDirIncludedInPath) {
        	
        }
       final int offset = sb.lastIndexOf("/");
       if (offset == -1) {
           sb.setLength(0);
       } else {
           sb.setLength(offset);
       }
       if (pExc == null) {
           return FileVisitResult.CONTINUE;
       } else {
           throw pExc;
       }
   }  

    public void visitDirectory(String pPath, Path pDir, BasicFileAttributes pAttrs) throws IOException {}
    public abstract void visitFile(String pPath, Path pFile, BasicFileAttributes pAttrs) throws IOException;
}
