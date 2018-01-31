package com.github.jochenw.afw.rm.api;

public interface RmResourcePlugin {
	public interface ResourceInstallationRequest {
		RmResourceRefRepository getRepository();
		RmResourceInfo getResource();
	}
	boolean isInstallable(ResourceInstallationRequest pRequest);
	void install(ResourceInstallationRequest pRequest);
}
