package com.github.jochenw.afw.validation.plugins;

public interface IPropertyValidator<T> extends IAtomicValidator<T> {
	String getProperty();
}
