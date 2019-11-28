package com.github.jochenw.afw.core.stream;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;

import org.junit.Test;

import com.github.jochenw.afw.core.stream.JsonStreamWriterTest.SerializableBean.SerializableSubBean;


/**
 * Test case for the {@link XmlStreamWriter}.
 */
public class JsonStreamWriterTest {
	/** A bean class for testing serialization/deserialization.
	 */
	@Streamable
	public static class SerializableBean {
		/** An inner bean class for testing serialization/deserialization.
		 */
		@Streamable
		public static class SerializableSubBean {
			@Streamable	private String string;
			@Streamable	private int primitiveInt;
			@Streamable	private Integer nonPrimitiveInt;
			@Streamable(id="someInt") private Integer anInteger;
			@Streamable(id="yaInt", terse=true) private Integer yaInteger;
			// This field will not be streamed, because it's not annotated with @Streamable.
			@SuppressWarnings("unused")
			private int nonStreamableInt;
			@Streamable	private long primitiveLong;
			@Streamable	private Long nonPrimitiveLong;
			@Streamable	private short primitiveShort;
			@Streamable	private Short nonPrimitiveShort;
			@Streamable	private byte primitiveByte;
			@Streamable private Byte nonPrimitiveByte;
			@Streamable private boolean primitiveBoolean;
			@Streamable private Boolean nonPrimitiveBoolean;
			@Streamable private char primitiveChar;
			@Streamable private Character nonPrimitiveChar;
		}

		@Streamable private SerializableSubBean subBean;
	}

	/**
	 * Test case for serializing single, atomic, values.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testAtomicValues() throws Exception {
		final JsonStreamWriter jsw = new JsonStreamWriter();
		{
			final SerializableBean bean0 = new SerializableBean();
			bean0.subBean = new SerializableSubBean();
			final String expect0 = "{\"subBean\":{\"primitiveBoolean\":false,\"primitiveByte\":0,\"primitiveChar\":\"\\u0000\",\"primitiveInt\":0,\"primitiveLong\":0,\"primitiveShort\":0}}";
			assertSerialized(jsw, bean0, expect0);
		}
		{
			final SerializableBean bean1 = new SerializableBean();
			bean1.subBean = new SerializableSubBean();
			bean1.subBean.primitiveBoolean = true;
			bean1.subBean.nonPrimitiveBoolean = Boolean.FALSE;
			final String expect1 = "{\"subBean\":{\"nonPrimitiveBoolean\":false,\"primitiveBoolean\":true,\"primitiveByte\":0,\"primitiveChar\":\"\\u0000\",\"primitiveInt\":0,\"primitiveLong\":0,\"primitiveShort\":0}}";
			assertSerialized(jsw, bean1, expect1);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveInt = 2;
			bean.subBean.nonPrimitiveInt = Integer.valueOf(1);
			final String expect = "{\"subBean\":{\"nonPrimitiveInt\":1,\"primitiveBoolean\":false,\"primitiveByte\":0,\"primitiveChar\":\"\\u0000\",\"primitiveInt\":2,\"primitiveLong\":0,\"primitiveShort\":0}}";
			assertSerialized(jsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveShort = (short) 2;
			bean.subBean.nonPrimitiveShort = Short.valueOf((short) 1);
			final String expect = "{\"subBean\":{\"nonPrimitiveShort\":1,\"primitiveBoolean\":false,\"primitiveByte\":0,\"primitiveChar\":\"\\u0000\",\"primitiveInt\":0,\"primitiveLong\":0,\"primitiveShort\":2}}";
			assertSerialized(jsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveLong = 2l;
			bean.subBean.nonPrimitiveLong = Long.valueOf(1l);
			final String expect = "{\"subBean\":{\"nonPrimitiveLong\":1,\"primitiveBoolean\":false,\"primitiveByte\":0,\"primitiveChar\":\"\\u0000\",\"primitiveInt\":0,\"primitiveLong\":2,\"primitiveShort\":0}}";
			assertSerialized(jsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveByte = (byte) 2;
			bean.subBean.nonPrimitiveByte = Byte.valueOf((byte) 1);
			final String expect = "{\"subBean\":{\"nonPrimitiveByte\":1,\"primitiveBoolean\":false,\"primitiveByte\":2,\"primitiveChar\":\"\\u0000\",\"primitiveInt\":0,\"primitiveLong\":0,\"primitiveShort\":0}}";
			assertSerialized(jsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveChar = 'a';
			bean.subBean.nonPrimitiveChar = Character.valueOf('B');
			final String expect = "{\"subBean\":{\"nonPrimitiveChar\":\"B\",\"primitiveBoolean\":false,\"primitiveByte\":0,\"primitiveChar\":\"a\",\"primitiveInt\":0,\"primitiveLong\":0,\"primitiveShort\":0}}";
			assertSerialized(jsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.anInteger = Integer.valueOf(5);
			final String expect = "{\"subBean\":{\"primitiveBoolean\":false,\"primitiveByte\":0,\"primitiveChar\":\"\\u0000\",\"primitiveInt\":0,\"primitiveLong\":0,\"primitiveShort\":0,\"someInt\":5}}";
			assertSerialized(jsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.yaInteger = Integer.valueOf(5);
			final String expect = "{\"subBean\":{\"primitiveBoolean\":false,\"primitiveByte\":0,\"primitiveChar\":\"\\u0000\",\"primitiveInt\":0,\"primitiveLong\":0,\"primitiveShort\":0,\"yaInt\":5}}";
			assertSerialized(jsw, bean, expect);
		}
	}

	private void assertSerialized(JsonStreamWriter pWriter, SerializableBean pBean, String pExpect)
			throws IOException {
		final StringWriter sw = new StringWriter();
		pWriter.write(sw, pBean);
		String got = sw.toString();
		assertEquals(pExpect, got);
	}

}
