package com.jardo.usermodule.hbn.dao;

import org.hibernate.Session;

import com.jardo.usermodule.hbn.entities.UserEntity;

public class UserEntityDao {

	public void addUserEntity(Session session, UserEntity userEntity) {
		session.save(userEntity);
	}

}
