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
package com.github.jochenw.afw.core.log;

import java.util.ArrayList;
import java.util.List;

public class LogManager {
    public interface Listener {
        void logFactoryChanged(ILogFactory pFactory);
    }
    private static LogManager theInstance = new LogManager();

    private ILogFactory logFactory;
    private final List<Listener> listeners = new ArrayList<Listener>();

    public LogManager() {
        logFactory = new DefaultLogFactory();
        listeners.add((Listener) logFactory);
    }
    public synchronized void addListener(Listener pListener) {
        listeners.add(pListener);
    }
    public synchronized void setLogFactory(ILogFactory pFactory) {
        logFactory = pFactory;
        for (Listener listener : listeners) {
            listener.logFactoryChanged(pFactory);
        }
    }
    public synchronized ILogFactory getLogFactory() {
        return logFactory;
    }

    public static LogManager getInstance() {
        return theInstance;
    }

    public static ILog getLog(Class<?> pClass) {
        return getInstance().getLogFactory().getLog(pClass);
    }

    public static ILog getLog(String pId) {
        return getInstance().getLogFactory().getLog(pId);
    }

    public static IMLog getLog(Class<?> pClass, String pMethod) {
        return getInstance().getLogFactory().getLog(pClass, pMethod);
    }

    public static IMLog getLog(String pId, String pMethod) {
        return getInstance().getLogFactory().getLog(pId, pMethod);
    }
}
