package com.github.jochenw.afw.jsgen.api;

import java.util.List;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.util.AbstractBuilder;


public interface JSGSubroutine extends IAnnotatable, IProtectable, IBodyProvider {
	interface Parameter extends IAnnotatable {
		@Nonnull String getName();
		@Nonnull JSGQName getType();
	}

	class ParameterBuilder extends AbstractBuilder<ParameterBuilder> implements Parameter {
		private final AnnotationSet annotations = new AnnotationSet();
		private JSGQName type;
		private String name;
	
		@Override
		public AnnotationSet getAnnotations() {
			return annotations;
		}
	
		public ParameterBuilder name(String pName) {
			assertMutable();
			name = pName;
			return this;
		}
	
		@Override
		public String getName() {
			return name;
		}
	
		public ParameterBuilder type(JSGQName pType) {
			assertMutable();
			type = pType;
			return this;
		}
	
		@Override
		public JSGQName getType() {
			return type;
		}

		@Override
		protected ParameterBuilder self() { return this; }
	}

	@Nonnull List<JSGSubroutine.Parameter> getParameters();
	@Nonnull List<JSGQName> getExceptions();
	boolean isSuppressing(String pValue);
	JSGClass getSourceClass();
}
