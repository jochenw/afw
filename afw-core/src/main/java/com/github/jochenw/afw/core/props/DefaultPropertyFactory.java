/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.props;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.core.util.Exceptions;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/** Default implementation of {@link IPropertyFactory}.
 */
public class DefaultPropertyFactory extends AbstractPropertyFactory {
	/** A set of properties with a time stamp. A change in the time stamp
	 * allows to trigger a reload.
	 */
    public static class DatedProperties {
        private final long lastModifiedDate;
        private final Map<String,String> properties;

        DatedProperties(Map<String,String> pProperties, long pLastModifiedDate) {
            lastModifiedDate = pLastModifiedDate;
            properties = pProperties;
        }
    }
    private final URL propertyUrl;
    private final URL propertyFactoryUrl;
    private DatedProperties factoryProperties;
    private DatedProperties instanceProperties;
    private Map<String,String> myProperties;

    /**
     * Creates a new instance with the given URL's for factory,
     * and custom properties.
     * @param pPropertyUrl URL of the instance (custom) properties.
     * @param pPropertyFactoryUrl URL of the factory properties.
     */
    public DefaultPropertyFactory(URL pPropertyUrl, URL pPropertyFactoryUrl) {
        propertyFactoryUrl = pPropertyFactoryUrl;
        propertyUrl = pPropertyUrl;
        initProperties();
    }

    /**
     * Creates a new instance with the given properties.
     * @param pProperties The property set.
     */
    public DefaultPropertyFactory(Properties pProperties) {
    	this(pProperties, null);
    }

    /**
     * Creates a new instance with the given factory, and instance (custom) properties.
     * @param pFactoryProperties The factory properties.
     * @param pInstanceProperties The instance (custom) properties.
     */
    public DefaultPropertyFactory(Properties pFactoryProperties, Properties pInstanceProperties) {
    	final Function<Properties,Map<String,String>> mapCreator = (p) -> {
    		final Map<String,String> map = new HashMap<>();
    		if (p != null) {
    			p.forEach((k,v) -> {
    				if (k != null  &&  v != null) {
    					map.put(k.toString(), v.toString());
    				}
    			});
    		}
    		return map;
    	};
    	propertyFactoryUrl = null;
    	propertyUrl = null;
    	factoryProperties = new DatedProperties(mapCreator.apply(pFactoryProperties), 0l);
    	instanceProperties = new DatedProperties(mapCreator.apply(pInstanceProperties), 0l);
    	myProperties = new HashMap<String,String>();
    	myProperties.putAll(factoryProperties.properties);
    	myProperties.putAll(instanceProperties.properties);
    }

    private void initProperties() {
        factoryProperties = loadProperties(propertyFactoryUrl);
        instanceProperties = loadProperties(propertyUrl);
        final Map<String,String> props = new HashMap<>();
        if (factoryProperties != null) {
            props.putAll(factoryProperties.properties);
        }
        if (instanceProperties != null) {
            props.putAll(instanceProperties.properties);
        }
        myProperties = props;
    }

    protected DatedProperties loadProperties(URL pUrl) {
    	if (pUrl == null) {
    		return null;
    	}
        final Properties props = new Properties();
        long lastModifiedTime = 0;
        URLConnection conn = null;
        InputStream istream = null;
        Throwable th = null;
        try {
            conn = pUrl.openConnection();
            lastModifiedTime = conn.getLastModified();
            istream = conn.getInputStream();
            if (pUrl.toString().endsWith(".xml")) {
                props.loadFromXML(istream);
            } else {
                props.load(istream);
            }
            istream.close();
            istream = null;
        } catch (Throwable t) {
            th = t;
        } finally {
            if (istream != null) {
                try {
                    istream.close();
                } catch (Throwable t) {
                    if (th == null) {
                        th = t;
                    }
                }
            }
        }
        if (th != null) {
            throw Exceptions.show(th);
        }
        final Map<String,String> map = new HashMap<>();
        props.entrySet().stream().forEach((en) -> {
            map.put((String) en.getKey(), (String) en.getValue());
        });
        return new DatedProperties(map, lastModifiedTime);
    }
    
    @Override
    public synchronized Map<String, String> getPropertyMap() {
        return Collections.unmodifiableMap(myProperties);
    }

    /**
     * Called to reload the properties.
     */
    public synchronized void reload() {
        if (!isUptodate(factoryProperties, propertyFactoryUrl)
                ||  !isUptodate(instanceProperties, propertyUrl)) {
            final Map<String,String> oldProperties = getPropertyMap();
            initProperties();
            notifyListeners(oldProperties);
        }
    }

    private boolean isUptodate(DatedProperties pLoadedProperties, URL pPropertyUrl) {
        final long loadedLastModifiedTime = pLoadedProperties.lastModifiedDate;
        long currentLastModifiedTime = 0;
        URLConnection conn = null;
        Throwable th = null;
        try {
            conn = pPropertyUrl.openConnection();
            currentLastModifiedTime = conn.getLastModified();
        } catch (Throwable t) {
            th = t;
        }
        if (th != null) {
            throw Exceptions.show(th);
        }
        return loadedLastModifiedTime != currentLastModifiedTime;
    }

    /**
     * Returns the URL of the factory properties.
     * @return The URL of the factory properties.
     */
    public URL getFactoryUrl() {
        return propertyFactoryUrl;
    }
    /**
     * Returns the URL of the instance (custom) properties.
     * @return The URL of the instance (custom) properties.
     */
    public URL getInstanceUrl() {
        return propertyUrl;
    }

	@Override
	public synchronized String getPropertyValue(String pKey) {
		return myProperties.get(pKey);
	}

	/** Creates a new instance by reading the given property files, in that order. (Later files
	 * take precedence over former files.) It is an error, if neither of the property files exist.
	 * @param pUris The locations, where to look for property files.
	 * @return The created instance.
	 * @throws NullPointerException The parameter {@code pUris}, or either of the elements
	 * is null.
	 * @throws IllegalStateException Neither of the given files was found.
	 */
	public static DefaultPropertyFactory of(@Nonnull String... pUris) {
		return of(true, pUris);
	}

	/** Creates a new instance by reading the given property files, in that order. (Later files
	 * take precedence over former files.) It is an error, if neither of the property files exist.
	 * @param pFileRequired The value true indicates, that at least one of the files must exist.
	 *   Otherwise, a property factory without any properties may be returned.
	 * @param pUris The locations, where to look for property files.
	 * @return The created instance.
	 * @throws NullPointerException The parameter {@code pUris}, or either of the elements
	 * is null.
	 * @throws IllegalStateException Neither of the given files was found, and the parameter
	 * {@code pRequired} is true.
	 */
	public static DefaultPropertyFactory of(boolean pFileRequired, @Nonnull String... pUris) {
		final Properties props = new Properties();
		boolean found = false;
		for (String uri : pUris) {
			final Path p1 = Paths.get(uri + ".xml");
			final Path p2 = Paths.get(uri);
			final Path p;
			if (Files.isRegularFile(p2)) {
				p = p2;
			} else if (Files.isRegularFile(p1)) {
				p = p1;
			} else {
				p = null;
			}
			final Properties pr = new Properties();
			if (p != null) {
				try (InputStream in = Files.newInputStream(p)) {
					if (p.getFileName().toString().endsWith(".xml")) {
						pr.loadFromXML(in);
					} else {
						pr.load(in);
					}
				} catch (IOException e) {
					throw Exceptions.show(e);
				}
				props.putAll(pr);
				found = true;
			}
		}
		if (!found  &&  pFileRequired) {
			throw new IllegalStateException("Neither of the following property files has been found: "
					+ String.join(", ", pUris));
		}
		return new DefaultPropertyFactory(props);
	}

	@Override
	public IURLProperty getUrlProperty(String pKey) {
		// TODO Auto-generated method stub
		return null;
	}
}
