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


/**
 * A named value indicates a lookup into the model. The
 * model must return a value, which is associated with
 * the name.
 */
public class NamedValue {
    final String name;

    /**
     * Creates a new instance with the given name.
     * @param pName The name, which is being looked up.
     */
    public NamedValue(String pName) {
        name = pName;
    }

    /**
     * Returns the name.
     * @return The name, which is being looked up.
     */
    public String getName() {
        return name;
    }
}
