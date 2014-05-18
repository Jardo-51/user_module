package com.jardoapps.usermodule.hbn.dao;

import java.io.Serializable;

import org.hibernate.Query;
import org.hibernate.Session;

import com.jardoapps.usermodule.hbn.entities.PasswordResetTokenEntity;

public class PasswordResetTokenEntityDao extends CommonDao<PasswordResetTokenEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	public void cancelTokensForUser(Session session, int userId) {
		String queryStr = "UPDATE PasswordResetTokenEntity prt SET prt.valid = false WHERE prt.user.id = :userId";

		Query query = session.createQuery(queryStr);
		query.setParameter("userId", userId);
		query.executeUpdate();
	}

	public PasswordResetTokenEntity getNewestToken(Session session, String email) {
		String queryStr = "FROM PasswordResetTokenEntity prt WHERE prt.user.email = :email ORDER BY prt.time DESC";

		Query query = session.createQuery(queryStr);
		query.setParameter("email", email);
		query.setMaxResults(1);

		return (PasswordResetTokenEntity) query.uniqueResult();
	}

}
