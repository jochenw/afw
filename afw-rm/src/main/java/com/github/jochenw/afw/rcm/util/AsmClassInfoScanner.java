package com.github.jochenw.afw.rcm.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class AsmClassInfoScanner {
	public static class ClassInfo {
		private final String className;
		private final List<Annotation> annotations;

		public ClassInfo(String pClassName, List<Annotation> pAnnotations) {
			className = pClassName;
			annotations = pAnnotations;
		}

		public String getClassName() {
			return className;
		}
		public List<Annotation> getAnnotations() {
			return annotations;
		}
	}
	public static class Annotation {
		private final String type;
		private final List<Object> values;

		public Annotation(String pType, List<Object> pValues) {
			type = pType;
			values = pValues;
		}

		public String getType() { return type; }
		public List<Object> getValues() { return values; }

		public Object getValue(String pKey) {
			for (int i = 0;  i < values.size();  i += 2) {
				if (pKey.equals(values.get(i))) {
					return values.get(i+1);
				}
			}
			return null;
		}
	}

	public static class AnnotationVisitor extends ClassVisitor {
		private final List<Annotation> annotations = new ArrayList<>();
		private String className;
		public AnnotationVisitor() {
			super(Opcodes.ASM6);
		}
		@Override
		public void visit(int pVersion, int pAccess, String pName, String pSignature, String pSuperName,
				String[] pInterfaces) {
			super.visit(pVersion, pAccess, pName, pSignature, pSuperName, pInterfaces);
			className = Type.getObjectType(pName).getClassName();
		}
		@Override
		public org.objectweb.asm.AnnotationVisitor visitAnnotation(String pClassDescriptor, boolean visible) {
			final String className = Type.getType(pClassDescriptor).getClassName();
			final List<Object> values = new ArrayList<>();
			return new org.objectweb.asm.AnnotationVisitor(Opcodes.ASM6) {
				@Override
				public void visit(String name, Object value) {
					values.add(name);
					values.add(value);
				}

				@Override
				public void visitEnd() {
					annotations.add(new Annotation(className, values));
				}
			};
		}
	}
	
	public ClassInfo getClassInfo(InputStream pStream) {
		try {
			final ClassReader cr = new ClassReader(pStream);
			final AnnotationVisitor visitor = new AnnotationVisitor();
			cr.accept(visitor, ClassReader.SKIP_CODE+ClassReader.SKIP_DEBUG+ClassReader.SKIP_FRAMES);
			return new ClassInfo(visitor.className, visitor.annotations);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
