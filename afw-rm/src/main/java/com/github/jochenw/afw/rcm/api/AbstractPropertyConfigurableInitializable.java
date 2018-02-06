package com.github.jochenw.afw.rcm.api;

import java.util.Properties;

public abstract class AbstractPropertyConfigurableInitializable extends AbstractInitializable {
	protected Properties getProperties() {
		return getComponentFactory().requireInstance(Properties.class);
	}

	protected String requireProperty(String pSuffix, String pId) {
		return requireProperty(getProperties(), pSuffix, pId);
	}

	protected String requireProperty(String pKey) {
		return requireProperty(getProperties(), pKey);
	}

	protected String requireProperty(Properties pProperties, String pSuffix, String pId) {
		if (pSuffix == null  ||  pSuffix.length() == 0) {
			return requireProperty(pProperties, pId);
		} else if (pSuffix.endsWith(".")) {
			return requireProperty(pProperties, pSuffix + pId);
		} else {
			return requireProperty(pProperties, pSuffix + "." + pId);
		}
	}

	protected String requireProperty(Properties pProperties, String pKey) {
		final String value = getProperty(pProperties, pKey);
		if (value == null  ||  value.length() == 0) {
			throw new IllegalStateException("Missing, or empty property: " + pKey);
		}
		return value;
	}

	protected String getProperty(String pKey) {
		return getProperty(getProperties(), pKey);
	}

	protected String getProperty(String pSuffix, String pId) {
		return getProperty(getProperties(), pSuffix, pId);
	}

	protected String getProperty(Properties pProperties, String pSuffix, String pId) {
		if (pSuffix == null  ||  pSuffix.length() == 0) {
			return requireProperty(pProperties, pId);
		} else if (pSuffix.endsWith(".")) {
			return requireProperty(pProperties, pSuffix + pId);
		} else {
			return requireProperty(pProperties, pSuffix + "." + pId);
		}
	}

	protected String getProperty(Properties pProperties, String pKey) {
		return pProperties.getProperty(pKey);
	}
}
