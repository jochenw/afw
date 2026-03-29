package com.github.jochenw.afw.core.crypt;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.core.util.Tupel;


/**
 * Default implementation of {@link IKeyHandler}. Uses JCE, if possible,
 * otherwise Bouncycastle.
 */
public class DefaultKeyHandler implements IKeyHandler {
	/** Creates a new instance.
	 */
	public DefaultKeyHandler() {}

	final IKeyHandler keyHandler = newKeyHandler();


	private static IKeyHandler newKeyHandler() {
		try {
			Reflection.newObject("sun.security.x509.X509CertInfo");
			return new JceKeyHandler();
		} catch (Throwable t) {
			return new BcKeyHandler();
		}
	}
	@Override
	public @NonNull KeyPair createKeyPair() {
		return keyHandler.createKeyPair();
	}

	@Override
	public @NonNull Certificate generateCertificate(@NonNull String pDn, @NonNull KeyPair pKeyPair, int pValidity) {
		return keyHandler.generateCertificate(pDn, pKeyPair, pValidity);
	}

	@Override
	public @NonNull KeyStore createKeyStore(@NonNull PrivateKey pPrivateKey,
			                                @NonNull Certificate pCertificate,
			                                @NonNull String pAlias,
			                                @Nullable String pStoreType,
			                                @NonNull String pStorePass,
			                                @Nullable String pKeyPass) {
		return keyHandler.createKeyStore(pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
	}

	@Override
	public void createKeyStore(@NonNull OutputStream pOut,
			                   @NonNull PrivateKey pPrivateKey,
			                   @NonNull Certificate pCertificate,
			                   @NonNull String pAlias,
			                   @Nullable String pStoreType,
			                   @NonNull String pStorePass,
			                   @Nullable String pKeyPass) {
		keyHandler.createKeyStore(pOut, pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
	}

	@Override
	public Tupel<PrivateKey, Certificate> readPrivateKey(@NonNull InputStream pIn,
			                                             @NonNull String pAlias,
			                                             @NonNull String pStorePass,
			                                             @Nullable String pKeyPass) {
		return keyHandler.readPrivateKey(pIn, pAlias, pStorePass, pKeyPass);
	}

	@Override
	public byte[] encrypt(Key pKey, byte[] pBytes) {
		return keyHandler.encrypt(pKey, pBytes);
	}

	@Override
	public byte[] decrypt(Key pKey, byte[] pBytes) {
		return keyHandler.decrypt(pKey, pBytes);
	}
}
