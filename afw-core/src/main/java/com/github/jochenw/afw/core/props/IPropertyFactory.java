package com.github.jochenw.afw.core.props;

import java.util.Map;

public interface IPropertyFactory {
    public interface ChangeListener {
        void valueChanged(IPropertyFactory pFactory, Map<String,String> oldValues, Map<String,String> newValues);
    }
    void addListener(ChangeListener pListener);
    Map<String,String> getPropertyMap();
    IProperty<String> getProperty(String pKey);
    IProperty<String> getProperty(String pKey, String pDefaultValue);
    IProperty<String> getProperty(String pKey, String pDefaultValue, IProperty.ChangeListener<String> pListener);
    IIntProperty getIntProperty(String pKey, int pDefaultValue);
    IIntProperty getIntProperty(String pKey, int pDefaultValue, IProperty.ChangeListener<Integer> pListener);
    ILongProperty getLongProperty(String pKey, long pDefaultValue);
    ILongProperty getLongProperty(String pKey, long pDefaultValue, IProperty.ChangeListener<Long> pListener);
    IBooleanProperty getBooleanProperty(String pKey, boolean pDefaultValue);
    IBooleanProperty getBooleanProperty(String pKey, boolean pDefaultValue, IProperty.ChangeListener<Boolean> pListener);
    IBooleanProperty getBooleanProperty(String pKey);
    IBooleanProperty getBooleanProperty(String pKey, IProperty.ChangeListener<Boolean> pListener);
	String getPropertyValue(String pKey);
}
