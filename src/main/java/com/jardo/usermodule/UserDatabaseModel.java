package com.jardo.usermodule;

import java.net.InetAddress;
import java.util.Date;

import com.jardo.usermodule.containers.PasswordResetToken;
import com.jardo.usermodule.containers.User;
import com.jardo.usermodule.containers.UserPassword;

public interface UserDatabaseModel {

	/**
	 * Adds a new password reset token.
	 * 
	 * @param token
	 *            token to be added
	 * @return True on success, otherwise false.
	 * @see #cancelAllPasswordResetTokens(int)
	 * @see #getNewestPasswordResetToken(String)
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
	 * Cancels all password reset tokens for user with specified id.
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
	 */
	int getUserIdByEmail(String email);

	/**
	 * Returns password of user with the specified id.
	 * 
	 * @param userId
	 *            id of user whose password should be returned
	 * @return Password of user with the specified id or null, if no such user
	 *         exists.
	 */
	UserPassword getUserPassword(int userId);

	/**
	 * Check whether there is a user registered with the given email.
	 * 
	 * @param email
	 *            email address which should be checked
	 * @return True if the email is already registered, otherwise false.
	 */
	boolean isEmailRegistered(String email);

	/**
	 * Check whether there is a user registered with the given name.
	 * 
	 * @param email
	 *            email address which should be checked
	 * @return True if the user name is already registered, otherwise false.
	 */
	boolean isUserNameRegistered(String name);

	/**
	 * Records a log in attempt for the user with specified id.
	 * 
	 * @param userId
	 *            user which was trying to log in
	 * @param logInSuccessfull
	 *            information if the log in was successful or not (for instance
	 *            wrong password)
	 * @param usersIp
	 *            users ip address
	 * @return True on success, otherwise false.
	 */
	boolean makeLogInRecord(int userId, boolean logInSuccessful, InetAddress usersIp);

	/**
	 * Sets a new password for the user with the specified id.
	 * 
	 * @param userId
	 *            user whose password should be set
	 * @param password
	 *            password to be set
	 * @return True on success, otherwise false.
	 */
	boolean setUserPassword(int userId, UserPassword password);

}
