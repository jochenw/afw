package com.github.jochenw.afw.core.cli;

import java.util.NoSuchElementException;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.cli.Cli.Context;
import com.github.jochenw.afw.core.cli.Cli.UsageException;
import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.rflct.ISetter;


/** An {@link Option} object represents a configurable option value.
 * @param <B> Type of the options bean.
 * @param <O> Type of the option value.
 */
public abstract class Option<B,O> {
	private final @NonNull Cli<B> cli;
	private final @NonNull Class<O> type;
	private final @NonNull String primaryName;
	private final @NonNull String[] secondaryNames;
	private FailableBiConsumer<Context<B>,O,?> argsHandler;
	private String defaultValue;
	private boolean required;
	private boolean immutable;

	/** Creates a new instance.
	 * @param pCli The {@link Cli}, that creates this option.
	 * @param pPrimaryName The options primary name. Always non-null.
	 * @param pSecondaryNames The options secondary names, if any.
	 * @param pType The option values type.
	 */
	protected Option(@NonNull Cli<B> pCli, @NonNull Class<O> pType, @NonNull String pPrimaryName,
			         @NonNull String[] pSecondaryNames) {
		cli = pCli;
		type = pType;
		primaryName = pPrimaryName;
		secondaryNames = pSecondaryNames;
	}

	/** Asserts, that this option is still mutable.
	 * @throws IllegalStateException The {@link #end()} method
	 *   has already been invoked, and this object is no
	 *   longer mutable.
	 */
	protected void assertMutable() {
		if (immutable) {
			throw new IllegalStateException("The end() method has already been"
					+ " invoked, and this object is no longer mutable,");
		}
	}

	boolean isImmutable() {
		return immutable;
	}

	/** Returns the {@link Cli}, that created this option.
	 * @return The {@link Cli}, that created this option.
	 */
	public Cli<B> getCli() { return cli; }
	/** Returns the option values type.
	 * @return The option values type.
	 * @see #end()
	 */
	public Class<O> getType() { return type; }
	/** Returns the options primary name. Never null.
	 * @return The options primary name. Never null.
	 */
	public @NonNull String getPrimaryName() { return primaryName; }
	/** Returns the options secondary names, if any, or null.
	 * @return The options secondary names, if any, or null.
	 */
	public @NonNull String[] getSecondaryNames() { return secondaryNames; }
	/** Returns, whether an option value is required. The only exception is
	 * a boolean value. (Simply specifying the option name sets a
	 * boolean option value to true.) For all non-boolean
	 * options, this will return false.
	 * @return True, if an option value is required, otherwise false.
	 */
	public boolean isNullValueValid() { return false; }

	/** Called to convert the value string into the actual
	 * option value.
	 * @param pOptValue The actual value string, or null,
	 *   if the default value should be used.
	 * @return This option.
	 * @throws UsageException The value string is invalid,
	 *   and cannot be converted.
	 */
	public abstract O getValue(String pOptValue) throws UsageException;

	/** Returns the options argument handler. The argument handler will be
	 * invoked, as soon as the actual option value is available.
	 * @return The options argument handler. 
	 * @see #handler(Functions.FailableBiConsumer)
	 */
	public FailableBiConsumer<Context<B>,O,?> getArgHandler() { return argsHandler; }

	/** Sets the options argument handler. The argument handler will be
	 * invoked, as soon as the actual option value is available.
	 * @param pHandler The options argument handler. 
	 * @return This option.
	 * @see Context#getBean()
	 * @see Context#getOptName()
	 * @see #getArgHandler()
	 */
	public Option<B,O> handler(FailableBiConsumer<Context<B>,O,?> pHandler) {
		assertMutable();
		argsHandler = pHandler;
		return this;
	}

	/** Sets the options argument handler by creating a
	 * {@link ISetter setter} for the given property, which
	 * will be invoked by the created argument handler.
	 * In other words, if the property name is "foo", then
	 * the created argument handler will set the field "foo",
	 * in, or invoke the setter method "setFoo"
	 * on the options bean without any validation of the property value.
	 * @param pProperty The property name, as specified by
	 * {@link ISetter#of(Class,String)}.
	 * @return This option.
	 * @throws IllegalArgumentException The method
	 * {@link ISetter#of(Class, String)} could not create a
	 * setter for the given property.
	 * @throws NullPointerException The parameter {@code pProperty} is null.
	 */
	public Option<B,O> property(@NonNull String pProperty) {
	    final @NonNull B bean = getCli().getBean();
	    @SuppressWarnings("unchecked")
		final @NonNull Class<B> beanType = (@NonNull Class<B>) bean.getClass();
		final ISetter<B,O> setter = ISetter.of(beanType, pProperty);
		final FailableBiConsumer<Context<B>,O,RuntimeException> argsHandler = (c,o) -> {
			setter.set(c.getBean(), o);
		};
		return handler(argsHandler);
	}

	/** Returns the options default value, if any, or null.
	 * @return The options default value, if any, or null.
	 * @see #defaultValue(String)
	 */
	public String getDefaultValue() { return defaultValue; }

	/** Sets the options default value.
	 * @param pDefaultValue The options default value, if any, or null.
	 * @see #getDefaultValue()
	 * @return This option.
	 */
	public Option<B,O> defaultValue(String pDefaultValue) {
		assertMutable();
		defaultValue = pDefaultValue;
		return this;
	}

	/** Returns, whether this option is required.
	 * @return True, if this option is required, otherwise false.
	 * @see #required(boolean)
	 * @see #required()
	 */
	public boolean isRequired() { return required; }

	/** Sets, whether this option is required.
	 * @param pRequired True, if this option is required, otherwise false.
	 * @return This option.
	 * @see #isRequired()
	 * @see #required()
	 */
	public Option<B,O> required(boolean pRequired) {
		assertMutable();
		required = pRequired;
		return this;
	}

	/** Sets, that this option is required. Equivalent to
	 * <pre>
	 *   required(true)
	 * </pre>
	 * @return This option.
	 * @see #isRequired()
	 * @see #required(boolean)
	 */
	public Option<B,O> required() { return required(true); }

	/** Specifies, that this option may be used more than once.
	 * If so, then the option value will be a list, rather
	 * than a single object.
	 * @return A new option object with the same primary,
	 *   and secondary names, but a different value type.
	 *   The new option object will have
	 *   {@code isRepeatable() == true}.
	 * @see #isRepeatable()
	 */
	public RepeatableOption<B,O> repeatable() {
		final RepeatableOption<B,O> rptOpt = new RepeatableOption<B,O>(cli, this, primaryName, secondaryNames);
		getCli().register(rptOpt);
		return rptOpt;
		
	}

	/** Returns, whether this option is repeatable.
	 * @return True, if this option is repeatable, otherwise false.
	 * @see #repeatable()
	 */
	public boolean isRepeatable() { return false; }

	/** Terminates configuration of this option object, and returns the
	 * {@link Cli}, for continued use in a builder pipeline.
	 * @return The {@link Cli} object, that created this option
	 * object.
	 * @see #getCli()
	 * @throws NoSuchElementException No argument handler has been specified on the option.
	 * @throws IllegalStateException This method may be invoked only
	 *   once, but this is the second invocation.
	 */
	public Cli<B> end() {
		if (immutable) {
			throw new IllegalStateException("The end() method has already been invoked on this object.");
		} else {
			if (getArgHandler() == null) {
				throw new NoSuchElementException("No argument handler has been specified for option "
						+ getPrimaryName() + ", and any value would be discarded.");
			}
			immutable = true;
		}
		return cli;
	}
}
