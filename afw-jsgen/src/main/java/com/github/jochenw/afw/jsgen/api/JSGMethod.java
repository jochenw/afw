package com.github.jochenw.afw.jsgen.api;

import java.lang.annotation.Annotation;

public class JSGMethod implements IJSGMethod {
	private final IAnnotatable annotatable = new DefaultAnnotatable();
	private Protection protection;
	private final IJSGSource source;
	private final String name;
	private final JSGQName type;
	private final boolean isAbstract, isFinal, isStatic;

	public JSGMethod(IJSGSource pSource, Protection pProtection, JSGQName pType, String pName,
			         boolean pAbstract, boolean pFinal, boolean pStatic) {
		source = pSource;
		protection = pProtection;
		type = pType;
		name = pName;
		isAbstract = pAbstract;
		isFinal = pFinal;
		isStatic = pStatic;
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

}
