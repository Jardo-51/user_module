package com.jardo.usermodule.hbn;

import java.net.InetAddress;
import java.util.Date;

import com.jardo.usermodule.UserDatabaseModel;
import com.jardo.usermodule.containers.PasswordResetToken;
import com.jardo.usermodule.containers.User;
import com.jardo.usermodule.containers.UserPassword;

public class UserDatabaseModelHbn implements UserDatabaseModel {

	public boolean addPasswordResetToken(PasswordResetToken token) {
		// TODO Auto-generated method stub
		return false;
	}

	public int addUser(User newUser) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean cancelAllPasswordResetTokens(int userId) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean confirmUserRegistration(String email) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean deleteUser(int userId) {
		// TODO Auto-generated method stub
		return false;
	}

	public int getRegisteredUserCount(Date since) {
		// TODO Auto-generated method stub
		return 0;
	}

	public PasswordResetToken getNewestPasswordResetToken(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUserByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUserByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getUserIdByEmail(String email) {
		// TODO Auto-generated method stub
		return 0;
	}

	public UserPassword getUserPassword(int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEmailRegistered(String email) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserNameRegistered(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean makeLogInRecord(int userId, boolean logInSuccessfull, InetAddress usersIp) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean setUserPassword(int userId, UserPassword password) {
		// TODO Auto-generated method stub
		return false;
	}

}
