/*
 * Copyright 2019 jwi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.stream;

import static org.junit.Assert.*;

import java.io.StringReader;
import org.junit.Test;

/**
 *
 * @author jwi
 */
public class XmlStreamReaderTest {
    private static final String XML_STRING_0 =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<SerializableBean>\n"
            + "  <subBean>\n"
            + "    <primitiveBoolean>false</primitiveBoolean>\n"
            + "    <primitiveByte>0</primitiveByte>\n"
            + "    <primitiveChar>&#32;</primitiveChar>\n"
            + "    <primitiveInt>0</primitiveInt>\n"
            + "    <primitiveLong>0</primitiveLong>\n"
            + "    <primitiveShort>0</primitiveShort>\n"
            + "  </subBean>\n"
            + "</SerializableBean>";

    private static final String XML_STRING_1 =
    		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    		+ "<SerializableBean>\n"
    		+ "  <subBean>\n"
    		+ "    <nonPrimitiveBoolean>false</nonPrimitiveBoolean>\n"
    		+ "    <primitiveBoolean>true</primitiveBoolean>\n"
    		+ "    <primitiveByte>0</primitiveByte>\n"
    		+ "    <primitiveChar>&#32;</primitiveChar>\n"
    		+ "    <primitiveInt>0</primitiveInt>\n"
    		+ "    <primitiveLong>0</primitiveLong>\n"
    		+ "    <primitiveShort>0</primitiveShort>\n"
    		+ "  </subBean>\n"
    		+ "</SerializableBean>";

    private static final String XML_STRING_2 =
    		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    		+ "<SerializableBean>\n"
    		+ "  <subBean>\n"
    		+ "     <nonPrimitiveInt>1</nonPrimitiveInt>\n"
    		+ "     <primitiveBoolean>false</primitiveBoolean>\n"
    		+ "     <primitiveByte>0</primitiveByte>\n"
    		+ "     <primitiveChar>&#32;</primitiveChar>\n"
    		+ "     <primitiveInt>2</primitiveInt>\n"
    		+ "     <primitiveLong>0</primitiveLong>\n"
    		+ "     <primitiveShort>0</primitiveShort>\n"
    		+ "  </subBean>\n"
    		+ "</SerializableBean>";

    private static final String XML_STRING_3 =
    		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    		+ "<SerializableBean>\n"
    		+ "  <subBean>\n"
    		+ "    <nonPrimitiveShort>1</nonPrimitiveShort>\n"
    		+ "    <primitiveBoolean>false</primitiveBoolean>\n"
    		+ "    <primitiveByte>0</primitiveByte>\n"
    		+ "    <primitiveChar>&#32;</primitiveChar>\n"
    		+ "    <primitiveInt>0</primitiveInt>\n"
    		+ "    <primitiveLong>0</primitiveLong>\n"
    		+ "    <primitiveShort>2</primitiveShort>\n"
    		+ "  </subBean>\n"
    		+ "</SerializableBean>";
 
    private static final String XML_STRING_4 =
    		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    		+ "<SerializableBean>\n"
    		+ "  <subBean>\n"
    		+ "    <nonPrimitiveLong>1</nonPrimitiveLong>\n"
    		+ "    <primitiveBoolean>false</primitiveBoolean>\n"
    		+ "    <primitiveByte>0</primitiveByte>\n"
    		+ "    <primitiveChar>&#32;</primitiveChar>\n"
    		+ "    <primitiveInt>0</primitiveInt>\n"
    		+ "    <primitiveLong>2</primitiveLong>\n"
    		+ "    <primitiveShort>0</primitiveShort>\n"
    		+ "  </subBean>\n"
    		+ "</SerializableBean>";

    private static final String XML_STRING_5 =
    		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
    		+ "<SerializableBean>\n"
    		+ "  <subBean>\n"
    		+ "    <nonPrimitiveByte>1</nonPrimitiveByte>\n"
    		+ "    <primitiveBoolean>false</primitiveBoolean>\n"
    		+ "    <primitiveByte>2</primitiveByte>\n"
    		+ "    <primitiveChar>&#32;</primitiveChar>\n"
    		+ "    <primitiveInt>0</primitiveInt>\n"
    		+ "    <primitiveLong>0</primitiveLong>\n"
    		+ "    <primitiveShort>0</primitiveShort>\n"
    		+ "  </subBean>\n"
    		+ "</SerializableBean>";
    
    @Test
    public void testAtomicValues() throws Exception {
        final XmlStreamReader xsr = new XmlStreamReader();
        {
        	final XmlStreamWriterTest.SerializableBean bean = xsr.read(new StringReader(XML_STRING_0), XmlStreamWriterTest.SerializableBean.class);
        	assertNotNull(bean);
        	final XmlStreamWriterTest.SerializableBean.SerializableSubBean subBean = bean.getSubBean();
        	assertNotNull(subBean);
        	assertFalse(subBean.isPrimitiveBoolean());
        	assertEquals((byte) 0, subBean.getPrimitiveByte());
        	assertEquals(' ', subBean.getPrimitiveChar());
        	assertEquals((short) 0, subBean.getPrimitiveShort());
        	assertEquals(0, subBean.getPrimitiveInt());
        	assertEquals(0l, subBean.getPrimitiveLong());
        	assertNull(subBean.getAnInteger());
        	assertNull(subBean.getNonPrimitiveBoolean());
        	assertNull(subBean.getNonPrimitiveByte());
        	assertNull(subBean.getNonPrimitiveChar());
        	assertNull(subBean.getNonPrimitiveShort());
        	assertNull(subBean.getNonPrimitiveInt());
        	assertNull(subBean.getNonPrimitiveLong());
        }
        {
        	final XmlStreamWriterTest.SerializableBean bean = xsr.read(new StringReader(XML_STRING_1), XmlStreamWriterTest.SerializableBean.class);
        	assertNotNull(bean);
        	final XmlStreamWriterTest.SerializableBean.SerializableSubBean subBean = bean.getSubBean();
        	assertNotNull(subBean);
        	assertTrue(subBean.isPrimitiveBoolean());
        	assertEquals(Boolean.FALSE, subBean.getNonPrimitiveBoolean());
        	assertEquals((byte) 0, subBean.getPrimitiveByte());
        	assertEquals(' ', subBean.getPrimitiveChar());
        	assertEquals((short) 0, subBean.getPrimitiveShort());
        	assertEquals(0, subBean.getPrimitiveInt());
        	assertEquals(0l, subBean.getPrimitiveLong());
        	assertNull(subBean.getAnInteger());
        	assertNull(subBean.getNonPrimitiveByte());
        	assertNull(subBean.getNonPrimitiveChar());
        	assertNull(subBean.getNonPrimitiveShort());
        	assertNull(subBean.getNonPrimitiveInt());
        	assertNull(subBean.getNonPrimitiveLong());
        }
        {
        	final XmlStreamWriterTest.SerializableBean bean = xsr.read(new StringReader(XML_STRING_2), XmlStreamWriterTest.SerializableBean.class);
        	assertNotNull(bean);
        	final XmlStreamWriterTest.SerializableBean.SerializableSubBean subBean = bean.getSubBean();
        	assertNotNull(subBean);
        	assertFalse(subBean.isPrimitiveBoolean());
        	assertEquals((byte) 0, subBean.getPrimitiveByte());
        	assertEquals(' ', subBean.getPrimitiveChar());
        	assertEquals((short) 0, subBean.getPrimitiveShort());
        	assertEquals(2, subBean.getPrimitiveInt());
        	assertEquals(0l, subBean.getPrimitiveLong());
        	assertNull(subBean.getAnInteger());
        	assertNull(subBean.getNonPrimitiveBoolean());
        	assertNull(subBean.getNonPrimitiveByte());
        	assertNull(subBean.getNonPrimitiveChar());
        	assertNull(subBean.getNonPrimitiveShort());
        	assertEquals(Integer.valueOf(1), subBean.getNonPrimitiveInt());
        	assertNull(subBean.getNonPrimitiveLong());
        }
        {
        	final XmlStreamWriterTest.SerializableBean bean = xsr.read(new StringReader(XML_STRING_3), XmlStreamWriterTest.SerializableBean.class);
        	assertNotNull(bean);
        	final XmlStreamWriterTest.SerializableBean.SerializableSubBean subBean = bean.getSubBean();
        	assertNotNull(subBean);
        	assertFalse(subBean.isPrimitiveBoolean());
        	assertEquals((byte) 0, subBean.getPrimitiveByte());
        	assertEquals(' ', subBean.getPrimitiveChar());
        	assertEquals((short) 2, subBean.getPrimitiveShort());
        	assertEquals(0, subBean.getPrimitiveInt());
        	assertEquals(0l, subBean.getPrimitiveLong());
        	assertNull(subBean.getAnInteger());
        	assertNull(subBean.getNonPrimitiveBoolean());
        	assertNull(subBean.getNonPrimitiveByte());
        	assertNull(subBean.getNonPrimitiveChar());
        	assertEquals(Short.valueOf((short) 1), subBean.getNonPrimitiveShort());
        	assertNull(subBean.getNonPrimitiveInt());
        	assertNull(subBean.getNonPrimitiveLong());
        }
        {
        	final XmlStreamWriterTest.SerializableBean bean = xsr.read(new StringReader(XML_STRING_4), XmlStreamWriterTest.SerializableBean.class);
        	assertNotNull(bean);
        	final XmlStreamWriterTest.SerializableBean.SerializableSubBean subBean = bean.getSubBean();
        	assertNotNull(subBean);
        	assertFalse(subBean.isPrimitiveBoolean());
        	assertEquals((byte) 0, subBean.getPrimitiveByte());
        	assertEquals(' ', subBean.getPrimitiveChar());
        	assertEquals((short) 0, subBean.getPrimitiveShort());
        	assertEquals(0, subBean.getPrimitiveInt());
        	assertEquals(2l, subBean.getPrimitiveLong());
        	assertNull(subBean.getAnInteger());
        	assertNull(subBean.getNonPrimitiveBoolean());
        	assertNull(subBean.getNonPrimitiveByte());
        	assertNull(subBean.getNonPrimitiveChar());
        	assertNull(subBean.getNonPrimitiveShort());
        	assertNull(subBean.getNonPrimitiveInt());
        	assertEquals(Long.valueOf(1l), subBean.getNonPrimitiveLong());
        }
        {
        	final XmlStreamWriterTest.SerializableBean bean = xsr.read(new StringReader(XML_STRING_5), XmlStreamWriterTest.SerializableBean.class);
        	assertNotNull(bean);
        	final XmlStreamWriterTest.SerializableBean.SerializableSubBean subBean = bean.getSubBean();
        	assertNotNull(subBean);
        	assertFalse(subBean.isPrimitiveBoolean());
        	assertEquals((byte) 2, subBean.getPrimitiveByte());
        	assertEquals(' ', subBean.getPrimitiveChar());
        	assertEquals((short) 0, subBean.getPrimitiveShort());
        	assertEquals(0, subBean.getPrimitiveInt());
        	assertEquals(0l, subBean.getPrimitiveLong());
        	assertNull(subBean.getAnInteger());
        	assertNull(subBean.getNonPrimitiveBoolean());
        	assertEquals(Byte.valueOf((byte) 1), subBean.getNonPrimitiveByte());
        	assertNull(subBean.getNonPrimitiveChar());
        	assertNull(subBean.getNonPrimitiveShort());
        	assertNull(subBean.getNonPrimitiveInt());
        	assertNull(subBean.getNonPrimitiveLong());
        }
    }
}
