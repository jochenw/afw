package com.github.jochenw.afw.core.inject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.props.BooleanProperty;
import com.github.jochenw.afw.core.props.IBooleanProperty;
import com.github.jochenw.afw.core.props.IIntProperty;
import com.github.jochenw.afw.core.props.ILongProperty;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.props.IntProperty;
import com.github.jochenw.afw.core.props.LongProperty;
import com.github.jochenw.afw.core.props.StringProperty;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.impl.DefaultOnTheFlyBinder;

/** Default implementation of {@link DefaultOnTheFlyBinder} for
 * {@code AFW Core}, extending the latter with support for
 * instances of {@link com.github.jochenw.afw.core.log.ILog},
 * and {@link com.github.jochenw.afw.core.props.IProperty}.
 */
public class AfwCoreOnTheFlyBinder extends DefaultOnTheFlyBinder {
	/** Creates a new instance.
	 */
	public AfwCoreOnTheFlyBinder() {}

	@Override
	protected Object getLogger(IComponentFactory pFactory, Class<?> pType, String pId) {
		if (pType == ILog.class) {
			return pFactory.requireInstance(ILogFactory.class).getLog(pId);
		} else {
			return null;
		}
	}

	@Override
	protected Object getProperty(@NonNull IComponentFactory pFactory,
			                     @NonNull Class<?> pType,
			                     @NonNull String pId,
			                     @NonNull String pDefaultValue,
			                     boolean pNullable) {
		if (pType == String.class) {
			return pFactory.requireInstance(IPropertyFactory.class).getPropertyValue(pId);
		} else if (pType == Integer.class) {
			final String value = pFactory.requireInstance(IPropertyFactory.class).getPropertyValue(pId);
			final Integer intValue = asInt(value, pDefaultValue);
			if (intValue == null) {
				if (pNullable) {
					return null;
				} else {
					throw new IllegalStateException("Invalid value for property " + pId
							                        + ": Expected valid integer value, got " + value
							                        + ", and no suitable default value is available.");
				}
			}
			return intValue;
		} else if (pType == Long.class) {
			final String value = pFactory.requireInstance(IPropertyFactory.class).getPropertyValue(pId);
			final Long longValue = asLong(value, pDefaultValue);
			if (longValue == null) {
				if (pNullable) {
					return null;
				} else {
					throw new IllegalStateException("Invalid value for property " + pId
							                        + ": Expected valid long value, got " + value
							                        + ", and no suitable default value is available.");
				}
			}
			return longValue;
		} else if (pType == Short.class) {
			final String value = pFactory.requireInstance(IPropertyFactory.class).getPropertyValue(pId);
			final Short shortValue = asShort(value, pDefaultValue);
			if (shortValue == null) {
				if (pNullable) {
					return null;
				} else {
					throw new IllegalStateException("Invalid value for property " + pId
							                        + ": Expected valid short value, got " + value
							                        + ", and no suitable default value is available.");
				}
			}
			return shortValue;
		} else if (pType == URL.class) {
			final String value = pFactory.requireInstance(IPropertyFactory.class).getPropertyValue(pId);
			final URL urlValue = asUrl(value, pDefaultValue);
			if (urlValue == null) {
				if (pNullable) {
					return null;
				} else {
					throw new IllegalStateException("Invalid value for property " + pId
							                        + ": Expected valid URL value, got " + value
							                        + ", and no suitable default value is available.");
				}
			}
			return urlValue;
		} else if (pType == Path.class) {
			final String value = pFactory.requireInstance(IPropertyFactory.class).getPropertyValue(pId);
			final Path pathValue = asPath(value, pDefaultValue);
			if (pathValue == null) {
				if (pNullable) {
					return null;
				} else {
					throw new IllegalStateException("Invalid value for property " + pId
							                        + ": Expected valid path value, got " + value
							                        + ", and no suitable default value is available.");
				}
			}
			return pathValue;
		} else if (pType == File.class) {
			final String value = pFactory.requireInstance(IPropertyFactory.class).getPropertyValue(pId);
			final File fileValue = asFile(value, pDefaultValue);
			if (fileValue == null) {
				if (pNullable) {
					return null;
				} else {
					throw new IllegalStateException("Invalid value for property " + pId
							                        + ": Expected valid file value, got " + value
							                        + ", and no suitable default value is available.");
				}
			}
			return fileValue;
		} else if (pType == Boolean.class) {
			final String value = pFactory.requireInstance(IPropertyFactory.class).getPropertyValue(pId);
			final Boolean booleanValue = asBoolean(value, pDefaultValue);
			if (booleanValue == null) {
				if (pNullable) {
					return null;
				} else {
					throw new IllegalStateException("Invalid value for property " + pId
							                        + ": Expected valid boolean value, got " + value
							                        + ", and no suitable default value is available.");
				}
			}
			return booleanValue;
		} else if (pType == StringProperty.class) {
			return pFactory.requireInstance(IPropertyFactory.class).getProperty(pId);
		} else if (pType == IIntProperty.class  ||  pType == IntProperty.class) {
			final Integer defaultValue = asInt(null, pDefaultValue);
			if (defaultValue == null) {
				throw new IllegalStateException("Invalid default value for property " + pId
						                        + ": Expected valid int value, got " + pDefaultValue);
			}
			return pFactory.requireInstance(IPropertyFactory.class).getIntProperty(pId, defaultValue.intValue());
		} else if (pType == ILongProperty.class  ||  pType == LongProperty.class) {
			final Long defaultValue = asLong(null, pDefaultValue);
			if (defaultValue == null) {
				throw new IllegalStateException("Invalid default value for property " + pId
						                        + ": Expected valid long value, got " + pDefaultValue);
			}
			return pFactory.requireInstance(IPropertyFactory.class).getLongProperty(pId, defaultValue.longValue());
		} else if (pType == IBooleanProperty.class  ||  pType == BooleanProperty.class) {
			final Boolean defaultValue = asBoolean(null, pDefaultValue);
			if (defaultValue == null) {
				throw new IllegalStateException("Invalid default value for property " + pId
						                        + ": Expected valid boolean value, got " + pDefaultValue);
			}
			return pFactory.requireInstance(IPropertyFactory.class).getBooleanProperty(pId, defaultValue.booleanValue());
		} else {
			return null;
		}
	}

	/** Converts the given string value into the actual integer value.
	 * @param pValue The actual property value, possibly null, or empty.
	 * @param pDefaultValue The default property value, which is being applied,
	 *   if the actual property value is null, or empty.
	 * @return The converted property value.
	 */
	protected Integer asInt(String pValue, String pDefaultValue) {
		if (!Strings.isEmpty(pValue)) {
			try {
				return Integer.parseInt(pValue);
			} catch (NumberFormatException e) {
				// Do nothing, like empty value.
			}
		}
		if (!Strings.isEmpty(pDefaultValue)) {
			try {
				return Integer.parseInt(pDefaultValue);
			} catch (NumberFormatException e) {
				// Do nothing, like empty value.
			}
		}
		return null;
	}

	/** Converts the given string value into the actual long value.
	 * @param pValue The actual property value, possibly null, or empty.
	 * @param pDefaultValue The default property value, which is being applied,
	 *   if the actual property value is null, or empty.
	 * @return The converted property value.
	 */
	protected Long asLong(String pValue, String pDefaultValue) {
		if (!Strings.isEmpty(pValue)) {
			try {
				return Long.parseLong(pValue);
			} catch (NumberFormatException e) {
				// Do nothing, like empty value.
			}
		}
		if (!Strings.isEmpty(pDefaultValue)) {
			try {
				return Long.parseLong(pDefaultValue);
			} catch (NumberFormatException e) {
				// Do nothing, like empty value.
			}
		}
		return null;
	}

	/** Converts the given string value into the actual short value.
	 * @param pValue The actual property value, possibly null, or empty.
	 * @param pDefaultValue The default property value, which is being applied,
	 *   if the actual property value is null, or empty.
	 * @return The converted property value.
	 */
	protected Short asShort(String pValue, String pDefaultValue) {
		if (!Strings.isEmpty(pValue)) {
			try {
				return Short.parseShort(pValue);
			} catch (NumberFormatException e) {
				// Do nothing, like empty value.
			}
		}
		if (!Strings.isEmpty(pDefaultValue)) {
			try {
				return Short.parseShort(pDefaultValue);
			} catch (NumberFormatException e) {
				// Do nothing, like empty value.
			}
		}
		return null;
	}

	/** Converts the given string value into the actual URL value.
	 * @param pValue The actual property value, possibly null, or empty.
	 * @param pDefaultValue The default property value, which is being applied,
	 *   if the actual property value is null, or empty.
	 * @return The converted property value.
	 */
	protected URL asUrl(String pValue, String pDefaultValue) {
		if (!Strings.isEmpty(pValue)) {
			try {
				return new URL(pValue);
			} catch (MalformedURLException e) {
				// Do nothing, like empty value.
			}
		}
		if (!Strings.isEmpty(pDefaultValue)) {
			try {
				return new URL(pDefaultValue);
			} catch (MalformedURLException e) {
				// Do nothing, like empty value.
			}
		}
		return null;
	}

	/** Converts the given string value into the actual path value.
	 * @param pValue The actual property value, possibly null, or empty.
	 * @param pDefaultValue The default property value, which is being applied,
	 *   if the actual property value is null, or empty.
	 * @return The converted property value.
	 */
	protected Path asPath(String pValue, String pDefaultValue) {
		if (!Strings.isEmpty(pValue)) {
			return Paths.get(pValue);
		}
		if (!Strings.isEmpty(pDefaultValue)) {
			return Paths.get(pDefaultValue);
		}
		return null;
	}

	/** Converts the given string value into the actual file value.
	 * @param pValue The actual property value, possibly null, or empty.
	 * @param pDefaultValue The default property value, which is being applied,
	 *   if the actual property value is null, or empty.
	 * @return The converted property value.
	 */
	protected File asFile(String pValue, String pDefaultValue) {
		if (!Strings.isEmpty(pValue)) {
			return new File(pValue);
		}
		if (!Strings.isEmpty(pDefaultValue)) {
			return new File(pDefaultValue);
		}
		return null;
	}

	/** Converts the given string value into the actual boolean value.
	 * @param pValue The actual property value, possibly null, or empty.
	 * @param pDefaultValue The default property value, which is being applied,
	 *   if the actual property value is null, or empty.
	 * @return The converted property value.
	 */
	protected Boolean asBoolean(String pValue, String pDefaultValue) {
		if (!Strings.isEmpty(pValue)) {
			return Boolean.valueOf(pValue);
		}
		if (!Strings.isEmpty(pDefaultValue)) {
			return Boolean.valueOf(pDefaultValue);
		}
		return null;
	}
}
