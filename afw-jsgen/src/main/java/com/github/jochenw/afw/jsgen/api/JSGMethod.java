package com.github.jochenw.afw.jsgen.api;

import javax.annotation.Nonnull;


public class JSGMethodBuilder extends JSGSubroutineBuilder<JSGMethodBuilder> implements JSGMethod {
	@Nonnull private JSGQName type;
	@Nonnull private IProtectable.Protection protection;
	@Nonnull private String name;
	private boolean isStatic;
	private boolean isAbstract;
	private boolean isFinal;
	private boolean isSynchronized;

	@Override
	protected JSGMethodBuilder self() { return this; }

	@Nonnull public JSGMethodBuilder returnType(@Nonnull JSGQName pType) {
		assertMutable();
		type = pType;
		return this;
	}

	@Nonnull public JSGMethodBuilder returnType(@Nonnull Class<?> pType) {
		return returnType(JSGQName.valueOf(pType));
	}

	@Nonnull public JSGMethodBuilder returnType(@Nonnull String pType) {
		return returnType(JSGQName.valueOf(pType));
	}

	@Override
	@Nonnull public JSGQName getReturnType() {
		return type;
	}

	@Nonnull JSGMethodBuilder name(@Nonnull String pName) {
		assertMutable();
		name = pName;
		return this;
	}

	@Override
	@Nonnull public String getName() {
		return name;
	}

	@Nonnull JSGMethodBuilder makeAbstract() {
		return makeAbstract(true);
	}
	
	@Nonnull JSGMethodBuilder makeAbstract(boolean pAbstract) {
		assertMutable();
		isAbstract = pAbstract;
		return this;
	}
	
	@Override
	public boolean isAbstract() {
		return isAbstract;
	}

	@Nonnull JSGMethodBuilder makeStatic() {
		return makeStatic(true);
	}
	
	@Nonnull JSGMethodBuilder makeStatic(boolean pStatic) {
		assertMutable();
		isStatic = pStatic;
		return this;
	}

	@Override
	public boolean isStatic() {
		return isStatic;
	}

	@Nonnull JSGMethodBuilder makeFinal() {
		return makeFinal(true);
	}

	@Nonnull JSGMethodBuilder makeFinal(boolean pFinal) {
		assertMutable();
		isFinal = pFinal;
		return this;
	}

	@Override
	public boolean isFinal() {
		return isFinal;
	}

	@Nonnull JSGMethodBuilder makeSynchronized() {
		return makeSynchronized(true);
	}

	@Nonnull JSGMethodBuilder makeSynchronized(boolean pSynchronized) {
		assertMutable();
		isSynchronized = pSynchronized;
		return this;
	}

	@Override
	public boolean isSynchronized() {
		return isSynchronized;
	}

	private static final JSGQName OVERRIDE = JSGQName.valueOf(Override.class);

	@Nonnull JSGMethodBuilder overriding() {
		return overriding(true);
	}

	@Nonnull JSGMethodBuilder overriding(boolean pOverriding) {
		assertMutable();
		if (pOverriding) {
			annotation(OVERRIDE);
		}
		return this;
	}
	
	@Override
	public boolean isOverriding() {
		return isAnnotatedWith(OVERRIDE);
	}

}
