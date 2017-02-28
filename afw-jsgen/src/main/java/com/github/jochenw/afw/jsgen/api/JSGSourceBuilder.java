package com.github.jochenw.afw.jsgen.api;

import java.lang.annotation.Annotation;

import com.github.jochenw.afw.core.util.AbstractBuilder;

public class JSGSourceBuilder extends AbstractBuilder implements IJSGSource {
	private final JSGSourceFactory factory;
	private final JSGQName name;
	private boolean isStatic, isAbstract, isFinal;
	private Protection protection = Protection.PACKAGE_PROTECTED;
	private final DefaultAnnotatable annotatable = new DefaultAnnotatable();

	public JSGSourceBuilder(JSGSourceFactory pFactory, JSGQName pName) {
		name = pName;
		factory = pFactory;
	}

	public JSGSourceFactory getFactory() {
		return factory;
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
	public JSGQName getName() {
		return name;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Override
	public boolean isFinal() {
		return isFinal;
	}

	public JSGSourceBuilder makeStatic() {
		return makeStatic(true);
	}


	public JSGSourceBuilder makeStatic(boolean pStatic) {
		assertMutable();
		isStatic = pStatic;
		return this;
	}

	public JSGSourceBuilder makeAbstract() {
		return makeAbstract(true);
	}


	public JSGSourceBuilder makeAbstract(boolean pAbstract) {
		assertMutable();
		isAbstract = pAbstract;
		return this;
	}

	public JSGSourceBuilder makeFinal() {
		return makeFinal(true);
	}


	public JSGSourceBuilder makeFinal(boolean pFinal) {
		assertMutable();
		isFinal = pFinal;
		return this;
	}

	public JSGSourceBuilder makePublic() {
		return protection(Protection.PUBLIC);
	}

	public JSGSourceBuilder makeProtected() {
		return protection(Protection.PROTECTED);
	}

	public JSGSourceBuilder makeDefaultProtected() {
		return protection(Protection.PACKAGE_PROTECTED);
	}

	public JSGSourceBuilder makePrivate() {
		return protection(Protection.PRIVATE);
	}

	public JSGSourceBuilder protection(Protection pProtection) {
		assertMutable();
		protection = pProtection;
		return this;
	}
}
