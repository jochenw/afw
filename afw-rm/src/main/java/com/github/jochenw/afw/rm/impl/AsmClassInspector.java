package com.github.jochenw.afw.rm.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import com.github.jochenw.afw.rm.api.ClassInfo;
import com.github.jochenw.afw.rm.api.Resource;

public class AsmClassInspector {
	public ClassInfo getClassInfo(InputStream pStream) {
		final String resourceClassId = "L" + Type.getInternalName(Resource.class) + ";";
		try {
			final ClassNode classNode = new ClassNode(Opcodes.ASM6);
			final ClassReader cr = new ClassReader(pStream);
			cr.accept(classNode, 0);
			final List<AnnotationNode> annotations = classNode.visibleAnnotations;
			if (annotations != null) {
				for (AnnotationNode annotation : annotations) {
					if (resourceClassId.equals(annotation.desc)) {
						String type = null;
						String title = null;
						String description = null;
						String version = null;
						if (annotation.values != null) {
							for (int i = 0;  i < annotation.values.size();  i += 2) {
								final Object key = annotation.values.get(i).toString();
								final Object value = annotation.values.get(i+1).toString();
								if ("type".equals(key)) {
									type = value.toString();
								} else if ("title".equals(key)) {
									title = value.toString();
								} else if ("version".equals(key)) {
									version = value.toString();
								} else if ("description".equals(key)) {
									description = value.toString();
								}
										
							}
						}
						final String className = Type.getObjectType(classNode.name).getClassName();
						return new ClassInfo(className, type, title, description, version);
					}
				}
			}
			return null;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
