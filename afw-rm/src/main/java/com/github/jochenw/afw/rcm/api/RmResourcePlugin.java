package com.github.jochenw.afw.rcm.api;

import com.github.jochenw.afw.rcm.api.RmTargetLifecyclePlugin.Context;


public interface RmResourcePlugin {
	public interface ResourceInstallationRequest {
		RmResourceRefRepository getRepository();
		RmResourceInfo getResource();
		RmResourceRef getResourceRef();
		Context getContext();
	}
	String getContextId();
	boolean isInstallable(ResourceInstallationRequest pRequest);
	void install(ResourceInstallationRequest pRequest);
}
