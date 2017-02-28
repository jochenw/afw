package com.github.jochenw.afw.validation.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ METHOD, FIELD, PARAMETER })
@Documented
public @interface NumberRange {
	String code();
	boolean nullable() default false;
	String le() default NotNull.NO_DEFAULT;
	String lt() default NotNull.NO_DEFAULT;
	String ge() default NotNull.NO_DEFAULT;
	String gt() default NotNull.NO_DEFAULT;
}
