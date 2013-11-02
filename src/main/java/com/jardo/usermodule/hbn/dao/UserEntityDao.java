package com.jardo.usermodule.hbn.dao;

import org.hibernate.Query;
import org.hibernate.Session;

import com.jardo.usermodule.hbn.entities.UserEntity;

public class UserEntityDao extends CommonDao<UserEntity> {

	public boolean deleteUserEntity(Session session, int userId) {
		String queryStr = "UPDATE UserEntity u SET u.deleted = true WHERE u.id = :userId";

		Query query = session.createQuery(queryStr);
		query.setParameter("userId", userId);

		int updatedRows = query.executeUpdate();
		return updatedRows == 1;
	}
}
