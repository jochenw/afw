package com.github.jochenw.afw.jsgen.api;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.core.util.AbstractBuilder;

public class JSGMethodBuilder extends AbstractBuilder implements IJSGMethod {
	private final IAnnotatable annotatable = new DefaultAnnotatable();
	private final IJSGSource source;
	private Protection protection;
	private String name;
	private JSGQName type;
	private boolean isStatic;
	private boolean isAbstract;
	private boolean isFinal;

	public JSGMethodBuilder(IJSGSource pSource) {
		source = pSource;
	}

	@Override
	public IAnnotation getAnnotation(JSGQName pName) {
		return annotatable.getAnnotation(pName);
	}

	@Override
	public IAnnotation getAnnotation(Class<? extends Annotation> pName) {
		return annotatable.getAnnotation(pName);
	}

	@Override
	public boolean isAnnotatedWith(JSGQName pName) {
		return annotatable.isAnnotatedWith(pName);
	}

	@Override
	public boolean isAnnotatedWith(Class<? extends Annotation> pName) {
		return annotatable.isAnnotatedWith(pName);
	}

	@Override
	public Protection getProtection() {
		return protection;
	}

	@Override
	public IJSGSource getSource() {
		return source;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public JSGQName getType() {
		return type;
	}

	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Override
	public boolean isFinal() {
		return isFinal;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}
	
	public JSGMethodBuilder name(String pName) {
		name = pName;
		return this;
	}

	public JSGMethodBuilder type(JSGQName pType) {
		type = pType;
		return this;
	}

	public JSGMethodBuilder protection(Protection pProtection) {
		protection = pProtection;
		return this;
	}

	public JSGMethodBuilder makePublic() {
		return protection(Protection.PUBLIC);
	}

	public JSGMethodBuilder makeProtected() {
		return protection(Protection.PROTECTED);
	}

	public JSGMethodBuilder makePackageProtected() {
		return protection(Protection.PACKAGE_PROTECTED);
	}

	public JSGMethodBuilder makePrivate() {
		return protection(Protection.PRIVATE);
	}

	public JSGMethodBuilder makeStatic(boolean pStatic) {
		isStatic = pStatic;
		return this;
	}

	public JSGMethodBuilder makeStatic() {
		return makeStatic(true);
	}

	public JSGMethodBuilder makeAbstract(boolean pAbstract) {
		isAbstract = pAbstract;
		return this;
	}

	public JSGMethodBuilder makeAbstract() {
		return makeAbstract(true);
	}

	public JSGMethodBuilder makeFinal(boolean pFinal) {
		isFinal = pFinal;
		return this;
	}

	public JSGMethodBuilder makeFinal() {
		return makeFinal(true);
	}

	public IJSGMethod build() {
		if (isMutable()) {
			makeImmutable();
		}
		return new JSGMethod(source, protection, type, name, isAbstract, isFinal, isStatic);
	}
}
