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

package com.jardoapps.usermodule.hbn.dao;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Query;

import com.jardoapps.usermodule.containers.UserPassword;
import com.jardoapps.usermodule.hbn.entities.UserEntity;

public class UserEntityDao extends CommonDao<UserEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	public boolean confirmRegistration(String email) {
		String queryStr = "UPDATE UserEntity u SET u.registrationConfirmed = true WHERE u.email = :email";
		Query query = createQuery(queryStr);
		query.setParameter("email", email);

		int updatedRows = query.executeUpdate();
		return updatedRows == 1;
	}

	public int getRegisteredUserCount(Date since) {
		if (since == null) {
			since = new Date(0);
		}

		String queryStr = "SELECT count(*) FROM UserEntity u WHERE u.registrationConfirmed = true AND u.deleted = false AND u.registrationDate >= :since";

		Query query = createQuery(queryStr);
		query.setParameter("since", since);

		Long result = getSingleResult(query);
		return result.intValue();
	}

	public int getUserIdByEmail(String email) {
		String queryStr = "SELECT u.id FROM UserEntity u WHERE u.email= :email AND u.deleted = false";

		Query query = createQuery(queryStr);
		query.setParameter("email", email);

		Integer result = getSingleResult(query);
		if (result == null) {
			return -1;
		}

		return result.intValue();
	}

	public UserPassword getUserPassword(int userId) {
		String queryStr = String.format("SELECT new %s(u.passwordHash, u.passwordSalt) FROM UserEntity u WHERE u.id= :id AND u.deleted = false", UserPassword.class.getName());

		Query query = createQuery(queryStr);
		query.setParameter("id", userId);

		return getSingleResult(query);
	}

	public boolean deleteUserEntity(int userId) {
		String queryStr = "UPDATE UserEntity u SET u.deleted = true WHERE u.id = :userId";

		Query query = createQuery(queryStr);
		query.setParameter("userId", userId);

		int updatedRows = query.executeUpdate();
		return updatedRows == 1;
	}

	public UserEntity findByEmail(String email) {
		String queryStr = "FROM UserEntity u WHERE u.email = :email AND u.deleted = false";

		Query query = createQuery(queryStr);
		query.setParameter("email", email);
		query.setMaxResults(1);

		return getSingleResult(query);
	}

	public UserEntity findByName(String name) {
		String queryStr = "FROM UserEntity u WHERE u.name = :name AND u.deleted = false";

		Query query = createQuery(queryStr);
		query.setParameter("name", name);
		query.setMaxResults(1);

		return getSingleResult(query);
	}

	public boolean isEmailRegistered(String email) {
		String queryStr = "SELECT count(*) FROM UserEntity u WHERE u.email = :email AND u.deleted = false";

		Query query = createQuery(queryStr);
		query.setParameter("email", email);

		Long result = getSingleResult(query);
		return result > 0;
	}

	public boolean isUserNameRegistered(String name) {
		String queryStr = "SELECT count(*) FROM UserEntity u WHERE u.name = :name AND u.deleted = false";

		Query query = createQuery(queryStr);
		query.setParameter("name", name);

		Long result = getSingleResult(query);
		return result > 0;
	}

	public boolean setUserPassword(int userId, UserPassword password) {
		String queryStr = "UPDATE UserEntity u SET u.passwordHash = :hash, u.passwordSalt = :salt WHERE u.id = :userId";

		Query query = createQuery(queryStr);
		query.setParameter("userId", userId);
		query.setParameter("hash", password.getHash());
		query.setParameter("salt", password.getSalt());

		int updatedRows = query.executeUpdate();
		return updatedRows == 1;
	}
}
