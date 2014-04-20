package com.jardoapps.usermodule.utils;

public class EmailUtils {

	/**
	 * Regex for checking email validity. It is designed primarily to determine
	 * if an email address is suitable for user registration and therefore is
	 * more strict than the RFC 822 specification. For instance address
	 * <code>user@localhost</code> is considered valid by RFC 822, but is
	 * <b>not</b> considered suitable for user registration.
	 */
	public static final String EMAIL_REGEX = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";

	/**
	 * Check whether the given email matches the {@link #EMAIL_REGEX}. Note that
	 * this method is <b>not</b> compliant with RFC 822. See documentation of
	 * EMAIL_REGEX for more information.
	 * 
	 * @param email
	 *            email address to check
	 * @return true if the given email is considered valid, otherwise false
	 */
	public static boolean isEmailValid(String email) {
		if (email == null) {
			return false;
		}
		return email.matches(EMAIL_REGEX);
	}
}
