package com.github.jochenw.afw.rcm.plugins;

import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.rcm.api.AbstractInitializable;
import com.github.jochenw.afw.rcm.api.RmPluginRepository;
import com.github.jochenw.afw.rcm.api.RmResourcePlugin;
import com.github.jochenw.afw.rcm.api.RmResourceRefGuesser;
import com.github.jochenw.afw.rcm.api.RmTargetLifecyclePlugin;

public abstract class AbstractPluginRepository extends AbstractInitializable implements RmPluginRepository {
	protected class Data {
		private final List<RmResourcePlugin> resourcePlugins = new ArrayList<>();
		private final List<RmTargetLifecyclePlugin> lifecyclePlugins = new ArrayList<>();
		private final List<RmResourceRefGuesser> guesserPlugins = new ArrayList<>();
		public List<RmResourcePlugin> getResourcePlugins() {
			return resourcePlugins;
		}
		public List<RmTargetLifecyclePlugin> getLifecyclePlugins() {
			return lifecyclePlugins;
		}
		public List<RmResourceRefGuesser> getGuesserPlugins() {
			return guesserPlugins;
		}
		public void addPlugin(String pClassName) {
			final Object o = getComponentFactory().newInstance(pClassName);
			addPlugin(o);
		}
		public void addPlugin(Object pPlugin) {
			boolean ok = false;
			if (pPlugin instanceof RmResourcePlugin) {
				resourcePlugins.add((RmResourcePlugin) pPlugin);
				ok = true;
			}
			if (pPlugin instanceof RmTargetLifecyclePlugin) {
				lifecyclePlugins.add((RmTargetLifecyclePlugin) pPlugin);
				ok = true;
			}
			if (pPlugin instanceof RmResourceRefGuesser) {
				guesserPlugins.add((RmResourceRefGuesser) pPlugin);
				ok = true;
			}
			if (!ok) {
				throw new IllegalStateException("Plugin " + pPlugin.getClass() + "is not a valid plugin.");
			}
		}
	}

	private List<RmResourcePlugin> resourcePlugins;
	private List<RmTargetLifecyclePlugin> lifecyclePlugins;
	private List<RmResourceRefGuesser> guesserPlugins;

	@Override
	public List<RmResourcePlugin> getResourcePlugins() {
		if (resourcePlugins == null) {
			init();
		}
		return resourcePlugins;
	}

	@Override
	public List<RmTargetLifecyclePlugin> getTargetLifecyclePlugins() {
		if (lifecyclePlugins == null) {
			init();
		}
		return lifecyclePlugins;
	}

	@Override
	public List<RmResourceRefGuesser> getResourceRefGuessers() {
		if (guesserPlugins == null) {
			init();
		}
		return guesserPlugins;
	}

	protected void init() {
		final Data data = new Data();
		init(data);
		resourcePlugins = data.getResourcePlugins();
		lifecyclePlugins = data.getLifecyclePlugins();
		guesserPlugins = data.getGuesserPlugins();
	}

	protected abstract void init(Data pData);
}
