package com.github.jochenw.afw.core.crypt;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.function.BiConsumer;

import javax.crypto.Cipher;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.core.util.Tupel;

public class DefaultKeyHandler implements IKeyHandler {
	@Override
	public KeyPair createKeyPair() {
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(4096, SecureRandom.getInstanceStrong());
			return keyPairGenerator.generateKeyPair();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public Certificate generateCertificate(String pDn, KeyPair pKeyPair, int pValidity) {
		try {
			final Object x509CertInfo = Reflection.newObject("sun.security.x509.X509CertInfo");
			final Date from = new Date();
			final Date to = new Date(from.getTime() + 9999*1000l*24*60*60);
			final Object certificateValidity = Reflection.newObject("sun.security.x509.CertificateValidity", from, to);
			BigInteger serialNumber = new BigInteger(64, new SecureRandom());
			final Object x500Name = Reflection.newObject("sun.security.x509.X500Name", pDn);

			final Object md5WithRsaOid = Reflection.newObject("sun.security.util.ObjectIdentifier", new int[] { 1, 2, 840, 113549, 1, 1, 11 });
			final Object algorithmId = Reflection.newObject("sun.security.x509.AlgorithmId", md5WithRsaOid);

			final Method setMethod = x509CertInfo.getClass().getDeclaredMethod("set", String.class, Object.class);
			final BiConsumer<String,Object> certInfoSetter = (s,o) -> {
				try {
					setMethod.setAccessible(true);
					setMethod.invoke(x509CertInfo, s, o);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};

			certInfoSetter.accept("validity", certificateValidity);
			certInfoSetter.accept("serialNumber", Reflection.newObject("sun.security.x509.CertificateSerialNumber", serialNumber));
			certInfoSetter.accept("subject", x500Name);
			certInfoSetter.accept("issuer", x500Name);
			certInfoSetter.accept("key", Reflection.newObject("sun.security.x509.CertificateX509Key", PublicKey.class, pKeyPair.getPublic()));
			certInfoSetter.accept("version", Reflection.newObject("sun.security.x509.CertificateVersion", Integer.TYPE, Integer.valueOf(2)));
			certInfoSetter.accept("algorithmID", Reflection.newObject("sun.security.x509.CertificateAlgorithmId", algorithmId));
			final Object x509CertImpl = Reflection.newObject("sun.security.x509.X509CertImpl", x509CertInfo);
			final Method signMethod = x509CertImpl.getClass().getDeclaredMethod("sign", PrivateKey.class, String.class);
			signMethod.invoke(x509CertImpl, pKeyPair.getPrivate(), "SHA256WithRSA");

			return (Certificate) x509CertImpl;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public KeyStore createKeyStore(PrivateKey pPrivateKey, Certificate pCertificate, String pAlias, String pStoreType,
			String pStorePass, String pKeyPass) {
		try {
			KeyStore keyStore = KeyStore.getInstance(Objects.notNull(pStoreType, "JKS"));
			keyStore.load(null, pStorePass.toCharArray());
			final String pwd = Objects.notNull(pKeyPass, pStorePass);
			keyStore.setKeyEntry(pAlias, pPrivateKey, pwd.toCharArray(), new Certificate[] {pCertificate});
			return keyStore;
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public void createKeyStore(OutputStream pOut, PrivateKey pPrivateKey, Certificate pCertificate, String pAlias,
			String pStoreType, String pStorePass, String pKeyPass) {
		try {
			final KeyStore keyStore = createKeyStore(pPrivateKey, pCertificate, pAlias, pStoreType, pStorePass, pKeyPass);
			keyStore.store(pOut, pStorePass.toCharArray());
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public Tupel<PrivateKey, Certificate> readPrivateKey(InputStream pIn, String pAlias, String pStorePass,
			String pKeyPass) {
		try {
			final KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(pIn, pStorePass.toCharArray());
			final String pwd = Objects.notNull(pKeyPass, pStorePass);
			final PrivateKey pk = (PrivateKey) keyStore.getKey(pAlias, pwd.toCharArray());
			final Certificate cert = (Certificate) keyStore.getCertificate(pAlias);
			return new Tupel<PrivateKey,Certificate>(pk, cert);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public byte[] encrypt(Key pKey, byte[] pBytes) {
		try {
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, pKey);
			cipher.update(pBytes);
			return cipher.doFinal();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	@Override
	public byte[] decrypt(Key pKey, byte[] pBytes) {
		try {
			final Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			cipher.init(Cipher.DECRYPT_MODE, pKey);
			cipher.update(pBytes);
			return cipher.doFinal();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

}
