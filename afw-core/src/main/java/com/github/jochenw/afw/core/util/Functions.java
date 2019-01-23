package com.github.jochenw.afw.core.util;

public class Functions {
	@FunctionalInterface
	public interface FailableConsumer<T1 extends Object, T2 extends Throwable> {
		public void accept(T1 pObject) throws T2;
	}

	@FunctionalInterface
	public interface FailableBiConsumer<T1 extends Object, T2 extends Object, T3 extends Throwable> {
		public void accept(T1 pObject1, T2 pObject2) throws T3;
	}

	@FunctionalInterface
	public interface FailableFunction<I extends Object, O extends Object, T extends Throwable> {
		public O apply(I pInput) throws T;
	}

	@FunctionalInterface
	public interface FailableBiFunction<I1 extends Object, I2 extends Object, O extends Object, T extends Throwable> {
		public O apply(I1 pInput1, I2 pInput2) throws T;
	}

	@FunctionalInterface
	public interface FailableRunnable<T extends Throwable> {
		public void run() throws T;
	}

	@FunctionalInterface
	public interface FailableCallable<O extends Object,T extends Throwable> {
		public O call() throws T;
	}

	public static void run(FailableRunnable<? extends Throwable> pRunnable) {
		try {
			pRunnable.run();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static <O> O call(FailableCallable<O,? extends Throwable> pCallable) {
		try {
			return pCallable.call();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static <I1,I2,O> O apply(FailableBiFunction<I1,I2,O,? extends Throwable> pBiFunction, I1 pInput1, I2 pInput2) {
		try {
			return pBiFunction.apply(pInput1, pInput2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static <I,O> O apply(FailableFunction<I,O,? extends Throwable> pFunction, I pInput) {
		try {
			return pFunction.apply(pInput);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static <O> void accept(FailableConsumer<O,? extends Throwable> pConsumer, O pObject) {
		try {
			pConsumer.accept(pObject);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	public static <O1,O2> void accept(FailableBiConsumer<O1,O2,? extends Throwable> pBiConsumer, O1 pObject1, O2 pObject2) {
		try {
			pBiConsumer.accept(pObject1, pObject2);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
