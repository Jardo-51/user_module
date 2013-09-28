package com.jardo.usermodule;

import java.util.Date;

import com.jardo.usermodule.containers.User;
import com.jardo.usermodule.defines.EmailType;

public class UserManager {

	private final UserDatabaseModel databaseModel;

	private final EmailSender emailSender;

	private final SessionModel sessionModel;

	private boolean isPasswordResetTokenValid(String userEmail, String tokenKey) {
		return false;
	}

	private boolean makeLogInRecord() {
		return false;
	}

	protected String calculatePasswordHash(String password, String salt) {
		return null;
	}

	protected String generatePasswordSalt() {
		return null;
	}

	public boolean cancelPasswordResetTokens(int userId) {
		return false;
	}

	public boolean cancelRegistration(int userId, String password) {
		return false;
	}

	public boolean confirmRegistration(String email, String registrationControlCode) {
		return false;
	}

	public boolean createPasswordResetToken(String email) {
		return false;
	}

	public User getCurrentUser() {
		return null;
	}

	public int getRegisteredUserCount(Date since) {
		return 0;
	}

	public boolean changePassword(int userId, String oldPassword, String newPassword) {
		return false;
	}

	public boolean isPasswordValid(int userId, String password) {
		return false;
	}

	public boolean logIn(String userNameOrEmail, String password) {
		return false;
	}

	public void logOut() {

	}

	public boolean registerUser(String email, String name, String password, boolean registrationConfirmed) {
		return false;
	}

	public boolean resendRegistrationEmail(String address) {
		return false;
	}

	public boolean resetPassword(String userEmail, String tokenKey, String newPassword) {
		return false;
	}

	public boolean sendTestingEmail(EmailType emailType, String address) {
		return false;
	}

	public UserManager(UserDatabaseModel databaseModel, EmailSender emailSender, SessionModel sessionModel) {
		this.databaseModel = databaseModel;
		this.emailSender = emailSender;
		this.sessionModel = sessionModel;
	}

}
