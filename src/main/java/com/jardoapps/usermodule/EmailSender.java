package com.jardoapps.usermodule;

import com.jardoapps.usermodule.containers.User;

public interface EmailSender {

	boolean sendLostPasswordEmail(String email, String tokenKey);

	/**
	 * Sends an email to inform the newly registered user that he has been
	 * registered by another user (probably an administrator).
	 * 
	 * @param email
	 *            email of the registered user
	 * @param userName
	 *            name of the registered user (can be null)
	 * @param userId
	 *            id of the registered user
	 * @param registrationControlCode
	 *            generated code which is needed to confirm the registration
	 * @param registrator
	 *            user who registered this user (user who is currently logged
	 *            in, can potentially be null)
	 * @return true if the mail has been sent correctly, otherwise false
	 */
	boolean sendManualRegistrationEmail(String email, String userName, int userId, String registrationControlCode, User registrator);

	boolean sendRegistrationEmail(String email, String userName, int userId, String registrationControlCode);

}
