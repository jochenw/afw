package com.github.jochenw.afw.jsgen.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.jsgen.api.IAnnotatable.Annotation;
import com.github.jochenw.afw.jsgen.api.IAnnotatable.AnnotationSet;
import com.github.jochenw.afw.jsgen.api.IProtectable;
import com.github.jochenw.afw.jsgen.api.JSGConstructor;
import com.github.jochenw.afw.jsgen.api.JSGField;
import com.github.jochenw.afw.jsgen.api.JSGImportSorter;
import com.github.jochenw.afw.jsgen.api.JSGInnerClass;
import com.github.jochenw.afw.jsgen.api.JSGMethod;
import com.github.jochenw.afw.jsgen.api.JSGQName;
import com.github.jochenw.afw.jsgen.api.JSGSource;
import com.github.jochenw.afw.jsgen.api.JSGStaticInitializer;
import com.github.jochenw.afw.jsgen.api.JSGSubroutine;
import com.github.jochenw.afw.jsgen.util.Objects;


public class DefaultJavaSourceFormatter implements JSGSourceFormatter {
	private static class Data {
		private final JSGSourceTarget target;
		private int numIndents;

		Data(JSGSourceTarget pTarget) {
			target = pTarget;
		}

		public void write(Object pObject) {
			try {
				target.write(pObject);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		public void newLine() {
			try {
				target.newLine();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		public void indent() {
			try {
				for (int i = 0;  i < numIndents;  i++) {
					target.write("    ");
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		public void incIndent() {
			++numIndents;
		}

		public void decIndent() {
			--numIndents;
		}
	}

	@Override
	public void write(JSGSource pSource, JSGSourceTarget pTarget) throws IOException {
		final Data data = new Data(pTarget);
		data.write("package ");
		data.write(pSource.getType().getPackageName());
		data.write(";");
		data.newLine();
		data.newLine();
		final List<List<JSGQName>> importLists = getSortedImports();
		if (importLists != null  &&  !importLists.isEmpty()) {
			for (int i = 0;  i < importLists.size();  i++) {
				final List<JSGQName> importList = importLists.get(i);
				if (importList != null) {
					for (JSGQName n : importList) {
						data.write("import ");
						data.write(n.getQName());
						data.write(";");
						data.newLine();
					}
					data.newLine();
				}
			}
		}
		
		write(pSource.getAnnotations(), data);
		if (!pSource.getAnnotations().isEmpty()) {
			data.newLine();
		}
		write(pSource.getProtection(), data);
		if (pSource.isInterface()) {
			data.write("interface ");
		} else {
			data.write("class");
		}
		data.write(" ");
		data.write(pSource.getType().getClassName());
		final List<JSGQName> extendedClasses = pSource.getExtendedClasses();
		if (!extendedClasses.isEmpty()) {
			for (int i = 0;  i < extendedClasses.size();  i++) {
				if (i == 0) {
					data.write(" extends ");
				} else {
					data.write(", ");
				}
				data.write(extendedClasses.get(i));
			}
		}
		final List<JSGQName> implementedInterfaces = pSource.getImplementedInterfaces();
		if (!implementedInterfaces.isEmpty()) {
			for (int i = 0;  i < implementedInterfaces.size();  i++) {
				if (i == 0) {
					data.write(" implements");
				} else {
					data.write(", ");
				}
				data.write(implementedInterfaces.get(i));
			}
		}
		data.write(" {");
		data.incIndent();
		data.newLine();
		pSource.getContent().forEach((o) -> {
			if (o instanceof JSGMethod) {
				write((JSGMethod) o, data);
			} else if (o instanceof JSGConstructor) {
				write((JSGConstructor) o, data);
			} else if (o instanceof JSGField) {
				write((JSGField) o, data);
			} else if (o instanceof JSGInnerClass) {
				write((JSGInnerClass) o, data);
			} else if (o instanceof JSGStaticInitializer) {
				write((JSGStaticInitializer) o, data);
			} else {
				throw new IllegalStateException("Invalid object type: " + o.getClass().getName());
			}
		});
		data.decIndent();
		data.write("}");
		data.newLine();
	}

	private List<JSGQName> importedNames;
	private JSGImportSorter importSorter;

	public List<JSGQName> getImportedNames() {
		return importedNames;
	}

	public void setImportedNames(List<JSGQName> importedNames) {
		this.importedNames = importedNames;
	}

	
	public JSGImportSorter getImportSorter() {
		return importSorter;
	}

	public void setImportSorter(JSGImportSorter importSorter) {
		this.importSorter = importSorter;
	}

	protected List<List<JSGQName>> getSortedImports() {
		final List<List<JSGQName>> lists = new ArrayList<>();
		if (importedNames != null) {
			if (importSorter == null) {
				lists.add(importedNames);
			} else {
				for (JSGQName name : importedNames) {
					final int category = importSorter.getCategory(name);
					while(lists.size() < category+1) {
						lists.add(null);
					}
					List<JSGQName> names = lists.get(category);
					if (names == null) {
						names = new ArrayList<>();
						lists.set(category, names);
					}
					names.add(name);
				}
			}
		}
		return lists;
	}
	
	protected void write(IProtectable.Protection pProtection, Data pTarget) {
		switch (pProtection) {
		  case PUBLIC: pTarget.write("public "); break;
		  case PROTECTED: pTarget.write("protected "); break;
		  case PRIVATE: pTarget.write("private "); break;
		  case PACKAGE: break;
		  default: throw new IllegalStateException("Invalid protection: " + pProtection);
		}
	}

	protected void write(AnnotationSet pAnnotations, Data pTarget) {
		if (!pAnnotations.isEmpty()) {
			pAnnotations.forEach((a) -> write(a, pTarget));
		}
	}

	protected void write(Annotation pAnnotation, Data pTarget) {
		throw new IllegalStateException("Not implemented");
	}

	protected void write(JSGSubroutine pSubroutine, Data pTarget) {
		pTarget.indent();
		write(pSubroutine.getAnnotations(), pTarget);
		if (!pSubroutine.getAnnotations().isEmpty()) {
			pTarget.newLine();
		}
		write(pSubroutine.getProtection(), pTarget);
		if (pSubroutine instanceof JSGMethod) {
			final JSGMethod method = (JSGMethod) pSubroutine;
			if (method.isAbstract()) {
				pTarget.write("abstract ");
			}
			if (method.isStatic()) {
				pTarget.write("static ");
			}
			if (method.isFinal()) {
				pTarget.write("final ");
			}
			if (method.isSynchronized()) {
				pTarget.write("synchronized ");
			}
			pTarget.write(method.getReturnType());
		}
		pTarget.write(" ");
		if (pSubroutine instanceof JSGMethod) {
			final JSGMethod method = (JSGMethod) pSubroutine;
			pTarget.write(method.getName());
		} else {
			pTarget.write(pSubroutine.getSourceClass().getType().getClassName());
		}
		pTarget.write("(");
		for (int i = 0;  i < pSubroutine.getParameters().size();  i++) {
			final JSGSubroutine.Parameter param = pSubroutine.getParameters().get(i);
			if (i > 0) {
				pTarget.write(", ");
			}
			write(param.getAnnotations(), pTarget);
			if (!param.getAnnotations().isEmpty()) {
				pTarget.write(" ");
			}
			pTarget.write(param.getType());
			pTarget.write(" ");
			pTarget.write(param.getName());
		}
		pTarget.write(") ");
		final List<JSGQName> exceptions = pSubroutine.getExceptions();
		if (!exceptions.isEmpty()) {
			for (int i = 0;  i < exceptions.size();  i++) {
				if (i == 0) {
					pTarget.write("throws ");
				} else {
					pTarget.write(", ");
				}
				pTarget.write(exceptions.get(i));
			}
		}
		pTarget.write(" {");
		pTarget.incIndent();
		pTarget.newLine();
		pSubroutine.body().getContents().forEach((o) -> {
			writeLine(o, pTarget);
		});
		pTarget.decIndent();
		pTarget.indent();
		pTarget.write("}");
		pTarget.newLine();
	}

	protected void writeLine(Object pObject, Data pTarget) {
		pTarget.indent();
		write(pObject, pTarget);
		pTarget.newLine();
	}

	protected void write(Object pObject, Data pTarget) {
		Objects.requireNonNull(pObject, "Object");
		if (pObject.getClass().isArray()) {
			final Object[] array = (Object[]) pObject;
			for (int i = 0;  i < array.length;  i++) {
				write(array[i], pTarget);
			}
		} else if (pObject instanceof Iterable) {
			@SuppressWarnings("unchecked")
			final Iterable<Object> iterable = (Iterable<Object>) pObject;
			for (Object o : iterable) {
				write(o, pTarget);
			}
		} else if (pObject instanceof JSGQName) {
			pTarget.write(pObject);
		} else if (pObject instanceof Class<?>) {
			final Class<?> cl = (Class<?>) pObject;
			pTarget.write(JSGQName.valueOf(cl));
		} else {
			pTarget.write(pObject.toString());
		}
	}
	
	protected void write(JSGField pField, Data pTarget) {
		
	}

	protected void write(JSGInnerClass pInnerClass, Data pTarget) {
		throw new IllegalStateException("Not implemented");
	}

	protected void write(JSGStaticInitializer pInitializer, Data pTarget) {
		throw new IllegalStateException("Not implemented");
	}
}
