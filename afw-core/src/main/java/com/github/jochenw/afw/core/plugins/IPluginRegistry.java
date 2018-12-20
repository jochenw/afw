package com.github.jochenw.afw.core.plugins;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IPluginRegistry {
	public interface IExtensionPoint<O extends Object> {
		@Nonnull List<O> getPlugins();
		public default void forEach(@Nonnull Consumer<O> pConsumer) {
			getPlugins().forEach(pConsumer);
		}
		void addPlugin(O pPlugin);
	}
	public interface Initializer extends Consumer<IPluginRegistry> {
		String getId();
		List<String> getDependsOn();
	}
	public static abstract class AbstractInitializer implements Initializer {
		private String id;
		private List<String> dependsOn;

		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public List<String> getDependsOn() {
			return dependsOn;
		}
		public void setDependsOn(List<String> dependsOn) {
			this.dependsOn = dependsOn;
		}
	}

	<O extends Object> void addExtensionPoint(@Nonnull Class<O> pType, @Nonnull String pId);
	public default <O extends Object> void addExtensionPoint(@Nonnull Class<O> pType) {
		addExtensionPoint(pType, "");
	}
	@Nullable <O extends Object> IExtensionPoint<O> getExtensionPoint(@Nonnull Class<O> pType, @Nonnull String pId);
	public default @Nullable <O extends Object> IExtensionPoint<O> getExtensionPoint(@Nonnull Class<O> pType) {
		return getExtensionPoint(pType, "");
	}
	public default @Nonnull <O extends Object> IExtensionPoint<O> requireExtensionPoint(@Nonnull Class<O> pType, @Nonnull String pId)
		throws NoSuchElementException {
		final IExtensionPoint<O> ep = getExtensionPoint(pType, pId);
		if (ep == null) {
			throw new NoSuchElementException("No such extension point registered: type=" + pType.getName() + ", id=" + pId);
		}
		return ep;
	}
	public default @Nonnull <O extends Object> IExtensionPoint<O> requireExtensionPoint(@Nonnull Class<O> pType) {
		final IExtensionPoint<O> ep = getExtensionPoint(pType);
		if (ep == null) {
			throw new NoSuchElementException("No such extension point registered: type=" + pType.getName());
		}
		return ep;
	}
	public default <O extends Object> void forEach(@Nonnull Class<O> pType, @Nonnull String pId, @Nonnull Consumer<O> pConsumer) {
		requireExtensionPoint(pType, pId).forEach(pConsumer);
	}
	public default <O extends Object> void forEach(@Nonnull Class<O> pType, @Nonnull Consumer<O> pConsumer) {
		requireExtensionPoint(pType).forEach(pConsumer);
	}
	public default <O extends Object> List<O> getPlugins(Class<O> pType, String pId) {
		return requireExtensionPoint(pType, pId).getPlugins();
	}
	public default <O extends Object> List<O> getPlugins(Class<O> pType) {
		return requireExtensionPoint(pType).getPlugins();
	}
	public default <O extends Object> void addPlugin(Class<O> pType, String pId, O pPlugin) {
		requireExtensionPoint(pType, pId).addPlugin(pPlugin);
	}
	public default <O extends Object> void addPlugin(Class<O> pType, O pPlugin) {
		requireExtensionPoint(pType).addPlugin(pPlugin);
	}
}
