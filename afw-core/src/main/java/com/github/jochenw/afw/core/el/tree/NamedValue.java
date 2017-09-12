package com.github.jochenw.afw.core.el.tree;


/**
 * A named value indicates a lookup into the model. The
 * model must return a value, which is associated with
 * the name.
 */
public class NamedValue {
    final String name;

    /**
     * Creates a new instance with the given name.
     */
    public NamedValue(String pName) {
        name = pName;
    }

    /**
     * Returns the name.
     */
    public String getName() {
        return name;
    }
}
