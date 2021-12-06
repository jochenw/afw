package com.github.jochenw.afw.core.crypt;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.core.util.Tupel;

/** Default implementation of {@link IKeyHandler}. Uses JCE, if
 * possible, otherwise BouncyCastle.
 */
public class DefaultKeyHandler implements IKeyHandler {
	private static final IKeyHandler handler = newDefaultKeyHandler();

	private static IKeyHandler newDefaultKeyHandler() {
		try {
			// Check, whether the JCE Crypto code is available.
			Reflection.newObject("sun.security.x509.X509CertInfo");
			// If so, use the JceKeyHandler.
			return new JceKeyHandler();
		} catch (Throwable t) {
			// Otherwise, use the BcKeyHandler.
			return new BcKeyHandler();
		}
	}

	@Override
	public KeyPair createKeyPair() {
		return handler.createKeyPair();
	}

	@Override
	public Certificate generateCertificate(String pDn, KeyPair pKeyPair, int pValidity) {
		return handler.generateCertificate(pDn, pKeyPair, pValidity);
	}

	@Override
	public KeyStore createKeyStore(PrivateKey pPrivateKey, Certificate pCertificate, String pAlias, String pStoreType,
			String pStorePass, String pKeyPass) {
		return handler.createKeyStore(pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
	}

	@Override
	public void createKeyStore(OutputStream pOut, PrivateKey pPrivateKey, Certificate pCertificate, String pAlias,
			String pStoreType, String pStorePass, String pKeyPass) {
		handler.createKeyStore(pOut, pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
	}

	@Override
	public Tupel<PrivateKey, Certificate> readPrivateKey(InputStream pIn, String pAlias, String pStorePass,
			String pKeyPass) {
		return handler.readPrivateKey(pIn, pAlias, pStorePass, pKeyPass);
	}

	@Override
	public byte[] encrypt(Key pKey, byte[] pBytes) {
		return handler.encrypt(pKey, pBytes);
	}

	@Override
	public byte[] decrypt(Key pKey, byte[] pBytes) {
		return handler.decrypt(pKey, pBytes);
	}
}
