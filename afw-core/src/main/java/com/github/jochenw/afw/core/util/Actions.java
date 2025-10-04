/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.util;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jspecify.annotations.NonNull;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableBiFunction;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableFunction;


/** Utility class for supporting various kinds of actions.
 */
public class Actions {
	/** Creates a new instance. Private constructor,
	 * because all methods are static.
	 */
	private Actions() {}

	/** Executes all the given tasks in parallel, without any
	 * determined order, using a thread pool with the given size.
	 * @param pTasks The tasks, that are being executed.
	 * @param pNumberOfThreads Number of threads, that may be
	 *   executed in parallel. 
	 */
	public static void executeAll(int pNumberOfThreads, Runnable... pTasks) {
		final ExecutorService executor = Executors.newFixedThreadPool(pNumberOfThreads);
		@SuppressWarnings("unchecked")
		final CompletableFuture<Void>[] completableFutures = Objects.arrayOf(CompletableFuture.class, pTasks.length);
		for (int i = 0;  i < pTasks.length;  i++) {
			completableFutures[i] = CompletableFuture.runAsync(pTasks[i], executor);
		}
		try {
			CompletableFuture.allOf(completableFutures).get();
		} catch (ExecutionException e) {
			final @NonNull Throwable cause = Objects.requireNonNull(e.getCause());
			throw Exceptions.show(cause);
		} catch (InterruptedException e) {
			throw Exceptions.show(e);
		}
	}

	/** Executes all the given tasks in parallel, without any
	 * determined order, using a thread pool with the given size.
	 * @param pTasks The tasks, that are being executed.
	 * @param pNumberOfThreads Number of threads, that may be
	 *   executed in parallel. 
	 */
	public static void executeAll(int pNumberOfThreads, Collection<Runnable> pTasks) {
		final Runnable[] runnables = pTasks.toArray(new Runnable[pTasks.size()]);
		executeAll(pNumberOfThreads, runnables);
	}

	/** A status object represents the state of an action, which has been performed.
	 * @param <O> Type of the actions result object, if any, or {@link Void}.
	 */
	public static class Status<O> {
		/** The state of the action, as a constant value.
		 */
		public static enum State {
			/** The action has failed by throwing an Exception. Use {@link Status#getFailure()}
			 * to retrieve the Exception object.
			 */
			failure,
			/** The action has failed by throwing an ErrorStatusException. Use {@link Status#getErrorCode()},
			 * {@link Status#getErrorMsg()} ,and {@link Status#getErrorDetails()} for more detailed
			 * information on the error.
			 */
			error,
			/** The action has failed by throwing a WarningStatusException. Use {@link Status#getErrorCode()},
			 * {@link Status#getErrorMsg()} ,and {@link Status#getErrorDetails()} for more detailed
			 * information on the error, and {@link Status#getResult()} to retrieve the result object.
			 */
			warning,
			/** The action has been performed successfully, without error, or warning. Use
			 * {@link Status#getResult()} to retrieve the result object.
			 */
			success;
		}

		private final State state;
		private final Throwable failure;
		private final O result;
		private final String errorCode, errorMsg, errorDetails;

		/** Creates a new instance.
		 * @param pState The actions state.
		 * @param pResult The actions result object, if any, or null.
		 * @param pFailure The actions failure, if any, or null.
		 * @param pErrorCode The actions error code, if any, or null.
		 * @param pErrorMsg The actions error message, if any, or null.
		 * @param pErrorDetails The actions error details, if any, or null.
		 */
		public Status(State pState, O pResult, Throwable pFailure, String pErrorCode, String pErrorMsg, String pErrorDetails) {
			state = Objects.requireNonNull(pState, "State");
			result = pResult;
			failure = pFailure;
			errorCode = pErrorCode;
			errorMsg = pErrorMsg;
			errorDetails = pErrorDetails;
		}

		/** Returns the actions state.
		 * @return The actions state.
		 */
		public State getState() { return state; }

		/** Returns the {@link Throwable}, that has caused the action to fail, or null.
		 * @return The Throwable, that has caused the action to fail. Null, if the
		 *   action has been performed successfully.
		 */
		public Throwable getFailure() { return failure; }

		/** If the actions state is either of {@link State#error}, or {@link State#warning}:
		 * Returns the error code.
		 * @return The actions error code, or null ({@link State#failure}, or {@link State#success}.
		 */
		public String getErrorCode() { return errorCode; }

		/** If the actions state is either of {@link State#error}, or {@link State#warning}:
		 * Returns the short error message.
		 * @return The actions short error message, or null ({@link State#failure}, or {@link State#success}.
		 */
		public String getErrorMsg() { return errorMsg; }

		/** If the actions state is either of {@link State#error}, or {@link State#warning}:
		 * Returns the detailed error message.
		 * @return The actions detailed error message, or null ({@link State#failure}, or {@link State#success}.
		 */
		public String getErrorDetails() { return errorDetails; }

		/** Returns the actions result object, if any, or null.
		 * @return The actions result object, if any, or null.
		 */
		public O getResult() { return result; }
	}

	/** An actions primary context object. This is used mainly for reporting the actions result,
	 * and state.
	 * @param <O> Type of the actions result object, if any, or null.
	 */
	public static class Context<O> {
		/** Causes the action to fail with status {@link Status.State#error}
		 * by throwing an {@link ActionErrorException} with the
		 * given error code, error message, and error details.
		 * @param pErrorCode The error code.
		 * @param pErrorMsg The error message.
		 * @param pErrorDetails The error details.
		 * @return Nothing, the method <em>always</em> throws the created exception.
		 * However, declaring the thrown exception as a result, allows to use code like
		 * <pre>
		 *   throw context.error(code, msg, details);
		 * </pre>
		 * rather, than just,
		 * <pre>
		 *   context.error(code, msg, details);
		 * </pre>
		 * The former has the advantage, that it informs the compilers code flow
		 * analysis about what's happening.
		 */
		public ActionErrorException error(String pErrorCode, String pErrorMsg, String pErrorDetails) {
			throw new ActionErrorException(pErrorCode, pErrorMsg, pErrorDetails);
		}
	
		/** Causes the action to fail with status {@link Status.State#warning}
		 * by throwing an {@link ActionWarningException} with the
		 * given result object, error code, error message, and
		 * error details.
		 * @param pResult The result object.
		 * @param pErrorCode The error code.
		 * @param pErrorMsg The error message.
		 * @param pErrorDetails The error details.
		 * @return Nothing, the method <em>always</em> throws the created exception.
		 * However, declaring the thrown exception as a result, allows to use code like
		 * <pre>
		 *   throw context.warning(result, code, msg, details);
		 * </pre>
		 * rather, than just,
		 * <pre>
		 *   context.warning(result, code, msg, details);
		 * </pre>
		 * The former has the advantage, that it informs the compilers code flow
		 * analysis about what's happening.
		 */
		public ActionErrorException warning(O pResult, String pErrorCode, String pErrorMsg, String pErrorDetails) {
			throw new ActionWarningException(pResult, pErrorCode, pErrorMsg, pErrorDetails);
		}
	}

	/** An Exception, which is thrown to indicate the action state {@link Status.State#error}.
	 */
	public static class ActionErrorException extends RuntimeException {
		private static final long serialVersionUID = 1132107670465496535L;
		private final String errorCode, errorMsg, errorDetails;

		/** Creates a new instance with the given error code, error message, and error details.
		 * @param pErrorCode The error code.
		 * @param pErrorMsg The error message.
		 * @param pErrorDetails The error details.
		 */
		public ActionErrorException(String pErrorCode, String pErrorMsg, String pErrorDetails) {
			super(pErrorCode + ": " + pErrorMsg);
			errorCode = pErrorCode;
			errorMsg = pErrorMsg;
			errorDetails = pErrorDetails;
		}

		/** Returns the error code.
		 * @return The error code.
		 */
		public String getErrorCode() { return errorCode; }
		/** Returns the error message.
		 * @return The error message.
		 */
		public String getErrorMsg() { return errorMsg; }
		/** Returns the error details.
		 * @return The error details.
		 */
		public String getErrorDetails() { return errorDetails; }
	}

	/** An Exception, which is thrown to indicate the action state {@link Status.State#warning}. 
	 */
	public static class ActionWarningException extends ActionErrorException {
		private static final long serialVersionUID = 6228572470668976054L;
		private final Object result;

		/** Creates a new instance with the given result object, error code, error message,
		 * and error details.
		 * @param pResult The result object.
		 * @param pErrorCode The error code.
		 * @param pErrorMsg The error message.
		 * @param pErrorDetails The error details.
		 */
		public ActionWarningException(Object pResult, String pErrorCode, String pErrorMsg, String pErrorDetails) {
			super(pErrorCode, pErrorMsg, pErrorDetails);
			result = pResult;
		}

		/** Returns the result object.
		 * @param <O> Type of the result object.
		 * @return The result object.
		 */
		public <O> O getResult() {
			@SuppressWarnings("unchecked")
			final O o = (O) result;
			return o;
		}
		/** Returns the warning code.
		 * @return The warning code.
		 */
		public String getWarningCode() { return getErrorCode(); }
		/** Returns the warning message.
		 * @return The warning message.
		 */
		public String getWarningMsg() { return getErrorMsg(); }
		/** Returns the warning details.
		 * @return The warning details.
		 */
		public String getWarningDetails() { return getErrorDetails(); }

	}

	/** A status provider is an object, that can perform actions, providing a status.
	 */
	public abstract static class StatusProvider {
		/**
		 * Performs the given action, and returns the result object, if any, or null.
		 * @param <O> Type of the result object, if any, or {@link Void}.
		 * @param <C> Type of the context object, if any, or {@link Void}.
		 * @param pContext The context object, if any, or null.
		 * @param pAction The action, which is being performed.
		 * @return The status object.
		 * @throws ActionErrorException The action was terminated with an error.
		 * @throws ActionWarningException The action was terminated with a warning.
		 * @see #call(FailableFunction)
		 * @see #perform(Object, FailableBiFunction)
		 * @see #run(Object, FailableBiConsumer)
		 */
		public <O,C> O call(C pContext, FailableBiFunction<Context<O>,C,O,?> pAction) {
			final FailableBiFunction<Context<O>,C,O,?> action = Objects.requireNonNull(pAction, "Action");
			final Context<O> ac = new Context<>();
			@SuppressWarnings("null")
			O o = null;
			try {
				o = (O) action.apply(ac, pContext);
			} catch (Throwable e) {
				throw Exceptions.show(e);
			}
			return o;
		}

		/**
		 * Performs the given action, and returns the result object, if any, or null.
		 * The action takes no context object.
		 * @param <O> Type of the result object, if any, or {@link Void}.
		 * @param pAction The action, which is being performed.
		 * @return The status object.
		 * @throws ActionErrorException The action was terminated with an error.
		 * @throws ActionWarningException The action was terminated with a warning.
		 * @see #call(Object, FailableBiFunction)
		 * @see #perform(FailableFunction)
		 * @see #run(FailableConsumer)
		 */
		public <O> O call(FailableFunction<Context<O>,O,?> pAction) {
			final FailableFunction<Context<O>,O,?> contextAction = Objects.requireNonNull(pAction, "Action");
			final FailableBiFunction<Context<O>,Void,O,?> action = (c,v) -> contextAction.apply(c);
			return call(null, action);
		}

		/**
		 * Performs the given action, and returns a status object.
		 * @param <O> Type of the result object, if any, or {@link Void}.
		 * @param <C> Type of the context object, if any, or {@link Void}.
		 * @param pContext The context object, if any, or null.
		 * @param pAction The action, which is being performed.
		 * @return The status object.
		 * @see #call(Object, FailableBiFunction)
		 * @see #perform(FailableFunction)
		 * @see #run(Object, FailableBiConsumer)
		 */
		public <O,C> Status<O> perform(C pContext, FailableBiFunction<Context<O>,C,O,?> pAction) {
			final FailableBiFunction<Context<O>,C,O,?> action = Objects.requireNonNull(pAction, "Action");
			final Context<O> ac = new Context<>();
			@SuppressWarnings("null")
			O o = (O) null;
			try {
				o = action.apply(ac, pContext);
				return new Status<O>(Status.State.success, o, null, null, null, null);
			} catch (ActionWarningException ae) {
				return new Status<O>(Status.State.warning, ae.getResult(), ae, ae.getErrorCode(), ae.getErrorMsg(), ae.getErrorDetails());
			} catch (ActionErrorException ae) {
				return new Status<O>(Status.State.error, o, ae, ae.getErrorCode(), ae.getErrorMsg(), ae.getErrorDetails());
			} catch (Throwable t) {
				return new Status<O>(Status.State.failure, o, t, null, null, null);
			}
		}

		/** Performs the given action, which takes no context object, and returns
		 * a status object.
		 * @param <O> Type of the result object, if any, or {@link Void}.
		 * @param pAction The action, which is being performed.
		 * @return The status object.
		 * @see #call(FailableFunction)
		 * @see #perform(Object, FailableBiFunction)
		 * @see #run(FailableConsumer)
		 */
		public <O> Status<O> perform(FailableFunction<Context<O>,O,?> pAction) {
			final FailableFunction<Context<O>,O,?> contextAction = Objects.requireNonNull(pAction, "Action");
			final FailableBiFunction<Context<O>,Void,O,?> action = (c,v) -> contextAction.apply(c);
			return perform(null, action);
		}

		/**
		 * Performs the given action, and returns a status object.
		 * @param <C> Type of the context object, if any, or {@link Void}.
		 * @param pContext The context object, if any, or null.
		 * @param pAction The action, which is being performed.
		 * @return The status object.
		 * @see #call(Object, FailableBiFunction)
		 * @see #perform(Object, FailableBiFunction)
		 * @see #run(FailableConsumer)
		 */
		public <C> Status<Void> run(C pContext, FailableBiConsumer<Context<Void>,C,?> pAction) {
			final FailableBiConsumer<Context<Void>,C,?> action = Objects.requireNonNull(pAction, "Action");
			final Context<Void> ac = new Context<Void>();
			try {
				action.accept(ac, pContext);
				return new Status<Void>(Status.State.success, null, null, null, null, null);
			} catch (ActionWarningException ae) {
				return new Status<Void>(Status.State.warning, null, ae, ae.getErrorCode(), ae.getErrorMsg(), ae.getErrorDetails());
			} catch (ActionErrorException ae) {
				return new Status<Void>(Status.State.error, null, ae, ae.getErrorCode(), ae.getErrorMsg(), ae.getErrorDetails());
			} catch (Throwable t) {
				return new Status<Void>(Status.State.failure, null, t, null, null, null);
			}
		}

		/** Performs the given action, and returns a status object.
		 * The action takes no context object, and produces no result.
		 * @param pAction The action, which is being performed.
		 * @return The status object.
		 * @see #call(FailableFunction)
		 * @see #perform(FailableFunction)
		 * @see #run(Object, FailableBiConsumer)
		 */
		public Status<Void> run(FailableConsumer<Context<Void>,?> pAction) {
			final FailableConsumer<Context<Void>,?> contextAction = Objects.requireNonNull(pAction, "Action");
			final FailableBiConsumer<Context<Void>,Void,?> action = (c,v) -> contextAction.accept(c);
			return run(null, action);
		}
	}

	private static final StatusProvider statusProvider = new StatusProvider() {
	};


	/**
	 * Performs the given action, and returns the result object.
	 * Basically, this is the same as {@link #perform(Object, FailableBiFunction)},
	 * except that the latter returns a status object.
	 * @param <O> Type of the result object, if any, or {@link Void}.
	 * @param <C> Type of the context object, if any, or {@link Void}.
	 * @param pContext The context object, if any, or null.
	 * @param pAction The action, which is being performed.
	 * @return The status object.
	 * @see #call(FailableFunction)
	 * @see #perform(Object, FailableBiFunction)
	 * @see #run(Object, FailableBiConsumer)
	 * @throws ActionErrorException The action was terminated with an error.
	 * @throws ActionWarningException The action was terminated with a warning.
	 */
	public static <O,C> O call(C pContext, FailableBiFunction<Context<O>,C,O,?> pAction) {
		return statusProvider.call(pContext, pAction);
	}

	/** Performs the given action, which takes no context object, and returns
	 * the result object.
	 * Basically, this is the same as {@link #perform(FailableFunction)},
	 * except that the latter returns a status object.
	 * @param <O> Type of the result object, if any, or {@link Void}.
	 * @param pAction The action, which is being performed.
	 * @return The status object.
	 * @see #call(Object, FailableBiFunction)
	 * @see #perform(FailableFunction)
	 * @see #run(FailableConsumer)
	 * @throws ActionErrorException The action was terminated with an error.
	 * @throws ActionWarningException The action was terminated with a warning.
	 */
	public static <O> O call(FailableFunction<Context<O>,O,?> pAction) {
		return statusProvider.call(pAction);
	}

	/**
	 * Performs the given action, and returns a status object.
	 * @param <O> Type of the result object, if any, or {@link Void}.
	 * @param <C> Type of the context object, if any, or {@link Void}.
	 * @param pContext The context object, if any, or null.
	 * @param pAction The action, which is being performed.
	 * @return The status object.
	 * @see #call(Object, FailableBiFunction)
	 * @see #perform(FailableFunction)
	 * @see #run(Object, FailableBiConsumer)
	 */
	public static <O,C> Status<O> perform(C pContext, FailableBiFunction<Context<O>,C,O,?> pAction) {
		return statusProvider.perform(pContext, pAction);
	}

	/** Performs the given action, which takes no context object, and returns
	 * a status object.
	 * @param <O> Type of the result object, if any, or {@link Void}.
	 * @param pAction The action, which is being performed.
	 * @return The status object.
	 * @see #call(FailableFunction)
	 * @see #perform(Object, FailableBiFunction)
	 * @see #run(FailableConsumer)
	 */
	public static <O> Status<O> perform(FailableFunction<Context<O>,O,?> pAction) {
		return statusProvider.perform(pAction);
	}

	/**
	 * Performs the given action, and returns a status object.
	 * @param <C> Type of the context object, if any, or {@link Void}.
	 * @param pContext The context object, if any, or null.
	 * @param pAction The action, which is being performed.
	 * @return The status object.
	 * @see #call(Object, FailableBiFunction)
	 * @see #perform(Object, FailableBiFunction)
	 * @see #run(FailableConsumer)
	 */
	public static <C> Status<Void> run(C pContext, FailableBiConsumer<Context<Void>,C,?> pAction) {
		return statusProvider.run(pContext, pAction);
	}

	/** Performs the given action, and returns a status object.
	 * The action takes no context object, and produces no result.
	 * @param pAction The action, which is being performed.
	 * @return The status object.
	 * @see #call(FailableFunction)
	 * @see #perform(FailableFunction)
	 * @see #run(Object, FailableBiConsumer)
	 */
	public static Status<Void> run(FailableConsumer<Context<Void>,?> pAction) {
		return statusProvider.run(pAction);
	}
}
