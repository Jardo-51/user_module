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
