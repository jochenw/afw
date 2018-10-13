package com.github.jochenw.afw.jsgen.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.util.AbstractBuilder;


public class JSGSourceBuilder extends AbstractBuilder implements JSGSource {
	private final JSGQName type;
	private final List<Object> content = new ArrayList<>();
	private final AnnotationSet annotations = new AnnotationSet();
	private Protection protection;

	public JSGSourceBuilder(JSGQName pType) {
		type = pType;
	}

	@Nonnull public JSGSourceBuilder protection(@Nonnull IProtectable.Protection pProtection) {
		assertMutable();
		protection = pProtection;
		return this;
	}

	@Nonnull public JSGSourceBuilder makePublic() {
		return protection(Protection.PUBLIC);
	}

	@Nonnull public JSGSourceBuilder makeProtected() {
		return protection(Protection.PROTECTED);
	}

	@Nonnull public JSGSourceBuilder makePackageProtected() {
		return protection(Protection.PACKAGE);
	}

	@Nonnull public JSGSourceBuilder makePrivate() {
		return protection(Protection.PRIVATE);
	}

	@Override
	@Nonnull public IProtectable.Protection getProtection() {
		return protection;
	}

	
	@Override
	public AnnotationSet getAnnotations() {
		return annotations;
	}

	@Override
	public JSGQName getType() {
		return type;
	}
	
	@Override
	public JSGSource build() {
		return (JSGSource) super.build();
	}

	@Nonnull public JSGMethodBuilder newMethod(@Nonnull JSGQName pType, @Nonnull String pName) {
		return newMethod(IProtectable.Protection.PACKAGE, pType, pName);
	}

	@Nonnull public JSGMethodBuilder newMethod(@Nonnull String pName) {
		return newMethod(IProtectable.Protection.PACKAGE, JSGQName.VOID_TYPE, pName);
	}

	@Nonnull JSGMethodBuilder newMethod(@Nonnull Class<?> pType, @Nonnull String pName) {
		return newMethod(IProtectable.Protection.PACKAGE, pType, pName);
	}

	@Nonnull JSGMethodBuilder newMethod(@Nonnull IProtectable.Protection pProtection, @Nonnull JSGQName pType, @Nonnull String pName) {
		final JSGMethodBuilder jmb = new JSGMethodBuilder().protection(pProtection).returnType(pType).name(pName);
		content.add(jmb);
		return jmb;
	}

	@Nonnull JSGMethodBuilder newMethod(@Nonnull IProtectable.Protection pProtection, @Nonnull Class<?> pType, @Nonnull String pName) {
		return newMethod(pProtection, JSGQName.valueOf(pType), pName);
	}

	@Override
	@Nonnull public List<Object> getContent() { return content; }
}
