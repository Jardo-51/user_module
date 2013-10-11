package com.jardo.usermodule;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import com.jardo.usermodule.containers.PasswordResetToken;
import com.jardo.usermodule.containers.User;
import com.jardo.usermodule.containers.UserPassword;
import com.jardo.usermodule.defines.EmailType;

public class UserManager {

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
		return string.matches("/^.+@.+\\..+$/");
	}

	public boolean cancelPasswordResetTokens(int userId) {
		return databaseModel.cancelAllPasswordResetTokens(userId);
	}

	public boolean cancelRegistration(int userId, String password) {

		UserPassword storedPassword = databaseModel.getUserPassword(userId);
		if (storedPassword == null) {
			return false;
		}

		String passwordHash = calculatePasswordHash(password, storedPassword.getSalt());
		if (storedPassword.getHash().equalsIgnoreCase(passwordHash) == false) {
			return false;
		}

		return databaseModel.deleteUser(userId);
	}

	public boolean changePassword(int userId, String oldPassword, String newPassword) {

		if (!isPasswordValid(userId, oldPassword)) {
			return false;
		}

		UserPassword userPassword = createUserPassword(newPassword);

		boolean ok = databaseModel.setUserPassword(userId, userPassword);
		if (!ok) {
			return false;
		}

		return true;
	}

	public boolean confirmRegistration(String email, String registrationControlCode) {

		User user = databaseModel.getUserByEmail(email);
		if (user == null) {
			return false;
		}

		if (!user.getRegistrationControlCode().equalsIgnoreCase(registrationControlCode)) {
			return false;
		}

		boolean ok = databaseModel.confirmUserRegistration(email);
		if (!ok) {
			return false;
		}

		return true;
	}

	public boolean createPasswordResetToken(String email) {

		int userId = databaseModel.getUserIdByEmail(email);
		if (userId < 0) {
			return false;
		}

		databaseModel.cancelAllPasswordResetTokens(userId);

		String tokenKey = generateRandomMD5Hash();

		PasswordResetToken token = new PasswordResetToken(userId, tokenKey, new Date());

		boolean ok = databaseModel.addPasswordResetToken(token);
		if (!ok) {
			return false;
		}

		ok = emailSender.sendLostPasswordEmail(email, tokenKey);
		if (!ok) {
			return false;
		}

		return true;
	}

	public User getCurrentUser() {
		return sessionModel.getCurrentUser();
	}

	public int getRegisteredUserCount(Date since) {
		return databaseModel.getRegisteredUserCount(since);
	}

	public boolean isPasswordValid(int userId, String password) {

		UserPassword storedPassword = databaseModel.getUserPassword(userId);
		if (storedPassword == null) {
			return false;
		}

		String hash = calculatePasswordHash(password, storedPassword.getSalt());

		return storedPassword.getHash().equalsIgnoreCase(hash);
	}

	public boolean logIn(String userNameOrEmail, String password) {

		User user;

		if (isEmail(userNameOrEmail)) {
			user = databaseModel.getUserByEmail(userNameOrEmail);
		} else {
			user = databaseModel.getUserByName(userNameOrEmail);
		}

		if (user == null) {
			return false;
		}

		if (!user.isRegistrationConfirmed()) {
			return false;
		}

		String passwordHash = calculatePasswordHash(password, user.getPassword().getSalt());
		if (!user.getPassword().getHash().equalsIgnoreCase(passwordHash)) {
			makeLogInRecord(user.getId(), false);
			return false;
		}

		sessionModel.setCurrentUser(user);
		makeLogInRecord(user.getId(), true);
		databaseModel.cancelAllPasswordResetTokens(user.getId());

		return true;
	}

	public void logOut() {
		sessionModel.setCurrentUser(null);
	}

	public boolean registerUser(String email, String name, String password, boolean registrationConfirmed) {

		if (name != null) {
			if (databaseModel.isUserNameRegistered(name)) {
				return false;
			}
		}

		if (databaseModel.isEmailRegistered(email)) {
			return false;
		}

		String controlCode = generateRandomMD5Hash();

		String passwordSalt = generatePasswordSalt();

		String passwordHash = calculatePasswordHash(password, passwordSalt);

		UserPassword userPassword = new UserPassword(passwordHash, passwordSalt);

		User newUser = new User(-1, name, email, controlCode, registrationConfirmed, userPassword);

		int newUserId = databaseModel.addUser(newUser);
		if (newUserId < 0) {
			return false;
		}

		if (registrationConfirmed) {
			// don't send registration email
			return true;
		}

		if (emailSender.sendRegistrationEmail(email, name, newUserId, controlCode)) {
			return true;
		} else {
			databaseModel.deleteUser(newUserId);
			return false;
		}
	}

	public boolean resendRegistrationEmail(String address) {

		User user = databaseModel.getUserByEmail(address);
		if (user == null) {
			return false;
		}

		if (user.isRegistrationConfirmed()) {
			return false;
		}

		if (emailSender.sendRegistrationEmail(address, user.getName(), user.getId(), user.getRegistrationControlCode())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean resetPassword(String userEmail, String tokenKey, String newPassword) {

		PasswordResetToken token = databaseModel.getNewestPasswordResetToken(userEmail);
		if (token == null) {
			return false;
		}

		long tokenExpiration = token.getCreationTime().getTime() + PASSWORD_RESET_TOKEN_EXPIRATION_TIME_IN_MINUTES
				* 60000L;
		long now = new Date().getTime();

		if (now > tokenExpiration) {
			return false;
		}

		int userId = databaseModel.getUserIdByEmail(userEmail);
		if (userId < 0) {
			return false;
		}

		UserPassword userPassword = createUserPassword(newPassword);

		if (databaseModel.setUserPassword(userId, userPassword)) {
			return true;
		} else {
			return false;
		}
	}

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
