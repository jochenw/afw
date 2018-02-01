package com.github.jochenw.afw.rm.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.github.jochenw.afw.rm.api.RmResourceInfo;
import com.github.jochenw.afw.rm.api.RmResourceRefGuesser;
import com.github.jochenw.afw.rm.api.RmVersion;
import com.github.jochenw.afw.rm.util.AnnotationScanner;
import com.github.jochenw.afw.rm.util.AnnotationScanner.Annotation;
import com.github.jochenw.afw.rm.util.Strings;

public class AnnotationScanningResourceRefGuesser implements RmResourceRefGuesser {
	private final Charset charset;

	public AnnotationScanningResourceRefGuesser(Charset pCharset) {
		Objects.requireNonNull(pCharset, "Charset");
		charset = pCharset;
	}

	public AnnotationScanningResourceRefGuesser() {
		this(StandardCharsets.UTF_8);
	}

	@Override
	public RmResourceInfo getInfo(final RmResourceInfoRequest pRequest) throws IOException {
		try (InputStream is = pRequest.open();
			 InputStreamReader isr = new InputStreamReader(is, charset);
			 BufferedReader br = new BufferedReader(isr)) {
			for (Annotation annotation : AnnotationScanner.parse(br)) {
				if ("Resource".equals(annotation.getName())) {
					final String title = annotation.getAttribute("title");
					final String description = annotation.getAttribute("description");
					final String versionStr = annotation.getAttribute("version");
					final String type = annotation.getAttribute("type");
					if (Strings.isTrimmedEmpty(type)) {
						pRequest.getLogger().warning("Missing, or empty type attribute for "
								+ pRequest.getResourceRef().getLocation() + ", ignoring this resource");
						continue;  // No type, ignore this annotation.
					}
					if (Strings.isTrimmedEmpty(versionStr)) {
						pRequest.getLogger().warning("Missing, or empty version attribute for "
								+ pRequest.getResourceRef().getLocation() + ", ignoring this resource");
						continue;  // No version, ignore this annotation.
					}
					final RmVersion version;
					try {
						version = RmVersion.of(versionStr);
					} catch (Throwable t) {
						pRequest.getLogger().error("Invalid version attribute for "
								+ pRequest.getResourceRef().getLocation() + ", ignoring this resource");
						continue;
					}
					return new RmResourceInfo() {
						@Override
						public RmVersion getVersion() {
							return version;
						}
						
						@Override
						public String getUri() {
							return pRequest.getResourceRef().getUri();
						}
						
						@Override
						public String getLocation() {
							return pRequest.getResourceRef().getLocation();
						}
						
						@Override
						public String getType() {
							return type;
						}
						
						@Override
						public String getTitle() {
							return title;
						}
						
						@Override
						public String getDescription() {
							return description;
						}
					};
				}
			}
		}
		return null;
	}

}
