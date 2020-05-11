package com.github.jochenw.afw.core.stream;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** This annotations presence indicates, that an object can be serialzed to, or
 * deserialized from a data stream.
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented()
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Streamable {
	/** The streamable objects, or fields id. The id's interpretation is left to
	 * the {@link StreamReader}, or {@link StreamWriter}.
	 * @return he streamable objects, or fields id. The id's interpretation is left to
	 * the {@link StreamReader}, or {@link StreamWriter}.
	 */
	public String id() default "";
	/** Whether to display the value in a terse manner. The attributes interpretation
	 * is left to the {@link StreamReader}, or {@link StreamWriter}. For example, the
	 * {@link XmlStreamWriter} will use the value to decide, whether a value is
	 * being written as an XML attribute, or a child element. The {@link JsonStreamWriter},
	 * on the other hand, will ignore this value.
	 * @return Returns, whether to display the value in a terse manner.
	 */
	public boolean terse() default false;
}
