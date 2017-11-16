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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jardoapps.usermodule.containers.PasswordResetToken;
import com.jardoapps.usermodule.containers.SocialAccountDetails;
import com.jardoapps.usermodule.containers.UserPassword;
import com.jardoapps.usermodule.defines.EmailType;
import com.jardoapps.usermodule.utils.EmailUtils;

/**
 * The main class which contains all the user management logic such as user
 * registration, user log in/out, password changing, password resetting and
 * other.
 * 
 * @author Jaroslav Brtiš
 * 
 */
public class UserManager implements Serializable {

	public static class FatalException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public FatalException(String message, Throwable e) {
			super(message, e);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(UserManager.class);

	private static final byte MD5_HASH_LENGTH = 16;
	private static final long MILIS_IN_MINUTE = 60000L;
	private static final byte PASSWORD_SALT_LENGTH = 32;

	private static final String PASSWORD_HASH_ENCODING = "UTF-16";

	private static final long serialVersionUID = 1L;

	@Inject
	private UserManagementProperties properties;

	@Inject
	private UserDatabaseModel databaseModel;

	@Inject
	private EmailSender emailSender;

	@Inject
	private SessionModel sessionModel;

	private SecureRandom randomGenerator;

	private transient MessageDigest sha256;

	private ResultCode checkRegistrationConfirmationPreconditions(User user, String registrationControlCode) {

		if (user == null) {
			return ResultCode.NO_SUCH_USER;
		}

		if (user.isRegistrationConfirmed()) {
			return ResultCode.REGISTRATION_ALREADY_CONFIRMED;
		}

		if (!user.getRegistrationControlCode().equalsIgnoreCase(registrationControlCode)) {
			return ResultCode.INVALID_REGISTRATION_CONTROL_CODE;
		}

		return ResultCode.OK;
	}

	private ResultCode checkRegistrationPreconditions(String userEmail, String userName) {

		if (userName != null) {
			if (databaseModel.isUserNameRegistered(userName)) {
				return ResultCode.USER_NAME_ALREADY_REGISTERED;
			}
		}

		if (databaseModel.isEmailRegistered(userEmail)) {
			return ResultCode.EMAIL_ALREADY_REGISTERED;
		}

		return ResultCode.OK;
	}

	private UserPassword createUserPassword(String password) {
		String salt = generatePasswordSalt();
		String hash = calculatePasswordHash(password, salt);

		return new UserPassword(hash, salt);
	}

	private User createUserWithSocialAccount(SocialAccountDetails details) {
		return new User(0, details.getUserName(), details.getEmail(), null, true, null, UserRanks.NORMAL_USER);
	}

	private String generateRandomMD5Hash() {
		byte[] bytes = new byte[MD5_HASH_LENGTH];
		randomGenerator.nextBytes(bytes);
		return toHex(bytes);
	}

	private boolean makeLogInRecord(int userId, boolean logInSuccessfull, String usersIp) {

		boolean result = databaseModel.makeLogInRecord(userId, logInSuccessfull, usersIp);
		if (!result) {
			LOGGER.warn("DB error: Failed to make login record: userId={}, ip={}, successfull={}", userId, usersIp, logInSuccessfull);
		}

		return result;
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		try {
			sha256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("Failed to create password hasher.", e);
		}
	}

	private String toHex(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		for (int idx = 0; idx < bytes.length; ++idx) {
			byte b = bytes[idx];
			result.append(digits[(b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}
		return result.toString();
	}

	protected synchronized String calculatePasswordHash(String password, String salt) {
		try {
			sha256.update(salt.getBytes(PASSWORD_HASH_ENCODING));
			return toHex(sha256.digest(password.getBytes(PASSWORD_HASH_ENCODING)));
		} catch (UnsupportedEncodingException e) {
			throw new FatalException("The system doesn't support password hash encoding: " + PASSWORD_HASH_ENCODING, e);
		}
	}

	protected String generatePasswordSalt() {
		byte[] salt = new byte[PASSWORD_SALT_LENGTH];
		randomGenerator.nextBytes(salt);
		return toHex(salt);
	}

	public ResultCode cancelPasswordResetTokens(int userId) {
		boolean ok = databaseModel.cancelAllPasswordResetTokens(userId);
		if (ok) {
			return ResultCode.OK;
		} else {
			LOGGER.error("DB error: Failed to cancel password reset tokens for user with id={}.", userId);
			return ResultCode.DATABASE_ERROR;
		}
	}

	/**
	 * Cancels registration of user with given id. This method is intended to be
	 * called by the logged in user who wants to cancel his own registration and
	 * therefore his password is required.
	 * 
	 * @param userId
	 *            id of user whose registration is being canceled
	 * @param password
	 *            log in password of the user whose registration is being
	 *            canceled
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#NO_SUCH_USER NO_SUCH_USER},
	 *         {@link ResultCode#INVALID_PASSWORD INVALID_PASSWORD},
	 *         {@link ResultCode#DATABASE_ERROR DATABASE_ERROR}
	 */
	public ResultCode cancelRegistration(int userId, String password) {

		UserPassword storedPassword = databaseModel.getUserPassword(userId);
		if (storedPassword == null) {
			return ResultCode.NO_SUCH_USER;
		}

		String passwordHash = calculatePasswordHash(password, storedPassword.getSalt());
		if (storedPassword.getHash().equalsIgnoreCase(passwordHash) == false) {
			return ResultCode.INVALID_PASSWORD;
		}

		if (databaseModel.deleteUser(userId)) {
			return ResultCode.OK;
		} else {
			LOGGER.error("DB error: Failed to delete user with id={}.", userId);
			return ResultCode.DATABASE_ERROR;
		}
	}

	/**
	 * Changes password for user with the given id. This method is intended to
	 * be called by the logged in user who wants to change his own password and
	 * therefore his old password is required.
	 * 
	 * @param userId
	 *            id of user whose password is being changed
	 * @param oldPassword
	 *            current users password
	 * @param newPassword
	 *            new password
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#INVALID_PASSWORD INVALID_PASSWORD} (bad old
	 *         password), {@link ResultCode#DATABASE_ERROR DATABASE_ERROR}
	 */
	public ResultCode changePassword(int userId, String oldPassword, String newPassword) {

		if (!isPasswordValid(userId, oldPassword)) {
			return ResultCode.INVALID_PASSWORD;
		}

		UserPassword userPassword = createUserPassword(newPassword);

		boolean ok = databaseModel.setUserPassword(userId, userPassword);
		if (!ok) {
			LOGGER.error("DB error: Failed to set password for user with id={}.", userId);
			return ResultCode.DATABASE_ERROR;
		}

		return ResultCode.OK;
	}

	/**
	 * A convenience method for checking the password from user input during
	 * registration, password changing, etc. Checks the following conditions (in
	 * the exact order): <li>Password cannot be empty. <li>Password has at least
	 * minimal length. <li>Password confirmation is not empty. <li>Password
	 * confirmation matches password.
	 */
	public PasswordCheckResult checkPassword(String password, String passwordConfirmation) {

		if (password == null || password.isEmpty()) {
			return PasswordCheckResult.PASSWORD_EMPTY;
		}

		if (password.length() < properties.getMinPasswordLength()) {
			return PasswordCheckResult.PASSWORD_TOO_SHORT;
		}

		if (passwordConfirmation == null || passwordConfirmation.isEmpty()) {
			return PasswordCheckResult.PASSWORD_CONFIRMATION_EMPTY;
		}

		if (!passwordConfirmation.equals(password)) {
			return PasswordCheckResult.PASSWORD_CONFIRMATION_MISMATCH;
		}

		return PasswordCheckResult.OK;
	}

	/**
	 * Confirms new user registration which has been done manually by an
	 * existing user (with method
	 * {@link #registerUserManually(String, String, int, boolean)
	 * registerUserManually}).
	 * 
	 * @param email
	 *            email of the newly registered user
	 * @param registrationControlCode
	 *            a security code which has been randomly generated during the
	 *            manual registration. It's the same code which has been passed
	 *            to the
	 *            {@link EmailSender#sendManualRegistrationEmail(String, String, int, String, User)
	 *            email sender} during the registration.
	 * @param password
	 *            the user which has been registered by an existing user doesn't
	 *            have a specified password yet, so he needs to specify it when
	 *            confirming the registration
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#NO_SUCH_USER NO_SUCH_USER},
	 *         {@link ResultCode#REGISTRATION_ALREADY_CONFIRMED
	 *         REGISTRATION_ALREADY_CONFIRMED},
	 *         {@link ResultCode#INVALID_REGISTRATION_CONTROL_CODE
	 *         INVALID_REGISTRATION_CONTROL_CODE},
	 *         {@link ResultCode#DATABASE_ERROR DATABASE_ERROR}
	 * @see #registerUserManually(String, String, int, boolean)
	 * @see #confirmRegistration(String, String)
	 * @see EmailSender#sendManualRegistrationEmail(String, String, int, String,
	 *      User)
	 */
	public ResultCode confirmManualRegistration(String email, String registrationControlCode, String password) {

		User user = databaseModel.getUserByEmail(email);

		ResultCode checkResult = checkRegistrationConfirmationPreconditions(user, registrationControlCode);
		if (checkResult != ResultCode.OK) {
			return checkResult;
		}

		boolean ok = databaseModel.confirmUserRegistration(email);
		if (!ok) {
			LOGGER.error("DB error: Failed to confirm registration for email {}.", email);
			return ResultCode.DATABASE_ERROR;
		}

		UserPassword userPassword = createUserPassword(password);
		ok = databaseModel.setUserPassword(user.getId(), userPassword);
		if (!ok) {
			LOGGER.error("DB error: Failed to set password for user with id={}.", user.getId());
			return ResultCode.DATABASE_ERROR;
		}

		return ResultCode.OK;
	}

	/**
	 * Confirms the registration of the user who has registered himself.
	 * 
	 * @param email
	 *            email which the user used to register his account
	 * @param registrationControlCode
	 *            a security code which has been randomly generated during the
	 *            registration. It's the same code which has been passed to the
	 *            {@link EmailSender#sendRegistrationEmail(String, String, int, String)
	 *            email sender}.
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#NO_SUCH_USER NO_SUCH_USER},
	 *         {@link ResultCode#REGISTRATION_ALREADY_CONFIRMED
	 *         REGISTRATION_ALREADY_CONFIRMED},
	 *         {@link ResultCode#INVALID_REGISTRATION_CONTROL_CODE
	 *         INVALID_REGISTRATION_CONTROL_CODE},
	 *         {@link ResultCode#DATABASE_ERROR DATABASE_ERROR}
	 * @see #registerUser(String, String, String, boolean)
	 * @see #confirmManualRegistration(String, String, String)
	 * @see EmailSender#sendRegistrationEmail(String, String, int, String)
	 */
	public ResultCode confirmRegistration(String email, String registrationControlCode) {

		User user = databaseModel.getUserByEmail(email);

		ResultCode checkResult = checkRegistrationConfirmationPreconditions(user, registrationControlCode);
		if (checkResult != ResultCode.OK) {
			return checkResult;
		}

		boolean ok = databaseModel.confirmUserRegistration(email);
		if (!ok) {
			LOGGER.error("DB error: Failed to confirm registration for email {}.", email);
			return ResultCode.DATABASE_ERROR;
		}

		return ResultCode.OK;
	}

	/**
	 * Creates a special token which enables user who forgot his password to
	 * reset it. A token is represented by a unique auto-generated key, which is
	 * passed to {@link EmailSender#sendLostPasswordEmail(String, String) email
	 * sender}. The email sender should send an email containing this key to the
	 * user. This key needs to be passed to method
	 * {@link #resetPassword(String, String, String) resetPassword}.
	 * <p>
	 * All created tokens become invalid in these cases:
	 * <li>after a time period specified by property
	 * {@link #PROP_PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES}.
	 * <li>when the user requests a new password reset token
	 * <li>when the user uses a valid token to reset his password
	 * 
	 * @param email
	 *            email of the user who forgot his password
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#NO_SUCH_USER NO_SUCH_USER},
	 *         {@link ResultCode#DATABASE_ERROR DATABASE_ERROR},
	 *         {@link ResultCode#FAILED_TO_SEND_EMAIL FAILED_TO_SEND_EMAIL}
	 * @see #resetPassword(String, String, String)
	 * @see EmailSender#sendLostPasswordEmail(String, String)
	 */
	public ResultCode createPasswordResetToken(String email) {

		int userId = databaseModel.getUserIdByEmail(email);
		if (userId < 0) {
			return ResultCode.NO_SUCH_USER;
		}

		boolean ok = databaseModel.cancelAllPasswordResetTokens(userId);
		if (!ok) {
			LOGGER.warn("DB error: Failed to cancel password reset tokens for user with id={}.", userId);
		}

		String tokenKey = generateRandomMD5Hash();

		PasswordResetToken token = new PasswordResetToken(userId, tokenKey, new Date());

		ok = databaseModel.addPasswordResetToken(token);
		if (!ok) {
			LOGGER.error("DB error: Failed to create password reset token for user with id={}.", userId);
			return ResultCode.DATABASE_ERROR;
		}

		ok = emailSender.sendLostPasswordEmail(email, tokenKey);
		if (!ok) {
			LOGGER.error("Email error: Failed to send lost password email to user with id={}.", userId);
			return ResultCode.FAILED_TO_SEND_EMAIL;
		}

		return ResultCode.OK;
	}

	/**
	 * Returns user who is currently logged in.
	 * 
	 * @return user who is currently logged in or null, if no user is logged in
	 */
	public User getCurrentUser() {
		return sessionModel.getCurrentUser();
	}

	/**
	 * Returns number of users who have been registered since the specified
	 * date.
	 * 
	 * @param since
	 *            date since when to count registered users
	 * @return number of users who have been registered since the specified date
	 */
	public int getRegisteredUserCount(Date since) {
		return databaseModel.getRegisteredUserCount(since);
	}

	/**
	 * Checks whether a password reset token is valid (can be used to reset
	 * users password). If a password reset token is invalid, it cannot be used
	 * to reset the password. See description of method
	 * {@link #createPasswordResetToken(String)} for info on when password reset
	 * tokens become invalid.
	 * 
	 * @param email
	 *            email of the user who requested the password reset token
	 * @param tokenKey
	 *            auto generated token key which has been send to the user via
	 *            email
	 * @return true if the token is valid, otherwise false
	 * @see #createPasswordResetToken(String)
	 * @see #resetPassword(String, String, String)
	 */
	public boolean isPasswordResetTokenValid(String email, String tokenKey) {

		PasswordResetToken token = databaseModel.getNewestPasswordResetToken(email);
		if (token == null) {
			return false;
		}

		if (!token.getKey().equalsIgnoreCase(tokenKey)) {
			return false;
		}

		long tokenExpiration = token.getCreationTime().getTime() + properties.getPasswordResetTokenExpirationMinutes() * MILIS_IN_MINUTE;
		long now = new Date().getTime();

		if (now > tokenExpiration) {
			return false;
		}

		return true;
	}

	/**
	 * Checks whether the given password is valid for user with given id.
	 * 
	 * @param userId
	 *            id of user to check
	 * @param password
	 *            password to check
	 * @return true if password is valid, otherwise false
	 */
	public boolean isPasswordValid(int userId, String password) {

		UserPassword storedPassword = databaseModel.getUserPassword(userId);
		if (storedPassword == null) {
			return false;
		}

		String hash = calculatePasswordHash(password, storedPassword.getSalt());

		return storedPassword.getHash().equalsIgnoreCase(hash);
	}

	/**
	 * Logs in a user with specified user name or email. A login record will be
	 * made with information containing time, users ip, and whether the login
	 * was successful or not. If the login is successful, all password reset
	 * tokens for the logged-in user will be canceled.
	 * 
	 * @param userNameOrEmail
	 *            user name or email of the user who is attempting to log in
	 * @param password
	 *            users password. The login will only be successful if the
	 *            password is correct.
	 * @param usersIp
	 *            ip address from which the user is trying to log in. It will be
	 *            part of the login record.
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#NO_SUCH_USER NO_SUCH_USER},
	 *         {@link ResultCode#REGISTRATION_NOT_CONFIRMED REGISTRATION_NOT_CONFIRMED},
	 *         {@link ResultCode#INVALID_PASSWORD INVALID_PASSWORD}
	 * @see #logInWithoutPassword(String)
	 * @see UserDatabaseModel#makeLogInRecord(int, boolean, String)
	 */
	public ResultCode logIn(String userNameOrEmail, String password, String usersIp) {

		User user;

		if (EmailUtils.isEmailValid(userNameOrEmail)) {
			user = databaseModel.getUserByEmail(userNameOrEmail);
		} else {
			user = databaseModel.getUserByName(userNameOrEmail);
		}

		if (user == null) {
			return ResultCode.NO_SUCH_USER;
		}

		if (!user.isRegistrationConfirmed()) {
			return ResultCode.REGISTRATION_NOT_CONFIRMED;
		}

		String passwordHash = calculatePasswordHash(password, user.getPassword().getSalt());
		if (!user.getPassword().getHash().equalsIgnoreCase(passwordHash)) {
			makeLogInRecord(user.getId(), false, usersIp);
			return ResultCode.INVALID_PASSWORD;
		}

		sessionModel.setCurrentUser(user);
		makeLogInRecord(user.getId(), true, usersIp);

		boolean ok = databaseModel.cancelAllPasswordResetTokens(user.getId());
		if (!ok) {
			LOGGER.warn("DB error: Failed to cancel password reset tokens for user with id={}.", user.getId());
		}

		return ResultCode.OK;
	}

	/**
	 * Tries to find a user by the provided social account details. If a user is found, they are returned.
	 * If no user is found, they will be created and returned.
	 * @param details
	 * @return Existing or newly created user.
	 * @since 0.2.0
	 */
	public User loginOrRegisterWithSocialAccount(SocialAccountDetails details, String usersIp) {

		User user = databaseModel.getUserBySocialAccount(details);
		if (user == null) {
			user = createUserWithSocialAccount(details);
			int newId = databaseModel.saveUserWithSocialAccount(user, details);
			user = user.withId(newId);
		}

		sessionModel.setCurrentUser(user);
		makeLogInRecord(user.getId(), true, usersIp);

		return user;
	}

	/**
	 * Enables to log in as any user without knowing his password. This method is intended for testing/administration
	 * purposes only and should not be publicly accessible via the application UI. It does not make any login records
	 * nor does it delete existing password reset tokens.
	 * @param userNameOrEmail
	 * @return {@link ResultCode#NO_SUCH_USER}, {@link ResultCode#REGISTRATION_NOT_CONFIRMED}
	 */
	public ResultCode logInWithoutPassword(String userNameOrEmail) {

		User user;

		if (EmailUtils.isEmailValid(userNameOrEmail)) {
			user = databaseModel.getUserByEmail(userNameOrEmail);
		} else {
			user = databaseModel.getUserByName(userNameOrEmail);
		}

		if (user == null) {
			return ResultCode.NO_SUCH_USER;
		}

		if (!user.isRegistrationConfirmed()) {
			return ResultCode.REGISTRATION_NOT_CONFIRMED;
		}

		sessionModel.setCurrentUser(user);

		return ResultCode.OK;
	}

	/**
	 * Logs out current user.
	 */
	public void logOut() {
		sessionModel.setCurrentUser(null);
	}

	/**
	 * Creates an account for with the specified email address. In order to
	 * succeed, there must not already be an account registered with the given
	 * email address.
	 * 
	 * @param email
	 *            email address which will be used to register the new account
	 * @param name
	 *            name of the user who is registering the account, can be null.
	 *            If its not null, there must not already be an account
	 *            registered with the same name, otherwise the registration will
	 *            fail.
	 * @param password
	 *            login password for the new account
	 * @param registrationConfirmed
	 *            specifies whether the registration will be confirmed. If true,
	 *            the account will not require any confirmation and the user can
	 *            immediately log in. If false, the user will not be able to log
	 *            in to the newly created account until he
	 *            {@link #confirmRegistration(String, String) confirms} his
	 *            registration. In this case, a registration
	 *            {@link EmailSender#sendRegistrationEmail(String, String, int, String)
	 *            email} containing a randomly generated confirmation code will
	 *            be sent to the given email address.
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#USER_NAME_ALREADY_REGISTERED USER_NAME_ALREADY_REGISTERED},
	 *         {@link ResultCode#EMAIL_ALREADY_REGISTERED EMAIL_ALREADY_REGISTERED},
	 *         {@link ResultCode#DATABASE_ERROR DATABASE_ERROR}
	 *         {@link ResultCode#FAILED_TO_SEND_EMAIL FAILED_TO_SEND_EMAIL}
	 * @see #confirmRegistration(String, String)
	 * @see #registerUserManually(String, String, int, boolean)
	 * @see EmailSender#sendRegistrationEmail(String, String, int, String)
	 */
	public ResultCode registerUser(String email, String name, String password, boolean registrationConfirmed) {

		ResultCode checkResult = checkRegistrationPreconditions(email, name);
		if (checkResult != ResultCode.OK) {
			return checkResult;
		}

		String controlCode = generateRandomMD5Hash();

		UserPassword userPassword = createUserPassword(password);

		User newUser = new User(-1, name, email, controlCode, registrationConfirmed, userPassword, UserRanks.NORMAL_USER);

		int newUserId = databaseModel.addUser(newUser);
		if (newUserId < 0) {
			LOGGER.error("DB error: Failed to add user: name={}, email={}.", name, email);
			return ResultCode.DATABASE_ERROR;
		}

		if (registrationConfirmed) {
			// don't send registration email
			return ResultCode.OK;
		}

		if (emailSender.sendRegistrationEmail(email, name, newUserId, controlCode)) {
			return ResultCode.OK;
		} else {
			LOGGER.error("Email error: Failed to send registration email to user with id={}.", newUserId);
			return ResultCode.FAILED_TO_SEND_EMAIL;
		}
	}

	/**
	 * This method is used to create new accounts by existing users (for
	 * instance the web administrators). The new account will not have a defined
	 * password and will be marked as "unconfirmed" (the user won't be able to
	 * log in). In order for the user to log in, the registration has to be
	 * confirmed via method {@link #confirmManualRegistration} which requires
	 * the generated registration confirmation code, and a user specified
	 * password. The confirmation code will be sent via
	 * {@link EmailSender#sendManualRegistrationEmail(String, String, int, String, User)
	 * email} to the specified email address.
	 * 
	 * @param email
	 *            email address which will be used to register the new account
	 * @param name
	 *            name of the user who is registered with the given email
	 *            address, can be null. If its not null, there must not already
	 *            be an account registered with the same name, otherwise the
	 *            registration will fail.
	 * @param rank
	 *            rank of the newly created user. This is just a convenience
	 *            number to provide a primitive version of access control
	 *            functionality. It does not affect any functionality of the
	 *            user module in any way. You can use predefined constants in
	 *            class {@link UserRanks}, or your own values.
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#USER_NAME_ALREADY_REGISTERED USER_NAME_ALREADY_REGISTERED},
	 *         {@link ResultCode#EMAIL_ALREADY_REGISTERED EMAIL_ALREADY_REGISTERED},
	 *         {@link ResultCode#DATABASE_ERROR DATABASE_ERROR}
	 *         {@link ResultCode#FAILED_TO_SEND_EMAIL FAILED_TO_SEND_EMAIL}
	 * @see #registerUser(String, String, String, boolean)
	 * @see #confirmManualRegistration(String, String, String)
	 * @see EmailSender#sendManualRegistrationEmail(String, String, int, String,
	 *      User)
	 */
	public ResultCode registerUserManually(String email, String name, int rank) {

		ResultCode checkResult = checkRegistrationPreconditions(email, name);
		if (checkResult != ResultCode.OK) {
			return checkResult;
		}

		String controlCode = generateRandomMD5Hash();

		UserPassword userPassword = createUserPassword("");

		User newUser = new User(-1, name, email, controlCode, false, userPassword, rank);

		int newUserId = databaseModel.addUser(newUser);
		if (newUserId < 0) {
			LOGGER.error("DB error: Failed to add user: name={}, email={}.", name, email);
			return ResultCode.DATABASE_ERROR;
		}

		User registrator = sessionModel.getCurrentUser();

		if (emailSender.sendManualRegistrationEmail(email, name, newUserId, controlCode, registrator)) {
			return ResultCode.OK;
		} else {
			LOGGER.error("Email error: Failed to send manual registration email to user with id={}.", newUserId);
			return ResultCode.FAILED_TO_SEND_EMAIL;
		}
	}

	/**
	 * Resends the registration email containing the registration control code
	 * in case the email failed to send during registration, or if the user did
	 * not receive the registration email for some reason.
	 * 
	 * @param address
	 *            email address to which the email will be sent. There must be
	 *            an account registered with the given address.
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#NO_SUCH_USER NO_SUCH_USER},
	 *         {@link ResultCode#REGISTRATION_ALREADY_CONFIRMED REGISTRATION_ALREADY_CONFIRMED},
	 *         {@link ResultCode#FAILED_TO_SEND_EMAIL FAILED_TO_SEND_EMAIL}
	 */
	public ResultCode resendRegistrationEmail(String address) {

		User user = databaseModel.getUserByEmail(address);
		if (user == null) {
			return ResultCode.NO_SUCH_USER;
		}

		if (user.isRegistrationConfirmed()) {
			return ResultCode.REGISTRATION_ALREADY_CONFIRMED;
		}

		if (emailSender.sendRegistrationEmail(address, user.getName(), user.getId(), user.getRegistrationControlCode())) {
			return ResultCode.OK;
		} else {
			LOGGER.error("Email error: Failed to re-send registration email to user with id={}.", user.getId());
			return ResultCode.FAILED_TO_SEND_EMAIL;
		}
	}

	/**
	 * Enables the user who forgot their password to set a new password. In
	 * order to reset their password, the user first needs to
	 * {@link #createPasswordResetToken(String) request a password reset token}.
	 * 
	 * @param userEmail
	 *            email address of the account which the user forgot the
	 *            password to
	 * @param tokenKey
	 *            a random generated token key which has been send to the user
	 *            via {@link EmailSender#sendLostPasswordEmail(String, String)
	 *            email}
	 * @param newPassword
	 *            new password
	 * @return {@link ResultCode#OK OK} on success or these possible errors:
	 *         {@link ResultCode#NO_SUCH_USER NO_SUCH_USER},
	 *         {@link ResultCode#DATABASE_ERROR DATABASE_ERROR}
	 * @see #createPasswordResetToken(String)
	 * @see EmailSender#sendLostPasswordEmail(String, String)
	 */
	public ResultCode resetPassword(String userEmail, String tokenKey, String newPassword) {

		boolean tokenValid = isPasswordResetTokenValid(userEmail, tokenKey);
		if (!tokenValid) {
			return ResultCode.NO_VALID_PASSWORD_RESET_TOKEN;
		}

		int userId = databaseModel.getUserIdByEmail(userEmail);
		if (userId < 0) {
			return ResultCode.NO_SUCH_USER;
		}

		UserPassword userPassword = createUserPassword(newPassword);

		if (!databaseModel.setUserPassword(userId, userPassword)) {
			LOGGER.error("DB error: Failed to set password for user with id={}", userId);
			return ResultCode.DATABASE_ERROR;
		}

		if (!databaseModel.cancelAllPasswordResetTokens(userId)) {
			LOGGER.warn("DB error: Failed to cancel password reset tokens for user with id={}", userId);
		}

		return ResultCode.OK;
	}

	/**
	 * Sends a testing email of specified type (registration/lost password)
	 * containing fake data. It is intended to be called by web masters to
	 * test/debug email sending functionality.
	 * 
	 * @param emailType
	 *            type of email to send (registration, lost password, ...)
	 * @param address
	 *            email address to which the email will be sent
	 * @return True on success, otherwise false.
	 */
	public boolean sendTestingEmail(EmailType emailType, String address) {
		switch (emailType) {
			case REGISTRATION:
				return emailSender.sendRegistrationEmail(address, "[userName]", 0, "63ab83e73fee9c2113f625fab4ac8c65");
			case MANUAL_REGISTRATION:
				User registrator = new User(0, "[adminName]", "admin@test.com", "", true, null, UserRanks.ADMIN);
				return emailSender.sendManualRegistrationEmail(address, "[userName]", 0, "63ab83e73fee9c2113f625fab4ac8c65", registrator);
			case LOST_PASSWORD:
				return emailSender.sendLostPasswordEmail(address, "63ab83e73fee9c2113f625fab4ac8c65");
			default:
				return false;
		}
	}

	/**
	 * Creates a new instance of UserManager. The constructor creates new
	 * instances of random generators, which is quite an expensive operation, so
	 * you should prefer creating and keeping just a single instance of
	 * UserManager over creating a new instance every time you need its
	 * functionality.
	 */
	public UserManager() {

		try {
			this.randomGenerator = SecureRandom.getInstance("SHA1PRNG");
			this.sha256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			LOGGER.warn("Failed to create random generator.", e);
		}

	}

}
