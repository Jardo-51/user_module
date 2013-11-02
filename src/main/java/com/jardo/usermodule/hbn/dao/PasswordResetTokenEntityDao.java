package com.jardo.usermodule.hbn.dao;

import org.hibernate.Query;
import org.hibernate.Session;

import com.jardo.usermodule.hbn.entities.PasswordResetTokenEntity;

public class PasswordResetTokenEntityDao extends CommonDao<PasswordResetTokenEntity> {

	public void cancelTokensForUser(Session session, int userId) {
		String queryStr = "UPDATE PasswordResetTokenEntity prt SET prt.valid = false WHERE prt.user.id = :userId";

		Query query = session.createQuery(queryStr);
		query.setParameter("userId", userId);
		query.executeUpdate();
	}

}
