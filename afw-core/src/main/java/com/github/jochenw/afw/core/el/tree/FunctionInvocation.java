package com.github.jochenw.afw.core.el.tree;

import java.util.List;


/**
 * A function invocation.
 */
public class FunctionInvocation {
    private final String name;
    private final List<Object> args;

    /**
     * Creates a new instance.
     */
    public FunctionInvocation(String pName, List<Object> pArgs) {
        name = pName;
        args = pArgs;
    }

    /**
     * Returns the functions name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the functions argument list.
     */
    public List<Object> getArgs() {
        return args;
    }
}
