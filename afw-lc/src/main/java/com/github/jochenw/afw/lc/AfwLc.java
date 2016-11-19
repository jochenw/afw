package com.github.jochenw.afw.lc;

import com.github.jochenw.afw.lc.guice.GuiceComponentFactoryBuilder;

public class AfwLc {
	public static ComponentFactoryBuilder newComponentFactoryBuilder() {
		return new GuiceComponentFactoryBuilder();
	}
}
