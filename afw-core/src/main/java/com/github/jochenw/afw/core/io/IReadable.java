package com.github.jochenw.afw.core.io;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Functions.FailableFunction;
import com.github.jochenw.afw.core.util.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.Streams;


/** An IReadable is an abstraction of named objects with a content, that
 * can be read from an {@link InputStream}.
 */
public interface IReadable {
	/**
	 * This exception is being thrown, if the methods {@link IReadable#read(FailableConsumer)},
	 * or {@link IReadable#read(FailableConsumer, Charset)} are invoked more than once on a
	 * {@link IReadable}, which isn't repeatable.
	 */
	public static class NoLongerReadableException extends RuntimeException {
		private static final long serialVersionUID = -3152908789785936807L;

		/**
		 * Creates a new instance with the given message, and cause.
		 * @param pMessage The exceptions message.
		 * @param pCause The exceptions cause.
		 */
		public NoLongerReadableException(String pMessage, Throwable pCause) {
			super(pMessage, pCause);
		}

		/**
		 * Creates a new instance with the given message, and no cause.
		 * @param pMessage The exceptions message.
		 */
		public NoLongerReadableException(String pMessage) {
			super(pMessage);
		}

		/**
		 * Creates a new instance with the no message, and the given cause.
		 * @param pCause The exceptions cause.
		 */
		public NoLongerReadableException(Throwable pCause) {
			super(pCause);
		}
	}
	/**
	 * Returns, whether this {@link IReadable} is readable. If so, the methods
	 * {@link IReadable#read(FailableConsumer)}, or {@link IReadable#read(FailableConsumer, Charset)}
	 * may be invoked, at least once.
	 * @return True, if this {@link IReadable} is readable. If so, the methods
	 * {@link IReadable#read(FailableConsumer)}, or {@link IReadable#read(FailableConsumer, Charset)}
	 * may be invoked, at least once.
	 */
	public boolean isReadable();
	/**
	 * Returns, whether this {@link IReadable} is repeatable. If so, the methods
	 * {@link IReadable#read(FailableConsumer)}, or {@link IReadable#read(FailableConsumer, Charset)}
	 * may be invoked more than once.
	 * @return True, if this {@link IReadable} is repeatable. If so, the methods
	 * {@link IReadable#read(FailableConsumer)}, or {@link IReadable#read(FailableConsumer, Charset)}
	 * may be invoked more than once.
	 */
	public boolean isRepeatable();
	/**
	 * Returns the {@link IReadable readable's} name. The definition of a name is mostly unspecified,
	 * and depends on the creation method. Basically, the name's purpose is to allow a human to
	 * resolve the underlying object. The name is not intended for automatic processing.
	 * @return The {@link IReadable readable's} name. The definition of a name is mostly unspecified,
	 * and depends on the creation method. Basically, the name's purpose is to allow a human to
	 * resolve the underlying object. The name is not intended for automatic processing.
	 */
	public String getName();
	/**
	 * Opens the underlying object, and invokes the given {@link FailableConsumer} for processing the
	 * data, that's being read from the {@link InputStream input stream}.
	 * @param pConsumer The consumer, that's processing the {@link InputStream input stream}.
	 * @throws NoLongerReadableException The {@link IReadable} isn't repeatable, and has
	 *   already been opened.
	 */
	public void read(FailableConsumer<InputStream,?> pConsumer) throws NoLongerReadableException;
	/**
	 * Opens the underlying object, and invokes the given {@link FailableFunction} for processing the
	 * data, that's being read from the {@link InputStream input stream}, and returning a
	 * result object.
	 * @param pFunction The function, that's processing the {@link InputStream input stream},
	 *   and returning the result object.
	 * @param <O> Result type. Both the invoked function's, and this methods.
	 * @throws NoLongerReadableException The {@link IReadable} isn't repeatable, and has
	 *   already been opened.
	 * @return The value, that has been returned by the invoked function.
	 */
	public <O> O apply(FailableFunction<InputStream,O,?> pFunction) throws NoLongerReadableException;
	/**
	 * Opens the underlying object, and invokes the given {@link FailableConsumer} for processing the
	 * data, that's being read from the {@link Reader reader}.
	 * @param pConsumer The consumer, that's processing the {@link Reader reader}.
	 * @param pCharset The character set, that's being used for conversion of bytes into
	 *   characters.
	 * @throws NoLongerReadableException The {@link IReadable} isn't repeatable, and has
	 *   already been opened.
	 */
	public default void read(FailableConsumer<Reader,?> pConsumer, Charset pCharset) throws NoLongerReadableException {
		read((in) -> {
			try (Reader rdr = new InputStreamReader(in, pCharset)) {
				pConsumer.accept(rdr);
			}
		});
	}
	/**
	 * Opens the underlying object, and invokes the given {@link FailableFunction} for processing the
	 * data, that's being read from the {@link Reader reader}, and returning a
	 * result object.
	 * @param pFunction The function, that's processing the {@link Reader reader},
	 *   and returning the result object.
	 * @param pCharset The character set, that's being used for conversion of bytes into
	 *   characters.
	 * @param <O> Result type. Both the invoked function's, and this methods.
	 * @throws NoLongerReadableException The {@link IReadable} isn't repeatable, and has
	 *   already been opened.
	 * @return The value, that has been returned by the invoked function.
	 */
	public default <O> O apply(FailableFunction<Reader,O,?> pFunction, Charset pCharset) throws NoLongerReadableException {
		final Holder<O> holder = new Holder<>();
		read((in) -> {
			try (Reader rdr = new InputStreamReader(in, pCharset)) {
				holder.set(pFunction.apply(rdr));
			}
		});
		return holder.get();
	}
	/**
	 * Converts the current {@link IReadable} into a repeatable version.
	 * @return A repeatable {@link IReadable}, that supplies the same data streams,
	 *   than the current one.
	 * @throws NoLongerReadableException The current {@link IReadable} isn't
	 *   readable, and opening it failed.
	 */
	public IReadable repeatable() throws NoLongerReadableException;

	/** Creates a new {@link IReadable}, which supplies the {@link InputStream
	 * input stream}, that's provided by the given {@link FailableSupplier},
	 * and has the given name.
	 * @param pName The value, that's being returned by invoking the
	 *   created {@link IReadable readable's} method {@link IReadable#getName()}.
	 * @param pSupplier Supplies the value, that's being returned by invoking the
	 *   created {@link IReadable readable's} method {@link IReadable#read(FailableConsumer)}.
	 * @return A new instance of {@link IReadable}, with the given name, and data stream.
	 */
	public static IReadable of(String pName, FailableSupplier<InputStream,?> pSupplier) {
		return new IReadable() {
			private boolean opened;
			@Override
			public synchronized boolean isReadable() {
				return !opened;
			}
			@Override
			public boolean isRepeatable() {
				return false;
			}
			@Override
			public String getName() {
				return pName;
			}

			@Override
			public void read(FailableConsumer<InputStream, ?> pConsumer) {
				synchronized(this) {
					if (opened) {
						throw new NoLongerReadableException("This IReadable has already been read: " + pName);
					} else {
						opened = true;
					}
				}
				try (InputStream in = pSupplier.get()) {
					pConsumer.accept(in);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}

			@Override
			public <O> O apply(FailableFunction<InputStream, O, ?> pFunction) {
				synchronized(this) {
					if (opened) {
						throw new NoLongerReadableException("This IReadable has already been read: " + pName);
					} else {
						opened = true;
					}
				}
				final Holder<O> holder = new Holder<O>();
				try (InputStream in = pSupplier.get()) {
					holder.set(pFunction.apply(in));
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
				return holder.get();
			}

			@Override
			public IReadable repeatable() {
				final IReadable r = this;
				final ByteArrayOutputStream baos = new ByteArrayOutputStream();
				r.read((FailableConsumer<InputStream, ?>) (in) -> Streams.copy(in, baos));
				final byte[] bytes = baos.toByteArray();
				return new IReadable() {
					@Override
					public boolean isReadable() {
						return true;
					}
					@Override
					public boolean isRepeatable() {
						return true;
					}
					@Override
					public String getName() {
						return r.getName();
					}
					@Override
					public void read(FailableConsumer<InputStream, ?> pConsumer) throws NoLongerReadableException {
						try (InputStream in = new ByteArrayInputStream(bytes)) {
							pConsumer.accept(in);
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
					}
					@Override
					public <O> O apply(FailableFunction<InputStream,O,?> pFunction) throws NoLongerReadableException {
						try (InputStream in = new ByteArrayInputStream(bytes)) {
							return pFunction.apply(in);
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
					}
					@Override
					public IReadable repeatable() throws NoLongerReadableException {
						return this;
					}
				};
			}
		};
	}

	/** Creates a new {@link IReadable}, which supplies the given file's
	 * content as an {@link InputStream input stream}, using the files path
	 * as the name.
	 * @param pPath The file, which supplies the data stream, and the name.
	 * @return A new instance of {@link IReadable}, with the given file' name,
	 *   and the file's contents as a data stream.
	 */
	public static IReadable of(Path pPath) {
		final Path p = Objects.requireNonNull(pPath, "Path");
		return of(p.toString(), () -> new BufferedInputStream(Files.newInputStream(p)));
	}

	/** Creates a new {@link IReadable}, which supplies the given file's
	 * content as an {@link InputStream input stream}, using the files path
	 * as the name.
	 * @param pFile The file, which supplies the data stream, and the name.
	 * @return A new instance of {@link IReadable}, with the given file' name,
	 *   and the file's contents as a data stream.
	 */
	public static IReadable of(File pFile) {
		final File f = Objects.requireNonNull(pFile, "File");
		return of(f.getPath(), () -> new BufferedInputStream(new FileInputStream(f)));
	}
	
	/** Creates a new {@link IReadable}, which supplies the given URL's
	 * content as an {@link InputStream input stream}, using the URL's
	 * string representation as the name.
	 * @param pUrl The URL, which supplies the data stream, and the name.
	 * @return A new instance of {@link IReadable}, with the given file' name,
	 *   and the file's contents as a data stream.
	 */
	public static IReadable of(URL pUrl) {
		final URL u = Objects.requireNonNull(pUrl, "URL");
		return of(u.toExternalForm(), () -> new BufferedInputStream(u.openStream()));
	}

}