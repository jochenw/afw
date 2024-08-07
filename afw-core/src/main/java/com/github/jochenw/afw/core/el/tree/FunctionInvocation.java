/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * @param pName Name of the function, which is being invoked.
     * @param pArgs The argument list, which is being passed to the function.
     */
    public FunctionInvocation(String pName, List<Object> pArgs) {
        name = pName;
        args = pArgs;
    }

    /**
     * Returns the functions name.
     * @return The functions name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the functions argument list.
     * @return The functions argument list.
     */
    public List<Object> getArgs() {
        return args;
    }
}
