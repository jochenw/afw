package com.github.jochenw.afw.rcm.api;

import com.github.jochenw.afw.rcm.api.RmResourcePlugin.ResourceInstallationRequest;

public abstract class AbstractInstallable extends AbstractInitializable {
	public abstract void install(ResourceInstallationRequest pRequest);
}
