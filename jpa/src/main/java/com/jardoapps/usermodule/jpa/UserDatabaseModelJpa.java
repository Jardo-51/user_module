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

package com.jardoapps.usermodule.jpa;

import java.io.Serializable;
import java.util.Date;

import javax.inject.Inject;
import javax.transaction.Transactional;

import com.jardoapps.usermodule.User;
import com.jardoapps.usermodule.UserDatabaseModel;
import com.jardoapps.usermodule.containers.PasswordResetToken;
import com.jardoapps.usermodule.containers.SocialAccountDetails;
import com.jardoapps.usermodule.containers.UserPassword;
import com.jardoapps.usermodule.jpa.dao.LogInRecordEntityDao;
import com.jardoapps.usermodule.jpa.dao.PasswordResetTokenEntityDao;
import com.jardoapps.usermodule.jpa.dao.SocialAccountDao;
import com.jardoapps.usermodule.jpa.dao.UserEntityDao;
import com.jardoapps.usermodule.jpa.entities.LogInRecordEntity;
import com.jardoapps.usermodule.jpa.entities.PasswordResetTokenEntity;
import com.jardoapps.usermodule.jpa.entities.SocialAccountEntity;
import com.jardoapps.usermodule.jpa.entities.UserEntity;

/**
 * A Hibernate implementation of the user database model. It uses internal entity 
 * classes, which are a part of the user module library. In order to use this implementation,
 * you have to tell Hibernate where are the entity classes.
 * This can be done in the hibernate.cfg.xml file:
 * <p>
 * <code>
 * {@literal <mapping class="com.jardoapps.usermodule.jpa.entities.UserEntity" />}<br>
 * {@literal <mapping class="com.jardoapps.usermodule.jpa.entities.PasswordResetTokenEntity" />}<br>
 * {@literal <mapping class="com.jardoapps.usermodule.jpa.entities.LogInRecordEntity" />}
 * </code>
 * <p>
 * If you're using Spring, you can set the SessionFactory's packagesToScan property to
 * <code>com.jardoapps.usermodule.jpa.entities</code>.
 * <p>
 * This class is a part of this library's public API.
 * 
 * @author Jaroslav Brtiš
 * 
 */
public class UserDatabaseModelJpa implements UserDatabaseModel, Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private UserEntityDao userEntityDao;

	@Inject
	private PasswordResetTokenEntityDao passwordResetTokenEntityDao;

	@Inject
	private LogInRecordEntityDao logInRecordEntityDao;

	@Inject
	private SocialAccountDao socialAccountDao;

	@Transactional
	public boolean addPasswordResetToken(PasswordResetToken token) {
		PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity(token);
		tokenEntity.setValid(true);

		passwordResetTokenEntityDao.add(tokenEntity);

		return true;
	}

	@Transactional
	public int addUser(User newUser) {
		UserEntity userEntity = new UserEntity();
		userEntity.copyUser(newUser);
		userEntity.setRegistrationDate(new Date());

		userEntityDao.add(userEntity);

		return userEntity.getId();
	}

	@Transactional
	public boolean cancelAllPasswordResetTokens(int userId) {
		passwordResetTokenEntityDao.cancelTokensForUser(userId);
		return true;
	}

	@Transactional
	public boolean confirmUserRegistration(String email) {
		return userEntityDao.confirmRegistration(email);
	}

	@Transactional
	public boolean deleteUser(int userId) {
		return userEntityDao.deleteUserEntity(userId);
	}

	@Transactional
	public int getRegisteredUserCount(Date since) {
		return userEntityDao.getRegisteredUserCount(since);
	}

	public PasswordResetToken getNewestPasswordResetToken(String email) {
		PasswordResetTokenEntity tokenEntity = passwordResetTokenEntityDao.getNewestToken(email);

		if (tokenEntity == null) {
			return null;
		}

		return new PasswordResetToken(tokenEntity.getUser().getId(), tokenEntity.getKey(), tokenEntity.getTime());
	}

	public User getUserByEmail(String email) {
		UserEntity userEntity = userEntityDao.findByEmail(email);

		if (userEntity == null) {
			return null;
		}

		return userEntity.toUser();
	}

	public User getUserByName(String name) {
		UserEntity userEntity = userEntityDao.findByName(name);

		if (userEntity == null) {
			return null;
		}

		return userEntity.toUser();
	}

	public User getUserBySocialAccount(SocialAccountDetails details) {

		UserEntity userEntity = socialAccountDao.getUserByAccount(details.getAccountType(), details.getUserId());

		if (userEntity == null) {
			return null;
		}

		return userEntity.toUser();
	}

	public int getUserIdByEmail(String email) {
		return userEntityDao.getUserIdByEmail(email);
	}

	public UserPassword getUserPassword(int userId) {
		return userEntityDao.getUserPassword(userId);
	}

	public boolean isEmailRegistered(String email) {
		return userEntityDao.isEmailRegistered(email);
	}

	public boolean isUserNameRegistered(String name) {
		return userEntityDao.isUserNameRegistered(name);
	}

	@Transactional
	public boolean makeLogInRecord(int userId, boolean logInSuccessfull, String usersIp) {

		UserEntity user = new UserEntity();
		user.setId(userId);

		LogInRecordEntity logInRecordEntity = new LogInRecordEntity();
		logInRecordEntity.setUser(user);
		logInRecordEntity.setTime(new Date());
		logInRecordEntity.setIp(usersIp);
		logInRecordEntity.setSuccessful(logInSuccessfull);

		logInRecordEntityDao.add(logInRecordEntity);

		return true;
	}

	@Transactional
	public int saveUserWithSocialAccount(User user, SocialAccountDetails details) {

		UserEntity userEntity = new UserEntity();
		userEntity.copyUser(user);
		userEntity.setRegistrationDate(new Date());

		userEntityDao.add(userEntity);

		SocialAccountEntity socialAccountEntity = new SocialAccountEntity();
		socialAccountEntity.setUser(userEntity);
		socialAccountEntity.setAccountType(details.getAccountType());
		socialAccountEntity.setOriginalAccountId(details.getUserId());

		socialAccountDao.add(socialAccountEntity);

		return userEntity.getId();
	}

	@Transactional
	public boolean setUserPassword(int userId, UserPassword password) {
		return userEntityDao.setUserPassword(userId, password);
	}

}
