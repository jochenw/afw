package com.github.jochenw.afw.core.template;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

import com.github.jochenw.afw.core.util.Exceptions;


/**
 * Interface of a template engine. The intention here is, to provide different implementations,
 * based on real engines. However, there is also a standalone implementation, the
 * {@link SimpleTemplateEngine}.
 * 
 * The interface is based on the assumption, that template processing consists of two steps:
 * <ol>
 *   <li>The template (a text file) is bein read, parsed, and compiled into an executable
 *     object, the {@link ITemplateEngine.Template}.</li>
 *   <li>The executable object is being invoked one, or more times, with a suitable data
 *     object (the model) and written out.</li>
 * </ol>
 * 
 * @param <M> The data model type.
 */
public interface ITemplateEngine<M> {
	/** Interface of a compiled template.
	 * @param <T> The data model type.
	 */
	public interface Template<T> {
		/** Called to write out the template to a given output byte stream.
		 * @param pModel The data model.
		 * @param pOut The target stream.
		 * @param pCharset The character set to use when converting the text to a byte stream.
		 * @see #write(Object, Writer)
		 */
		public default void write(T pModel, OutputStream pOut, Charset pCharset) {
			try (Writer w = new OutputStreamWriter(pOut, pCharset)) {
				write(pModel, w);
			} catch (IOException e) {
				throw Exceptions.show(e);
			}
		}
		/** Called to write out the template to a given output character stream.
		 * @param pModel The data model.
		 * @param pWriter The target character stream.
		 */
		public void write(T pModel, Writer pWriter);
	}

	/** Called to read the template text, and convert it into an executable template object.
	 * @param pTemplateText The templates text.
	 * @return The compiled template.
	 */
	public default Template<M> compile(String pTemplateText) {
		return getTemplate(new StringReader(pTemplateText));
	}
	/** Called to read the template text from the given URI, and convert it into an executable template object.
	 * @param pUri The URI, from which to read the template text.
	 * @return The compiled template.
	 */
	public Template<M> getTemplate(String pUri);
	/** Called to read the template text from the given {@link Reader}, and convert it into an executable template object.
	 * @param pReader The {@link Reader}, from which to read the template text.
	 * @return The compiled template.
	 */
	public Template<M> getTemplate(Reader pReader);
}
