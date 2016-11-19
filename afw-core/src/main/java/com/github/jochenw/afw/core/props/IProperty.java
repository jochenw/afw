package com.github.jochenw.afw.core.props;

public interface IProperty<O> {
    public interface ChangeListener<T> {
        void valueChanged(IProperty<T> pProperty, T pOldValue, T pNewValue);
    }
    String getKey();
    String getStringValue();
    O getValue();
    O getDefaultValue();
    void addListener(ChangeListener<O> pListener);
}
