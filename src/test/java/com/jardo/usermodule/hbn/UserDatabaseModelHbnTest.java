package com.jardo.usermodule.hbn;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Date;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

public class UserDatabaseModelHbnTest extends UMDatabaseTestCase {

	private final UserDatabaseModelHbn databaseModel;

	public UserDatabaseModelHbnTest() {
		super();
		databaseModel = new UserDatabaseModelHbn(HibernateUtil.getSessionFactory());
	}

	@Override
	protected IDataSet createInitialDataSet() throws FileNotFoundException, DataSetException {
		ReplacementDataSet result = loadFlatXmlDataSet("src/test/resources/dataSets/databaseModelHbnTest/initial.xml");

		result.addReplacementObject("[NOW]", new Date());

		return result;
	}

	@Override
	@Before
	protected void setUp() throws DatabaseUnitException, SQLException, Exception {
		DatabaseOperation.CLEAN_INSERT.execute(getConnection(), getDataSet());
	}

	@Test
	public void testAddPasswordResetToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testCancelAllPasswordResetTokens() {
		fail("Not yet implemented");
	}

	@Test
	public void testConfirmUserRegistration() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRegisteredUserCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNewestPasswordResetToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserByEmail() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserByName() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserIdByEmail() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetUserPassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsEmailRegistered() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsUserNameRegistered() {
		fail("Not yet implemented");
	}

	@Test
	public void testMakeLogInRecord() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetUserPassword() {
		fail("Not yet implemented");
	}

}
