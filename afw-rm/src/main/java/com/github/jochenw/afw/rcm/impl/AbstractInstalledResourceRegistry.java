package com.github.jochenw.afw.rcm.impl;

import java.util.ArrayList;
import java.util.List;

import com.github.jochenw.afw.rcm.api.InstalledResourceRegistry;
import com.github.jochenw.afw.rcm.api.RmResourceInfo;
import com.github.jochenw.afw.rcm.api.RmVersion;
import com.github.jochenw.afw.rcm.util.Objects;

public abstract class AbstractInstalledResourceRegistry implements InstalledResourceRegistry {
	protected static class InstalledResource {
		private final String versionStr;
		private final RmVersion version;
		private final String type;
		private final String title;
		private final String description;

		public InstalledResource(String pVersionStr, RmVersion pVersion, String pType, String pTitle, String pDescription) {
			versionStr = pVersionStr;
			version = pVersion;
			type = pType;
			title = pTitle;
			description = pDescription;
		}

		public String getVersionStr() {
			return versionStr;
		}

		public RmVersion getVersion() {
			return version;
		}

		public String getType() {
			return type;
		}

		public String getTitle() {
			return title;
		}

		public String getDescription() {
			return description;
		}
	}

	private List<InstalledResource> installedResources;

	protected abstract List<InstalledResource> readInstalledResources();
	
	@Override
	public boolean isInstalled(RmResourceInfo pResource) {
		synchronized(this) {
			if (installedResources == null) {
				installedResources = readInstalledResources();
				Objects.requireNonNull(installedResources, "InstalledResources");
			}
		}
		for (InstalledResource ir : installedResources) {
			if (pResource.getVersion().equals(ir.getVersion())  &&  pResource.getType().equals(ir.getType())
					                                            &&  pResource.getTitle().equals(ir.getTitle())) {
				return true;
			}
		}
		return false;
	}

}
