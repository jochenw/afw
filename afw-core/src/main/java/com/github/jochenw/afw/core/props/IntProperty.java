package com.github.jochenw.afw.core.props;

public class IntProperty extends AbstractProperty<Integer> implements IIntProperty {
    public IntProperty(String pKey, Integer pDefaultValue) {
        super(pKey, pDefaultValue);
    }

    @Override
    protected Integer convert(String pStrValue) {
        if (pStrValue == null) {
            return getDefaultValue();
        }
        try {
            final int i = Integer.parseInt(pStrValue);
            return Integer.valueOf(i);
        } catch (NumberFormatException e) {
            return getDefaultValue();
        }
    }

    @Override
    public int getIntValue() {
        return getValue().intValue();
    }

    @Override
    public int getIntDefaultValue() {
        return getDefaultValue().intValue();
    }

}
