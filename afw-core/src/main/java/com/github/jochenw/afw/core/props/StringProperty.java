package com.github.jochenw.afw.core.props;

public class StringProperty extends AbstractProperty<String> {
    public StringProperty(String pKey, String pDefaultValue) {
        super(pKey, pDefaultValue);
    }

    @Override
    protected String convert(String pStrValue) {
        return pStrValue;
    }

}
