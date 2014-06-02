package com.jardoapps.usermodule;

/**
 * Return values of a convenience method
 * {@link UserManager#checkPassword(String, String)} which checks entered
 * password from user input.
 * 
 * @author Jaroslav Brtiš
 * 
 */
public enum PasswordCheckResult {
	OK,
	PASSWORD_EMPTY,
	PASSWORD_TOO_SHORT,
	PASSWORD_CONFIRMATION_EMPTY,
	PASSWORD_CONFIRMATION_MISMATCH
}
