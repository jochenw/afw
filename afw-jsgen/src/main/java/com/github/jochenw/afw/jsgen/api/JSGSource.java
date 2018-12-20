package com.github.jochenw.afw.jsgen.api;

public class JSGSourceBuilder extends JSGClassBuilder<JSGSourceBuilder> implements JSGSource {
	public JSGSourceBuilder(JSGQName pType) {
		super(pType);
	}

	@Override
	protected JSGSourceBuilder self() {
		return this;
	}
}
