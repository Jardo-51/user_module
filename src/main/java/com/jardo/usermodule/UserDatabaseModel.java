package com.jardo.usermodule;

import java.net.InetAddress;
import java.util.Date;

import com.jardo.usermodule.containers.PasswordResetToken;
import com.jardo.usermodule.containers.User;
import com.jardo.usermodule.containers.UserPassword;

public interface UserDatabaseModel {

	boolean addPasswordResetToken(PasswordResetToken token);

	int addUser(User newUser);

	boolean cancelAllPasswordResetTokens(int userId);

	boolean confirmUserRegistration(String email);

	boolean deleteUser(int userId);

	int getRegisteredUserCount(Date since);

	PasswordResetToken getNewestPasswordResetToken(String email);

	User getUserByEmail(String email);

	User getUserByName(String name);

	int getUserIdByEmail(String email);

	UserPassword getUserPassword(int userId);

	boolean isEmailRegistered(String email);

	boolean isUserNameRegistered(String name);

	boolean makeLogInRecord(int userId, boolean logInSuccessfull, InetAddress usersIp);

	boolean setUserPassword(int userId, UserPassword password);

}
