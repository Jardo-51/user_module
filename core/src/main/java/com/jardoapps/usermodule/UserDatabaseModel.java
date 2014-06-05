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

import java.util.Date;

import com.jardoapps.usermodule.containers.PasswordResetToken;
import com.jardoapps.usermodule.containers.UserPassword;

/**
 * This interface is used by {@link UserManager} to access the database.
 * 
 * @author Jaroslav Brtiš
 */
public interface UserDatabaseModel {

	/**
	 * Adds a new password reset token. This token should be later accessible by
	 * method {@link #getNewestPasswordResetToken(String)}, or could be canceled
	 * by {@link #cancelAllPasswordResetTokens(int)}.
	 * 
	 * @param token
	 *            token to be added
	 * @return True on success, otherwise false.
	 */
	boolean addPasswordResetToken(PasswordResetToken token);

	/**
	 * Adds new user to database.
	 * 
	 * @param newUser
	 *            user to be added
	 * @return Positive integer value representing the new user ID on success,
	 *         or negative integer value on failure.
	 */
	int addUser(User newUser);

	/**
	 * Cancels all password reset tokens for user with specified id. The
	 * canceled tokens should no longer be returned by method
	 * {@link #getNewestPasswordResetToken(String)}.
	 * 
	 * @param userId
	 *            id of user whose password reset tokens should be canceled
	 * @return True on success, otherwise false.
	 * @see #addPasswordResetToken(PasswordResetToken)
	 * @see #getNewestPasswordResetToken(String)
	 */
	boolean cancelAllPasswordResetTokens(int userId);

	/**
	 * Changes the status of user registration to confirmed.
	 * 
	 * @param email
	 *            email address of user whose registration should be confirmed
	 * @return True on success, otherwise false (no such user, database error).
	 * @see User#isRegistrationConfirmed()
	 */
	boolean confirmUserRegistration(String email);

	/**
	 * Deletes user with specified id from database.
	 * 
	 * @param userId
	 *            id of user to be deleted
	 * @return True on success, otherwise false (no such user, database error).
	 */
	boolean deleteUser(int userId);

	/**
	 * Returns number of users which have been added by method
	 * {@link #addUser(User)} since the specified date.
	 * 
	 * @param since
	 *            date since when to count registered users
	 * @return number of users registered since the specified date
	 */
	int getRegisteredUserCount(Date since);

	/**
	 * Returns password reset token for user with specified email, which has the
	 * latest {@link PasswordResetToken#getCreationTime() creation time}.
	 * 
	 * @param email
	 *            email address of user whose password reset token should be
	 *            returned
	 * @return Newest password reset token for user with specified email or
	 *         null, if no password reset token exists for that user.
	 * @see #addPasswordResetToken(PasswordResetToken)
	 * @see #cancelAllPasswordResetTokens(int)
	 */
	PasswordResetToken getNewestPasswordResetToken(String email);

	/**
	 * Returns user registered with the specified {@link User#getEmail() email
	 * address}.
	 * 
	 * @param email
	 *            email address of the user which should be returned
	 * @return User with the specified email address or null, if no such user
	 *         exists.
	 * @see #getUserByName(String)
	 * @see #getUserIdByEmail(String)
	 */
	User getUserByEmail(String email);

	/**
	 * Returns user registered with the specified {@link User#getName() name}.
	 * 
	 * @param name
	 *            name of the user which should be returned
	 * @return User with the specified name or null, if no such user exists.
	 * @see #getUserByEmail(String)
	 * @see #getUserIdByEmail(String)
	 */
	User getUserByName(String name);

	/**
	 * Returns id of user registered with the specified {@link User#getEmail()
	 * email address}.
	 * 
	 * @param email
	 *            email address of the user whose id should be returned
	 * @return Id of user with the specified email address or -1, if no such
	 *         user exists.
	 * @see #getUserByEmail(String)
	 * @see #getUserByName(String)
	 */
	int getUserIdByEmail(String email);

	/**
	 * Returns password of user with the specified id.
	 * 
	 * @param userId
	 *            id of user whose password should be returned
	 * @return Password of user with the specified id or null, if no such user
	 *         exists.
	 * @see #setUserPassword(int, UserPassword)
	 */
	UserPassword getUserPassword(int userId);

	/**
	 * Check whether there is a user registered with the given email. The result
	 * should be true also for registered users whose registration was not
	 * confirmed yet.
	 * 
	 * @param email
	 *            email address which should be checked
	 * @return True if the email is already registered, otherwise false.
	 */
	boolean isEmailRegistered(String email);

	/**
	 * Check whether there is a user registered with the given name. The result
	 * should be true also for registered users whose registration was not
	 * confirmed yet.
	 * 
	 * @param name
	 *            user name which should be checked
	 * @return True if the user name is already registered, otherwise false.
	 */
	boolean isUserNameRegistered(String name);

	/**
	 * Records a log in attempt for the user with specified id.
	 * 
	 * @param userId
	 *            user who was trying to log in
	 * @param logInSuccessful
	 *            information if the log in was successful or not (for instance
	 *            wrong password)
	 * @param usersIp
	 *            users ip address
	 * @return True on success, otherwise false.
	 */
	boolean makeLogInRecord(int userId, boolean logInSuccessful, String usersIp);

	/**
	 * Sets a new password for the user with the specified id.
	 * 
	 * @param userId
	 *            user whose password should be set
	 * @param password
	 *            password to be set
	 * @return True on success, otherwise false.
	 * @see #getUserPassword(int)
	 */
	boolean setUserPassword(int userId, UserPassword password);

}
