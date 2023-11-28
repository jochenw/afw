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
package com.github.jochenw.afw.core.props;

import com.github.jochenw.afw.core.util.MutableInteger;
import com.github.jochenw.afw.core.util.Streams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static org.junit.Assert.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;


/** Test for the {@link DefaultPropertyFactory}.
 */
public class DefaultPropertyFactoryTest {
    private Path tmpDir;

    private Path getTmpDir() throws IOException {
        if (tmpDir == null) {
            tmpDir = Files.createTempDirectory("props");
        }
        return tmpDir;
    }

    private DefaultPropertyFactory newFactory() throws IOException {
        final Path tmpdir = getTmpDir();
        URL factoryUrl = DefaultPropertyFactoryTest.class.getResource("test-factory.properties");
        URL instanceUrl = DefaultPropertyFactoryTest.class.getResource("test.properties");
        final Path testFactoryProperties = tmpdir.resolve("test-factory.properties");
        final Path testProperties = tmpdir.resolve("test.properties");
        factoryUrl = copy(factoryUrl, testFactoryProperties);
        instanceUrl = copy(instanceUrl, testProperties);
        assertNotNull(factoryUrl);
        assertNotNull(instanceUrl);
        return new DefaultPropertyFactory(instanceUrl, factoryUrl);
    }

    private URL copy(URL pUrl, Path pOut) throws IOException {
        final Path dir = pOut.getParent();
        if (!Files.isDirectory(dir)) {
            Files.createDirectories(dir);
        }

        try (InputStream in = pUrl.openStream();
             OutputStream out = Files.newOutputStream(pOut)) {
            Streams.copy(in, out);
        }
        return pOut.toUri().toURL();
    }


    /** Test case for the {@link IProperty.ChangeListener}
     * @throws Exception The test failed.
     */
    @Test
    public void test() throws Exception {
        MutableInteger mi = new MutableInteger();
        final IProperty.ChangeListener<Integer> cl = new IProperty.ChangeListener<Integer>() {
            @Override
            public void valueChanged(IProperty<Integer> pProperty, Integer pOldValue, Integer pNewValue) {
                mi.setValue(mi.getValue()+1);
            }
        };
        assertEquals(0, mi.getValue());
        final DefaultPropertyFactory dpf = newFactory();
        assertNotNull(dpf);
        final Map<String,String> props = dpf.getPropertyMap();
        assertEquals("41", props.get("some.int.property"));
        assertEquals(props.get("some.long.property"), "38686348746874");
        assertEquals(props.get("some.string.property"), "foo bar baz");
        final IIntProperty ip;
        ip = dpf.getIntProperty("some.int.property", 0, cl);
        assertEquals(1, mi.getValue());
        assertEquals(41, ip.getIntValue());
        assertEquals(Integer.valueOf(41), ip.getValue());
        assertEquals(0, ip.getIntDefaultValue());
        assertEquals("41", ip.getStringValue());
        assertEquals("some.int.property", ip.getKey());
        final IIntProperty ip2 = dpf.getIntProperty("some.int.property.that.doesnt.exist", 0);
        assertEquals(0, ip2.getIntValue());
        assertEquals(Integer.valueOf(0), ip2.getValue());
        assertEquals(0, ip2.getIntDefaultValue());
        assertNull(ip2.getStringValue());
        assertEquals("some.int.property.that.doesnt.exist", ip2.getKey());
        final IProperty<String> sp = dpf.getProperty("some.string.property");
        assertEquals("foo bar baz", sp.getValue());
        assertSame(sp.getValue(), sp.getStringValue());
        assertNull(sp.getDefaultValue());
        assertEquals("some.string.property", sp.getKey());

        final Properties newProps = new Properties();
        newProps.put("some.int.property", "43");
        newProps.put("some.int.property.that.doesnt.exist", "-1");
        newProps.put("some.string.property", "fooBarBaz");
        newProps.put("some.long.property", "5678");
        final URL url = dpf.getInstanceUrl();
        assertEquals("file", url.getProtocol());
        final File f = new File(url.getFile());
        try (OutputStream ostream = new FileOutputStream(f)) {
            newProps.store(ostream, null);
        }
        assertEquals(1, mi.getValue());
        dpf.reload();
        assertEquals(2, mi.getValue());
        assertEquals(43, ip.getIntValue());
        assertEquals(-1, ip2.getIntValue());
        assertEquals("fooBarBaz", sp.getValue());
    }
}
