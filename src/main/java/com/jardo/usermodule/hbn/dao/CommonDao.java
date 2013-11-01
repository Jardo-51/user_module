package com.jardo.usermodule.hbn.dao;

import org.hibernate.Session;

public class CommonDao<T> {

	public void add(Session session, T entity) {
		session.save(entity);
	}

}
