package com.github.jochenw.afw.core.crypt;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Base64.Encoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Tupel;


public interface IKeyHandler {
	/**
	 * Generates a new key pair
	 * @return The generated key pair.
	 */
	public @Nonnull KeyPair createKeyPair();
	/**
	 * Generates a self-signed certificate with the give key pair, the given distinguished name,
	 * and the given validity in days.
	 * @param pDn The certificates distinguished name. (Example: CN=localhost).
	 * @param pKeyPair The private, and public key, which are being used.
	 * @param pValidity How long should the certificate be valid (in days)?
	 * @return The generated certificate.
	 */
	public @Nonnull Certificate generateCertificate(@Nonnull String pDn,
			                                        @Nonnull KeyPair pKeyPair, int pValidity);
	/**
	 * Creates a new key store with exactly one entry, representing the given private key,
	 * and certificate.
	 * @param pPrivateKey The single entries private key.
	 * @param pCertificate The single entries certificate.
	 * @param pAlias The single entries alias.
	 * @param pStoreType The key store type. May be null, in which case "JKS" is being used.
	 * @param pStorePass The key stores password.
	 * @param pKeyPass The entries key password. May be null, in which case the store password
	 *   is being used.
	 * @return The generated certificate.
	 */
	public @Nonnull KeyStore createKeyStore(@Nonnull PrivateKey pPrivateKey,
                                            @Nonnull Certificate pCertificate, @Nonnull String pAlias,
                                            @Nullable String pStoreType, @Nonnull String pStorePass,
                                            @Nonnull String pKeyPass);
	/**
	 * Creates a new key store with exactly one entry, representing the given private key,
	 * and certificate. The created key store is being written to the given {@link OutputStream}.
	 * @param pOut The OutputStream, to which the key store is being written.
	 * @param pPrivateKey The single entries private key.
	 * @param pCertificate The single entries certificate.
	 * @param pAlias The single entries alias.
	 * @param pStoreType The key store type. May be null, in which case "JKS" is being used.
	 * @param pStorePass The key stores password.
	 * @param pKeyPass The entries key password. May be null, in which case the store password
	 *   is being used.
	 */
	public void createKeyStore(@Nonnull OutputStream pOut, @Nonnull PrivateKey pPrivateKey,
			                   @Nonnull Certificate pCertificate, @Nonnull String pAlias,
			                   @Nullable String pStoreType, @Nonnull String pStorePass,
			                   @Nullable String pKeyPass);

	/** Reads an private key/certificate entry from a key store, which is in turn read
	 * from the given {@link InputStream}.
	 * @param pIn The {@link InputStream}, from which to read the key store
	 * @param pAlias The alias, which is being read.
	 * @param pStorePass The key stores password.
	 * @param pKeyPass The entries key password. May be null, in which case the store password
	 *   is being used.
	 * @return A tupel with the entries private key, and the certificate.
	 */
	public Tupel<PrivateKey,Certificate> readPrivateKey(@Nonnull InputStream pIn, @Nonnull String pAlias,
			                                            @Nonnull String pStorePass, @Nullable String pKeyPass);

	/** Encrypts a string, using the given private key to a byte array.
	 * @param pKey The (private) key to use for encryption.
	 * @param pBytes The byte array, which is being encrypted.
	 */
	public byte[] encrypt(Key pKey, byte[] pBytes);

	/** Encrypts a string, using the given private key to a string.
	 * @param pKey The (private) key to use for encryption.
	 * @param pString The string, which is being encrypted.
	 */
	public default String encryptToString(Key pKey, String pString) {
		final byte[] bytes = encrypt(pKey, pString.getBytes(StandardCharsets.UTF_8));
		final Encoder mimeEncoder = Base64.getMimeEncoder(0, System.lineSeparator().getBytes(StandardCharsets.UTF_8));
		return mimeEncoder.encodeToString(bytes);
	}

	/** Decrypts a byte array, using the given public key, to a byte array.
	 */
	public byte[] decrypt(Key pKey, byte[] pBytes);
	
	/** Decrypts the given, base64 encoded byte array, using the given key, and converts it to a string.
	 * @param pKey The (public) key, which is being used for decryption.
	 * @param pString The (encrypted) string.
	 * @return The decrypted string.
	 */
	public default String decrypt(Key pKey, String pString) {
		final byte[] base64DecodedBytes = Base64.getMimeDecoder().decode(pString);
		final byte[] decryptedBytes = decrypt(pKey, base64DecodedBytes);
		return new String(decryptedBytes, StandardCharsets.UTF_8);
	}
}
