package com.jardoapps.usermodule.jpa.dao;

import java.io.Serializable;

import javax.persistence.Query;

import com.jardoapps.usermodule.jpa.entities.SocialAccountEntity;
import com.jardoapps.usermodule.jpa.entities.UserEntity;

public class SocialAccountDao  extends CommonDao<SocialAccountEntity> implements Serializable {

	private static final long serialVersionUID = 1L;

	public UserEntity getUserByAccount(String accountType, String originalAccountId) {

		Query query = createQuery("SELECT sa.user FROM SocialAccountEntity sa WHERE sa.accountType = :accountType AND sa.originalAccountId = :originalAccountId");

		query.setParameter("accountType", accountType);
		query.setParameter("originalAccountId", originalAccountId);

		return getSingleResult(query);
	}

}
