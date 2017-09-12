package com.github.jochenw.afw.validation.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@Documented
public @interface Length {
	String code() default "";
	boolean nullable() default false;
	int minExclusive() default -1;
	int minInclusive() default -1;
	int maxExclusive() default -1;
	int maxInclusive() default -1;
}
