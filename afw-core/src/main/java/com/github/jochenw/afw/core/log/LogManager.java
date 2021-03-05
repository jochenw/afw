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


/**
 * A utility class, which provides static methods for dealing with loggers.
 */
public class LogManager {
	/**
	 * A listener is being notified, when the default logger factory changes.
	 */
    public interface Listener {
    	/**
    	 * Called with the new default logger factory.
    	 * @param pFactory The new default logger factory.
    	 */
        void logFactoryChanged(ILogFactory pFactory);
    }
    private static LogManager theInstance = new LogManager();

    private ILogFactory logFactory;
    private final List<Listener> listeners = new ArrayList<Listener>();

    /**
     * Private constructor, because we want this to be a singleton.
     */
    private LogManager() {
        logFactory = new DefaultLogFactory();
        listeners.add((Listener) logFactory);
    }
    /** Adds a listener to the manager.
     * @param pListener The listener, which is being added.
     */
    public synchronized void addListener(Listener pListener) {
        listeners.add(pListener);
    }
    /** Sets a new default logger factory. This will trigger
     * notification of the listeners.
     * @param pFactory The new default logger factory.
     */
    public synchronized void setLogFactory(ILogFactory pFactory) {
        logFactory = pFactory;
        for (Listener listener : listeners) {
            listener.logFactoryChanged(pFactory);
        }
    }
    /**
     * Returns the default logger factory.
     * @return The default logger factory.
     */
    public synchronized ILogFactory getLogFactory() {
        return logFactory;
    }

    /** Returns the singleton instance.
     * @return The singleton instance.
     */
    public static LogManager getInstance() {
        return theInstance;
    }

    /** Returns a logger with the given classes fully qualified name
     * as the logger id. Basically, this is equivalent to
     * <pre>
     *   getLog(pClass.getName())
     * </pre>
     * @param pClass The class, which determines the logger id.
     *   {@link Class#getName()} will be invoked on that object.
     * @return The created logger.
     */
    public static ILog getLog(Class<?> pClass) {
        return getInstance().getLogFactory().getLog(pClass);
    }

    /** Returns a logger with the given logger id.
     * @param pId The created loggers id.
     * @return The created logger.
     */
    public static ILog getLog(String pId) {
        return getInstance().getLogFactory().getLog(pId);
    }

    /** Returns a method logger with the given classes fully
     * qualified name as the logger id. Basically, this is
     * equivalent to
     * <pre>
     *   getLog(pClass.getName(), pMethod)
     * </pre>
     * @param pClass The class, which determines the logger id.
     *   {@link Class#getName()} will be invoked on that object.
     * @param pMethod The method name, which is being logged as
     * part of every message.
     * @return The created logger.
     */
    public static IMLog getLog(Class<?> pClass, String pMethod) {
        return getInstance().getLogFactory().getLog(pClass, pMethod);
    }

    /** Returns a method logger with the given logger id.
     * @param pId The logger id.
     * @param pMethod The method name, which is being logged as
     *   part of every message.
     * @return The created logger.
     */
    public static IMLog getLog(String pId, String pMethod) {
        return getInstance().getLogFactory().getLog(pId, pMethod);
    }
}
