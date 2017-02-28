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
public @interface NotNull {
	public static final String NO_DEFAULT = "";

	String code();
	String defaultValue() default NO_DEFAULT;
}
