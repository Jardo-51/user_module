package com.jardoapps.usermodule.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EmailUtilsTest {

	@Test
	public void testIsEmailValid() {
		assertEquals(false, EmailUtils.isEmailValid(null));
		assertEquals(false, EmailUtils.isEmailValid(""));
		assertEquals(false, EmailUtils.isEmailValid("user"));
		assertEquals(false, EmailUtils.isEmailValid("user@localhost"));
		assertEquals(false, EmailUtils.isEmailValid("user@example."));
		assertEquals(false, EmailUtils.isEmailValid("user@example.123"));
		assertEquals(false, EmailUtils.isEmailValid("user@@example.com"));
		assertEquals(false, EmailUtils.isEmailValid("us@er@example.com"));
		assertEquals(true, EmailUtils.isEmailValid("user@example.com"));
		assertEquals(true, EmailUtils.isEmailValid("user123@example123.com"));
		assertEquals(true, EmailUtils.isEmailValid("valid.user@example.com"));
		assertEquals(true, EmailUtils.isEmailValid("valid-user@example.com"));
		assertEquals(true, EmailUtils.isEmailValid("valid_user@example.com"));
		assertEquals(true, EmailUtils.isEmailValid("validUser@exam-ple.com"));
		assertEquals(true, EmailUtils.isEmailValid("valid.user@example.co.uk"));
		assertEquals(true, EmailUtils.isEmailValid("Valid.User@example.co.uk"));
		assertEquals(true, EmailUtils.isEmailValid("valid1.user2@example3.co.uk"));
	}

}
