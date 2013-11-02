package com.jardo.usermodule.hbn;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Date;

import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.junit.Test;

import com.jardo.usermodule.containers.PasswordResetToken;
import com.jardo.usermodule.containers.User;
import com.jardo.usermodule.containers.UserPassword;

public class UserDatabaseModelHbnTest extends UMDatabaseTestCase {

	private final UserDatabaseModelHbn databaseModel;

	public UserDatabaseModelHbnTest() {
		super();
		databaseModel = new UserDatabaseModelHbn(HibernateUtil.getSessionFactory());
	}

	@Override
	protected IDataSet createInitialDataSet() throws FileNotFoundException, DataSetException {
		ReplacementDataSet result = loadFlatXmlDataSet("src/test/resources/dataSets/userDatabaseModelHbnTest/initial.xml");

		result.addReplacementObject("[NOW]", new Date());

		return result;
	}

	@Test
	public void testAddPasswordResetToken() throws SQLException, Exception {
		fillDatabase("src/test/resources/dataSets/userDatabaseModelHbnTest/beforeAddPasswordResetToken.xml");

		PasswordResetToken token = new PasswordResetToken(1, "ea587b759f423f0bfadfe7aeba0ee3fe", new Date());

		boolean result = databaseModel.addPasswordResetToken(token);
		assertEquals(true, result);

		IDataSet expectedDataSet = loadFlatXmlDataSet("src/test/resources/dataSets/userDatabaseModelHbnTest/afterAddPasswordResetToken.xml");
		assertTableContent(expectedDataSet, "um_password_reset_token", new String[] { "date_time" });
	}

	@Test
	public void testAddUser() throws SQLException, Exception {
		fillDatabase("src/test/resources/dataSets/userDatabaseModelHbnTest/beforeAddUser.xml");

		UserPassword password = new UserPassword("ea1baa4cad9d822a51a1aa267a618fb2ac6d5d98a89709a595487ea493a69e90", "7886788cb39bf33c856ef18206a81ce4b498dc5a1a4199abc0cb0fb686eab008");
		User user = new User(-1, "carl", "carl@test.com", "ea587b759f423f0bfadfe7aeba0ee3fe", false, password);

		int result = databaseModel.addUser(user);

		assertEquals(4, result);

		IDataSet expectedDataSet = loadFlatXmlDataSet("src/test/resources/dataSets/userDatabaseModelHbnTest/afterAddUser.xml");

		assertTableContent(expectedDataSet, "um_user", new String[] { "reg_date", "rank" });
	}

	@Test
	public void testCancelAllPasswordResetTokens() throws DatabaseUnitException, SQLException, Exception {
		fillDatabase("src/test/resources/dataSets/userDatabaseModelHbnTest/beforeCancelAllPasswordResetTokens.xml");
		databaseModel.cancelAllPasswordResetTokens(1);

		IDataSet expectedDataSet = loadFlatXmlDataSet("src/test/resources/dataSets/userDatabaseModelHbnTest/afterCancelAllPasswordResetTokens.xml");
		assertTableContent(expectedDataSet, "um_password_reset_token", new String[] {});
	}

	@Test
	public void testConfirmUserRegistration() throws DatabaseUnitException, SQLException, Exception {
		fillDatabase("src/test/resources/dataSets/userDatabaseModelHbnTest/beforeConfirmRegistration.xml");
		databaseModel.confirmUserRegistration("john@test.com");

		IDataSet expectedDataSet = loadFlatXmlDataSet("src/test/resources/dataSets/userDatabaseModelHbnTest/afterConfirmRegistration.xml");
		assertTableContent(expectedDataSet, "um_user", new String[] {});
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
