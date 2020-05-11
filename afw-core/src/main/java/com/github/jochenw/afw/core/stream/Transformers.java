package com.github.jochenw.afw.core.stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

/**
 * Utility class for working with transformers.
 */
public class Transformers {
	private static final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();

	/**
	 * Creates a new {@link TransformerHandler} with default settings.
	 * @return The newly created {@link TransformerHandler}, which has default settings.
	 */
	public static TransformerHandler newTransformerHandler() {
		try {
			return stf.newTransformerHandler();
		} catch (TransformerException te) {
			throw new IllegalStateException("Failed to create TransformerHandler: " + te.getMessage(), te);
		}
	}

	/**
	 * Creates a new {@link TransformerHandler} with default settings, and the given
	 * {@link Result}.
	 * 
	 * Example: Consider the following snippet:
	 * <pre>
	 *   OutputStream out;
	 *   final TransformerHandler th = Transformers.newTransformerHandler(
	 *           new StreamResult(out));
	 * </pre>
	 * This snippet would be equivalent to:
	 * <pre>
	 *   OutputStream out;
	 *   SaxTransformerFactory stf =
	 *       (SaxTransformerFactory) TransformerFactory.newInstance();
	 *   TransformerHandler th = stf.newTransformerHandler();
	 *   Transformer t = th.getTransformer();
	 *   th.setResult(new StreamResult(out));
	 * </pre>
	 *
	 * @param pResult The created transformer handlers {@link Result}.
	 * @return The newly created {@link TransformerHandler}, which has default settings,
	 * and the given {@link Result}.
	 */
	public static TransformerHandler newTransformerHandler(Result pResult) {
		try {
			final TransformerHandler th = stf.newTransformerHandler();
			th.setResult(pResult);
			return th;
		} catch (TransformerException te) {
			throw new IllegalStateException("Failed to create TransformerHandler: " + te.getMessage(), te);
		}
	}

	/**
	 * <p>Creates a new {@link TransformerHandler} with the given output properties,
	 * and the given {@link Result}.</p>
	 * 
	 * Example: Consider the following snippet:
	 * <pre>
	 *   OutputStream out;
	 *   final TransformerHandler th = Transformers.newTransformerHandler(
	 *           new StreamResult(out), OutputKeys.INDENT, "yes",
	 *           OutputKeys.OMIT_XML_DECLARATION, "yes");
	 * </pre>
	 * This snippet would be equivalent to:
	 * <pre>
	 *   OutputStream out;
	 *   SaxTransformerFactory stf =
	 *       (SaxTransformerFactory) TransformerFactory.newInstance();
	 *   TransformerHandler th = stf.newTransformerHandler();
	 *   Transformer t = th.getTransformer();
	 *   th.setResult(new StreamResult(out));
	 *   t.setOutputProperty(OutputKeys.INDENT, "yes");
	 *   t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	 * </pre>
	 *
	 * @param pResult The result, to which the transformer handlers output is being
	 * written.
	 * @param pOutputProperties An array of name/value pairs, which are being
	 *   configured on the transformer handler.
	 * @return The newly created {@link TransformerHandler}, which has default settings,
	 * and the given {@link Result}.
	 */
	public static TransformerHandler newTransformerHandler(@Nonnull Result pResult, @Nullable String... pOutputProperties) {
		try {
			final TransformerHandler th = stf.newTransformerHandler();
			th.setResult(pResult);
			if (pOutputProperties != null) {
				final Transformer t = th.getTransformer();
				for (int i = 0;  i < pOutputProperties.length;  i += 2) {
					final String outputProperty = pOutputProperties[i];
					final String value = pOutputProperties[i+1];
					t.setOutputProperty(outputProperty, value);
				}
			}
			return th;
		} catch (TransformerException te) {
			throw new IllegalStateException("Failed to create TransformerHandler: " + te.getMessage(), te);
		}
	}
}
