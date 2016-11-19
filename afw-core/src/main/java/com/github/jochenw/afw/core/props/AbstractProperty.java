package com.github.jochenw.afw.core.props;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public abstract class AbstractProperty<O> implements IProperty<O>, IPropertyFactory.ChangeListener {
    private final List<ChangeListener<O>> listeners = new ArrayList<>();
    private final String key;
    private String strValue;
    private final O defaultValue;
    private O value;

    AbstractProperty(String pKey, O pDefaultValue) {
        key = pKey;
        defaultValue = pDefaultValue;
    }
    
    @Override
    public String getKey() {
        return key;
    }

    @Override
    public synchronized String getStringValue() {
        return strValue;
    }

    @Override
    public synchronized O getValue() {
        return value;
    }

    @Override
    public O getDefaultValue() {
        return defaultValue;
    }

    @Override
    public synchronized void addListener(ChangeListener<O> pListener) {
        listeners.add(pListener);
    }

    @Override
    public synchronized void valueChanged(IPropertyFactory pFactory, Map<String, String> pOldValue,
            Map<String, String> pNewValue) {
        strValue = pNewValue.get(key);
        final O oldValue = value;
        value = convert(strValue);
        for (ChangeListener<O> listener : listeners) {
            listener.valueChanged(this, oldValue, value);
        }
    }

    protected abstract O convert(String pStrValue);
}
