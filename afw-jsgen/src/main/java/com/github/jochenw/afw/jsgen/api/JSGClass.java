package com.github.jochenw.afw.jsgen.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.util.AbstractBuilder;


public abstract class JSGClassBuilder<T extends JSGClassBuilder<T>> extends AbstractBuilder<T> implements JSGClass {
	private final JSGQName type;
	private final List<Object> content = new ArrayList<>();
	private final AnnotationSet annotations = new AnnotationSet();
	private Protection protection;
	private final List<JSGQName> extendedClasses = new ArrayList<>();
	private final List<JSGQName> implementedInterfaces = new ArrayList<>();
	private boolean isInterface;

	protected JSGClassBuilder(JSGQName pType) {
		type = pType;
	}

	@Override
	public JSGQName getType() {
		return type;
	}

	@Override
	public AnnotationSet getAnnotations() {
		return annotations;
	}

	@Nonnull public T protection(@Nonnull IProtectable.Protection pProtection) {
		assertMutable();
		protection = pProtection;
		return self();
	}

	@Nonnull public T makePublic() {
		return protection(Protection.PUBLIC);
	}

	@Nonnull public T makeProtected() {
		return protection(Protection.PROTECTED);
	}

	@Nonnull public T makePackageProtected() {
		return protection(Protection.PACKAGE);
	}

	@Nonnull public T makePrivate() {
		return protection(Protection.PRIVATE);
	}

	@Override
	@Nonnull public IProtectable.Protection getProtection() {
		return protection;
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

	@Nonnull JSGConstructorBuilder newConstructor() {
		return newConstructor(IProtectable.Protection.PUBLIC);
	}

	@Nonnull JSGConstructorBuilder newConstructor(IProtectable.Protection pProtection) {
		assertMutable();
		final JSGConstructorBuilder jmc = new JSGConstructorBuilder().protection(pProtection).sourceClass(this);
		content.add(jmc);
		return jmc;
	}
	
	@Nonnull JSGMethodBuilder newMethod(@Nonnull IProtectable.Protection pProtection, @Nonnull JSGQName pType, @Nonnull String pName) {
		final JSGMethodBuilder jmb = new JSGMethodBuilder().protection(pProtection).returnType(pType).name(pName).sourceClass(this);
		content.add(jmb);
		return jmb;
	}

	@Nonnull JSGMethodBuilder newMethod(@Nonnull IProtectable.Protection pProtection, @Nonnull Class<?> pType, @Nonnull String pName) {
		return newMethod(pProtection, JSGQName.valueOf(pType), pName);
	}

	@Override
	@Nonnull public List<Object> getContent() { return content; }

	@Nonnull
	public T extending(String pType) {
		return extending(JSGQName.valueOf(pType));
	}

	@Nonnull
	public T extending(Class<?> pType) {
		return extending(JSGQName.valueOf(pType));
	}

	@Nonnull
	public T extending(JSGQName pType) {
		assertMutable();
		extendedClasses.add(pType);
		return self();
	}

	@Override
	public List<JSGQName> getExtendedClasses() {
		return extendedClasses;
	}

	@Nonnull
	public T implementing(String pType) {
		return implementing(JSGQName.valueOf(pType));
	}

	@Nonnull
	public T implementing(Class<?> pType) {
		return implementing(JSGQName.valueOf(pType));
	}

	@Nonnull
	public T implementing(JSGQName pType) {
		assertMutable();
		implementedInterfaces.add(pType);
		return self();
	}

	@Override
	public List<JSGQName> getImplementedInterfaces() {
		return implementedInterfaces;
	}

	@Nonnull
	public T makeInterface() {
		return makeInterface(true);
	}

	@Nonnull
	public T makeInterface(boolean pInterface) {
		assertMutable();
		isInterface = pInterface;
		return self();
	}

	@Override
	public boolean isInterface() {
		return isInterface;
	}
}
