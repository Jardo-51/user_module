package com.jardo.usermodule;

import java.io.Serializable;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import com.jardo.usermodule.containers.PasswordResetToken;
import com.jardo.usermodule.containers.User;
import com.jardo.usermodule.containers.UserPassword;
import com.jardo.usermodule.defines.EmailType;

public class UserManager implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final int PASSWORD_RESET_TOKEN_EXPIRATION_TIME_IN_MINUTES = 15;

	private final UserDatabaseModel databaseModel;

	private final EmailSender emailSender;

	private final SessionModel sessionModel;

	private SecureRandom randomGenerator;

	private MessageDigest sha256;

	private UserPassword createUserPassword(String password) {
		String salt = generatePasswordSalt();
		String hash = calculatePasswordHash(password, salt);

		return new UserPassword(salt, hash);
	}

	private String generateRandomMD5Hash() {
		byte[] bytes = new byte[16];
		randomGenerator.nextBytes(bytes);
		return toHex(bytes);
	}

	private InetAddress getUsersIp() {
		//TODO:
		return null;
	}

	private boolean makeLogInRecord(int userId, boolean logInSuccessfull) {
		return databaseModel.makeLogInRecord(userId, logInSuccessfull, getUsersIp());
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
		return toHex( sha256.digest(password.getBytes()) );
	}

	protected String generatePasswordSalt() {
		byte[] salt = new byte[32];
		randomGenerator.nextBytes(salt);
		return toHex(salt);
	}

	protected boolean isEmail(String string) {
		return string.matches("[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})");
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
			return ResultCode.INVALID_CREDENTIALS;
		}

		if (databaseModel.deleteUser(userId))
			return ResultCode.OK;
		else
			return ResultCode.DATABASE_ERROR;
	}

	public ResultCode changePassword(int userId, String oldPassword, String newPassword) {

		if (!isPasswordValid(userId, oldPassword)) {
			return ResultCode.INVALID_CREDENTIALS;
		}

		UserPassword userPassword = createUserPassword(newPassword);

		boolean ok = databaseModel.setUserPassword(userId, userPassword);
		if (!ok) {
			return ResultCode.DATABASE_ERROR;
		}

		return ResultCode.OK;
	}

	public ResultCode confirmRegistration(String email, String registrationControlCode) {

		User user = databaseModel.getUserByEmail(email);
		if (user == null) {
			return ResultCode.NO_SUCH_USER;
		}

		if (user.isRegistrationConfirmed()) {
			return ResultCode.REGISTRATION_ALREADY_CONFIRMED;
		}

		if (!user.getRegistrationControlCode().equalsIgnoreCase(registrationControlCode)) {
			return ResultCode.INVALID_REGISTRATION_CONTROL_CODE;
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

		long tokenExpiration = token.getCreationTime().getTime() + PASSWORD_RESET_TOKEN_EXPIRATION_TIME_IN_MINUTES * 60000L;
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

	public ResultCode logIn(String userNameOrEmail, String password) {

		User user;

		if (isEmail(userNameOrEmail)) {
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
			makeLogInRecord(user.getId(), false);
			return ResultCode.INVALID_CREDENTIALS;
		}

		sessionModel.setCurrentUser(user);
		makeLogInRecord(user.getId(), true);
		databaseModel.cancelAllPasswordResetTokens(user.getId());

		return ResultCode.OK;
	}

	public void logOut() {
		sessionModel.setCurrentUser(null);
	}

	public ResultCode registerUser(String email, String name, String password, boolean registrationConfirmed) {

		if (name != null) {
			if (databaseModel.isUserNameRegistered(name)) {
				return ResultCode.USER_NAME_ALREADY_REGISTERED;
			}
		}

		if (databaseModel.isEmailRegistered(email)) {
			return ResultCode.EMAIL_ALREADY_REGISTERED;
		}

		String controlCode = generateRandomMD5Hash();

		String passwordSalt = generatePasswordSalt();

		String passwordHash = calculatePasswordHash(password, passwordSalt);

		UserPassword userPassword = new UserPassword(passwordHash, passwordSalt);

		User newUser = new User(-1, name, email, controlCode, registrationConfirmed, userPassword);

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
			case LOST_PASSWORD:
				return emailSender.sendLostPasswordEmail(address, "63ab83e73fee9c2113f625fab4ac8c65");
			default:
				return false;
		}
	}

	public UserManager(UserDatabaseModel databaseModel, EmailSender emailSender, SessionModel sessionModel) {
		this.databaseModel = databaseModel;
		this.emailSender = emailSender;
		this.sessionModel = sessionModel;

		try {
			this.randomGenerator = SecureRandom.getInstance("SHA1PRNG");
			this.sha256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

}
