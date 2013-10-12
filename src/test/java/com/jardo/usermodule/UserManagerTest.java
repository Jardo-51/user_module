package com.jardo.usermodule;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.jardo.usermodule.containers.UserPassword;

public class UserManagerTest {

	// password = 'password'
	private static final String STORED_PASSWORD_HASH = "EA1BAA4CAD9D822A51A1AA267A618FB2AC6D5D98A89709A595487EA493A69E90";
	private static final String STORED_PASSWORD_SALT = "7886788CB39BF33C856EF18206A81CE4B498DC5A1A4199ABC0CB0FB686EAB008";

	private EmailSender emailSender;

	private UserDatabaseModel databaseModel;

	private SessionModel sessionModel;

	private UserManager userManager;

	@Before
	public void setUp() throws Exception {
		databaseModel = Mockito.mock(UserDatabaseModel.class);
		emailSender = Mockito.mock(EmailSender.class);
		sessionModel = Mockito.mock(SessionModel.class);

		userManager = new UserManager(databaseModel, emailSender, sessionModel);
	}

	@Test
	public void testCancelPasswordResetTokens() {
		Mockito.when(databaseModel.cancelAllPasswordResetTokens(1)).thenReturn(true);

		ResultCode result = userManager.cancelPasswordResetTokens(1);
		Assert.assertEquals(ResultCode.OK, result);

		Mockito.verify(databaseModel).cancelAllPasswordResetTokens(1);
	}

	@Test
	public void testCancelRegistration() {
		UserPassword storedPassword = new UserPassword(STORED_PASSWORD_HASH, STORED_PASSWORD_SALT);
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(storedPassword);
		Mockito.when(databaseModel.deleteUser(1)).thenReturn(true);

		ResultCode result = userManager.cancelRegistration(1, "password");
		Assert.assertEquals(ResultCode.OK, result);

		Mockito.verify(databaseModel).deleteUser(1);
	}

	@Test
	public void testCancelRegistrationUserDoesntExist() {
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(null);

		ResultCode result = userManager.cancelRegistration(1, "password");
		Assert.assertEquals(ResultCode.NO_SUCH_USER, result);

		Mockito.verify(databaseModel, Mockito.times(0)).deleteUser(1);
	}

	@Test
	public void testCancelRegistrationWrongPassword() {
		UserPassword storedPassword = new UserPassword(STORED_PASSWORD_HASH, STORED_PASSWORD_SALT);
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(storedPassword);

		ResultCode result = userManager.cancelRegistration(1, "wrong_password");
		Assert.assertEquals(ResultCode.INVALID_CREDENTIALS, result);

		Mockito.verify(databaseModel, Mockito.times(0)).deleteUser(1);
	}

	@Test
	public void testChangePassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testConfirmRegistration() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreatePasswordResetToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCurrentUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRegisteredUserCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsPasswordValid() {
		fail("Not yet implemented");
	}

	@Test
	public void testLogIn() {
		fail("Not yet implemented");
	}

	@Test
	public void testLogOut() {
		fail("Not yet implemented");
	}

	@Test
	public void testRegisterUser() {
		fail("Not yet implemented");
	}

	@Test
	public void testResendRegistrationEmail() {
		fail("Not yet implemented");
	}

	@Test
	public void testResetPassword() {
		fail("Not yet implemented");
	}

	@Test
	public void testSendTestingEmail() {
		fail("Not yet implemented");
	}
}
