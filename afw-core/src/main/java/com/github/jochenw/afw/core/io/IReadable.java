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
	public static class NoLongerReadableException extends RuntimeException {
		private static final long serialVersionUID = -3152908789785936807L;

		public NoLongerReadableException(String message, Throwable cause) {
			super(message, cause);
		}

		public NoLongerReadableException(String message) {
			super(message);
		}

		public NoLongerReadableException(Throwable cause) {
			super(cause);
		}
	}
	public boolean isReadable();
	public boolean isRepeatable();
	public String getName();
	public void read(FailableConsumer<InputStream,?> pConsumer) throws NoLongerReadableException;
	public <O> O apply(FailableFunction<InputStream,O,?> pFunction) throws NoLongerReadableException;
	public default void read(FailableConsumer<Reader,?> pConsumer, Charset pCharset) throws NoLongerReadableException {
		read((in) -> {
			try (Reader rdr = new InputStreamReader(in, pCharset)) {
				pConsumer.accept(rdr);
			}
		});
	}
	public default <O> O apply(FailableFunction<Reader,O,?> pFunction, Charset pCharset) throws NoLongerReadableException {
		final Holder<O> holder = new Holder<>();
		read((in) -> {
			try (Reader rdr = new InputStreamReader(in, pCharset)) {
				holder.set(pFunction.apply(rdr));
			}
		});
		return holder.get();
	}
	public IReadable repeatable() throws NoLongerReadableException;

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

	public static IReadable of(Path pPath) {
		final Path p = Objects.requireNonNull(pPath, "Path");
		return of(p.toString(), () -> new BufferedInputStream(Files.newInputStream(p)));
	}

	public static IReadable of(File pFile) {
		final File f = Objects.requireNonNull(pFile, "File");
		return of(f.getPath(), () -> new BufferedInputStream(new FileInputStream(f)));
	}
	
	public static IReadable of(URL pUrl) {
		final URL u = Objects.requireNonNull(pUrl, "URL");
		return of(u.toExternalForm(), () -> new BufferedInputStream(u.openStream()));
	}

}
