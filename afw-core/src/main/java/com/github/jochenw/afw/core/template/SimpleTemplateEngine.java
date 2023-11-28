package com.github.jochenw.afw.core.template;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.jochenw.afw.core.el.DefaultPropertyResolver;
import com.github.jochenw.afw.core.el.ElEvaluator;
import com.github.jochenw.afw.core.el.ElReader;
import com.github.jochenw.afw.core.el.PropertyResolver;
import com.github.jochenw.afw.core.util.Exceptions;


/** Simple, standalone, implementation of {@link ITemplateEngine}. The main advantage
 * is, that you don't need Freemarker, or whatever, else.
 */
public class SimpleTemplateEngine implements ITemplateEngine<Map<String,Object>> {
	private PropertyResolver propertyResolver;
	private ElReader elReader;
	private ElEvaluator elEvalutor;
	private ClassLoader classLoader;
	private Charset templateCharset;
	private String uri;

	/**
	 * Creates a new instance with the given {@link PropertyResolver}, and the given
	 * {@link ElEvaluator}.
	 * @param pPropertyResolver The property resolver to use for 
	 * @param pEvaluator The evaluator, which is being used to resolve property values.
	 */
	public SimpleTemplateEngine(PropertyResolver pPropertyResolver, ElEvaluator pEvaluator) {
		propertyResolver = pPropertyResolver;
		elEvalutor = pEvaluator;
		elReader = new ElReader();
	}

	/**
	 * Creates a new instance with default values for property resolver, and el evaluator.
	 * {@link ElEvaluator}.
	 * @see DefaultPropertyResolver
	 * @see ElEvaluator
	 */
	public SimpleTemplateEngine() {
		this(new DefaultPropertyResolver(), null);
		elEvalutor = new ElEvaluator(propertyResolver);
		elReader = new ElReader();
	}

	/**
	 * Returns the {@link ClassLoader} to use when loading resources.
	 * @return The {@link ClassLoader} to use when loading resources.
	 * @see #setClassLoader(ClassLoader)
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	/**
	 * Sets the {@link ClassLoader} to use when loading resources.
	 * @param pClassLoader The {@link ClassLoader} to use when loading resources.
	 * @see #getClassLoader()
	 */
	public void setClassLoader(ClassLoader pClassLoader) {
		classLoader = pClassLoader;
	}

	/**
	 * Returns the {@link Charset} to use when reading templates as text files.
	 * @return The {@link Charset} to use when reading templates as text files.
	 * @see #setTemplateCharset(Charset)
	 */
	public Charset getTemplateCharset() {
		return templateCharset;
	}

	/**
	 * Sets the {@link Charset} to use when reading templates as text files.
	 * @param pTemplateCharset The {@link Charset} to use when reading templates as text files.
	 * @see #getTemplateCharset()
	 */
	public void setTemplateCharset(Charset pTemplateCharset) {
		templateCharset = pTemplateCharset;
	}

	/** Returns the {@link PropertyResolver} to use for interpolation of values.
	 * @return The {@link PropertyResolver} to use for interpolation of values.
	 * @see #setPropertyResolver(PropertyResolver)
	 */
	public PropertyResolver getPropertyResolver() {
		return propertyResolver;
	}

	/** Sets the {@link PropertyResolver} to use for interpolation of values.
	 * @param pPropertyResolver The {@link PropertyResolver} to use for interpolation of values.
	 * @see #getPropertyResolver()
	 */
	public void setPropertyResolver(PropertyResolver pPropertyResolver) {
		propertyResolver = pPropertyResolver;
	}

	/** Returns the {@link ElEvaluator} to use for evaluation of EL expressions.
	 * @return The {@link ElEvaluator} to use for evaluation of EL expressions.
	 * @see #setElEvalutor(ElEvaluator)
	 */
	public ElEvaluator getElEvalutor() {
		return elEvalutor;
	}

	/** Sets the {@link ElEvaluator} to use for evaluation of EL expressions.
	 * @param pElEvaluator The {@link ElEvaluator} to use for evaluation of EL expressions.
	 * @see #getElEvalutor()
	 */
	public void setElEvalutor(ElEvaluator pElEvaluator) {
		elEvalutor = pElEvaluator;
	}

	/** Returns the template URI.
	 * @return The template URI.
	 * @see #setUri(String)
	 */
	public String getUri() {
		return uri;
	}

	/** Sets the template URI.
	 * @param pUri The template URI.
	 * @see #getUri()
	 */
	public void setUri(String pUri) {
		uri = pUri;
	}

	/** Returns the {@link ElReader} to use for parsing EL expressions.
	 * @return The {@link ElReader} to use for parsing EL expressions.
	 */
	public ElReader getElReader() {
		return elReader;
	}

	/** Sets the {@link ElReader} to use for parsing EL expressions.
	 * @param pElReader The {@link ElReader} to use for parsing EL expressions.
	 */
	public void setElReader(ElReader pElReader) {
		elReader = pElReader;
	}

	@Override
	public Template<Map<String, Object>> getTemplate(String pUri) {
		final URL url = getClassLoader().getResource(pUri);
		if (url == null) {
			throw new IllegalArgumentException("Unable to locate template: " + pUri);
		}
		try (final InputStream in = url.openStream();
			 final Reader r = new InputStreamReader(in, getTemplateCharset())) {
			return getTemplate(r);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public Template<Map<String, Object>> getTemplate(Reader pReader) {
		final List<String> lines = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(pReader)) {
			for (;;) {
				final String line = br.readLine();
				if (line == null) {
					break;
				}
				lines.add(line);
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		return compile(lines);
	}

	/** Called to compile the given template lines.
	 * @param pLines The template lines, that are being compiled.
	 * @return The created template.
	 */
	protected Template<Map<String,Object>> compile(List<String> pLines) {
		final String[] array = pLines.toArray(new String[pLines.size()]);
		return new SimpleTemplateCompiler<Map<String,Object>>(propertyResolver, elReader, elEvalutor, uri).compile(array);
	}

	/** Creates a new instance with default settings: A {@link DefaultPropertyResolver},
	 * the current threads {@link Thread#getContextClassLoader()
	 * context class loader}, and the "UTF-8" character set.
	 * @return The created instance, ready to use.
	 */
	public static SimpleTemplateEngine newInstance() {
		final PropertyResolver pr = new DefaultPropertyResolver();
		final ElEvaluator evaluator = new ElEvaluator(pr);
		final ElReader reader = new ElReader();
		final SimpleTemplateEngine ste = new SimpleTemplateEngine();
		ste.setElEvalutor(evaluator);
		ste.setElReader(reader);
		ste.setPropertyResolver(pr);
		ste.setTemplateCharset(StandardCharsets.UTF_8);
		ste.setClassLoader(Thread.currentThread().getContextClassLoader());
		return ste;
	}
}
