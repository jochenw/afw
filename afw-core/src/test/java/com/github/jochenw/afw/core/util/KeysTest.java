package com.github.jochenw.afw.core.util;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.junit.Test;

import com.github.jochenw.afw.core.crypt.BcKeyHandler;
import com.github.jochenw.afw.core.crypt.DefaultKeyHandler;
import com.github.jochenw.afw.core.crypt.IKeyHandler;


/** Test for the {@link Keys} class.
 */
public class KeysTest {
	/** Test for encryption/decryption, using a {@link IKeyHandler}, and without a password.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testEncryptDecryptUsingKeyHandlerNoPassword() throws Exception {
		final IKeyHandler kh = new DefaultKeyHandler();
		test("keystore0.jks", kh, null);
	}

	/** Test for encryption/decryption, using a {@link IKeyHandler}, and a password.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testEncryptDecryptUsingKeyHandlerAndPassword() throws Exception {
		final IKeyHandler kh = new DefaultKeyHandler();
		test("keystore1.jks", kh, "123456");
	}

	/** Test for encryption/decryption, using a {@link BcKeyHandler}, and without a password.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testEncryptDecryptUsingBcKeyHandlerNoPassword() throws Exception {
		final IKeyHandler kh = new BcKeyHandler();
		test("keystore0.jks", kh, null);
	}

	/** Test for encryption/decryption, using a {@link IKeyHandler}, and a password.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testEncryptDecryptUsingBcKeyHandlerAndPassword() throws Exception {
		final IKeyHandler kh = new BcKeyHandler();
		test("keystore1.jks", kh, "123456");
	}

	/** Test for encryption/decryption, using static methods from the {@link Keys} class,
	 * and without a password.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testEncryptDecryptUsingKeysNoPassword() throws Exception {
		final @NonNull IKeyHandler kh = newKeysHandler();
		test("keystore2.jks", kh, null);
	}

	/** Test for encryption/decryption, using static methods from the {@link Keys} class,
	 * and a password.
	 * @throws Exception The test failed.
	 */
	@Test
	public void testEncryptDecryptUsingKeysAndPassword() throws Exception {
		final IKeyHandler kh = newKeysHandler();
		test("keystore3.jks", kh, "123456");
	}

	/** Creates a new {@link IKeyHandler}, which is internally using the
	 * {@link Keys} class, thereby enabling testing this class with the
	 * tests for the {@link IKeyHandler}.
	 * @return The created {@link IKeyHandler}.
	 */
	protected @NonNull IKeyHandler newKeysHandler() {
		return new IKeyHandler() {
			@Override
			public @NonNull KeyPair createKeyPair() {
				return Keys.createKeyPair();
			}

			@Override
			public @NonNull Certificate generateCertificate(@NonNull String pDn, @NonNull KeyPair pKeyPair, int pValidity) {
				return Keys.generateCertificate(pDn, pKeyPair, pValidity);
			}

			@Override
			public @NonNull KeyStore createKeyStore(@NonNull PrivateKey pPrivateKey,
					                                @NonNull Certificate pCertificate,
					                                @NonNull String pAlias,
					                                @Nullable String pStoreType,
					                                @NonNull String pStorePass,
					                                @Nullable String pKeyPass) {
				return Keys.createKeyStore(pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
			}

			@Override
			public void createKeyStore(@NonNull OutputStream pOut,
					                   @NonNull PrivateKey pPrivateKey,
					                   @NonNull Certificate pCertificate,
					                   @NonNull String pAlias,
					                   @Nullable String pStoreType,
					                   @NonNull String pStorePass,
					                   @Nullable String pKeyPass) {
				Keys.createKeyStore(pOut, pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
			}

			@Override
			public Tupel<PrivateKey, Certificate> readPrivateKey(@NonNull InputStream pIn,
					                                             @NonNull String pAlias,
					                                             @NonNull String pStorePass,
					                                             @Nullable String pKeyPass) {
				return Keys.readPrivateKey(pIn, pAlias, pStorePass, pKeyPass);
			}

			@Override
			public byte[] encrypt(Key pKey, byte[] pBytes) {
				return Keys.encrypt(pKey, pBytes);
			}

			@Override
			public byte[] decrypt(Key pKey, byte[] pBytes) {
				return Keys.decrypt(pKey, pBytes);
			}
		};
	};

	/** Tests the given {@link IKeyHandler} by creating a file with the
	 * given {@code pFileName}, that contains a self-certified keystore.
	 * The created keystore will then be used to encrypt, and decrypt, a 
	 * password, expecting the result to be the original, unencrypted
	 * password.
	 * @param pFileName The file name of the created key store.
	 * @param pHandler The key handler, that is being used for
	 *   encryption, and decryption.
	 * @param pKeyPassword The keys password.
	 * @throws IOException Creating, or reading the key store, has failed.
	 */
	protected void test(@NonNull String pFileName, @NonNull IKeyHandler pHandler, @Nullable String pKeyPassword) throws IOException {
		final Path dir = Paths.get("target/unit-tests/KeysTest");
		final Path file = dir.resolve(pFileName);
		java.nio.file.Files.createDirectories(dir);
		java.nio.file.Files.deleteIfExists(file);
		final KeyPair kp = pHandler.createKeyPair();
		final Certificate cert = pHandler.generateCertificate("CN=Unknown", kp, 9999);
		final @NonNull PrivateKey privateKey = Objects.requireNonNull(kp.getPrivate());
		final KeyStore ks0 = pHandler.createKeyStore(privateKey, cert, "main", null, "654321", pKeyPassword);
		try (OutputStream os = Files.newOutputStream(file)) {
			ks0.store(os, "654321".toCharArray());
		} catch (Throwable e) {
			throw Exceptions.show(e);
		}
		final Tupel<PrivateKey,Certificate> ks1;
		try (@NonNull InputStream in = Objects.requireNonNull(Files.newInputStream(file))) {
			ks1 = pHandler.readPrivateKey(in, "main", "654321", pKeyPassword);
		} catch (Throwable e) {
			throw Exceptions.show(e);
		}
		final String password = "My Secret Password";
		final String encryptedPassword = pHandler.encryptToString(ks1.getAttribute1(), password);
		final String decryptedPassword = pHandler.decrypt(ks1.getAttribute2().getPublicKey(), encryptedPassword);
		assertEquals(password, decryptedPassword);
	}
}
