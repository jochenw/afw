package com.github.jochenw.afw.rm.api;

import com.github.jochenw.afw.rm.api.RmResourcePlugin.ResourceInstallationRequest;

public abstract class AbstractInstallable extends AbstractInitializable {
	public abstract void install(ResourceInstallationRequest pRequest);
}
