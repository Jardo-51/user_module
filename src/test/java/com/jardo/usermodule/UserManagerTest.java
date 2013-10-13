package com.jardo.usermodule;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.jardo.usermodule.containers.User;
import com.jardo.usermodule.containers.UserPassword;

public class UserManagerTest {

	// password = 'password'
	private static final String STORED_PASSWORD_HASH = "EA1BAA4CAD9D822A51A1AA267A618FB2AC6D5D98A89709A595487EA493A69E90";
	private static final String STORED_PASSWORD_SALT = "7886788CB39BF33C856EF18206A81CE4B498DC5A1A4199ABC0CB0FB686EAB008";

	private EmailSender emailSender;

	private UserDatabaseModel databaseModel;

	private SessionModel sessionModel;

	private UserManager userManager;

	private final UserPassword storedPassword;

	private final User storedUser;

	public UserManagerTest() {
		storedPassword = new UserPassword(STORED_PASSWORD_HASH, STORED_PASSWORD_SALT);

		storedUser = new User(1, "John", "john@example.com", "5658ffccee7f0ebfda2b226238b1eb6e", true, storedPassword);
	}

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
	public void testCancelPasswordResetTokensDatabaseError() {
		Mockito.when(databaseModel.cancelAllPasswordResetTokens(1)).thenReturn(false);

		ResultCode result = userManager.cancelPasswordResetTokens(1);
		Assert.assertEquals(ResultCode.DATABASE_ERROR, result);

		Mockito.verify(databaseModel).cancelAllPasswordResetTokens(1);
	}

	@Test
	public void testCancelRegistration() {
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
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(storedPassword);

		ResultCode result = userManager.cancelRegistration(1, "wrong_password");
		Assert.assertEquals(ResultCode.INVALID_CREDENTIALS, result);

		Mockito.verify(databaseModel, Mockito.times(0)).deleteUser(1);
	}

	@Test
	public void testChangePassword() {
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(storedPassword);
		Mockito.when(databaseModel.setUserPassword(Mockito.eq(1), Mockito.any(UserPassword.class))).thenReturn(true);

		ResultCode result = userManager.changePassword(1, "password", "new_password");
		Assert.assertEquals(ResultCode.OK, result);

		ArgumentCaptor<UserPassword> passwordCaptor = ArgumentCaptor.forClass(UserPassword.class);
		Mockito.verify(databaseModel).setUserPassword(Mockito.eq(1), passwordCaptor.capture());

		UserPassword newPassword = passwordCaptor.getValue();
		Assert.assertNotEquals(newPassword.getHash(), STORED_PASSWORD_HASH);
		Assert.assertNotEquals(newPassword.getSalt(), STORED_PASSWORD_SALT);
	}

	@Test
	public void testChangePasswordWrongPassword() {
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(storedPassword);

		ResultCode result = userManager.changePassword(1, "wrong_old_password", "new_password");
		Assert.assertEquals(ResultCode.INVALID_CREDENTIALS, result);

		Mockito.verify(databaseModel, Mockito.never()).setUserPassword(Mockito.anyInt(), Mockito.any(UserPassword.class));
	}

	@Test
	public void testConfirmRegistration() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(storedUser);

		ResultCode result = userManager.confirmRegistration("john@example.com", storedUser.getRegistrationControlCode());
		Assert.assertEquals(ResultCode.OK, result);

		Mockito.verify(databaseModel).confirmUserRegistration("john@example.com");
	}

	@Test
	public void testConfirmRegistrationInvalidEmail() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(null);

		ResultCode result = userManager.confirmRegistration("john@example.com", storedUser.getRegistrationControlCode());
		Assert.assertEquals(ResultCode.NO_SUCH_USER, result);

		Mockito.verify(databaseModel, Mockito.never()).confirmUserRegistration("john@example.com");
	}

	@Test
	public void testConfirmRegistrationInvalidControlCode() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(storedUser);

		String invalidControlCode = "";
		ResultCode result = userManager.confirmRegistration("john@example.com", invalidControlCode);
		Assert.assertEquals(ResultCode.INVALID_REGISTRATION_CONTROL_CODE, result);

		Mockito.verify(databaseModel, Mockito.never()).confirmUserRegistration("john@example.com");
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
