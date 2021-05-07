package com.github.jochenw.afw.core.util;

import java.util.Locale;

/** Utility class, which provides information about the operating system.
 */
public class Systems {
	private static String OS = System.getProperty("os.name").toLowerCase(Locale.US);
	private static boolean isWindows = OS.contains("win");
	private static boolean isMac = OS.contains("mac");
	private static boolean isLinuxOrUnix = (OS.contains("nix") || OS.contains("nux") || OS.contains("aix") || OS.contains("sunos"));
	private static boolean isOther = !isWindows  &&  !isMac  &&  !isLinuxOrUnix;

	/**
	 * Returns, whether we are currently running under Windows.
	 * @return True, if are currently running under Windows, otherwise false.
	 */
	public static boolean isWindows() {
		return isWindows;
	}

	/**
	 * Returns, whether we are currently running under OS X (Mac).
	 * @return True, if are currently running under OS X (Mac), otherwise false.
	 */
	public static boolean isMac() {
        return isMac;
    }

	/**
	 * Returns, whether we are currently running under Linux, or Unix.
	 * @return True, if are currently running under Linux, or Unix, otherwise false.
	 */
    public static boolean isLinuxOrUnix() {
        return isLinuxOrUnix;
    }

	/**
	 * Returns, whether we are currently running under another operating system.
	 * @return True, if are currently running under another operating system,
	 *   otherwise false.
	 */
    public static boolean isOther() {
    	return isOther;
    }

    /**
     * Returns an operating system identifier.
     * @return Either of the strings "windows", "mac", "unix", or "other".
     */
    public static String getOS(){
        if (isWindows()) {
            return "windows";
        } else if (isMac()) {
            return "mac";
        } else if (isLinuxOrUnix()) {
            return "unix";
        } else {
            return "other";
        }
    }}
