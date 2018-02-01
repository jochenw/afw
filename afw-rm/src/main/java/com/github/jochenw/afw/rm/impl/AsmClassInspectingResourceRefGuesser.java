package com.github.jochenw.afw.rm.impl;

import java.io.IOException;
import java.io.InputStream;

import com.github.jochenw.afw.rm.api.ClassInfo;
import com.github.jochenw.afw.rm.api.RmResourceInfo;
import com.github.jochenw.afw.rm.api.RmResourceRefGuesser;
import com.github.jochenw.afw.rm.api.RmVersion;

public class AsmClassInspectingResourceRefGuesser implements RmResourceRefGuesser {
	@Override
	public RmResourceInfo getInfo(RmResourceInfoRequest pRequest) throws IOException {
		if (pRequest.getResourceRef().getUri().endsWith(".class")) {
			ClassInfo classInfo;
			try (InputStream is = pRequest.open()) {
				classInfo = new AsmClassInspector().getClassInfo(is);
			} catch (Throwable t) {
				classInfo = null;
			}
			final String type = classInfo.getType();
			final String versionStr = classInfo.getVersion();
			if (versionStr == null  ||  versionStr.length() == 0) {
				pRequest.getLogger().warning("Class annotated with @Resource, but version is missing, or empty: " + pRequest.getResourceRef().getLocation());
			} else {
				RmVersion version = null;
				if (classInfo != null) {
					try {
						version = RmVersion.of(versionStr);
					} catch (Throwable t) {
						pRequest.getLogger().warning("Class annotated with @Resource, but version (" + versionStr + ") is invalid: " + pRequest.getResourceRef().getLocation());
						version = null;
					}
				}
				if (version != null) {
					final ClassInfo clInfo = classInfo;
					final RmVersion v = version;
					return new RmResourceInfo() {
						@Override
						public String getTitle() {
							return clInfo.getTitle();
						}

						@Override
						public String getType() {
							return "class:" + clInfo.getClassName();
						}

						@Override
						public String getDescription() {
							return clInfo.getDescription();
						}

						@Override
						public RmVersion getVersion() {
							return v;
						}

						@Override
						public String getUri() {
							return pRequest.getResourceRef().getUri();
						}

						@Override
						public String getLocation() {
							return pRequest.getResourceRef().getLocation();
						}
					};
				}
			}
		}
		return null;
	}

}
