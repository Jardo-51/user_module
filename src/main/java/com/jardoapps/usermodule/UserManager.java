package com.jardoapps.usermodule;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Properties;

import com.jardoapps.usermodule.containers.PasswordResetToken;
import com.jardoapps.usermodule.containers.User;
import com.jardoapps.usermodule.containers.UserPassword;
import com.jardoapps.usermodule.defines.EmailType;
import com.jardoapps.usermodule.utils.EmailUtils;

public class UserManager implements Serializable {

	public static final String PROP_PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES = "passwordResetTokenExpirationMinutes";

	private static final long serialVersionUID = 1L;

	private final UserDatabaseModel databaseModel;

	private final EmailSender emailSender;

	private final SessionModel sessionModel;

	private int passwordResetTokenExpirationMinutes = 15;

	private SecureRandom randomGenerator;

	private MessageDigest sha256;

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

	private String generateRandomMD5Hash() {
		byte[] bytes = new byte[16];
		randomGenerator.nextBytes(bytes);
		return toHex(bytes);
	}

	private boolean makeLogInRecord(int userId, boolean logInSuccessfull, String usersIp) {
		return databaseModel.makeLogInRecord(userId, logInSuccessfull, usersIp);
	}

	private void parseProperties(Properties properties) {

		String property = properties.getProperty(PROP_PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES, "15");
		passwordResetTokenExpirationMinutes = Integer.parseInt(property);

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

	protected String calculatePasswordHash(String password, String salt) {
		sha256.update(salt.getBytes());
		return toHex(sha256.digest(password.getBytes()));
	}

	protected String generatePasswordSalt() {
		byte[] salt = new byte[32];
		randomGenerator.nextBytes(salt);
		return toHex(salt);
	}

	public ResultCode cancelPasswordResetTokens(int userId) {
		boolean ok = databaseModel.cancelAllPasswordResetTokens(userId);
		if (ok) {
			return ResultCode.OK;
		} else {
			return ResultCode.DATABASE_ERROR;
		}
	}

	public ResultCode cancelRegistration(int userId, String password) {

		UserPassword storedPassword = databaseModel.getUserPassword(userId);
		if (storedPassword == null) {
			return ResultCode.NO_SUCH_USER;
		}

		String passwordHash = calculatePasswordHash(password, storedPassword.getSalt());
		if (storedPassword.getHash().equalsIgnoreCase(passwordHash) == false) {
			return ResultCode.INVALID_PASSWORD;
		}

		if (databaseModel.deleteUser(userId))
			return ResultCode.OK;
		else
			return ResultCode.DATABASE_ERROR;
	}

	public ResultCode changePassword(int userId, String oldPassword, String newPassword) {

		if (!isPasswordValid(userId, oldPassword)) {
			return ResultCode.INVALID_PASSWORD;
		}

		UserPassword userPassword = createUserPassword(newPassword);

		boolean ok = databaseModel.setUserPassword(userId, userPassword);
		if (!ok) {
			return ResultCode.DATABASE_ERROR;
		}

		return ResultCode.OK;
	}

	/**
	 * Confirms new user registration which has been done manually by an
	 * existing user (with method
	 * {@link #registerUserManually(String, String, int, boolean)}).
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
	 * @return {@link ResultCode#OK}, {@link ResultCode#NO_SUCH_USER},
	 *         {@link ResultCode#REGISTRATION_ALREADY_CONFIRMED},
	 *         {@link ResultCode#INVALID_REGISTRATION_CONTROL_CODE},
	 *         {@link ResultCode#DATABASE_ERROR}
	 * @see #registerUserManually(String, String, int, boolean)
	 * @see #confirmRegistration(String, String)
	 */
	public ResultCode confirmManualRegistration(String email, String registrationControlCode, String password) {

		User user = databaseModel.getUserByEmail(email);

		ResultCode checkResult = checkRegistrationConfirmationPreconditions(user, registrationControlCode);
		if (checkResult != ResultCode.OK) {
			return checkResult;
		}

		boolean ok = databaseModel.confirmUserRegistration(email);
		if (!ok) {
			return ResultCode.DATABASE_ERROR;
		}

		UserPassword userPassword = createUserPassword(password);
		ok = databaseModel.setUserPassword(user.getId(), userPassword);
		if (!ok) {
			return ResultCode.DATABASE_ERROR;
		}

		return ResultCode.OK;
	}

	public ResultCode confirmRegistration(String email, String registrationControlCode) {

		User user = databaseModel.getUserByEmail(email);

		ResultCode checkResult = checkRegistrationConfirmationPreconditions(user, registrationControlCode);
		if (checkResult != ResultCode.OK) {
			return checkResult;
		}

		boolean ok = databaseModel.confirmUserRegistration(email);
		if (!ok) {
			return ResultCode.DATABASE_ERROR;
		}

		return ResultCode.OK;
	}

	public ResultCode createPasswordResetToken(String email) {

		int userId = databaseModel.getUserIdByEmail(email);
		if (userId < 0) {
			return ResultCode.NO_SUCH_USER;
		}

		databaseModel.cancelAllPasswordResetTokens(userId);

		String tokenKey = generateRandomMD5Hash();

		PasswordResetToken token = new PasswordResetToken(userId, tokenKey, new Date());

		boolean ok = databaseModel.addPasswordResetToken(token);
		if (!ok) {
			return ResultCode.DATABASE_ERROR;
		}

		ok = emailSender.sendLostPasswordEmail(email, tokenKey);
		if (!ok) {
			return ResultCode.FAILED_TO_SEND_EMAIL;
		}

		return ResultCode.OK;
	}

	public User getCurrentUser() {
		return sessionModel.getCurrentUser();
	}

	public int getRegisteredUserCount(Date since) {
		return databaseModel.getRegisteredUserCount(since);
	}

	public boolean isPasswordResetTokenValid(String email, String tokenKey) {

		PasswordResetToken token = databaseModel.getNewestPasswordResetToken(email);
		if (token == null) {
			return false;
		}

		if (!token.getKey().equalsIgnoreCase(tokenKey)) {
			return false;
		}

		long tokenExpiration = token.getCreationTime().getTime() + passwordResetTokenExpirationMinutes * 60000L;
		long now = new Date().getTime();

		if (now > tokenExpiration) {
			return false;
		}

		return true;
	}

	public boolean isPasswordValid(int userId, String password) {

		UserPassword storedPassword = databaseModel.getUserPassword(userId);
		if (storedPassword == null) {
			return false;
		}

		String hash = calculatePasswordHash(password, storedPassword.getSalt());

		return storedPassword.getHash().equalsIgnoreCase(hash);
	}

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
		databaseModel.cancelAllPasswordResetTokens(user.getId());

		return ResultCode.OK;
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

	public void logOut() {
		sessionModel.setCurrentUser(null);
	}

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
			return ResultCode.DATABASE_ERROR;
		}

		if (registrationConfirmed) {
			// don't send registration email
			return ResultCode.OK;
		}

		if (emailSender.sendRegistrationEmail(email, name, newUserId, controlCode)) {
			return ResultCode.OK;
		} else {
			return ResultCode.FAILED_TO_SEND_EMAIL;
		}
	}

	public ResultCode registerUserManually(String email, String name, int rank, boolean registrationConfirmed) {

		ResultCode checkResult = checkRegistrationPreconditions(email, name);
		if (checkResult != ResultCode.OK) {
			return checkResult;
		}

		String controlCode = generateRandomMD5Hash();

		UserPassword userPassword = createUserPassword("");

		User newUser = new User(-1, name, email, controlCode, registrationConfirmed, userPassword, rank);

		int newUserId = databaseModel.addUser(newUser);
		if (newUserId < 0) {
			return ResultCode.DATABASE_ERROR;
		}

		User registrator = sessionModel.getCurrentUser();

		if (emailSender.sendManualRegistrationEmail(email, name, newUserId, controlCode, registrator)) {
			return ResultCode.OK;
		} else {
			return ResultCode.FAILED_TO_SEND_EMAIL;
		}
	}

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
			return ResultCode.FAILED_TO_SEND_EMAIL;
		}
	}

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

		if (databaseModel.setUserPassword(userId, userPassword)) {
			return ResultCode.OK;
		} else {
			return ResultCode.DATABASE_ERROR;
		}
	}

	/**
	 * Sends a testing email of specified type (registration/lost password)
	 * containing random fake data. It is intended to be called by web masters
	 * to test/debug email sending functionality.
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

	public UserManager(UserDatabaseModel databaseModel, EmailSender emailSender, SessionModel sessionModel, Properties properties) {
		this.databaseModel = databaseModel;
		this.emailSender = emailSender;
		this.sessionModel = sessionModel;

		try {
			this.randomGenerator = SecureRandom.getInstance("SHA1PRNG");
			this.sha256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		parseProperties(properties);
	}

}
