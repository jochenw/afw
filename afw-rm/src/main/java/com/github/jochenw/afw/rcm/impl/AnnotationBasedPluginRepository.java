package com.github.jochenw.afw.rcm.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.jochenw.afw.rcm.api.AbstractInitializable;
import com.github.jochenw.afw.rcm.api.RmLifecyclePlugin;
import com.github.jochenw.afw.rcm.api.RmPluginRepository;
import com.github.jochenw.afw.rcm.api.RmResourceInfoProvider;
import com.github.jochenw.afw.rcm.api.RmResourceInstallationPlugin;
import com.github.jochenw.afw.rcm.api.RmResourcePlugin;
import com.github.jochenw.afw.rcm.api.RmResourceRefGuesser;
import com.github.jochenw.afw.rcm.api.RmTargetLifecyclePlugin;
import com.github.jochenw.afw.rcm.util.ClassPathClassFinder;
import com.github.jochenw.afw.rcm.util.ClassPathClassFinder.ClassInfo;;


public class AnnotationBasedPluginRepository extends AbstractInitializable implements RmPluginRepository {
	private List<RmResourcePlugin> resourcePlugins = null;
	private List<RmTargetLifecyclePlugin> targetLifecyclePlugins = null;
	private List<RmResourceRefGuesser> resourceGuessers = null;

	protected final void init() {
		final ClassPathClassFinder<String> pluginFinder = new ClassPathClassFinder<>();
		final Function<ClassInfo,String> filter = new Function<ClassInfo,String>() {
			@Override
			public String apply(ClassInfo pClassInfo) {
				boolean plugin = pClassInfo.getAnnotation(RmResourceInstallationPlugin.class) != null
						     ||  pClassInfo.getAnnotation(RmLifecyclePlugin.class) != null
						     ||  pClassInfo.getAnnotation(RmResourceInfoProvider.class) != null;
				if (!plugin) {
					return null;
				} else {
					return pClassInfo.getType();
				}
			}
		};
		pluginFinder.setClassFilter(filter);
		final List<String> pluginClasses = pluginFinder.findClasses(getComponentFactory().requireInstance(ClassLoader.class));
		final List<RmResourcePlugin> resourcePluginList = new ArrayList<>();
		final List<RmTargetLifecyclePlugin> targetLifecyclePluginList = new ArrayList<>();
		final List<RmResourceRefGuesser> resourceGuesserList = new ArrayList<>();
		
		for (String className : pluginClasses) {
			final Object o = getComponentFactory().newInstance(className);
			if (o instanceof RmResourcePlugin) {
				resourcePluginList.add((RmResourcePlugin) o);
			}
			if (o instanceof RmTargetLifecyclePlugin) {
				targetLifecyclePluginList.add((RmTargetLifecyclePlugin) o);
			}
			if (o instanceof RmResourceRefGuesser) {
				resourceGuesserList.add((RmResourceRefGuesser) o);
			}
		}

		resourcePlugins = resourcePluginList;
		targetLifecyclePlugins = targetLifecyclePluginList;
		resourceGuessers = resourceGuesserList;
	}
	
	@Override
	public List<RmResourcePlugin> getResourcePlugins() {
		if (resourcePlugins == null) {
			init();
		}
		return resourcePlugins;
	}

	protected Object getPlugin(ClassInfo pClassInfo) {
		return getComponentFactory().newInstance(pClassInfo.getType());
	}
	
	@Override
	public List<RmTargetLifecyclePlugin> getTargetLifecyclePlugins() {
		if (targetLifecyclePlugins == null) {
			init();
		}
		return targetLifecyclePlugins;
	}

	@Override
	public List<RmResourceRefGuesser> getResourceRefGuessers() {
		if (resourceGuessers == null) {
			init();
		}
		return resourceGuessers;
	}

}
