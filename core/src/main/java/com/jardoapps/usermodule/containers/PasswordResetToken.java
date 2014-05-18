package com.jardoapps.usermodule.containers;

import java.util.Date;

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
