package com.github.jochenw.afw.core.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.crypt.DefaultKeyHandler;
import com.github.jochenw.afw.core.crypt.IKeyHandler;


/** Utility class, which provides static encryption/decryption methods.
 */
public class Keys {
	private static volatile @Nonnull IKeyHandler keyHandler = new DefaultKeyHandler();

	/**
	 * Returns the default {@link IKeyHandler}, which is internally used.
	 * @return The default {@link IKeyHandler}, which is internally used.
	 */
	public static @Nonnull IKeyHandler getKeyHandler() {
		return keyHandler;
	}

	/**
	 * Sets the default {@link IKeyHandler}, which is internally used.
	 * @param pKeyHandler The default {@link IKeyHandler}, which is internally used.
	 */
	public static void setKeyHandler(@Nonnull IKeyHandler pKeyHandler) {
		keyHandler = Objects.requireNonNull(pKeyHandler, "IKeyHandler");
	}

	/**
	 * Generates a new key pair
	 * @return The generated key pair.
	 */
	public static @Nonnull KeyPair createKeyPair() {
		return getKeyHandler().createKeyPair();
	}

	/**
	 * Generates a self-signed certificate with the give key pair, the given distinguished name,
	 * and the given validity in days.
	 * @param pDn The certificates distinguished name. (Example: CN=localhost).
	 * @param pKeyPair The private, and public key, which are being used.
	 * @param pValidity How long should the certificate be valid (in days)?
	 * @return The generated certificate.
	 */
	public static @Nonnull Certificate generateCertificate(@Nonnull String pDn,
			                                               @Nonnull KeyPair pKeyPair, int pValidity) {
		return getKeyHandler().generateCertificate(pDn, pKeyPair, pValidity);
	}

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
	public static @Nonnull KeyStore createKeyStore(@Nonnull PrivateKey pPrivateKey,
                                                   @Nonnull Certificate pCertificate, @Nonnull String pAlias,
                                                   @Nullable String pStoreType, @Nonnull String pStorePass,
                                                   @Nonnull String pKeyPass) {
		return getKeyHandler().createKeyStore(pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
	}
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
	public static void createKeyStore(@Nonnull OutputStream pOut, @Nonnull PrivateKey pPrivateKey,
			                   @Nonnull Certificate pCertificate, @Nonnull String pAlias,
			                   @Nullable String pStoreType, @Nonnull String pStorePass,
			                   @Nullable String pKeyPass) {
		getKeyHandler().createKeyStore(pOut, pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
	}

	/** Reads an private key/certificate entry from a key store, which is in turn read
	 * from the given {@link InputStream}.
	 * @param pIn The {@link InputStream}, from which to read the key store
	 * @param pAlias The alias, which is being read.
	 * @param pStorePass The key stores password.
	 * @param pKeyPass The entries key password. May be null, in which case the store password
	 *   is being used.
	 * @return A tupel with the entries private key, and the certificate.
	 */
	public static Tupel<PrivateKey,Certificate> readPrivateKey(@Nonnull InputStream pIn, @Nonnull String pAlias,
			                                            @Nonnull String pStorePass, @Nullable String pKeyPass) {
		return getKeyHandler().readPrivateKey(pIn, pAlias, pStorePass, pKeyPass);
	}

	/** Encrypts a string, using the given private key to a byte array.
	 * @param pKey The (private) key to use for encryption.
	 * @param pBytes The byte array, which is being encrypted.
	 * @return The encrypted byte array.
	 */
	public static byte[] encrypt(Key pKey, byte[] pBytes) {
		return getKeyHandler().encrypt(pKey, pBytes);
	}

	/** Encrypts a string, using the given private key to a string.
	 * @param pKey The (private) key to use for encryption.
	 * @param pString The string, which is being encrypted.
	 * @return The encrypted byte array, as a base 64 encoded
	 *   string.
	 */
	public static String encryptToString(Key pKey, String pString) {
		return getKeyHandler().encryptToString(pKey, pString);
	}

	/** Decrypts a byte array, using the given public key, to a byte array.
	 * @param pKey The key, which is being used for decryption.
	 * @param pBytes The byte array, which is being decrypted.
	 * @return The decrypted byte array.
	 */
	public static byte[] decrypt(Key pKey, byte[] pBytes) {
		return getKeyHandler().decrypt(pKey, pBytes);
	}
	
	/** Decrypts the given, base64 encoded byte array, using the given key, and converts it to a string.
	 * @param pKey The (public) key, which is being used for decryption.
	 * @param pString The (encrypted) string.
	 * @return The decrypted string.
	 */
	public static String decrypt(Key pKey, String pString) {
		return getKeyHandler().decrypt(pKey, pString);
	}

}
