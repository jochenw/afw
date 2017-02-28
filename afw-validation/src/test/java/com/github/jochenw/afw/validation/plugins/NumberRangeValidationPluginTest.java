package com.github.jochenw.afw.validation.plugins;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.github.jochenw.afw.validation.api.NumberRange;


public class NumberRangeValidationPluginTest extends AbstractValidationPluginTestCase {
	private static final class LtBigDecimalFieldBean {
		@NumberRange(code="LTBD01", lt="5")
		private BigDecimal value;
	}
	private static final class LtBigIntegerFieldBean {
		@NumberRange(code="LTBI01", lt="5")
		private BigInteger value;
	}
	private static final class LtDoubleFieldBean {
		@NumberRange(code="LTD01", lt="5.0")
		private double value;
	}
	private static final class LtLongFieldBean {
		@NumberRange(code="LTLF01", lt="5")
		private long value;
	}
	private static final class GtLongFieldBean {
		@NumberRange(code="GT01", gt="5")
		private long value;
	}
	private static final class GeLongFieldBean {
		@NumberRange(code="GE01", ge="6")
		private long value;
	}
	private static final class LeLongFieldBean {
		@NumberRange(code="LE01", le="4")
		private long value;
	}
	private static final class LtLongMethodBean {
		private long value;

		@NumberRange(code="LTLM01", lt="5")
		public Long getValue() { return Long.valueOf(value); }
    }
	private static final class GtLongMethodBean {
		private long value;

		@NumberRange(code="GT01", gt="5")
		public Long getValue() { return Long.valueOf(value); }
	}
	private static final class GeLongMethodBean {
		private long value;

		@NumberRange(code="GE01", ge="6")
		public Long getValue() { return Long.valueOf(value); }
	}
	private static final class LeLongMethodBean {
		private long value;

		@NumberRange(code="LE01", le="4")
		public Long getValue() { return Long.valueOf(value); }
	}
	
	@Test
	public void testLt() {
		final LtLongFieldBean ltFBean = new LtLongFieldBean();
		final LtLongMethodBean ltMBean = new LtLongMethodBean();
		final LtDoubleFieldBean ltDoubleBean = new LtDoubleFieldBean();
		final LtBigDecimalFieldBean ltBDFieldBean = new LtBigDecimalFieldBean();
		final LtBigIntegerFieldBean ltBIFieldBean = new LtBigIntegerFieldBean();
		ltFBean.value = 3;
		ltMBean.value = 3;
		ltDoubleBean.value = 3.0;
		ltBDFieldBean.value = new BigDecimal("3.0");
		ltBIFieldBean.value = new BigInteger("3");
		assertValid(ltFBean);
		assertValid(ltMBean);
		assertValid(ltDoubleBean);
		assertValid(ltBDFieldBean);
		assertValid(ltBIFieldBean);
		ltFBean.value = 5;
		ltMBean.value = 5;
		ltDoubleBean.value = Double.parseDouble("5.0");
		ltBDFieldBean.value = new BigDecimal("5.0");
		ltBIFieldBean.value = new BigInteger("5");
		assertInvalid(ltFBean, "LTLF01");
		assertInvalid(ltMBean, "LTLM01");
		assertInvalid(ltDoubleBean, "LTD01");
		assertInvalid(ltBDFieldBean, "LTBD01");
		assertInvalid(ltBIFieldBean, "LTBI01");
		ltFBean.value = 4;
		ltMBean.value = 4;
		ltDoubleBean.value = 4.0;
		ltBDFieldBean.value = new BigDecimal("4.0");
		ltBIFieldBean.value = new BigInteger("4");
		assertValid(ltFBean);
		assertValid(ltMBean);
		assertValid(ltDoubleBean);
		assertValid(ltBDFieldBean);
		assertValid(ltBIFieldBean);
		ltFBean.value = 7;
		ltMBean.value = 7;
		ltDoubleBean.value = 7.0;
		ltBDFieldBean.value = new BigDecimal("7.0");
		ltBIFieldBean.value = new BigInteger("7");
		assertInvalid(ltFBean, "LTLF01");
		assertInvalid(ltMBean, "LTLM01");
		assertInvalid(ltDoubleBean, "LTD01");
		assertInvalid(ltBDFieldBean, "LTBD01");
		assertInvalid(ltBIFieldBean, "LTBI01");
	}

	@Test
	public void testGt() {
		final GtLongFieldBean gtFBean = new GtLongFieldBean();
		final GtLongMethodBean gtMBean = new GtLongMethodBean();
		gtFBean.value = 7;
		gtMBean.value = 7;
		assertValid(gtFBean);
		assertValid(gtMBean);
		gtFBean.value = 5;
		gtMBean.value = 5;
		assertInvalid(gtFBean, "GT01");
		assertInvalid(gtMBean, "GT01");
		gtFBean.value = 6;
		gtMBean.value = 6;
		assertValid(gtFBean);
		assertValid(gtMBean);
		gtFBean.value = 4;
		gtMBean.value = 4;
		assertInvalid(gtFBean, "GT01");
		assertInvalid(gtMBean, "GT01");
	}

	@Test
	public void testLe() {
		final LeLongFieldBean leFBean = new LeLongFieldBean();
		final LeLongMethodBean leMBean = new LeLongMethodBean();
		leFBean.value = 3;
		leMBean.value = 3;
		assertValid(leFBean);
		assertValid(leMBean);
		leFBean.value = 5;
		leMBean.value = 5;
		assertInvalid(leFBean, "LE01");
		assertInvalid(leMBean, "LE01");
		leFBean.value = 4;
		leMBean.value = 4;
		assertValid(leFBean);
		assertValid(leMBean);
		leFBean.value = 6;
		leMBean.value = 6;
		assertInvalid(leFBean, "LE01");
		assertInvalid(leMBean, "LE01");
	}

	@Test
	public void testGe() {
		final GeLongFieldBean geFBean = new GeLongFieldBean();
		final GeLongMethodBean geMBean = new GeLongMethodBean();
		geFBean.value = 7;
		geMBean.value = 7;
		assertValid(geFBean);
		assertValid(geMBean);
		geFBean.value = 5;
		geMBean.value = 5;
		assertInvalid(geFBean, "GE01");
		assertInvalid(geMBean, "GE01");
		geFBean.value = 6;
		geMBean.value = 6;
		assertValid(geFBean);
		assertValid(geMBean);
		geFBean.value = 4;
		geMBean.value = 4;
		assertInvalid(geFBean, "GE01");
		assertInvalid(geMBean, "GE01");
	}
}
