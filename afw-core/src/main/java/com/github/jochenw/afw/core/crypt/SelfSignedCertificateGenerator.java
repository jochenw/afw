package com.github.jochenw.afw.core.crypt;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Keys;
import com.github.jochenw.afw.core.util.Objects;


/**
 * An object, which can be used to create a keystore, which contains a self-signed certificate.
 */
public class SelfSignedCertificateGenerator {
	private @Nullable Consumer<String> logger;
	private @Inject IKeyHandler keyHandler;
	private @Nullable String fileName, keyPassword, storePassword, alias, storeType;
	private int validInDays;
	private String name, orgUnit, organization, location, stateOrProvince, country;

	/** Sets a logger.
	 * @param pLogger A logger, which is being used for logging messages. May be null,
	 *   in which case no logging is being done.
	 */
	public void setLogger(@Nullable Consumer<String> pLogger) {
		logger = pLogger;
	}

	/** Returns the logger, if any, or null.
	 * @return The logger, if any, or null
	 * @see #setLogger(Consumer)
	 * @see #log(String)
	 */
	public Consumer<String> getLogger() {
		return logger;
	}

	/** Logs a message, using the {@link #getLogger() logger}.
	 * If no logger is present, the message is ignored, and
	 * nothing happens.
	 * @param pMessage The message, which is being logged.
	 * @see #getLogger()
	 * @see #setLogger(Consumer)
	 */
	public void log(String pMessage) {
		if (logger != null) {
			logger.accept(pMessage);
		}
	}

	/** Returns the generated keys password, if any, or null.
	 * @return The generated keys password, if any, or null
	 * @see #setKeyPassword(String)
	 */
	public @Nullable String getKeyPassword() {
		return keyPassword;
	}

	/** Sets the generated keys password, if any, or null.
	 * @param pKeyPassword The generated keys password, if any, or null
	 * @see #getKeyPassword()
	 */
	public void setKeyPassword(@Nullable String pKeyPassword) {
		keyPassword = pKeyPassword;
	}

	/** Sets the {@link IKeyHandler}, which is used to generate the key.
	 * May be null, in which case the {@link Keys#getKeyHandler() default
	 * key handler} is used.
	 * @param pKeyHandler The {@link IKeyHandler}, which is used to generate the key.
	 * May be null, in which case the {@link Keys#getKeyHandler() default
	 * key handler} is used.
	 * @see #getKeyHandler()
	 * @see Keys#getKeyHandler()
	 */
	public void setKeyHandler(@Nullable IKeyHandler pKeyHandler) {
		keyHandler = pKeyHandler;
	}

	/** Returns the {@link IKeyHandler}, which is used to generate the key.
	 * May be null, in which case the {@link Keys#getKeyHandler() default
	 * key handler} is used.
	 * @return The {@link IKeyHandler}, which is used to generate the key.
	 * May be null, in which case the {@link Keys#getKeyHandler() default
	 * key handler} is used.
	 * @see #setKeyHandler(IKeyHandler)
	 * @see Keys#getKeyHandler()
	 */
	public IKeyHandler getKeyHandler() {
		return Objects.notNull(keyHandler, Keys.getKeyHandler());
	}

	/** Sets the store type. May be null, in which case "JKS"
	 * is used as the default.
	 * @param pStoreType The store type. May be null, in which
	 *   case the default value "JKS" is being used.
	 * @see #getStoreType()
	 */
	public void setStoreType(@Nullable String pStoreType) {
		storeType = pStoreType;
	}

	/** Returns the store type. If a value has been set, returns
	 * that value. Otherwise, returns "JKS" as the default value.
	 * @return The store type, if configured, or "JKS" as the
	 * default value. Never null.
	 * @see #setStoreType(String)
	 */
	public String getStoreType() {
		return Objects.notNull(storeType, "JKS");
	}

	/** Returns the private key entries alias. It is an error, if no
	 * alias has been configured.
	 * @return The private key entries alias, if configured, or null.
	 * @see #setAlias(String)
	 */
	public @Nullable String getAlias() {
		return alias;
	}

	/** Sets the private key entries alias. This parameter is
	 * mandatory.
	 * @param pAlias The private key entries alias. This value
	 *   must not be null.
	 * @see #getAlias()
	 * @throws NullPointerException The alias parameter is null.
	 */
	public void setAlias(@Nonnull String pAlias) {
		alias = java.util.Objects.requireNonNull(pAlias, "Alias");
	}

	/** Returns the path of the generated keystore file, if
	 * configured, or null. Setting this value is mandatory.
	 * @return The path of the generated keystore file, if
	 * configured, or null.
	 * @see #setFileName(String)
	 */
	public @Nullable String getFileName() {
		return fileName;
	}

	/** Sets the path of the generated keystore file.
	 * Setting this value is mandatory.
	 * @param pFileName The path of the generated keystore
	 * file, if configured, or null.
	 * @see #setFileName(String)
	 * @throws NullPointerException The parameter {@code pFileName} is null.
	 */
	public void setFileName(@Nonnull String pFileName) {
		fileName = java.util.Objects.requireNonNull(pFileName, "File name");
	}

	/** Returns the key stores password, if any, or null.
	 * @return The key stores password, if any, or null
	 * @see #setStorePassword(String)
	 */
	public @Nullable String getStorePassword() {
		return storePassword;
	}

	/** Sets the key stores password. May be null, in
	 *   which case the generated key store will not
	 *   have a password.
	 * @param pStorePassword The key stores password. May be null, in
	 *   which case the generated key store will not
	 *   have a password. 
	 * @see #getStorePassword()
	 */
	public void setStorePassword(@Nullable String pStorePassword) {
		storePassword = pStorePassword;
	}

	/** Returns the generated certificates validity duration in days.
	 * @return The generated certificates validity duration in days,
	 *   if configured, or 0.
	 */
	public int getValidInDays() {
		return validInDays;
	}

	/** Sets the generated certificates validity duration in days.
	 * Configuring this value is mandatory.
	 * @param pValidInDays The generated certificates validity duration in days.
	 * @throws IllegalArgumentException The parameter is not greater than 0.
	 */
	public void setValidInDays(int pValidInDays) {
		if (pValidInDays <= 0) {
			throw new IllegalArgumentException("Expected positive value, got " + pValidInDays);
		}
		validInDays = pValidInDays;
	}

	/** Returns the generated certificates canonical name. If you intend to use the
	 * generated certificate as a servers SSL certificate, then the canonical
	 * name must match the servers host name, that people are supposed to use.
	 * @return The generated certificates canonical name, if configured, or null.
	 *   Setting this value is mandatory.
	 * @see #setName(String)
	 */
	public @Nullable String getName() {
		return name;
	}

	/** Sets the generated certificates canonical name. If you intend to use the
	 * generated certificate as a servers SSL certificate, then the canonical
	 * name must match the servers host name, that people are supposed to use.
	 * Configuring this value is mandatory.
	 * @param pName The generated certificates canonical name. Must not be null.
	 * @throws NullPointerException The parameter {@code pName} is null.
	 * @see #getName()
	 */
	public void setName(@Nonnull String pName) {
		name = java.util.Objects.requireNonNull(pName, "Name");
	}

	/** Returns the name of the organizational unit, if any, or null.
	 * @return The name of the organizational unit, if any, or null.
	 * @see #setOrgUnit(String)
	 */
	public @Nullable String getOrgUnit() {
		return orgUnit;
	}

	/** Sets the name of the organizational unit, if any, or null.
	 * @param pOrgUnit The name of the organizational unit, if any, or null.
	 * @see #getOrgUnit()
	 */
	public void setOrgUnit(@Nullable String pOrgUnit) {
		orgUnit = pOrgUnit;
	}

	/** Returns the name of the organization, if any, or null.
	 * @return The name of the organization, if any, or null.
	 * @see #setOrganization(String)
	 */
	public @Nullable String getOrganization() {
		return organization;
	}

	/** Sets the name of the organization, if any, or null.
	 * @param pOrganization The name of the organization, if any, or null.
	 * @see #getOrganization()
	 */
	public void setOrganization(String pOrganization) {
		organization = pOrganization;
	}

	/** Returns the location, if any, or null.
	 * @return The location, if any, or null.
	 * @see #setLocation(String)
	 */
	public @Nullable String getLocation() {
		return location;
	}

	/** Sets the location, if any, or null.
	 * @param pLocation The location, if any, or null.
	 * @see #getLocation()
	 */
	public void setLocation(String pLocation) {
		location = pLocation;
	}

	/** Returns the state, or province, if any, or null.
	 * @return The state, or province, if any, or null.
	 * @see #setStateOrProvince(String)
	 */
	public String getStateOrProvince() {
		return stateOrProvince;
	}

	/** Sets the state, or province, if any, or null.
	 * @param pStateOrProvince The state, or province, if any, or null.
	 * @see #getStateOrProvince()
	 */
	public void setStateOrProvince(String pStateOrProvince) {
		stateOrProvince = pStateOrProvince;
	}

	/** Returns the country, if any, or null.
	 * @return The country, if any, or null.
	 * @see #setCountry(String)
	 */
	public String getCountry() {
		return country;
	}

	/** Sets the country, if any, or null.
	 * @param pCountry The country, if any, or null.
	 * @see #getCountry()
	 */
	public void setCountry(String pCountry) {
		country = pCountry;
	}

	/**
	 * Creates a private/public key pair, and a self-signed certificate,
	 * and stores those items in a new key store file as the single
	 * entry.
	 * @throws NullPointerException Either of the mandatory
	 *   parameters ({@link #getAlias()}, {@link #getFileName()},
	 *   {@link #getName()} is null.
	 * @throws IllegalArgumentException The parameter
	 *   {@link #getValidInDays()} has not been configured.
	 */
	public void createSelfSignedCertificate() {
		Objects.requireNonNull(alias, "Alias");
		Objects.requireNonNull(getFileName(), "File name");
		Objects.requireNonNull(getName(), "Name");
		setValidInDays(getValidInDays()); // Triggers an InvalidArgumentException, if not configured.
		final StringBuilder inputSb = new StringBuilder();
		final BiConsumer<String,String> appender = (sh,st) -> {
			if (st != null) {
				if (inputSb.length() > 0) {
					inputSb.append(", ");
				}
				inputSb.append(sh);
				inputSb.append('=');
				inputSb.append(st);
			}
		};
		appender.accept("CN", getName());
		appender.accept("OU", getOrgUnit());
		appender.accept("O", getOrganization());
		appender.accept("L", getLocation());
		appender.accept("S", getStateOrProvince());
		appender.accept("C", getCountry());
		final IKeyHandler kh = getKeyHandler();
		final KeyPair keyPair = kh.createKeyPair();
		final Certificate certificate = kh.generateCertificate(inputSb.toString(), keyPair, validInDays);
		final Path path = Paths.get(fileName);
		final Path dir = path.getParent();
		try {
			if (dir != null) {
				Files.createDirectories(dir);
			}
			try (OutputStream os = Files.newOutputStream(path)) {
				kh.createKeyStore(os, keyPair.getPrivate(), certificate, getAlias(), getStoreType(),
						          getStorePassword(), getKeyPassword());
			}
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}

