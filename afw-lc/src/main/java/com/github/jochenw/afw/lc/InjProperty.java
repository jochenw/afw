package com.github.jochenw.afw.lc;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface InjProperty {
	String id() default "";
	String defaultValue() default "";
}
