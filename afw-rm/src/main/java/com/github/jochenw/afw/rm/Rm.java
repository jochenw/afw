package com.github.jochenw.afw.rm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.github.jochenw.afw.rm.api.AbstractInitializable;
import com.github.jochenw.afw.rm.api.ComponentFactory;
import com.github.jochenw.afw.rm.api.ComponentFactory.Initializable;
import com.github.jochenw.afw.rm.api.InstalledResourceRegistry;
import com.github.jochenw.afw.rm.api.RmLogger;
import com.github.jochenw.afw.rm.api.RmResourceInfo;
import com.github.jochenw.afw.rm.api.RmResourcePlugin;
import com.github.jochenw.afw.rm.api.RmResourcePlugin.ResourceInstallationRequest;
import com.github.jochenw.afw.rm.api.RmResourceRef;
import com.github.jochenw.afw.rm.api.RmResourceRefGuesser;
import com.github.jochenw.afw.rm.api.RmResourceRefGuesser.RmResourceInfoRequest;
import com.github.jochenw.afw.rm.api.RmResourceRefRepository;

public class Rm extends AbstractInitializable {
	public static class DetectedResourceInfo implements ResourceInstallationRequest {
		private final RmResourceRefRepository repository;
		private final RmResourceRef resourceRef;
		private final RmResourceInfo resourceInfo;
		private RmResourcePlugin resourcePlugin;

		public DetectedResourceInfo(RmResourceRefRepository pRepository, RmResourceRef pResourceRef,
				                    RmResourceInfo pResourceInfo) {
			repository = pRepository;
			resourceRef = pResourceRef;
			resourceInfo = pResourceInfo;
		}

		public void setRmResourcePlugin(RmResourcePlugin pPlugin) {
			resourcePlugin = pPlugin;
		}

		RmResourcePlugin getPlugin() {
			return resourcePlugin;
		}

		@Override
		public RmResourceRefRepository getRepository() {
			return repository;
		}

		@Override
		public RmResourceInfo getResource() {
			return resourceInfo;
		}

		@Override
		public RmResourceRef getResourceRef() {
			return resourceRef;
		}
	}
	private Charset charset;
	private List<RmResourceRefRepository> resourceRepositories;
	private List<RmResourceRefGuesser> resourceRefGuessers;
	private InstalledResourceRegistry installedResourceRegistry;
	private List<RmResourcePlugin> resourcePlugins;
	private RmLogger logger;

	public static RmBuilder builder() {
		return new RmBuilder();
	}

	@Override
	public void init(ComponentFactory pComponentFactory) {
		super.init(pComponentFactory);
		final ComponentFactory componentFactory = pComponentFactory;
		final List<RmResourceRefRepository> repoList = componentFactory.requireList(RmResourceRefRepository.class);
		resourceRepositories = repoList;
		List<RmResourceRefGuesser> guesserList = componentFactory.requireList(RmResourceRefGuesser.class);
		resourceRefGuessers = guesserList;
		installedResourceRegistry = componentFactory.requireInstance(InstalledResourceRegistry.class);
		logger = componentFactory.requireInstance(RmLogger.class);
		List<RmResourcePlugin> pluginList = componentFactory.requireList(RmResourcePlugin.class);
		resourcePlugins = pluginList;
	}
	
	public List<RmResourcePlugin> getResourcePlugins() {
		return resourcePlugins;
	}
	
	void setCharset(Charset pCharset) {
		charset = pCharset;
	}

	Charset getCharset() {
		return charset;
	}

	public List<RmResourceRefRepository> getResourceRepositories() {
		return resourceRepositories;
	}

	public List<RmResourceRefGuesser> getResourceRefGuessers() {
		return resourceRefGuessers;
	}

	void setInstalledResourceRegistry(InstalledResourceRegistry pRegistry) {
		installedResourceRegistry = pRegistry;
	}

	public InstalledResourceRegistry getInstalledResourceRegistry() {
		return installedResourceRegistry;
	}
	
	public void run() {
		final List<DetectedResourceInfo> resources = filterResources(findResourceRefs());
		install(resources);
	}

	private void install(final List<DetectedResourceInfo> resources) {
		for (DetectedResourceInfo dri : resources) {
			dri.getPlugin().install(dri);
		}
	}

	protected List<DetectedResourceInfo> filterResources(List<DetectedResourceInfo> pResources) {
		for (final Iterator<DetectedResourceInfo> iter = pResources.listIterator();
			 iter.hasNext();  ) {
			final DetectedResourceInfo dri = iter.next();
			if (installedResourceRegistry.isInstalled(dri.resourceInfo)) {
				iter.remove();
			}
		}
		final Comparator<DetectedResourceInfo> comp = new Comparator<DetectedResourceInfo>() {
			@Override
			public int compare(DetectedResourceInfo o1, DetectedResourceInfo o2) {
				return o1.resourceInfo.getVersion().compareTo(o2.resourceInfo.getVersion());
			}
		};
		for (DetectedResourceInfo dri : pResources) {
			final String type = dri.getResource().getType();
			RmResourcePlugin plugin = null;
			for (RmResourcePlugin pl : resourcePlugins) {
				if (pl.isInstallable(dri)) {
					plugin = pl;
					break;
				}
			}
			if (plugin == null) {
				throw new IllegalStateException("No RmResourcePlugin registered for type=" + type);
			}
			dri.setRmResourcePlugin(plugin);
		}
		Collections.sort(pResources, comp);
		return pResources;
	}
	
	protected List<DetectedResourceInfo> findResourceRefs() {
		final List<DetectedResourceInfo> resources = new ArrayList<>();
		for (final RmResourceRefRepository rmrrr : resourceRepositories) {
			for (final RmResourceRef rrr : rmrrr.getResources(logger)) {
				final RmResourceInfoRequest req = new RmResourceInfoRequest() {
					@Override
					public InputStream open() throws IOException {
						return rmrrr.open(rrr);
					}
					
					@Override
					public RmResourceRef getResourceRef() {
						return rrr;
					}

					@Override
					public RmLogger getLogger() {
						return logger;
					}
				};
				RmResourceInfo rri = null;
				for (RmResourceRefGuesser rrg : getResourceRefGuessers()) {
					try {
						final RmResourceInfo i = rrg.getInfo(req);
						if (i != null) {
							rri = i;
							break;
						}
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
				if (rri != null) {
					resources.add(new DetectedResourceInfo(rmrrr, rrr, rri));
				}
			}
		}
		return resources;
	}
}
