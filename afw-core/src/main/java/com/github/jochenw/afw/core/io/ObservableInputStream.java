package com.github.jochenw.afw.core.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.Exceptions;



/**
 * A filtering {@link InputStream}, which allows to observe the data, that is being read.
 */
public class ObservableInputStream extends InputStream {
	/** This listener is being notified about events regarding the observed data stream.
	 */
	public interface Listener {
		/**
		 * The data stream has been read completely, and is about to return -1 (EOF).
		 * @throws IOException The listener failed.
		 */
		void endOfFile() throws IOException;
		/**
		 * The data stream is about to return the given data byte.
		 * @param pByte The byte, that the data stream is about to return.
		 * @throws IOException The listener failed.
		 */
		void reading(int pByte) throws IOException;
		/**
		 * The data stream is about to return the given data bytes.
		 * @param pBuffer A byte array, that contains the bytes that are being returned by
		 *   the data stream.
		 * @param pOffset The offset of the first byte in the array, that is being returned.
		 * @param pLen The number of bytes in the array, that are being returned.
		 * @throws IOException The listener failed.
		 */
		void reading(byte[] pBuffer, int pOffset, int pLen) throws IOException;
		/**
		 * The data stream is about to close.
		 * @throws IOException The listener failed.
		 */
		void closing() throws IOException;


		/** Creates a new {@code Listener} instance, which internally delegates
		 * events to all of the given listeners.
		 * @param pListeners The listeners, which are being notified
		 *   by the created instance.
		 * @return The created listener proxy.
		 */
		public static @NonNull Listener of(Listener... pListeners) {
			return new Listener() {
				@Override
				public void endOfFile() throws IOException {
					if (pListeners != null) {
						for (Listener observer : pListeners) {
							observer.endOfFile();
						}
					}
				}

				@Override
				public void reading(int pByte) throws IOException {
					if (pListeners != null) {
						for (Listener observer : pListeners) {
							observer.reading(pByte);
						}
					}
				}

				@Override
				public void reading(byte[] pBuffer, int pOffset, int pLen) throws IOException {
					if (pListeners != null) {
						for (Listener observer : pListeners) {
							observer.reading(pBuffer, pOffset, pLen);
						}
					}
				}

				@Override
				public void closing() throws IOException {
					if (pListeners != null) {
						for (Listener observer : pListeners) {
							observer.closing();
						}
					}
				}
	    	};
		}
		/** Creates a new {@code Listener} instance, which copies the incoming data
		 * to the given file.
		 * @param pPath The file, to which incoming data is being copied.
		 * @return The created {@code Listener} instance.
		 */
		public static @NonNull Listener of(@NonNull Path pPath) {
    		return of(() -> Files.newOutputStream(pPath));
    	}
		/** Creates a new {@code Listener} instance, which copies the incoming data
		 * to the given file.
		 * @param pFile The file, to which incoming data is being copied.
		 * @return The created {@code Listener} instance.
		 */
    	public static @NonNull Listener of(@NonNull File pFile) {
    		return of(() -> new FileOutputStream(pFile));
    	}
		/** Creates a new {@code Listener} instance, which copies the incoming data
		 * to the given output stream.
		 * @param pOut The output stream, to which incoming data is being copied.
		 * @return The created {@code Listener} instance.
		 */
    	public static @NonNull Listener of(@NonNull FailableSupplier<OutputStream,?> pOut) {
    		return new Listener() {
    			private BufferedOutputStream bOut;

				protected OutputStream getOut() throws IOException {
					if (bOut == null) {
						final OutputStream out;
						try {
							out = pOut.get();
						} catch (Throwable t) {
							throw Exceptions.show(t);
						}
						if (out == null) {
							throw new NullPointerException("Supplier returned a null OutputStream");
						}
						if (out instanceof BufferedOutputStream) {
							bOut = (BufferedOutputStream) out;
						} else {
							bOut = new BufferedOutputStream(out);
						}
					}
					return bOut;
				}

				@Override
				public void reading(byte[] pBuffer, int pOffset, int pLen) throws IOException {
					getOut().write(pBuffer, pOffset, pLen);
				}

				@Override
				public void reading(int pByte) throws IOException {
					getOut().write(pByte);
				}

				@Override
				public void closing() throws IOException {
					if (bOut != null) {
						bOut.close();
						bOut = null;
					}
				}

				@Override
				public void endOfFile() throws IOException {
					// Make sure, that the buffer is open, so that
					// Listener.of(Path), and Listener.of(File) will always produce a (possibly empty) file.
					getOut();
				}
    		};
    	}

	}

	private final InputStream in;
	private final Listener listener;

	/**
	 * Creates a new instance, which reads from the given {@link InputStream}, and
	 * notifies the given listener, while doing so.
	 * @param pIn The input stream, which is actually being read.
	 * @param pListener The listener, which are being notified about data events.
	 *   For multiple listeners, see {@link ObservableInputStream.Listener#of(ObservableInputStream.Listener...)}.
	 */
	public ObservableInputStream(InputStream pIn, Listener pListener) {
		in = pIn;
		listener = pListener;
	}

	@Override
	public int read() throws IOException {
		final int res = in.read();
		if (res == -1) {
			listener.endOfFile();
		} else {
			listener.reading(res);
		}
		return res;
	}

	@Override
	public int read(byte[] pB) throws IOException {
		final int res = in.read(pB);
		if (res == -1) {
			listener.endOfFile();
		} else {
			listener.reading(pB, 0, res);
		}
		return res;
	}

	@Override
	public int read(byte[] pB, int pOff, int pLen) throws IOException {
		final int res = in.read(pB, pOff, pLen);
		if (res == -1) {
			listener.endOfFile();
		} else {
			listener.reading(pB, pOff, res);
		}
		return res;
	}

	@Override
	public void close() throws IOException {
		Throwable th = null;
		try {
			listener.closing();
		} catch (Throwable t) {
			th = t;
		}
		try {
			in.close();
		} catch (Throwable t) {
			if (th != null) {
				th = t;
			}
		}
		if (th != null) {
			throw Exceptions.show(th, IOException.class);
		}
	}
}
