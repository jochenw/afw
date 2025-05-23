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
package com.github.jochenw.afw.core.log.slf4j;

import com.github.jochenw.afw.core.log.AbstractLog;
import com.github.jochenw.afw.core.log.AbstractLogFactory;
import com.github.jochenw.afw.core.log.ILogFactory;


/** Implementation of {@link ILogFactory}, which is based on SLF4J.
 */
public class Slf4jLogFactory extends AbstractLogFactory {
	/** Creates a new instance.
	 */
	public Slf4jLogFactory() {}

    @Override
    protected AbstractLog newLog(String pId) {
        return new Slf4jLog(this, pId);
    }

	@Override
	protected void init() {
		// Does nothing.
	}

}
