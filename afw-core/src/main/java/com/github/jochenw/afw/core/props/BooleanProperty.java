package com.github.jochenw.afw.core.props;

public class BooleanProperty extends AbstractProperty<Boolean> implements IBooleanProperty {
    public BooleanProperty(String pKey, Boolean pDefaultValue) {
        super(pKey, pDefaultValue);
    }

    @Override
    protected Boolean convert(String pStrValue) {
        if (pStrValue == null) {
            return getDefaultValue();
        }
        final boolean b = Boolean.parseBoolean(pStrValue);
        return Boolean.valueOf(b);
    }

    @Override
    public boolean getBooleanValue() {
        return getValue().booleanValue();
    }

    @Override
    public boolean getBooleanDefaultValue() {
        return getDefaultValue().booleanValue();
    }

}
