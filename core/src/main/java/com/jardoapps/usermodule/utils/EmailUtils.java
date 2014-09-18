/*
 * This file is part of the User Module library.
 * Copyright (C) 2014 Jaroslav Brtiš
 *
 * User Module library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * User Module library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with User Module library. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jardoapps.usermodule.utils;

/**
 * Class which provides functionality for working with email addresses.
 * 
 * @author Jaroslav Brtiš
 * 
 */
public final class EmailUtils {

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
	 * EMAIL_REGEX for more information. Top level domain validity is not
	 * checked.
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

	private EmailUtils() {

	}

}
