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

package com.jardoapps.usermodule;

/**
 * This interface is used by {@link UserManager} to send emails.
 * 
 * @author Jaroslav Brtiš
 */
public interface EmailSender {

	/**
	 * Sends a "password reset" email to the user who has forgot his password.
	 * The email should contain a link to the web page where the user can reset
	 * his password. The link should contain the token key, which has to be
	 * passed to method
	 * {@link UserManager#resetPassword(String, String, String)}.
	 * 
	 * @param email
	 *            email of user who has forgot his password
	 * @param tokenKey
	 *            generated key which is needed to reset the password
	 * @return true if the mail has been sent correctly, otherwise false
	 * @see UserManager#createPasswordResetToken(String)
	 */
	boolean sendLostPasswordEmail(String email, String tokenKey);

	/**
	 * Sends an email to inform the newly registered user that he has been
	 * registered by another user (probably an administrator). The email should
	 * contain a link to a web page where the user can confirm his registration.
	 * The link should contain the registration control code, which has to be
	 * passed to method
	 * {@link UserManager#confirmManualRegistration(String, String, String)} by
	 * the registration confirmation page.
	 * 
	 * @param email
	 *            email of the registered user
	 * @param userName
	 *            name of the registered user (can be null)
	 * @param userId
	 *            id of the registered user
	 * @param registrationControlCode
	 *            generated code which is needed to confirm the registration
	 * @param registrator
	 *            user who registered this user (user who is currently logged
	 *            in, can potentially be null)
	 * @return true if the mail has been sent correctly, otherwise false
	 * @see #sendRegistrationEmail(String, String, int, String)
	 * @see UserManager#registerUserManually(String, String, int, boolean)
	 */
	boolean sendManualRegistrationEmail(String email, String userName, int userId, String registrationControlCode, User registrator);

	/**
	 * Sends a confirmation email to the user who registered himself. The email
	 * should contain a link to the web page where the user can confirm his
	 * registration. The link should contain the registration control code,
	 * which has to be passed to method
	 * {@link UserManager#confirmRegistration(String, String)}.
	 * 
	 * @param email
	 *            email of the registered user
	 * @param userName
	 *            name of the registered user (can be null)
	 * @param userId
	 *            id of the registered user
	 * @param registrationControlCode
	 *            generated code which is needed to confirm the registration
	 * @return true if the mail has been sent correctly, otherwise false
	 * @see #sendManualRegistrationEmail(String, String, int, String, User)
	 * @see UserManager#registerUser(String, String, String, boolean)
	 */
	boolean sendRegistrationEmail(String email, String userName, int userId, String registrationControlCode);

}
