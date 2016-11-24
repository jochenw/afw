package com.github.jochenw.afw.lc.db;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Migration {
	String version() default "";
	String description() default "Undocumented migration";
}
