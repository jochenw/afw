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

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;

/**
 * Abstract base implementation of {@link IPropertyFactory}.
 */
public abstract class AbstractPropertyFactory implements IPropertyFactory {
    private final List<IPropertyFactory.ChangeListener> listeners = new ArrayList<>();

    /** Called to notify the listeners, that an updated property set is available.
     * @param pOldProperties The old property set. A property is changed, if its
     *    value in this property set differs from the value in the current
     *    (updated) property set.
     */
    protected synchronized void notifyListeners(Map<String,String> pOldProperties) {
        final Map<String,String> newProperties = getPropertyMap();
        listeners.stream().forEach((listener) -> {
            listener.valueChanged(this, pOldProperties, newProperties);
        });
    }
    @Override
    public IProperty<String> getProperty(@NonNull String pKey) {
        return getProperty(pKey, null);
    }

    @Override
    public IProperty<String> getProperty(@NonNull String pKey, String pDefaultValue) {
        return getProperty(pKey, pDefaultValue, null);
    }

    @Override
    public IProperty<String> getProperty(@NonNull String pKey, String pDefaultValue, IProperty.ChangeListener<String> pListener) {
        final StringProperty sp = new StringProperty(pKey, pDefaultValue);
        sp.valueChanged(this, null, getPropertyMap());
        if (pListener != null) {
            sp.addListener(pListener);
            pListener.valueChanged(sp, null, sp.getValue());
        }
        addListener(sp);
        return sp;
    }

    @Override
    public IIntProperty getIntProperty(@NonNull String pKey, int pDefaultValue) {
        return getIntProperty(pKey, pDefaultValue, null);
    }

    @Override
    public IIntProperty getIntProperty(@NonNull String pKey, int pDefaultValue, IProperty.ChangeListener<Integer> pListener) {
        final IntProperty ip = new IntProperty(pKey, pDefaultValue);
        ip.valueChanged(this, null, getPropertyMap());
        if (pListener != null) {
        	@SuppressWarnings("null")
			final IProperty.ChangeListener<@NonNull Integer> listener = pListener;
            ip.addListener(listener);
            pListener.valueChanged(ip, null, ip.getValue());
        }
        addListener(ip);
        return ip;
    }

    @Override
    public ILongProperty getLongProperty(@NonNull String pKey, long pDefaultValue) {
        return getLongProperty(pKey, pDefaultValue, null);
    }

    @Override
    public ILongProperty getLongProperty(@NonNull String pKey, long pDefaultValue, IProperty.ChangeListener<Long> pListener) {
        final LongProperty lp = new LongProperty(pKey, pDefaultValue);
        lp.valueChanged(this, null, getPropertyMap());
        if (pListener != null) {
        	@SuppressWarnings("null")
			final IProperty.ChangeListener<@NonNull Long> listener = pListener;
            lp.addListener(listener);
            pListener.valueChanged(lp, null, lp.getValue());
        }
        addListener(lp);
        return lp;
    }


    @Override
    public IBooleanProperty getBooleanProperty(@NonNull String pKey) {
        return getBooleanProperty(pKey, false);
    }

    @Override
    public IBooleanProperty getBooleanProperty(@NonNull String pKey, boolean pDefaultValue) {
        return getBooleanProperty(pKey, pDefaultValue, null);
    }

    @Override
    public IBooleanProperty getBooleanProperty(@NonNull String pKey, IProperty.ChangeListener<Boolean> pListener) {
        return getBooleanProperty(pKey, false, pListener);
    }

    @Override
    public IBooleanProperty getBooleanProperty(@NonNull String pKey, boolean pDefaultValue, IProperty.ChangeListener<Boolean> pListener) {
        final BooleanProperty bp = new BooleanProperty(pKey, pDefaultValue);
        bp.valueChanged(this, null, getPropertyMap());
        if (pListener != null) {
        	@SuppressWarnings("null")
			final IProperty.ChangeListener<@NonNull Boolean> listener = pListener;
            bp.addListener(listener);
            pListener.valueChanged(bp, null, bp.getValue());
        }
        addListener(bp);
        return bp;
    }

    @Override
    public synchronized void addListener(ChangeListener pListener) {
        listeners.add(pListener);
    }

	@Override
	public IURLProperty getUrlProperty(@NonNull String pKey, URL pDefaultValue) {
		return getUrlProperty(pKey, pDefaultValue, null);
	}

	@Override
	public IURLProperty getUrlProperty(@NonNull String pKey, URL pDefaultValue,
			                           IProperty.ChangeListener<URL> pListener) {
		final UrlProperty up = new UrlProperty(pKey, pDefaultValue);
		if (pListener != null) {
			up.addListener(pListener);
		}
		return up;
	}

	@Override
	public IPathProperty getPathProperty(@NonNull String pKey) {
		return getPathProperty(pKey, null, null);
	}

	@Override
	public IPathProperty getPathProperty(@NonNull String pKey, Path pDefaultValue) {
		return getPathProperty(pKey, pDefaultValue, null);
	}

	@Override
	public IPathProperty getPathProperty(@NonNull String pKey, Path pDefaultValue,
			                             IProperty.ChangeListener<Path> pListener) {
		final PathProperty pp = new PathProperty(pKey, pDefaultValue);
		if (pListener != null) {
			pp.addListener(pListener);
		}
		return pp;
	}
}
