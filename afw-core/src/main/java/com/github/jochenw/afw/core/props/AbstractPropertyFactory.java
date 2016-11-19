package com.github.jochenw.afw.core.props;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractPropertyFactory implements IPropertyFactory {
    private final List<IPropertyFactory.ChangeListener> listeners = new ArrayList<>();

    protected synchronized void notifyListeners(Map<String,String> oldProperties) {
        final Map<String,String> newProperties = getPropertyMap();
        listeners.stream().forEach((listener) -> {
            listener.valueChanged(this, oldProperties, newProperties);
        });
    }
    @Override
    public IProperty<String> getProperty(String pKey) {
        return getProperty(pKey, null);
    }

    @Override
    public IProperty<String> getProperty(String pKey, String pDefaultValue) {
        return getProperty(pKey, pDefaultValue, null);
    }

    @Override
    public IProperty<String> getProperty(String pKey, String pDefaultValue, IProperty.ChangeListener<String> pListener) {
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
    public IIntProperty getIntProperty(String pKey, int pDefaultValue) {
        return getIntProperty(pKey, pDefaultValue, null);
    }

    @Override
    public IIntProperty getIntProperty(String pKey, int pDefaultValue, IProperty.ChangeListener<Integer> pListener) {
        final IntProperty ip = new IntProperty(pKey, pDefaultValue);
        ip.valueChanged(this, null, getPropertyMap());
        if (pListener != null) {
            ip.addListener(pListener);
            pListener.valueChanged(ip, null, ip.getValue());
        }
        addListener(ip);
        return ip;
    }

    @Override
    public ILongProperty getLongProperty(String pKey, long pDefaultValue) {
        return getLongProperty(pKey, pDefaultValue, null);
    }

    @Override
    public ILongProperty getLongProperty(String pKey, long pDefaultValue, IProperty.ChangeListener<Long> pListener) {
        final LongProperty lp = new LongProperty(pKey, pDefaultValue);
        lp.valueChanged(this, null, getPropertyMap());
        if (pListener != null) {
            lp.addListener(pListener);
            pListener.valueChanged(lp, null, lp.getValue());
        }
        addListener(lp);
        return lp;
    }


    @Override
    public IBooleanProperty getBooleanProperty(String pKey) {
        return getBooleanProperty(pKey, false);
    }

    @Override
    public IBooleanProperty getBooleanProperty(String pKey, boolean pDefaultValue) {
        return getBooleanProperty(pKey, pDefaultValue, null);
    }

    @Override
    public IBooleanProperty getBooleanProperty(String pKey, IProperty.ChangeListener<Boolean> pListener) {
        return getBooleanProperty(pKey, false, pListener);
    }

    @Override
    public IBooleanProperty getBooleanProperty(String pKey, boolean pDefaultValue, IProperty.ChangeListener<Boolean> pListener) {
        final BooleanProperty bp = new BooleanProperty(pKey, pDefaultValue);
        bp.valueChanged(this, null, getPropertyMap());
        if (pListener != null) {
            bp.addListener(pListener);
            pListener.valueChanged(bp, null, bp.getValue());
        }
        addListener(bp);
        return bp;
    }

    @Override
    public synchronized void addListener(ChangeListener pListener) {
        listeners.add(pListener);
    }
}
