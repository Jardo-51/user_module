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

package com.jardoapps.usermodule.hbn;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.jardoapps.usermodule.User;
import com.jardoapps.usermodule.UserDatabaseModel;
import com.jardoapps.usermodule.containers.PasswordResetToken;
import com.jardoapps.usermodule.containers.UserPassword;
import com.jardoapps.usermodule.hbn.dao.LogInRecordEntityDao;
import com.jardoapps.usermodule.hbn.dao.PasswordResetTokenEntityDao;
import com.jardoapps.usermodule.hbn.dao.UserEntityDao;
import com.jardoapps.usermodule.hbn.entities.LogInRecordEntity;
import com.jardoapps.usermodule.hbn.entities.PasswordResetTokenEntity;
import com.jardoapps.usermodule.hbn.entities.UserEntity;

public class UserDatabaseModelHbn implements UserDatabaseModel, Serializable {

	private static final long serialVersionUID = 1L;

	private final SessionFactory sessionFactory;

	private final UserEntityDao userEntityDao;

	private final PasswordResetTokenEntityDao passwordResetTokenEntityDao;

	private final LogInRecordEntityDao logInRecordEntityDao;

	private void closeSession(Session session) {
		session.getTransaction().commit();
		session.close();
	}

	private Session openSession() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		return session;
	}

	public UserDatabaseModelHbn(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
		this.userEntityDao = new UserEntityDao();
		this.passwordResetTokenEntityDao = new PasswordResetTokenEntityDao();
		this.logInRecordEntityDao = new LogInRecordEntityDao();
	}

	public boolean addPasswordResetToken(PasswordResetToken token) {
		PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity(token);
		tokenEntity.setValid(true);

		Session session = openSession();
		passwordResetTokenEntityDao.add(session, tokenEntity);
		closeSession(session);

		return true;
	}

	public int addUser(User newUser) {
		UserEntity userEntity = new UserEntity();
		userEntity.copyUser(newUser);
		userEntity.setRegistrationDate(new Date());

		Session session = openSession();
		userEntityDao.add(session, userEntity);
		closeSession(session);

		return userEntity.getId();
	}

	public boolean cancelAllPasswordResetTokens(int userId) {
		Session session = openSession();
		passwordResetTokenEntityDao.cancelTokensForUser(session, userId);
		closeSession(session);
		return true;
	}

	public boolean confirmUserRegistration(String email) {
		Session session = openSession();
		boolean result = userEntityDao.confirmRegistration(session, email);
		closeSession(session);
		return result;
	}

	public boolean deleteUser(int userId) {
		Session session = openSession();
		boolean result = userEntityDao.deleteUserEntity(session, userId);
		closeSession(session);
		return result;
	}

	public int getRegisteredUserCount(Date since) {
		Session session = openSession();
		int result = userEntityDao.getRegisteredUserCount(session, since);
		closeSession(session);

		return result;
	}

	public PasswordResetToken getNewestPasswordResetToken(String email) {
		Session session = openSession();
		PasswordResetTokenEntity tokenEntity = passwordResetTokenEntityDao.getNewestToken(session, email);
		closeSession(session);

		if (tokenEntity == null) {
			return null;
		}

		return new PasswordResetToken(tokenEntity.getUser().getId(), tokenEntity.getKey(), tokenEntity.getTime());
	}

	public User getUserByEmail(String email) {
		Session session = openSession();
		UserEntity userEntity = userEntityDao.findByEmail(session, email);
		closeSession(session);

		if (userEntity == null) {
			return null;
		}

		return userEntity.toUser();
	}

	public User getUserByName(String name) {
		Session session = openSession();
		UserEntity userEntity = userEntityDao.findByName(session, name);
		closeSession(session);

		if (userEntity == null) {
			return null;
		}

		return userEntity.toUser();
	}

	public int getUserIdByEmail(String email) {
		Session session = openSession();
		int id = userEntityDao.getUserIdByEmail(session, email);
		closeSession(session);
		return id;
	}

	public UserPassword getUserPassword(int userId) {
		Session session = openSession();
		UserPassword password = userEntityDao.getUserPassword(session, userId);
		closeSession(session);
		return password;
	}

	public boolean isEmailRegistered(String email) {
		Session session = openSession();
		boolean result = userEntityDao.isEmailRegistered(session, email);
		closeSession(session);
		return result;
	}

	public boolean isUserNameRegistered(String name) {
		Session session = openSession();
		boolean result = userEntityDao.isUserNameRegistered(session, name);
		closeSession(session);
		return result;
	}

	public boolean makeLogInRecord(int userId, boolean logInSuccessfull, String usersIp) {

		UserEntity user = new UserEntity();
		user.setId(userId);

		LogInRecordEntity logInRecordEntity = new LogInRecordEntity();
		logInRecordEntity.setUser(user);
		logInRecordEntity.setTime(new Date());
		logInRecordEntity.setIp(usersIp);
		logInRecordEntity.setSuccessful(logInSuccessfull);

		Session session = openSession();
		logInRecordEntityDao.add(session, logInRecordEntity);
		closeSession(session);

		return true;
	}

	public boolean setUserPassword(int userId, UserPassword password) {
		Session session = openSession();
		boolean result = userEntityDao.setUserPassword(session, userId, password);
		closeSession(session);
		return result;
	}

}
