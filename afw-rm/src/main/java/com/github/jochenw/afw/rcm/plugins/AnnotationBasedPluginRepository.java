package com.github.jochenw.afw.rcm.plugins;

import java.util.List;
import java.util.function.Function;

import com.github.jochenw.afw.rcm.api.RmLifecyclePlugin;
import com.github.jochenw.afw.rcm.api.RmResourceInfoProvider;
import com.github.jochenw.afw.rcm.api.RmResourceInstallationPlugin;
import com.github.jochenw.afw.rcm.util.ClassPathClassFinder;
import com.github.jochenw.afw.rcm.util.ClassPathClassFinder.ClassInfo;;


public class AnnotationBasedPluginRepository extends AbstractPluginRepository {
	@Override
	protected final void init(Data pData) {
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
		
		for (String className : pluginClasses) {
			pData.addPlugin(className);
		}
	}
}
