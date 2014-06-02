package com.jardoapps.usermodule.containers;

import java.util.Date;

import com.jardoapps.usermodule.UserManager;

/**
 * Container class representing password reset tokens. Password reset tokens are
 * used to set a new password if the user forgets his current password. They
 * contain a random generated security code and creation date, so their validity
 * can be limited to a specified time period. See
 * {@link UserManager#createPasswordResetToken(String)} and
 * {@link UserManager#resetPassword(String, String, String)} for more details.
 * 
 * @author Jaroslav Brtiš
 * 
 */
public class PasswordResetToken {

	private final int userId;
	private final Date creationTime;
	private final String key;

	public Date getCreationTime() {
		return creationTime;
	}

	public String getKey() {
		return key;
	}

	public int getUserId() {
		return userId;
	}

	public PasswordResetToken(int userId, String key, Date creationTime) {
		this.userId = userId;
		this.creationTime = creationTime;
		this.key = key;
	}
}
