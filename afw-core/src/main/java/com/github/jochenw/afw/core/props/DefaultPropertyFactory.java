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

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import com.github.jochenw.afw.core.util.Exceptions;
import java.net.URLConnection;


public class DefaultPropertyFactory extends AbstractPropertyFactory {
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

    public DefaultPropertyFactory(URL pPropertyUrl, URL pPropertyFactoryUrl) {
        propertyFactoryUrl = pPropertyFactoryUrl;
        propertyUrl = pPropertyUrl;
        initProperties();
    }

    public DefaultPropertyFactory(Properties pProperties) {
    	this(pProperties, null);
    }

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

    public URL getFactoryUrl() {
        return propertyFactoryUrl;
    }
    public URL getInstanceUrl() {
        return propertyUrl;
    }

	@Override
	public synchronized String getPropertyValue(String pKey) {
		return myProperties.get(pKey);
	}
}
