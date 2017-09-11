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

package com.jardoapps.usermodule.jpa.dao;

import java.io.Serializable;

import javax.persistence.Query;

import com.jardoapps.usermodule.jpa.entities.PasswordResetTokenEntity;

public class PasswordResetTokenEntityDao extends CommonDao<PasswordResetTokenEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	public void cancelTokensForUser(int userId) {
		String queryStr = "UPDATE PasswordResetTokenEntity prt SET prt.valid = false WHERE prt.user.id = :userId";

		Query query = createQuery(queryStr);
		query.setParameter("userId", userId);
		query.executeUpdate();
	}

	public PasswordResetTokenEntity getNewestToken(String email) {
		String queryStr = "FROM PasswordResetTokenEntity prt WHERE prt.user.email = :email ORDER BY prt.time DESC";

		Query query = createQuery(queryStr);
		query.setParameter("email", email);
		query.setMaxResults(1);

		return getSingleResult(query);
	}

}
