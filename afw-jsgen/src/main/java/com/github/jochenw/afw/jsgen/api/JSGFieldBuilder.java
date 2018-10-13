package com.github.jochenw.afw.jsgen.api;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.util.AbstractBuilder;


public class JSGFieldBuilder extends AbstractBuilder<JSGFieldBuilder> implements JSGField {
	private AnnotationSet annotations = new AnnotationSet();
	private @Nonnull Protection protection;
	private @Nonnull JSGQName type;
	private @Nonnull String name;
	private boolean isStatic;

	@Override
	@Nonnull public AnnotationSet getAnnotations() {
		return annotations;
	}

	@Nonnull JSGFieldBuilder protection(@Nonnull Protection pProtection) {
		assertMutable();
		protection = pProtection;
		return this;
	}

	@Nonnull JSGFieldBuilder makePublic() {
		return protection(Protection.PUBLIC);
	}
	
	@Nonnull JSGFieldBuilder makeProtected() {
		return protection(Protection.PROTECTED);
	}

	@Nonnull JSGFieldBuilder makePackageProtected() {
		return protection(Protection.PACKAGE);
	}

	@Nonnull JSGFieldBuilder makePrivate() {
		return protection(Protection.PRIVATE);
	}
	
	@Override
	@Nonnull public Protection getProtection() {
		return protection;
	}

	@Nonnull JSGFieldBuilder type(@Nonnull JSGQName pType) {
		assertMutable();
		type = pType;
		return this;
	}

	@Override
	@Nonnull public JSGQName getType() {
		return type;
	}

	@Nonnull JSGFieldBuilder name(@Nonnull String pName) {
		assertMutable();
		name = pName;
		return this;
	}

	@Override
	public String getName() {
		return name;
	}

	@Nonnull JSGFieldBuilder makeStatic() {
		return makeStatic(true);
	}

	@Nonnull JSGFieldBuilder makeStatic(boolean pStatic) {
		assertMutable();
		isStatic = pStatic;
		return this;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Override
	protected JSGFieldBuilder self() { return this; }
}
