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
package com.github.jochenw.afw.core.log.simple;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILog.Level;

public class SimpleLogFactoryTest {
    @Test
    public void test() {
        final StringWriter sw = new StringWriter();
        final SimpleLogFactory slf = new SimpleLogFactory(sw);
        assertEquals(Level.DEBUG, slf.getLevel());
        final ILog log = slf.getLog("foo");
        log.trace("main", "First log line"); // This isn't logged, due to Level==DEBUG
        log.debug("main", "Second log line");
        log.info("main", "Third log line with arg", Integer.valueOf(0));
        log.warn("main", "Fourth log line");
        log.error("main", "Fifth log line");
        log.fatal("main", "Sixth log line");
        slf.setLevel(Level.TRACE);
        log.trace("Main", "Logged trace line");
        log.entering("Main", "Start");
        log.exiting("Main", "Stop");

        final String[] logLines = getLogLines(sw);
        assertEquals(8, logLines.length);
        assertEquals("DEBUG foo main: Second log line", logLines[0]);
        assertEquals("INFO foo main: Third log line with arg, 0", logLines[1]);
        assertEquals("WARN foo main: Fourth log line", logLines[2]);
        assertEquals("ERROR foo main: Fifth log line", logLines[3]);
        assertEquals("FATAL foo main: Sixth log line", logLines[4]);
        assertEquals("TRACE foo Main: Logged trace line", logLines[5]);
        assertEquals("DEBUG foo Main: -> Start", logLines[6]);
        assertEquals("DEBUG foo Main: <- Stop", logLines[7]);
    }

    private String[] getLogLines(Object pTarget) {
        String s = pTarget.toString();
        final List<String> list = new ArrayList<>();
        while (s.length() > 0) {
            int len = 0;
            int index = s.indexOf("\r\n");
            if (index == -1) {
                index = s.indexOf('\n');
                len = 1;
            } else {
                len = 2;
            }
            if (index == -1) {
                list.add(s);
                s = "";
            } else {
                list.add(s.substring(0, index));
                s = s.substring(index+len);
            }
        }
        // Remove times.
        for (int i = 0;  i < list.size();  i++) {
            final String line = list.get(i);
            final int index = line.indexOf(' ');
            if (index != -1) {
                list.set(i, line.substring(index+1));
            }
        }
        return list.toArray(new String[list.size()]);
    }
}
