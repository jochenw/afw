package com.github.jochenw.afw.core.props;

public class LongProperty extends AbstractProperty<Long> implements ILongProperty {
    public LongProperty(String pKey, Long pDefaultValue) {
        super(pKey, pDefaultValue);
    }

    @Override
    protected Long convert(String pStrValue) {
        if (pStrValue == null) {
            return getDefaultValue();
        }
        try {
            final long l = Long.parseLong(pStrValue);
            return Long.valueOf(l);
        } catch (NumberFormatException e) {
            return getDefaultValue();
        }
    }

    @Override
    public long getLongValue() {
        return getValue().longValue();
    }

    @Override
    public long getLongDefaultValue() {
        return getDefaultValue().longValue();
    }

}
