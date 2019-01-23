package com.github.jochenw.afw.core;

public interface ILifefycleController {
	public interface Listener {
		void start();
	}
	public interface TerminableListener extends Listener {
		void shutdown();
	}
	void start();
	void shutdown();
	void addListener(Listener pListener);
	void removeListener(Listener pListener);
}
