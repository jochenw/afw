package com.github.jochenw.afw.core.stream;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;

import org.junit.Test;

import com.github.jochenw.afw.core.stream.XmlStreamWriterTest.SerializableBean.SerializableSubBean;


/**
 * Test case for the {@link XmlStreamWriter}.
 */
public class XmlStreamWriterTest {
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
			private int nonStreamableInt;
			@Streamable	private long primitiveLong;
			@Streamable	private Long nonPrimitiveLong;
			@Streamable	private short primitiveShort;
			@Streamable	private Short nonPrimitiveShort;
			@Streamable	private byte primitiveByte;
			@Streamable private Byte nonPrimitiveByte;
			@Streamable private boolean primitiveBoolean;

                public String getString() {
                    return string;
                }

                public void setString(String string) {
                    this.string = string;
                }

                public int getPrimitiveInt() {
                    return primitiveInt;
                }

                public void setPrimitiveInt(int primitiveInt) {
                    this.primitiveInt = primitiveInt;
                }

                public Integer getNonPrimitiveInt() {
                    return nonPrimitiveInt;
                }

                public void setNonPrimitiveInt(Integer nonPrimitiveInt) {
                    this.nonPrimitiveInt = nonPrimitiveInt;
                }

                public Integer getAnInteger() {
                    return anInteger;
                }

                public void setAnInteger(Integer anInteger) {
                    this.anInteger = anInteger;
                }

                public Integer getYaInteger() {
                    return yaInteger;
                }

                public void setYaInteger(Integer yaInteger) {
                    this.yaInteger = yaInteger;
                }

                public int getNonStreamableInt() {
                    return nonStreamableInt;
                }

                public void setNonStreamableInt(int nonStreamableInt) {
                    this.nonStreamableInt = nonStreamableInt;
                }

                public long getPrimitiveLong() {
                    return primitiveLong;
                }

                public void setPrimitiveLong(long primitiveLong) {
                    this.primitiveLong = primitiveLong;
                }

                public Long getNonPrimitiveLong() {
                    return nonPrimitiveLong;
                }

                public void setNonPrimitiveLong(Long nonPrimitiveLong) {
                    this.nonPrimitiveLong = nonPrimitiveLong;
                }

                public short getPrimitiveShort() {
                    return primitiveShort;
                }

                public void setPrimitiveShort(short primitiveShort) {
                    this.primitiveShort = primitiveShort;
                }

                public Short getNonPrimitiveShort() {
                    return nonPrimitiveShort;
                }

                public void setNonPrimitiveShort(Short nonPrimitiveShort) {
                    this.nonPrimitiveShort = nonPrimitiveShort;
                }

                public byte getPrimitiveByte() {
                    return primitiveByte;
                }

                public void setPrimitiveByte(byte primitiveByte) {
                    this.primitiveByte = primitiveByte;
                }

                public Byte getNonPrimitiveByte() {
                    return nonPrimitiveByte;
                }

                public void setNonPrimitiveByte(Byte nonPrimitiveByte) {
                    this.nonPrimitiveByte = nonPrimitiveByte;
                }

                public boolean isPrimitiveBoolean() {
                    return primitiveBoolean;
                }

                public void setPrimitiveBoolean(boolean primitiveBoolean) {
                    this.primitiveBoolean = primitiveBoolean;
                }

                public Boolean getNonPrimitiveBoolean() {
                    return nonPrimitiveBoolean;
                }

                public void setNonPrimitiveBoolean(Boolean nonPrimitiveBoolean) {
                    this.nonPrimitiveBoolean = nonPrimitiveBoolean;
                }

                public char getPrimitiveChar() {
                    return primitiveChar;
                }

                public void setPrimitiveChar(char primitiveChar) {
                    this.primitiveChar = primitiveChar;
                }

                public Character getNonPrimitiveChar() {
                    return nonPrimitiveChar;
                }

                public void setNonPrimitiveChar(Character nonPrimitiveChar) {
                    this.nonPrimitiveChar = nonPrimitiveChar;
                }
			@Streamable private Boolean nonPrimitiveBoolean;
			@Streamable private char primitiveChar;
			@Streamable private Character nonPrimitiveChar;

                        
                }

		@Streamable private SerializableSubBean subBean;

        public SerializableSubBean getSubBean() {
            return subBean;
        }

        public void setSubBean(SerializableSubBean subBean) {
            this.subBean = subBean;
        }
	}

	/**
	 * Test case for serializing single, atomic, values.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testAtomicValues() throws Exception {
		final XmlStreamWriter xsw = new XmlStreamWriter();
		{
			final SerializableBean bean0 = new SerializableBean();
			bean0.subBean = new SerializableSubBean();
			final String expect0 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SerializableBean><SerializableSubBean><primitiveBoolean>false</primitiveBoolean><primitiveByte>0</primitiveByte><primitiveChar>&#0;</primitiveChar><primitiveInt>0</primitiveInt><primitiveLong>0</primitiveLong><primitiveShort>0</primitiveShort></SerializableSubBean></SerializableBean>";
			assertSerialized(xsw, bean0, expect0);
		}
		{
			final SerializableBean bean1 = new SerializableBean();
			bean1.subBean = new SerializableSubBean();
			bean1.subBean.primitiveBoolean = true;
			bean1.subBean.nonPrimitiveBoolean = Boolean.FALSE;
			final String expect1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SerializableBean><SerializableSubBean><nonPrimitiveBoolean>false</nonPrimitiveBoolean><primitiveBoolean>true</primitiveBoolean><primitiveByte>0</primitiveByte><primitiveChar>&#0;</primitiveChar><primitiveInt>0</primitiveInt><primitiveLong>0</primitiveLong><primitiveShort>0</primitiveShort></SerializableSubBean></SerializableBean>";
			assertSerialized(xsw, bean1, expect1);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveInt = 2;
			bean.subBean.nonPrimitiveInt = Integer.valueOf(1);
			final String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SerializableBean><SerializableSubBean><nonPrimitiveInt>1</nonPrimitiveInt><primitiveBoolean>false</primitiveBoolean><primitiveByte>0</primitiveByte><primitiveChar>&#0;</primitiveChar><primitiveInt>2</primitiveInt><primitiveLong>0</primitiveLong><primitiveShort>0</primitiveShort></SerializableSubBean></SerializableBean>";
			assertSerialized(xsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveShort = (short) 2;
			bean.subBean.nonPrimitiveShort = Short.valueOf((short) 1);
			final String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SerializableBean><SerializableSubBean><nonPrimitiveShort>1</nonPrimitiveShort><primitiveBoolean>false</primitiveBoolean><primitiveByte>0</primitiveByte><primitiveChar>&#0;</primitiveChar><primitiveInt>0</primitiveInt><primitiveLong>0</primitiveLong><primitiveShort>2</primitiveShort></SerializableSubBean></SerializableBean>";
			assertSerialized(xsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveLong = 2l;
			bean.subBean.nonPrimitiveLong = Long.valueOf(1l);
			final String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SerializableBean><SerializableSubBean><nonPrimitiveLong>1</nonPrimitiveLong><primitiveBoolean>false</primitiveBoolean><primitiveByte>0</primitiveByte><primitiveChar>&#0;</primitiveChar><primitiveInt>0</primitiveInt><primitiveLong>2</primitiveLong><primitiveShort>0</primitiveShort></SerializableSubBean></SerializableBean>";
			assertSerialized(xsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveByte = (byte) 2;
			bean.subBean.nonPrimitiveByte = Byte.valueOf((byte) 1);
			final String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SerializableBean><SerializableSubBean><nonPrimitiveByte>1</nonPrimitiveByte><primitiveBoolean>false</primitiveBoolean><primitiveByte>2</primitiveByte><primitiveChar>&#0;</primitiveChar><primitiveInt>0</primitiveInt><primitiveLong>0</primitiveLong><primitiveShort>0</primitiveShort></SerializableSubBean></SerializableBean>";
			assertSerialized(xsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.primitiveChar = 'a';
			bean.subBean.nonPrimitiveChar = Character.valueOf('B');
			final String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SerializableBean><SerializableSubBean><nonPrimitiveChar>B</nonPrimitiveChar><primitiveBoolean>false</primitiveBoolean><primitiveByte>0</primitiveByte><primitiveChar>a</primitiveChar><primitiveInt>0</primitiveInt><primitiveLong>0</primitiveLong><primitiveShort>0</primitiveShort></SerializableSubBean></SerializableBean>";
			assertSerialized(xsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.anInteger = Integer.valueOf(5);
			final String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SerializableBean><SerializableSubBean><primitiveBoolean>false</primitiveBoolean><primitiveByte>0</primitiveByte><primitiveChar>&#0;</primitiveChar><primitiveInt>0</primitiveInt><primitiveLong>0</primitiveLong><primitiveShort>0</primitiveShort><someInt>5</someInt></SerializableSubBean></SerializableBean>";
			assertSerialized(xsw, bean, expect);
		}
		{
			final SerializableBean bean = new SerializableBean();
			bean.subBean = new SerializableSubBean();
			bean.subBean.yaInteger = Integer.valueOf(5);
			final String expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SerializableBean><SerializableSubBean yaInt='5'><primitiveBoolean>false</primitiveBoolean><primitiveByte>0</primitiveByte><primitiveChar>&#0;</primitiveChar><primitiveInt>0</primitiveInt><primitiveLong>0</primitiveLong><primitiveShort>0</primitiveShort></SerializableSubBean></SerializableBean>";
			/* This transformer is required, because we do not know, whether an XML
			 * attribute will be written using ', or ".
			 */
			final Function<String, String> transformer = (s) -> s.replace("\"5\"", "'5'");
			assertSerialized(xsw, bean, expect, transformer);
		}
	}

	private void assertSerialized(XmlStreamWriter pWriter, SerializableBean pBean, String pExpect)
			throws IOException {
		assertSerialized(pWriter, pBean, pExpect, null);
	}
	private void assertSerialized(XmlStreamWriter pWriter, SerializableBean pBean,
			                      String pExpect, Function<String,String> pTransformer)
			throws IOException {
		final StringWriter sw = new StringWriter();
		pWriter.write(sw, pBean);
		String got = sw.toString();
		if (pTransformer != null) {
			got = pTransformer.apply(got);
		}
		assertEquals(pExpect, got);
	}

}
