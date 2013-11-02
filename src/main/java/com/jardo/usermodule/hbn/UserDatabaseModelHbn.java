package com.jardo.usermodule.hbn;

import java.net.InetAddress;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.jardo.usermodule.UserDatabaseModel;
import com.jardo.usermodule.containers.PasswordResetToken;
import com.jardo.usermodule.containers.User;
import com.jardo.usermodule.containers.UserPassword;
import com.jardo.usermodule.hbn.dao.PasswordResetTokenEntityDao;
import com.jardo.usermodule.hbn.dao.UserEntityDao;
import com.jardo.usermodule.hbn.entities.PasswordResetTokenEntity;
import com.jardo.usermodule.hbn.entities.UserEntity;

public class UserDatabaseModelHbn implements UserDatabaseModel {

	private final SessionFactory sessionFactory;

	private final UserEntityDao userEntityDao;

	private final PasswordResetTokenEntityDao passwordResetTokenEntityDao;

	private void closeSession(Session session) {
		session.getTransaction().commit();
		session.close();
	}

	private Session openSession() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		return session;
	}

	public UserDatabaseModelHbn(SessionFactory sessionFactory) {
		super();
		this.sessionFactory = sessionFactory;
		this.userEntityDao = new UserEntityDao();
		this.passwordResetTokenEntityDao = new PasswordResetTokenEntityDao();
	}

	public boolean addPasswordResetToken(PasswordResetToken token) {
		PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity(token);
		tokenEntity.setValid(true);

		Session session = openSession();
		passwordResetTokenEntityDao.add(session, tokenEntity);
		closeSession(session);

		return true;
	}

	public int addUser(User newUser) {
		UserEntity userEntity = new UserEntity();
		userEntity.copyUser(newUser);
		userEntity.setRegistrationDate(new Date());

		Session session = openSession();
		userEntityDao.add(session, userEntity);
		closeSession(session);

		return userEntity.getId();
	}

	public boolean cancelAllPasswordResetTokens(int userId) {
		Session session = openSession();
		passwordResetTokenEntityDao.cancelTokensForUser(session, userId);
		closeSession(session);
		return true;
	}

	public boolean confirmUserRegistration(String email) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean deleteUser(int userId) {
		Session session = openSession();
		boolean result = userEntityDao.deleteUserEntity(session, userId);
		closeSession(session);
		return result;
	}

	public int getRegisteredUserCount(Date since) {
		// TODO Auto-generated method stub
		return 0;
	}

	public PasswordResetToken getNewestPasswordResetToken(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUserByEmail(String email) {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUserByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getUserIdByEmail(String email) {
		// TODO Auto-generated method stub
		return 0;
	}

	public UserPassword getUserPassword(int userId) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isEmailRegistered(String email) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isUserNameRegistered(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean makeLogInRecord(int userId, boolean logInSuccessfull, InetAddress usersIp) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean setUserPassword(int userId, UserPassword password) {
		// TODO Auto-generated method stub
		return false;
	}

}
