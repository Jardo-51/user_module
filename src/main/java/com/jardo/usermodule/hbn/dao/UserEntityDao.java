package com.jardo.usermodule.hbn.dao;

import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;

import com.jardo.usermodule.containers.UserPassword;
import com.jardo.usermodule.hbn.entities.UserEntity;

public class UserEntityDao extends CommonDao<UserEntity> {

	public boolean confirmRegistration(Session session, String email) {
		String queryStr = "UPDATE UserEntity u SET u.registrationConfirmed = true WHERE u.email = :email";
		Query query = session.createQuery(queryStr);
		query.setParameter("email", email);

		int updatedRows = query.executeUpdate();
		return updatedRows == 1;
	}

	public int getRegisteredUserCount(Session session, Date since) {
		if (since == null) {
			since = new Date(0);
		}

		String queryStr = "SELECT count(*) FROM UserEntity u WHERE u.registrationConfirmed = true AND u.deleted = false AND u.registrationDate >= :since";

		Query query = session.createQuery(queryStr);
		query.setParameter("since", since);

		Long result = (Long) query.uniqueResult();
		return result.intValue();
	}

	public int getUserIdByEmail(Session session, String email) {
		String queryStr = "SELECT u.id FROM UserEntity u WHERE u.email= :email AND u.deleted = false";

		Query query = session.createQuery(queryStr);
		query.setParameter("email", email);

		Integer result = (Integer) query.uniqueResult();
		if (result == null) {
			return -1;
		}

		return result.intValue();
	}

	public UserPassword getUserPassword(Session session, int userId) {
		String queryStr = "SELECT u.passwordHash, u.passwordSalt FROM UserEntity u WHERE u.id= :id AND u.deleted = false";

		Query query = session.createQuery(queryStr);
		query.setParameter("id", userId);

		Object result[] = (Object[]) query.uniqueResult();
		if (result == null) {
			return null;
		}

		String hash = (String) result[0];
		String salt = (String) result[1];

		return new UserPassword(hash, salt);
	}

	public boolean deleteUserEntity(Session session, int userId) {
		String queryStr = "UPDATE UserEntity u SET u.deleted = true WHERE u.id = :userId";

		Query query = session.createQuery(queryStr);
		query.setParameter("userId", userId);

		int updatedRows = query.executeUpdate();
		return updatedRows == 1;
	}

	public UserEntity findByEmail(Session session, String email) {
		String queryStr = "FROM UserEntity u WHERE u.email = :email AND u.deleted = false";

		Query query = session.createQuery(queryStr);
		query.setParameter("email", email);
		query.setMaxResults(1);

		return (UserEntity) query.uniqueResult();
	}

	public UserEntity findByName(Session session, String name) {
		String queryStr = "FROM UserEntity u WHERE u.name = :name AND u.deleted = false";

		Query query = session.createQuery(queryStr);
		query.setParameter("name", name);
		query.setMaxResults(1);

		return (UserEntity) query.uniqueResult();
	}
}
