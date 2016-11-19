package com.github.jochenw.afw.core;

public interface ILifefycleController extends ILifecycleListener {
	void addListener(Object pListener);
	void removeListener(Object pListener);
}
