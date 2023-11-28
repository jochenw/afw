package com.github.jochenw.afw.core.components;

import static org.junit.Assert.*;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


/** Test for the {@link DefaultInstantiator}.
 */
public class DefaultInstantiatorTest {
	/** A bean class with multiple properties, of different types.
	 */
	public static class ABean {
		@SuppressWarnings("javadoc")
		protected int intProperty;
		@SuppressWarnings("javadoc")
		protected Integer intObjProperty;
		@SuppressWarnings("javadoc")
		protected long longProperty;
		@SuppressWarnings("javadoc")
		protected Long longObjProperty;
		@SuppressWarnings("javadoc")
		protected short shortProperty;
		@SuppressWarnings("javadoc")
		protected Short shortObjProperty;
		@SuppressWarnings("javadoc")
		protected byte byteProperty;
		@SuppressWarnings("javadoc")
		protected Byte byteObjProperty;
		@SuppressWarnings("javadoc")
		protected float floatProperty;
		@SuppressWarnings("javadoc")
		protected Float floatObjProperty;
		@SuppressWarnings("javadoc")
		protected double doubleProperty;
		@SuppressWarnings("javadoc")
		protected Double doubleObjProperty;
		@SuppressWarnings("javadoc")
		protected boolean boolProperty;
		@SuppressWarnings("javadoc")
		protected Boolean boolObjProperty;
		@SuppressWarnings("javadoc")
		protected String stringProperty;
		@SuppressWarnings("javadoc")
		protected BigInteger bigIntegerProperty;
		@SuppressWarnings("javadoc")
		protected BigDecimal bigDecimalProperty;
		@SuppressWarnings("javadoc")
		protected Path pathProperty;
		@SuppressWarnings("javadoc")
		protected File fileProperty;
		@SuppressWarnings("javadoc")
		protected URL urlProperty;

		/** Returns the intProperty field.
		 * @return The intProperty field.
		 */
		public int getIntProperty() {
			return intProperty;
		}
		/** Returns the intObjProperty field.
		 * @return The intObjProperty field.
		 */
		public Integer getIntObjProperty() {
			return intObjProperty;
		}
		/** Returns the longProperty field.
		 * @return The longProperty field.
		 */
		public long getLongProperty() {
			return longProperty;
		}
		/** Returns the longObjProperty field.
		 * @return The longObjProperty field.
		 */
		public Long getLongObjProperty() {
			return longObjProperty;
		}
		/** Returns the shortProperty field.
		 * @return The shortProperty field.
		 */
		public short getShortProperty() {
			return shortProperty;
		}
		/** Returns the shortObjProperty field.
		 * @return The shortObjProperty field.
		 */
		public Short getShortObjProperty() {
			return shortObjProperty;
		}
		/** Returns the byteProperty field.
		 * @return The byteProperty field.
		 */
		public byte getByteProperty() {
			return byteProperty;
		}
		/** Returns the byteObjProperty field.
		 * @return The byteObjProperty field.
		 */
		public Byte getByteObjProperty() {
			return byteObjProperty;
		}
		/** Returns the floatProperty field.
		 * @return The floatProperty field.
		 */
		public float getFloatProperty() {
			return floatProperty;
		}
		/** Returns the floatObjProperty field.
		 * @return The floatObjProperty field.
		 */
		public Float getFloatObjProperty() {
			return floatObjProperty;
		}
		/** Returns the doubleProperty field.
		 * @return The doubleProperty field.
		 */
		public double getDoubleProperty() {
			return doubleProperty;
		}
		/** Returns the doubleObjProperty field.
		 * @return The doubleObjProperty field.
		 */
		public Double getDoubleObjProperty() {
			return doubleObjProperty;
		}
		/** Returns the boolProperty field.
		 * @return The boolProperty field.
		 */
		public boolean isBoolProperty() {
			return boolProperty;
		}
		/** Returns the boolObjProperty field.
		 * @return The boolObjProperty field.
		 */
		public Boolean getBoolObjProperty() {
			return boolObjProperty;
		}
		/** Returns the stringProperty field.
		 * @return The stringProperty field.
		 */
		public String getStringProperty() {
			return stringProperty;
		}
		/** Returns the bigIntegerProperty field.
		 * @return The bigIntegerProperty field.
		 */
		public BigInteger getBigIntegerProperty() {
			return bigIntegerProperty;
		}
		/** Returns the bigDecimalProperty field.
		 * @return The bigDecimalProperty field.
		 */
		public BigDecimal getBigDecimalProperty() {
			return bigDecimalProperty;
		}
		/** Returns the pathProperty field.
		 * @return The pathProperty field.
		 */
		public Path getPathProperty() {
			return pathProperty;
		}
		/** Returns the fileProperty field.
		 * @return The fileProperty field.
		 */
		public File getFileProperty() {
			return fileProperty;
		}
		/** Returns the urlProperty field.
		 * @return The urlProperty field.
		 */
		public URL getUrlProperty() {
			return urlProperty;
		}
	}

	/** Basically the same class, except with getters.
	 */
	public static class BBean extends ABean {
		private int numberOfSetterInvocations;

		/** Sets the value of the intProperty field.
		 * @param pIntProperty The new value of the intProperty field.
		 */
		public void setIntProperty(int pIntProperty) {
			++numberOfSetterInvocations;
			intProperty = pIntProperty;
		}
		/** Sets the value of the intObjProperty field.
		 * @param pIntObjProperty The new value of the intObjProperty field.
		 */
		public void setIntObjProperty(Integer pIntObjProperty) {
			++numberOfSetterInvocations;
			intObjProperty = pIntObjProperty;
		}
		/** Sets the value of the longProperty field.
		 * @param pLongProperty The new value of the longProperty field.
		 */
		public void setLongProperty(long pLongProperty) {
			++numberOfSetterInvocations;
			longProperty = pLongProperty;
		}
		/** Sets the value of the longObjProperty field.
		 * @param pLongObjProperty The new value of the longObjProperty field.
		 */
		public void setLongObjProperty(Long pLongObjProperty) {
			++numberOfSetterInvocations;
			longObjProperty = pLongObjProperty;
		}
		/** Sets the value of the shortProperty field.
		 * @param pShortProperty The new value of the shortProperty field.
		 */
		public void setShortProperty(short pShortProperty) {
			++numberOfSetterInvocations;
			shortProperty = pShortProperty;
		}
		/** Sets the value of the shortObjProperty field.
		 * @param pShortObjProperty The new value of the shortObjProperty field.
		 */
		public void setShortObjProperty(Short pShortObjProperty) {
			++numberOfSetterInvocations;
			shortObjProperty = pShortObjProperty;
		}
		/** Sets the value of the byteProperty field.
		 * @param pByteProperty The new value of the byteProperty field.
		 */
		public void setByteProperty(byte pByteProperty) {
			++numberOfSetterInvocations;
			byteProperty = pByteProperty;
		}
		/** Sets the value of the byteObjProperty field.
		 * @param pByteObjProperty The new value of the byteObjProperty field.
		 */
		public void setByteObjProperty(Byte pByteObjProperty) {
			++numberOfSetterInvocations;
			byteObjProperty = pByteObjProperty;
		}
		/** Sets the value of the floatProperty field.
		 * @param pFloatProperty The new value of the floatProperty field.
		 */
		public void setFloatProperty(float pFloatProperty) {
			++numberOfSetterInvocations;
			floatProperty = pFloatProperty;
		}
		/** Sets the value of the floatObjProperty field.
		 * @param pFloatObjProperty The new value of the floatObjProperty field.
		 */
		public void setFloatObjProperty(Float pFloatObjProperty) {
			++numberOfSetterInvocations;
			floatObjProperty = pFloatObjProperty;
		}
		/** Sets the value of the doubleProperty field.
		 * @param pDoubleProperty The new value of the doubleProperty field.
		 */
		public void setDoubleProperty(double pDoubleProperty) {
			doubleProperty = pDoubleProperty;
			++numberOfSetterInvocations;
		}
		/** Sets the value of the doubleObjProperty field.
		 * @param pDoubleObjProperty The new value of the doubleObjProperty field.
		 */
		public void setDoubleObjProperty(Double pDoubleObjProperty) {
			++numberOfSetterInvocations;
			doubleObjProperty = pDoubleObjProperty;
		}
		/** Sets the value of the boolProperty field.
		 * @param pBoolProperty The new value of the boolProperty field.
		 */
		public void setBoolProperty(boolean pBoolProperty) {
			++numberOfSetterInvocations;
			boolProperty = pBoolProperty;
		}
		/** Sets the value of the boolObjProperty field.
		 * @param pBoolObjProperty The new value of the boolObjProperty field.
		 */
		public void setBoolObjProperty(Boolean pBoolObjProperty) {
			++numberOfSetterInvocations;
			boolObjProperty = pBoolObjProperty;
		}
		/** Sets the value of the stringProperty field.
		 * @param pStringProperty The new value of the stringProperty field.
		 */
		public void setStringProperty(String pStringProperty) {
			++numberOfSetterInvocations;
			stringProperty = pStringProperty;
		}
		/** Sets the value of the bigIntegerProperty field.
		 * @param pBigIntegerProperty The new value of the bigIntegerProperty field.
		 */
		public void setBigIntegerProperty(BigInteger pBigIntegerProperty) {
			++numberOfSetterInvocations;
			bigIntegerProperty = pBigIntegerProperty;
		}
		/** Sets the value of the bigDecimalProperty field.
		 * @param pBigDecimalProperty The new value of the bigDecimalProperty field.
		 */
		public void setBigDecimalProperty(BigDecimal pBigDecimalProperty) {
			++numberOfSetterInvocations;
			bigDecimalProperty = pBigDecimalProperty;
		}
		/** Sets the value of the pathProperty field.
		 * @param pPathProperty The new value of the pathProperty field.
		 */
		public void setPathProperty(Path pPathProperty) {
			++numberOfSetterInvocations;
			pathProperty = pPathProperty;
		}
		/** Sets the value of the fileProperty field.
		 * @param pFileProperty The new value of the fileProperty field.
		 */
		public void setFileProperty(File pFileProperty) {
			++numberOfSetterInvocations;
			fileProperty = pFileProperty;
		}
		/** Sets the value of the urlProperty field.
		 * @param pUrlProperty The new value of the urlProperty field.
		 */
		public void setUrlProperty(URL pUrlProperty) {
			++numberOfSetterInvocations;
			urlProperty = pUrlProperty;
		}
		/**
		 * Returns the number of setter invocations.
		 * Looking at this number ensures, that properties have been injected via setters,
		 * and not via field access.
		 * @return The number of setter invocations.
		 */
		public int getNumberOfSetterInvocations() {
			return numberOfSetterInvocations;
		}
	}

	/**
	 * Test creating an instance of {@link ABean}. Properties are being specified as a map,
	 * and injected using field access.
	 */
	@Test
	public void testCreateUsingFieldAccessAndMap() {
		final ABean aBean = new DefaultInstantiator().newInstance(Thread.currentThread().getContextClassLoader(), ABean.class.getName(),
				                                                  getBeanPropertiesAsMap());
		validate(aBean);
	}

	/**
	 * Test creating an instance of {@link ABean}. Properties are being specified as an array,
	 * and injected using field access.
	 */
	@Test
	public void testCreateUsingFieldAccessAndArray() {
		final ABean aBean = new DefaultInstantiator().newInstance(Thread.currentThread().getContextClassLoader(), ABean.class.getName(),
				                                                  getBeanPropertiesAsArray());
		validate(aBean);
	}

	/**
	 * Test creating an instance of {@link ABean}. Properties are being specified as a map, and injected using method access (setters).
	 */
	@Test
	public void testCreateUsingMethodAccessAndMap() {
		final BBean bBean = new DefaultInstantiator().newInstance(Thread.currentThread().getContextClassLoader(), BBean.class.getName(),
				                                                  getBeanPropertiesAsMap());
		validate(bBean);
		assertEquals(20, bBean.getNumberOfSetterInvocations());
	}

	/**
	 * Test creating an instance of {@link ABean}. Properties are being specified as an array, and injected using method access (setters).
	 */
	@Test
	public void testCreateUsingMethodAccessAndArray() {
		final BBean bBean = new DefaultInstantiator().newInstance(Thread.currentThread().getContextClassLoader(), BBean.class.getName(),
				                                                  getBeanPropertiesAsArray());
		validate(bBean);
		assertEquals(20, bBean.getNumberOfSetterInvocations());
	}

	/** Validates, that the given bean has the expected property values.
	 * @param pBean The bean, that is being tested.
	 */
	protected void validate(ABean pBean) {
		assertEquals(23, pBean.getIntProperty());
		assertEquals(24, pBean.getIntObjProperty().intValue());
		assertEquals(25l, pBean.getLongProperty());
		assertEquals(26l, pBean.getLongObjProperty().longValue());
		assertEquals((short) 27, pBean.getShortProperty());
		assertEquals((short) 28, pBean.getShortObjProperty().shortValue());
		assertEquals((byte) 48, pBean.getByteProperty());
		assertEquals((byte) 49, pBean.getByteObjProperty().byteValue());
		assertEquals(0.5, pBean.getFloatProperty(), 0.0001);
		assertEquals(0.6, pBean.getFloatObjProperty().floatValue(), 0.0001);
		assertEquals(0.7, pBean.getDoubleProperty(), 0.0001);
		assertEquals(0.8, pBean.getDoubleObjProperty().doubleValue(), 0.0001);
		assertEquals(STR_EXAMPLE, pBean.getStringProperty());
		assertEquals(new BigInteger(BIGINT_EXAMPLE), pBean.getBigIntegerProperty());
		assertEquals(new BigDecimal(BIGDEC_EXAMPLE), pBean.getBigDecimalProperty());
		assertEquals(Paths.get("./README.txt"), pBean.getPathProperty());
		assertEquals("pom.xml", pBean.getFileProperty().getName());
		assertEquals("http://127.0.0.1:8080/", pBean.getUrlProperty().toExternalForm());
	}

	private static final String STR_EXAMPLE = "The quick, brown fox jumps over the lazy dog.";
	private final String BIGINT_EXAMPLE = String.valueOf(Long.MAX_VALUE) + String.valueOf(Long.MAX_VALUE);
	private final String BIGDEC_EXAMPLE = BIGINT_EXAMPLE + ".0";

	/**
	 * Returns the expected bean properties as a string array.
	 * @return The expected set of bean properties.
	 */
	protected String[] getBeanPropertiesAsArray() {
		return new String[]{ "intProperty", "23",
				"intObjProperty", "24", "longProperty", "25",
				"longObjProperty", "26", "shortProperty", "27",
				"shortObjProperty", "28", "byteProperty", "48",
				"byteObjProperty", "49", "floatProperty", "0.5",
				"floatObjProperty", "0.6", "doubleProperty", "0.7",
				"doubleObjProperty", "0.8", "boolProperty", "true",
				"boolObjProperty", "false",
				"stringProperty", STR_EXAMPLE,
				"bigIntegerProperty", BIGINT_EXAMPLE,
				"bigDecimalProperty", BIGDEC_EXAMPLE,
				"pathProperty", "./README.txt",
				"fileProperty", "./pom.xml",
				"urlProperty", "http://127.0.0.1:8080/"
				};
	}

	/**
	 * Returns the expected bean properties as a map.
	 * @return The expected set of bean properties.
	 */
	protected Map<String,String> getBeanPropertiesAsMap() {
		final Map<String,String> map = new HashMap<>();
		final String[] propertiesArray = getBeanPropertiesAsArray();
		for (int i = 0;  i < propertiesArray.length;  i += 2) {
			map.put(propertiesArray[i], propertiesArray[i+1]);
		}
		return map;
	}
}
