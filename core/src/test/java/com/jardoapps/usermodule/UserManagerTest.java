/*
 * This file is part of the User Module library.
 * Copyright (C) 2014 Jaroslav Brtiš
 *
 * User Module library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * User Module library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with User Module library. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jardoapps.usermodule;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.jardoapps.usermodule.containers.PasswordResetToken;
import com.jardoapps.usermodule.containers.UserPassword;
import com.jardoapps.usermodule.defines.EmailType;

@RunWith(MockitoJUnitRunner.class)
public class UserManagerTest {

	private static final long ONE_DAY = 86400000L;

	// password = 'password'
	private static final String STORED_PASSWORD_HASH = "C0794DCF71360C8A6302C49B3228CBCFFC8CD07BBC55250EAC7D2C599B9AE2BD";
	private static final String STORED_PASSWORD_SALT = "7886788CB39BF33C856EF18206A81CE4B498DC5A1A4199ABC0CB0FB686EAB008";

	private static final String HASH_ENCODING = "UTF-16";

	@Spy
	private UserManagementProperties properties = new UserManagementPropertiesImpl();

	@Mock
	private EmailSender emailSender;

	@Mock
	private UserDatabaseModel databaseModel;

	@Mock
	private SessionModel sessionModel;

	@InjectMocks
	private UserManager userManager;

	private final String inetAddress;

	private final UserPassword storedPassword;

	private final User storedUser;

	private final User userWithUnfinishedRegistration;

	private MessageDigest sha256;

	private void assertPasswordData(String password, UserPassword passwordData) throws UnsupportedEncodingException {
		sha256.update(passwordData.getSalt().getBytes(HASH_ENCODING));
		String expectedHash = toHex(sha256.digest(password.getBytes(HASH_ENCODING)));

		Assert.assertEquals(expectedHash, passwordData.getHash().toLowerCase());
	}

	private String toHex(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		for (int idx = 0; idx < bytes.length; ++idx) {
			byte b = bytes[idx];
			result.append(digits[(b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}
		return result.toString();
	}

	public UserManagerTest() {
		storedPassword = new UserPassword(STORED_PASSWORD_HASH, STORED_PASSWORD_SALT);

		storedUser = new User(1, "John", "john@example.com", "5658ffccee7f0ebfda2b226238b1eb6e", true, storedPassword, UserRanks.NORMAL_USER);
		userWithUnfinishedRegistration = new User(2, "Carl", "carl@example.com", "0b0606b79c0bb1babe52bbfdd4ae8e7f", false, storedPassword, UserRanks.NORMAL_USER);

		inetAddress = "127.0.0.1";

		try {
			this.sha256 = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			this.sha256 = null;
		}
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
		Assert.assertEquals(ResultCode.INVALID_PASSWORD, result);

		Mockito.verify(databaseModel, Mockito.times(0)).deleteUser(1);
	}

	@Test
	public void testChangePassword() throws Exception {
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(storedPassword);
		Mockito.when(databaseModel.setUserPassword(Mockito.eq(1), Mockito.any(UserPassword.class))).thenReturn(true);

		ResultCode result = userManager.changePassword(1, "password", "new_password");
		Assert.assertEquals(ResultCode.OK, result);

		ArgumentCaptor<UserPassword> passwordCaptor = ArgumentCaptor.forClass(UserPassword.class);
		Mockito.verify(databaseModel).setUserPassword(Mockito.eq(1), passwordCaptor.capture());

		UserPassword newPassword = passwordCaptor.getValue();
		assertPasswordData("new_password", newPassword);
	}

	@Test
	public void testChangePasswordWrongPassword() {
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(storedPassword);

		ResultCode result = userManager.changePassword(1, "wrong_old_password", "new_password");
		Assert.assertEquals(ResultCode.INVALID_PASSWORD, result);

		Mockito.verify(databaseModel, Mockito.never()).setUserPassword(Mockito.anyInt(), Mockito.any(UserPassword.class));
	}

	@Test
	public void testCheckPassword() {

		PasswordCheckResult result = userManager.checkPassword(null, null);
		Assert.assertEquals(PasswordCheckResult.PASSWORD_EMPTY, result);

		result = userManager.checkPassword("", null);
		Assert.assertEquals(PasswordCheckResult.PASSWORD_EMPTY, result);

		result = userManager.checkPassword("abcd", null);
		Assert.assertEquals(PasswordCheckResult.PASSWORD_TOO_SHORT, result);

		result = userManager.checkPassword("abcdef", null);
		Assert.assertEquals(PasswordCheckResult.PASSWORD_CONFIRMATION_EMPTY, result);

		result = userManager.checkPassword("abcdef", "");
		Assert.assertEquals(PasswordCheckResult.PASSWORD_CONFIRMATION_EMPTY, result);

		result = userManager.checkPassword("abcdef", "abcd");
		Assert.assertEquals(PasswordCheckResult.PASSWORD_CONFIRMATION_MISMATCH, result);

		result = userManager.checkPassword("abcdef", "abcdef");
		Assert.assertEquals(PasswordCheckResult.OK, result);
	}

	@Test
	public void testConfirmManualRegistration() throws Exception {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(userWithUnfinishedRegistration);
		Mockito.when(databaseModel.confirmUserRegistration("john@example.com")).thenReturn(true);
		Mockito.when(databaseModel.setUserPassword(Mockito.eq(2), Mockito.any(UserPassword.class))).thenReturn(true);

		ResultCode result = userManager.confirmManualRegistration("john@example.com", userWithUnfinishedRegistration.getRegistrationControlCode(), "password");
		Assert.assertEquals(ResultCode.OK, result);

		Mockito.verify(databaseModel).confirmUserRegistration("john@example.com");

		ArgumentCaptor<UserPassword> passwordCaptor = ArgumentCaptor.forClass(UserPassword.class);
		Mockito.verify(databaseModel).setUserPassword(Mockito.eq(2), passwordCaptor.capture());

		UserPassword newPassword = passwordCaptor.getValue();
		assertPasswordData("password", newPassword);
	}

	@Test
	public void testConfirmRegistration() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(userWithUnfinishedRegistration);
		Mockito.when(databaseModel.confirmUserRegistration("john@example.com")).thenReturn(true);

		ResultCode result = userManager.confirmRegistration("john@example.com", userWithUnfinishedRegistration.getRegistrationControlCode());
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
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(userWithUnfinishedRegistration);

		String invalidControlCode = "d9d8172ffa4e21f955e8ad125f9dbc32";
		ResultCode result = userManager.confirmRegistration("john@example.com", invalidControlCode);
		Assert.assertEquals(ResultCode.INVALID_REGISTRATION_CONTROL_CODE, result);

		Mockito.verify(databaseModel, Mockito.never()).confirmUserRegistration("john@example.com");
	}

	@Test
	public void testConfirmRegistrationAlreadyConfirmed() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(storedUser);

		ResultCode result = userManager.confirmRegistration("john@example.com", storedUser.getRegistrationControlCode());
		Assert.assertEquals(ResultCode.REGISTRATION_ALREADY_CONFIRMED, result);

		Mockito.verify(databaseModel, Mockito.never()).confirmUserRegistration("john@example.com");
	}

	@Test
	public void testCreatePasswordResetToken() {
		Mockito.when(databaseModel.getUserIdByEmail("john@example.com")).thenReturn(1);
		Mockito.when(databaseModel.addPasswordResetToken(Mockito.any(PasswordResetToken.class))).thenReturn(true);
		Mockito.when(emailSender.sendLostPasswordEmail(Mockito.anyString(), Mockito.anyString())).thenReturn(true);

		ResultCode result = userManager.createPasswordResetToken("john@example.com");
		Assert.assertEquals(ResultCode.OK, result);

		// check database data

		ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
		Mockito.verify(databaseModel).addPasswordResetToken(tokenCaptor.capture());

		PasswordResetToken newToken = tokenCaptor.getValue();
		Assert.assertEquals(1, newToken.getUserId());

		// check email data

		ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> tokenKeyCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(emailSender).sendLostPasswordEmail(emailCaptor.capture(), tokenKeyCaptor.capture());

		Assert.assertEquals("john@example.com", emailCaptor.getValue());
		Assert.assertEquals(newToken.getKey(), tokenKeyCaptor.getValue());
	}

	@Test
	public void testCreatePasswordResetTokenFailedToSendEmail() {
		Mockito.when(databaseModel.getUserIdByEmail("john@example.com")).thenReturn(1);
		Mockito.when(databaseModel.addPasswordResetToken(Mockito.any(PasswordResetToken.class))).thenReturn(true);
		Mockito.when(emailSender.sendLostPasswordEmail(Mockito.anyString(), Mockito.anyString())).thenReturn(false);

		ResultCode result = userManager.createPasswordResetToken("john@example.com");
		Assert.assertEquals(ResultCode.FAILED_TO_SEND_EMAIL, result);
	}

	@Test
	public void testCreatePasswordResetTokenInvalidEmail() {
		Mockito.when(databaseModel.getUserIdByEmail("john@example.com")).thenReturn(-1);

		ResultCode result = userManager.createPasswordResetToken("john@example.com");
		Assert.assertEquals(ResultCode.NO_SUCH_USER, result);

		Mockito.verify(databaseModel, Mockito.never()).addPasswordResetToken(Mockito.any(PasswordResetToken.class));
		Mockito.verifyZeroInteractions(emailSender);
	}

	@Test
	public void testGetCurrentUser() {
		Mockito.when(sessionModel.getCurrentUser()).thenReturn(storedUser);

		User result = userManager.getCurrentUser();
		Assert.assertSame(storedUser, result);
	}

	@Test
	public void testGetRegisteredUserCount() {
		Mockito.when(databaseModel.getRegisteredUserCount(Mockito.any(Date.class))).thenReturn(5);

		Date since = new Date();

		int result = userManager.getRegisteredUserCount(since);
		Assert.assertEquals(5, result);

		ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
		Mockito.verify(databaseModel).getRegisteredUserCount(dateCaptor.capture());
		Assert.assertEquals(since, dateCaptor.getValue());
	}

	@Test
	public void testIsPasswordValidTrue() {
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(storedPassword);

		boolean result = userManager.isPasswordValid(1, "password");
		Assert.assertEquals(true, result);
	}

	@Test
	public void testIsPasswordValidFalse() {
		Mockito.when(databaseModel.getUserPassword(1)).thenReturn(storedPassword);

		boolean result = userManager.isPasswordValid(1, "wrong_password");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testIsPasswordValidNoSuchUser() {
		Mockito.when(databaseModel.getUserPassword(2)).thenReturn(null);

		boolean result = userManager.isPasswordValid(2, "password");
		Assert.assertEquals(false, result);
	}

	@Test
	public void testLogInWithEmail() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(storedUser);

		ResultCode result = userManager.logIn("john@example.com", "password", inetAddress);
		Assert.assertEquals(ResultCode.OK, result);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(sessionModel).setCurrentUser(userCaptor.capture());
		Assert.assertSame(storedUser, userCaptor.getValue());

		ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(databaseModel).makeLogInRecord(Mockito.eq(1), Mockito.eq(true), ipCaptor.capture());
		Assert.assertSame(inetAddress, ipCaptor.getValue());

		Mockito.verify(databaseModel).cancelAllPasswordResetTokens(1);
	}

	@Test
	public void testLogInWithEmailNoSuchUser() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(null);

		ResultCode result = userManager.logIn("john@example.com", "password", inetAddress);
		Assert.assertEquals(ResultCode.NO_SUCH_USER, result);
	}

	@Test
	public void testLogInWithEmailInvalidPassword() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(storedUser);

		ResultCode result = userManager.logIn("john@example.com", "wrong_password", inetAddress);
		Assert.assertEquals(ResultCode.INVALID_PASSWORD, result);

		Mockito.verify(sessionModel, Mockito.never()).setCurrentUser(Mockito.notNull(User.class));

		ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(databaseModel).makeLogInRecord(Mockito.eq(1), Mockito.eq(false), ipCaptor.capture());
		Assert.assertSame(inetAddress, ipCaptor.getValue());
	}

	@Test
	public void testLogInWithEmailRegistrationNotConfirmed() {
		Mockito.when(databaseModel.getUserByEmail("carl@example.com")).thenReturn(userWithUnfinishedRegistration);

		ResultCode result = userManager.logIn("carl@example.com", "wrong_password", inetAddress);
		Assert.assertEquals(ResultCode.REGISTRATION_NOT_CONFIRMED, result);

		Mockito.verify(sessionModel, Mockito.never()).setCurrentUser(Mockito.notNull(User.class));
	}

	@Test
	public void testLogInWithoutPasswordWithEmail() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(storedUser);

		ResultCode result = userManager.logInWithoutPassword("john@example.com");
		Assert.assertEquals(ResultCode.OK, result);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(sessionModel).setCurrentUser(userCaptor.capture());
		Assert.assertSame(storedUser, userCaptor.getValue());

		Mockito.verify(databaseModel, Mockito.never()).makeLogInRecord(Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyString());
		Mockito.verify(databaseModel, Mockito.never()).cancelAllPasswordResetTokens(Mockito.anyInt());
	}

	@Test
	public void testLogInWithoutPasswordWithEmailNoSuchUser() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(null);

		ResultCode result = userManager.logInWithoutPassword("john@example.com");
		Assert.assertEquals(ResultCode.NO_SUCH_USER, result);
	}

	@Test
	public void testLogInWithoutPasswordWithEmailRegistrationNotConfirmed() {
		Mockito.when(databaseModel.getUserByEmail("carl@example.com")).thenReturn(userWithUnfinishedRegistration);

		ResultCode result = userManager.logInWithoutPassword("carl@example.com");
		Assert.assertEquals(ResultCode.REGISTRATION_NOT_CONFIRMED, result);

		Mockito.verify(sessionModel, Mockito.never()).setCurrentUser(Mockito.notNull(User.class));
	}

	@Test
	public void testLogInWithoutPasswordWithUserName() {
		Mockito.when(databaseModel.getUserByName("john")).thenReturn(storedUser);

		ResultCode result = userManager.logInWithoutPassword("john");
		Assert.assertEquals(ResultCode.OK, result);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(sessionModel).setCurrentUser(userCaptor.capture());
		Assert.assertSame(storedUser, userCaptor.getValue());

		Mockito.verify(databaseModel, Mockito.never()).makeLogInRecord(Mockito.anyInt(), Mockito.anyBoolean(), Mockito.anyString());
		Mockito.verify(databaseModel, Mockito.never()).cancelAllPasswordResetTokens(Mockito.anyInt());
	}

	@Test
	public void testLogInWithUserName() {
		Mockito.when(databaseModel.getUserByName("john")).thenReturn(storedUser);

		ResultCode result = userManager.logIn("john", "password", inetAddress);
		Assert.assertEquals(ResultCode.OK, result);

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(sessionModel).setCurrentUser(userCaptor.capture());
		Assert.assertSame(storedUser, userCaptor.getValue());

		ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(databaseModel).makeLogInRecord(Mockito.eq(1), Mockito.eq(true), ipCaptor.capture());
		Assert.assertSame(inetAddress, ipCaptor.getValue());

		Mockito.verify(databaseModel).cancelAllPasswordResetTokens(1);
	}

	@Test
	public void testLogOut() {
		userManager.logOut();
		Mockito.verify(sessionModel).setCurrentUser(null);
	}

	@Test
	public void testRegisterUser() throws Exception {
		Mockito.when(databaseModel.isEmailRegistered("carl@example.com")).thenReturn(false);
		Mockito.when(databaseModel.isUserNameRegistered("Carl")).thenReturn(false);
		Mockito.when(databaseModel.addUser(Mockito.notNull(User.class))).thenReturn(2);
		Mockito.when(emailSender.sendRegistrationEmail(Mockito.eq("carl@example.com"), Mockito.eq("Carl"), Mockito.eq(2), Mockito.notNull(String.class))).thenReturn(true);

		ResultCode result = userManager.registerUser("carl@example.com", "Carl", "password", false);
		Assert.assertEquals(ResultCode.OK, result);

		// check database model call

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(databaseModel).addUser(userCaptor.capture());

		User addedUser = userCaptor.getValue();
		Assert.assertEquals(-1, addedUser.getId());
		Assert.assertEquals("carl@example.com", addedUser.getEmail());
		Assert.assertEquals("Carl", addedUser.getName());
		Assert.assertEquals(false, addedUser.isRegistrationConfirmed());
		assertPasswordData("password", addedUser.getPassword());
		Assert.assertEquals(UserRanks.NORMAL_USER, addedUser.getRank());

		// check email sender call

		Mockito.verify(emailSender).sendRegistrationEmail("carl@example.com", "Carl", 2, addedUser.getRegistrationControlCode());
	}

	@Test
	public void testRegisterUserEmailAlreadyRegistered() {
		Mockito.when(databaseModel.isEmailRegistered("carl@example.com")).thenReturn(true);
		Mockito.when(databaseModel.isUserNameRegistered("Carl")).thenReturn(false);

		ResultCode result = userManager.registerUser("carl@example.com", "Carl", "password", false);
		Assert.assertEquals(ResultCode.EMAIL_ALREADY_REGISTERED, result);
	}

	@Test
	public void testRegisterUserNameAlreadyRegistered() {
		Mockito.when(databaseModel.isEmailRegistered("carl@example.com")).thenReturn(false);
		Mockito.when(databaseModel.isUserNameRegistered("Carl")).thenReturn(true);

		ResultCode result = userManager.registerUser("carl@example.com", "Carl", "password", false);
		Assert.assertEquals(ResultCode.USER_NAME_ALREADY_REGISTERED, result);
	}

	@Test
	public void testRegisterUserNameFailedToSendEmail() {
		Mockito.when(databaseModel.isEmailRegistered("carl@example.com")).thenReturn(false);
		Mockito.when(databaseModel.isUserNameRegistered("Carl")).thenReturn(false);
		Mockito.when(databaseModel.addUser(Mockito.notNull(User.class))).thenReturn(2);
		Mockito.when(emailSender.sendRegistrationEmail(Mockito.eq("carl@example.com"), Mockito.eq("Carl"), Mockito.eq(2), Mockito.notNull(String.class))).thenReturn(false);

		ResultCode result = userManager.registerUser("carl@example.com", "Carl", "password", false);
		Assert.assertEquals(ResultCode.FAILED_TO_SEND_EMAIL, result);

		// check database model call

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(databaseModel).addUser(userCaptor.capture());

		User addedUser = userCaptor.getValue();
		Assert.assertEquals(-1, addedUser.getId());
		Assert.assertEquals("carl@example.com", addedUser.getEmail());
		Assert.assertEquals("Carl", addedUser.getName());
		Assert.assertEquals(false, addedUser.isRegistrationConfirmed());

		// check email sender call

		Mockito.verify(emailSender).sendRegistrationEmail("carl@example.com", "Carl", 2, addedUser.getRegistrationControlCode());
	}

	@Test
	public void testRegisterUserRegistrationConfirmed() {
		Mockito.when(databaseModel.isEmailRegistered("carl@example.com")).thenReturn(false);
		Mockito.when(databaseModel.isUserNameRegistered("Carl")).thenReturn(false);
		Mockito.when(databaseModel.addUser(Mockito.notNull(User.class))).thenReturn(2);
		Mockito.when(emailSender.sendRegistrationEmail(Mockito.eq("carl@example.com"), Mockito.eq("Carl"), Mockito.eq(2), Mockito.notNull(String.class))).thenReturn(true);

		ResultCode result = userManager.registerUser("carl@example.com", "Carl", "password", true);
		Assert.assertEquals(ResultCode.OK, result);

		// check database model call

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(databaseModel).addUser(userCaptor.capture());

		User addedUser = userCaptor.getValue();
		Assert.assertEquals(-1, addedUser.getId());
		Assert.assertEquals("carl@example.com", addedUser.getEmail());
		Assert.assertEquals("Carl", addedUser.getName());
		Assert.assertEquals(true, addedUser.isRegistrationConfirmed());

		Mockito.verifyZeroInteractions(emailSender);
	}

	@Test
	public void testRegisterUserManually() throws Exception {

		Mockito.when(databaseModel.isEmailRegistered("carl@example.com")).thenReturn(false);
		Mockito.when(databaseModel.isUserNameRegistered("Carl")).thenReturn(false);
		Mockito.when(databaseModel.addUser(Mockito.notNull(User.class))).thenReturn(2);
		Mockito.when(
				emailSender.sendManualRegistrationEmail(Mockito.eq("carl@example.com"), Mockito.eq("Carl"), Mockito.eq(2), Mockito.notNull(String.class), Mockito.any(User.class)))
				.thenReturn(true);

		User registrator = storedUser;
		Mockito.when(sessionModel.getCurrentUser()).thenReturn(registrator);

		ResultCode result = userManager.registerUserManually("carl@example.com", "Carl", UserRanks.NORMAL_USER);
		Assert.assertEquals(ResultCode.OK, result);

		// check database model call

		ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
		Mockito.verify(databaseModel).addUser(userCaptor.capture());

		User addedUser = userCaptor.getValue();
		Assert.assertEquals(-1, addedUser.getId());
		Assert.assertEquals("carl@example.com", addedUser.getEmail());
		Assert.assertEquals("Carl", addedUser.getName());
		Assert.assertEquals(false, addedUser.isRegistrationConfirmed());
		assertPasswordData("", addedUser.getPassword());
		Assert.assertEquals(UserRanks.NORMAL_USER, addedUser.getRank());

		// check email sender call

		Mockito.verify(emailSender).sendManualRegistrationEmail("carl@example.com", "Carl", 2, addedUser.getRegistrationControlCode(), registrator);
	}

	@Test
	public void testResendRegistrationEmail() {
		Mockito.when(databaseModel.getUserByEmail("carl@example.com")).thenReturn(userWithUnfinishedRegistration);
		Mockito.when(emailSender.sendRegistrationEmail("carl@example.com", "Carl", 2, userWithUnfinishedRegistration.getRegistrationControlCode())).thenReturn(true);

		ResultCode result = userManager.resendRegistrationEmail("carl@example.com");
		Assert.assertEquals(ResultCode.OK, result);

		Mockito.verify(emailSender).sendRegistrationEmail("carl@example.com", "Carl", 2, userWithUnfinishedRegistration.getRegistrationControlCode());
	}

	@Test
	public void testResendRegistrationEmailFailedToSendEmail() {
		Mockito.when(databaseModel.getUserByEmail("carl@example.com")).thenReturn(userWithUnfinishedRegistration);
		Mockito.when(emailSender.sendRegistrationEmail("carl@example.com", "Carl", 2, userWithUnfinishedRegistration.getRegistrationControlCode())).thenReturn(false);

		ResultCode result = userManager.resendRegistrationEmail("carl@example.com");
		Assert.assertEquals(ResultCode.FAILED_TO_SEND_EMAIL, result);
	}

	@Test
	public void testResendRegistrationEmailNoSuchUser() {
		Mockito.when(databaseModel.getUserByEmail("bender@example.com")).thenReturn(null);

		ResultCode result = userManager.resendRegistrationEmail("bender@example.com");
		Assert.assertEquals(ResultCode.NO_SUCH_USER, result);
	}

	@Test
	public void testResendRegistrationEmailRegistrationAlreadyConfirmed() {
		Mockito.when(databaseModel.getUserByEmail("john@example.com")).thenReturn(storedUser);

		ResultCode result = userManager.resendRegistrationEmail("john@example.com");
		Assert.assertEquals(ResultCode.REGISTRATION_ALREADY_CONFIRMED, result);
	}

	@Test
	public void testResetPassword() throws Exception {
		PasswordResetToken token = new PasswordResetToken(1, "a4d21f702e44af5d0ce7228dae878672", new Date());
		Mockito.when(databaseModel.getNewestPasswordResetToken("john@example.com")).thenReturn(token);
		Mockito.when(databaseModel.getUserIdByEmail("john@example.com")).thenReturn(1);
		Mockito.when(databaseModel.setUserPassword(Mockito.eq(1), Mockito.notNull(UserPassword.class))).thenReturn(true);

		ResultCode result = userManager.resetPassword("john@example.com", token.getKey(), "new_password");
		Assert.assertEquals(ResultCode.OK, result);

		ArgumentCaptor<UserPassword> passwordCaptor = ArgumentCaptor.forClass(UserPassword.class);
		Mockito.verify(databaseModel).setUserPassword(Mockito.eq(1), passwordCaptor.capture());

		UserPassword newPassword = passwordCaptor.getValue();
		assertPasswordData("new_password", newPassword);

		Mockito.verify(databaseModel).cancelAllPasswordResetTokens(1);
	}

	@Test
	public void testResetPasswordNoSuchUser() {
		PasswordResetToken token = new PasswordResetToken(1, "a4d21f702e44af5d0ce7228dae878672", new Date());
		Mockito.when(databaseModel.getNewestPasswordResetToken("john@example.com")).thenReturn(token);
		Mockito.when(databaseModel.getUserIdByEmail("john@example.com")).thenReturn(-1);

		ResultCode result = userManager.resetPassword("john@example.com", token.getKey(), "new_password");
		Assert.assertEquals(ResultCode.NO_SUCH_USER, result);
	}

	@Test
	public void testResetPasswordNoToken() {
		Mockito.when(databaseModel.getNewestPasswordResetToken("john@example.com")).thenReturn(null);
		Mockito.when(databaseModel.getUserIdByEmail("john@example.com")).thenReturn(1);

		ResultCode result = userManager.resetPassword("john@example.com", "a4d21f702e44af5d0ce7228dae878672", "new_password");
		Assert.assertEquals(ResultCode.NO_VALID_PASSWORD_RESET_TOKEN, result);
	}

	@Test
	public void testResetPasswordTokenExpired() {
		long now = new Date().getTime();
		PasswordResetToken token = new PasswordResetToken(1, "a4d21f702e44af5d0ce7228dae878672", new Date(now - ONE_DAY));
		Mockito.when(databaseModel.getNewestPasswordResetToken("john@example.com")).thenReturn(token);
		Mockito.when(databaseModel.getUserIdByEmail("john@example.com")).thenReturn(1);
		Mockito.when(databaseModel.setUserPassword(Mockito.eq(1), Mockito.notNull(UserPassword.class))).thenReturn(true);

		ResultCode result = userManager.resetPassword("john@example.com", token.getKey(), "new_password");
		Assert.assertEquals(ResultCode.NO_VALID_PASSWORD_RESET_TOKEN, result);

		Mockito.verify(databaseModel, Mockito.never()).setUserPassword(Mockito.eq(1), Mockito.any(UserPassword.class));
	}

	@Test
	public void testResetPasswordWrongTokenKey() {
		PasswordResetToken token = new PasswordResetToken(1, "a4d21f702e44af5d0ce7228dae878672", new Date());
		Mockito.when(databaseModel.getNewestPasswordResetToken("john@example.com")).thenReturn(token);
		Mockito.when(databaseModel.getUserIdByEmail("john@example.com")).thenReturn(1);
		Mockito.when(databaseModel.setUserPassword(Mockito.eq(1), Mockito.notNull(UserPassword.class))).thenReturn(true);

		ResultCode result = userManager.resetPassword("john@example.com", "4ea15b4ed08e48a6d766e976a4387fd2", "new_password");
		Assert.assertEquals(ResultCode.NO_VALID_PASSWORD_RESET_TOKEN, result);

		Mockito.verify(databaseModel, Mockito.never()).setUserPassword(Mockito.eq(1), Mockito.any(UserPassword.class));
	}

	@Test
	public void testSendTestingEmail() {
		Mockito.when(emailSender.sendLostPasswordEmail(Mockito.eq("john@example.com"), Mockito.notNull(String.class))).thenReturn(true);

		boolean result = userManager.sendTestingEmail(EmailType.LOST_PASSWORD, "john@example.com");
		Assert.assertEquals(true, result);

		Mockito.reset(emailSender);
		Mockito.when(emailSender.sendRegistrationEmail(Mockito.eq("john@example.com"), Mockito.notNull(String.class), Mockito.anyInt(), Mockito.notNull(String.class))).thenReturn(
				true);

		result = userManager.sendTestingEmail(EmailType.REGISTRATION, "john@example.com");
		Assert.assertEquals(true, result);
	}

	@Test
	public void testSendTestingEmailFailedToSendEmail() {
		Mockito.when(emailSender.sendLostPasswordEmail(Mockito.eq("john@example.com"), Mockito.notNull(String.class))).thenReturn(false);

		boolean result = userManager.sendTestingEmail(EmailType.LOST_PASSWORD, "john@example.com");
		Assert.assertEquals(false, result);

		Mockito.reset(emailSender);
		Mockito.when(emailSender.sendRegistrationEmail(Mockito.eq("john@example.com"), Mockito.notNull(String.class), Mockito.anyInt(), Mockito.notNull(String.class))).thenReturn(
				false);

		result = userManager.sendTestingEmail(EmailType.REGISTRATION, "john@example.com");
		Assert.assertEquals(false, result);
	}
}
