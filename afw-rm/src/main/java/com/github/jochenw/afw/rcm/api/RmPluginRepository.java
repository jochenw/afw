package com.github.jochenw.afw.rcm.api;

import java.util.List;

public interface RmPluginRepository {
	public List<RmResourcePlugin> getResourcePlugins();
	public List<RmTargetLifecyclePlugin> getTargetLifecyclePlugins();
	public List<RmResourceRefGuesser> getResourceRefGuessers();
}
