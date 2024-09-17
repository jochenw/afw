package com.github.jochenw.afw.core.template;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.template.ITemplateEngine.Template;


/** Default implementation of {@link Template}: Simple standalone interpreter.
 * @param <M> Type of the data model.
 */
public class SimpleTemplate<M> implements Template<M> {
	/** Internal context object of the {@link SimpleTemplate}, which represents the template
	 * interpreters internal state.
	 * @param <T> Type of the data model.
	 */
	public static class Context<T> {
		/** Creates a new instance,
		 */
		public Context() {}

		@SuppressWarnings("null")
		private T model;
		private PrintWriter writer;

		/** Returns the data model, on which the template is being applied.
		 * @return The data model.
		 */
		public T getModel() {
			return model;
		}

		/** Sets the data model, on which the template is being applied.
		 * @param pModel The data model.
		 */
		public void setModel(T pModel) {
			model = pModel;
		}

		/** Returns the output stream, to which the evaluated template is being written.
		 * @return The output stream, to which the evaluated template is being written.
		 */
		public PrintWriter getWriter() {
			return writer;
		}

		/** Sets the output stream, to which the evaluated template is being written.
		 * @param pWriter The output stream, to which the evaluated template is being written.
		 */
		public void setWriter(PrintWriter pWriter) {
			writer = pWriter;
		}

		/** Writes the given string to the output stream.
		 * @param pValue A string, which is being written to the output stream.
		 */
		public void write(String pValue) {
			writer.write(pValue);
		}

		/** Writes the given string to the output stream, appending a line terminator.
		 * @param pValue A string, which is being written to the output stream, appending a line terminator.
		 */
		public void writeln(String pValue) {
			writer.write(pValue);
			writeln();
		}

		/** Writes a line terminator to the output stream.
		 */
		public void writeln() {
			writer.write("\n");
		}
	}

	private final Iterable<Consumer<Context<M>>> lines;

	/** Creates a new instance, with the given context consumers.
	 * @param pLines The context consumers. (Basically, the parsed template.
	 */
	public SimpleTemplate(Iterable<Consumer<Context<M>>> pLines) {
		lines = pLines;
	}
	
	@Override
	public void write(M pModel, Writer pWriter) {
		final Context<M> ctx = new Context<M>();
		ctx.setModel(pModel);
		final PrintWriter pw = new PrintWriter(pWriter);
		ctx.setWriter(pw);
		lines.forEach((l) -> l.accept(ctx));
		pw.flush();
	}
}
