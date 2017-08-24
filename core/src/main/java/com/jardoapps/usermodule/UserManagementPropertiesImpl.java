package com.jardoapps.usermodule;

public class UserManagementPropertiesImpl implements UserManagementProperties {

	private int passwordResetTokenExpirationMinutes = 15;

	private int minPasswordLength = 6;

	@Override
	public int getPasswordResetTokenExpirationMinutes() {
		return passwordResetTokenExpirationMinutes;
	}

	public void setPasswordResetTokenExpirationMinutes(int passwordResetTokenExpirationMinutes) {
		this.passwordResetTokenExpirationMinutes = passwordResetTokenExpirationMinutes;
	}

	@Override
	public int getMinPasswordLength() {
		return minPasswordLength;
	}

	public void setMinPasswordLength(int minPasswordLength) {
		this.minPasswordLength = minPasswordLength;
	}

}
